package me.simoncrafter.CraftersDisplayLibrary.entity;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedPositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.w3c.dom.Attr;

import java.util.List;

/**
 * An invisible {@link Shulker} used purely as a physical collision box, rather than for any
 * rendering - e.g. an invisible wall or step a player can stand on or bump into.
 * <p>
 * A {@link Shulker} teleported to an arbitrary sub-block position visually snaps to the world grid
 * instead of moving smoothly - a long-standing Minecraft client quirk. To work around it, the
 * generic {@code E} entity managed by {@link AbstractEntityBackedPositionObject} (position/rotation
 * sync, see {@link AbstractEntityBackedPositionObject#updateEntity(int)}) is a small, invisible,
 * invulnerable {@link ArmorStand} with {@link ArmorStand#setMarker(boolean) the marker property} set
 * (no hitbox, no AI, minimal footprint) rather than the {@link Shulker} itself; the actual
 * {@link Shulker} - held in {@link #shulker} - just rides it as a passenger, which moves smoothly
 * with the marker every tick regardless of the Shulker-specific snapping quirk. Only the
 * {@link Shulker}, not the marker, provides real collision.
 * <p>
 * Because a passenger renders at its vehicle's position plus Minecraft's own (version-dependent)
 * mounting height offset, {@link #MOUNT_HEIGHT_OFFSET} compensates by spawning the marker that much
 * lower, so the {@link Shulker} ends up rendering exactly at this object's intended
 * {@code location + finalTransform.translation} rather than floating above it. This value is an
 * untested best guess - tune it if the collision box renders at the wrong height.
 * <p>
 * The {@link Shulker}'s actual hitbox stays an axis-aligned box regardless of the yaw/pitch that
 * {@link AbstractEntityBackedPositionObject#applyEntityRotation()} pushes to the marker from this
 * object's left rotation - Minecraft does not support rotated entity bounding boxes. The rotation is
 * still applied like it is for every other entity-backed object, but must not be relied on to rotate
 * the actual collision volume; only the {@link #getLocalTransform() local transform}'s scale
 * meaningfully changes the collision box's size.
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call {@link #spawnEntity()}
 * once the object's transform/location is set up to actually spawn the backing entities - it is
 * idempotent and simply returns the existing marker on subsequent calls.
 */
public class ShulkerBasedCollisionBox extends AbstractEntityBackedPositionObject<ArmorStand> {

    /**
     * Best-guess compensation for the vertical offset Minecraft applies when a passenger mounts a
     * vehicle - unverified in-game (this class was written without the ability to launch a server),
     * so treat it as a starting point to tune, not a known-correct constant.
     */
    private static final double MOUNT_HEIGHT_OFFSET = 0.5;

    /** The actual collision-providing entity, riding {@link #entity} (the invisible marker). */
    protected Shulker shulker;


    private ShulkerBasedCollisionBox(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation) {
        super(List.of(), new Transformation(translation, leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc);
    }

    /** Creates a collision box with full control over its left rotation. */
    public static ShulkerBasedCollisionBox create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation) {
        return new ShulkerBasedCollisionBox(loc, scale, translation, leftRotation);
    }

    /** Creates an axis-aligned collision box with no rotation. */
    public static ShulkerBasedCollisionBox create(Location loc, Vector3f scale, Vector3f translation) {
        return new ShulkerBasedCollisionBox(loc, scale, translation, new Quaternionf(0, 0, 0, 1));
    }

    /**
     * Spawns the backing marker {@link ArmorStand} and its {@link Shulker} passenger at this
     * object's current location and transform, both configured as a pure collision proxy:
     * invulnerable, silent, gravity-less and invisible (the {@link Shulker} additionally has its AI
     * disabled, so it never opens/peeks or attacks on its own - the marker has no AI to disable in
     * the first place). Safe to call multiple times - if already spawned, the marker is returned
     * unchanged and nothing new is spawned. Both entities are tagged with {@link Tags#CDL_ENTITY} so
     * they can be identified later as belonging to this library.
     *
     * @return the (possibly newly-spawned) marker entity; see {@link #getShulker()} for the entity
     * that actually provides collision
     */
    public ArmorStand spawnEntity() {
        if (entity != null) {
            return entity;
        }

        Vector3f translation = resolveEntityTransform().getTranslation();
        Location renderLocation = getLocation().add(translation.x, translation.y, translation.z);
        Location markerLocation = renderLocation.clone().subtract(0, MOUNT_HEIGHT_OFFSET, 0);


        entity = markerLocation.getWorld().spawn(markerLocation, ArmorStand.class);
        entity.setMarker(true);
        entity.setInvulnerable(true);
        entity.setSilent(true);
        entity.setGravity(false);
        entity.setInvisible(true);
        entity.setSmall(true);
        entity.setVisibleByDefault(!hiddenByDefault);
        entity.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);

        shulker = markerLocation.getWorld().spawn(markerLocation, Shulker.class);
        shulker.setAI(false);
        shulker.setInvulnerable(true);
        shulker.setSilent(true);
        shulker.setGravity(false);
        shulker.setInvisible(true);
        shulker.getAttribute(Attribute.GENERIC_MAX_HEALTH).setBaseValue(999999999999999999d);
        shulker.getAttribute(Attribute.GENERIC_ARMOR).setBaseValue(999999999999999999d);
        shulker.setLootTable(LootTables.EMPTY.getLootTable());
        shulker.setVisibleByDefault(!hiddenByDefault);
        shulker.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);

        entity.addPassenger(shulker);

        updateEntity(0);

        return entity;
    }

    /** {@inheritDoc} No block-scale or origin correction is needed - a Shulker has no rendered transform. */
    @Override
    protected Transformation resolveEntityTransform() {
        return getFinalTransform();
    }

    /**
     * {@inheritDoc}
     * <p>
     * Overridden (rather than relying on the inherited generic behaviour) for two reasons specific to
     * this class:
     * <ol>
     *     <li>The generic implementation teleports {@link #entity} straight to
     *     {@code location + translation}, with no {@link #MOUNT_HEIGHT_OFFSET} correction. Since that
     *     offset is what makes the {@link #shulker} passenger render at the intended spot in the first
     *     place, using the generic logic here would undo the correction on every single update
     *     (including the one {@link #spawnEntity()} triggers immediately after placing the marker at
     *     the correctly-offset spawn position).</li>
     *     <li>The {@link #shulker} is only ever moved by riding {@link #entity} - Bukkit does not
     *     reliably resync a vehicle's passengers as part of {@code Entity#teleport}, so without an
     *     explicit teleport here the Shulker's actual hitbox can lag behind at its last real position
     *     even though the (invisible) marker itself moved correctly.</li>
     * </ol>
     */
    @Override
    protected void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;

        Transformation trans = resolveEntityTransform();
        Vector3f scale = trans.getScale();
        Vector3f translation = trans.getTranslation();
        Location renderLocation = getLocation().add(translation.x, translation.y, translation.z);
        Location markerLocation = renderLocation.clone().subtract(0, MOUNT_HEIGHT_OFFSET*((scale.x + scale.y + scale.z)/3), 0);


        if (time > 1) {
            animateEntityPositions(markerLocation, time);
        } else {
            teleportEntities(markerLocation);
            applyEntityRotation();
            afterUpdateEntity();
        }
    }

    private void animateEntityPositions(Location markerTarget, int duration) {
        Location markerStart = entity.getLocation().clone();

        GlobalAnimationTickHandler.registerNewGeneralAnimation(this, new CustomTypeAnimationInterpolationFunction<Location, PositionObject>(duration, markerStart, markerTarget, this){
            @Override
            public void nextTick(int duration, int tick, Location startLocation, Location endLocation, PositionObject obj) {
                float progress = Math.min((float) tick / duration, 1.0f);

                Vector3f startVec = startLocation.toVector().toVector3f();
                Vector3f endVec = endLocation.toVector().toVector3f();
                Vector3f interpolatedVec = new Vector3f(startVec).lerp(endVec, progress);
                Location interpolatedPos = new Location(startLocation.getWorld(), interpolatedVec.x, interpolatedVec.y, interpolatedVec.z);

                teleportEntities(interpolatedPos);
                applyEntityRotation();
                afterUpdateEntity();
            }
        });
    }



    private void teleportEntities(Location location) {
        entity.removePassenger(shulker);
        entity.teleport(location);
        if (shulker != null && shulker.isValid()) {
            shulker.teleport(location.add(0, MOUNT_HEIGHT_OFFSET, 0));
            entity.addPassenger(shulker);
        }
    }


    /**
     * Also syncs the {@link Shulker} passenger's {@code GENERIC_SCALE} attribute - the inherited
     * {@link AbstractEntityBackedPositionObject#updateEntity(int)} only syncs {@link #entity} (the
     * marker), which provides no collision regardless of its scale.
     */
    @Override
    protected void afterUpdateEntity() {
        if (shulker == null || !shulker.isValid()) return;
        Vector3f scale = resolveEntityTransform().getScale();
        AttributeInstance attr = shulker.getAttribute(Attribute.GENERIC_SCALE);
        if (attr != null) attr.setBaseValue((scale.x + scale.y + scale.z) / 3);
    }

    /** The marker {@link ArmorStand}, or {@code null} if {@link #spawnEntity()} has not been called yet. */
    public ArmorStand getMarkerEntity() {
        return entity;
    }

    /** The {@link Shulker} that actually provides collision, or {@code null} if not yet spawned. */
    public Shulker getShulker() {
        return shulker;
    }



    /**
     * {@inheritDoc}
     * <p>
     * Produces a new, unspawned {@code ShulkerBasedCollisionBox} with the same transform, location
     * and children (shallow copy) as this one. The copy's backing entities are always {@code null} -
     * call {@link #spawnEntity()} on it separately to bring it to life.
     */
    @Override
    public IDisplayable clone() {
        Transformation local = getLocalTransform();
        ShulkerBasedCollisionBox copy = new ShulkerBasedCollisionBox(
                getLocation(),
                new Vector3f(local.getScale()),
                new Vector3f(local.getTranslation()),
                new Quaternionf(local.getLeftRotation()));
        copy.setChildren(getChildren());
        copy.hiddenByDefault = hiddenByDefault;
        return copy;
    }

    /** {@inheritDoc} Also applies to the {@link Shulker} passenger, which is what actually needs to disappear/appear for a given player. */
    @Override
    public IDisplayable hideByDefault(boolean hide) {
        super.hideByDefault(hide);
        if (shulker != null && shulker.isValid()) {
            shulker.setVisibleByDefault(!hide);
        }
        return this;
    }

    /** {@inheritDoc} Also applies to the {@link Shulker} passenger, which is what actually needs to disappear/appear for a given player. */
    @Override
    public IDisplayable showForPlayer(Player player) {
        super.showForPlayer(player);
        if (shulker != null && shulker.isValid()) {
            player.showEntity(PluginHolder.getPlugin(), shulker);
        }
        return this;
    }

    /** {@inheritDoc} Also applies to the {@link Shulker} passenger, which is what actually needs to disappear/appear for a given player. */
    @Override
    public IDisplayable hideForPlayer(Player player) {
        super.hideForPlayer(player);
        if (shulker != null && shulker.isValid()) {
            player.hideEntity(PluginHolder.getPlugin(), shulker);
        }
        return this;
    }

    /**
     * {@inheritDoc} Also removes the {@link Shulker} passenger. Removing the marker vehicle only
     * dismounts a passenger in vanilla Minecraft, it does not despawn it, so the passenger must be
     * removed explicitly.
     */
    @Override
    public void remove() {
        super.remove();
        if (shulker != null) shulker.remove();
        shulker = null;
    }
}
