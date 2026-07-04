package me.simoncrafter.CraftersDisplayLibrary.def.active;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.Tags;
import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A single display backed by a Bukkit {@link ItemDisplay}, rendering an actual Minecraft
 * {@link ItemStack} (e.g. a sword, a diamond, a custom head) instead of a solid-colour panel
 * or a block.
 * <p>
 * This is the {@code ItemDisplay}-based counterpart to {@link ColorDisplay} and
 * {@link BlockDisplayObject}: it extends {@link PositionObject} and participates in the same
 * scene-graph transform tree, but because the backing entity renders a real item rather than a
 * coloured rectangle, it does <b>not</b> implement {@code IColorableDisplay}. Use
 * {@link #setItem(ItemStack)} / {@link #getItem()} to control which item is shown, and
 * {@link #setItemDisplayTransform(ItemDisplay.ItemDisplayTransform)} /
 * {@link #getItemDisplayTransform()} to control how the item is rendered (e.g. GUI, HEAD,
 * GROUND, FIXED).
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call
 * {@link #spawnDisplay()} once the object's transform/location is set up to actually spawn the
 * backing entity - it is idempotent and simply returns the existing entity on subsequent calls.
 * Every inherited transform-mutating method from {@link PositionObject} (move/rotate/scale,
 * animated or not) is overridden here to also push the new {@link Transformation} onto the live
 * entity via {@link #updateEntity(int)}. Unlike {@link ColorDisplay} (which fakes a colour panel
 * out of an empty {@code TextDisplay} and therefore needs the {@code scaleToBlock} correction),
 * an {@link ItemDisplay} entity's own scale of 1 already renders at its natural item size, so no
 * such correction is applied here.
 */
public class ItemDisplayObject extends PositionObject implements IHidable {

    private ItemDisplay entity = null;
    private ItemStack item;
    private ItemDisplay.ItemDisplayTransform itemDisplayTransform = ItemDisplay.ItemDisplayTransform.FIXED;
    private Display.Billboard billboard = Display.Billboard.FIXED;
    private boolean hiddenByDefault = false;

    private ItemDisplayObject(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, ItemStack item, ItemDisplay.ItemDisplayTransform itemDisplayTransform, Display.Billboard billboard) {
        super(List.of(), new Transformation(translation, leftRotation, scale, rightRotation), loc);
        this.item = item;
        this.itemDisplayTransform = itemDisplayTransform;
        this.billboard = billboard;
    }

    /**
     * Spawns the backing {@link ItemDisplay} entity at this object's current location and
     * transform. Safe to call multiple times - if the entity already exists, it is returned
     * unchanged and no new entity is spawned. Tags the entity with {@link Tags#CDL_ENTITY} so
     * it can be identified later (e.g. during chunk/world cleanup) as belonging to this library.
     *
     * @return the (possibly newly-spawned) backing entity
     */
    public ItemDisplay spawnDisplay() {
        if (entity != null) {
            return entity;
        }

        entity = getLocation().getWorld().spawn(getLocation(), ItemDisplay.class);
        entity.setBillboard(billboard);
        entity.setItemStack(item);
        entity.setItemDisplayTransform(itemDisplayTransform);
        entity.setVisibleByDefault(!hiddenByDefault);
        entity.setTransformation(getFinalTransform());
        entity.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);

        return entity;
    }

    /**
     * Removes the current backing entity (if any) and spawns a fresh one in its place,
     * re-applying the current item, transform and visibility state. Useful to force a full
     * resync after external state (e.g. server chunk reload) may have desynced the entity.
     *
     * @return the newly-spawned entity
     */
    public ItemDisplay respawnEntity() {
        if (entity == null) {
            return spawnDisplay();
        }
        entity.remove();
        entity = null;
        return spawnDisplay();
    }

    /** Creates an item display with full control over both rotation components, item transform and billboard mode. */
    public static ItemDisplayObject create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, ItemStack item, ItemDisplay.ItemDisplayTransform itemDisplayTransform, Display.Billboard billboard) {
        return new ItemDisplayObject(loc, scale, translation, leftRotation, rightRotation, item, itemDisplayTransform, billboard);
    }

    /** Creates a fixed-billboard item display with FIXED item transform and no right rotation. */
    public static ItemDisplayObject create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, ItemStack item) {
        return new ItemDisplayObject(loc, scale, translation, leftRotation, new Quaternionf(0, 0, 0, 1), item, ItemDisplay.ItemDisplayTransform.FIXED, Display.Billboard.FIXED);
    }

    /** Creates a fixed-billboard item display with FIXED item transform and no rotation at all. */
    public static ItemDisplayObject create(Location loc, Vector3f scale, Vector3f translation, ItemStack item) {
        return new ItemDisplayObject(loc, scale, translation, new Quaternionf(0, 0, 0, 1), new Quaternionf(0, 0, 0, 1), item, ItemDisplay.ItemDisplayTransform.FIXED, Display.Billboard.FIXED);
    }

    /** {@inheritDoc} Teleports the backing entity directly, bypassing the transform/animation system. */
    @Override
    public void moveEntityStatic(Location location) {
        if (entity != null) {
            entity.teleport(location);
        }
        super.moveEntityStatic(location);
    }

    /** The backing entity, or {@code null} if {@link #spawnDisplay()} has not been called yet. */
    public ItemDisplay getEntity() {
        return entity;
    }

    /** {@inheritDoc} Also teleports the backing entity by the same delta. */
    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);

        if (entity != null) {
            Vector oldLocation = entity.getLocation().toVector();
            Vector newLocation = loc.toVector();
            Vector difference = newLocation.subtract(oldLocation);

            entity.teleport(entity.getLocation().clone().add(difference));
        }
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void setParentTransform(Transformation transformation, int time) {
        super.setParentTransform(transformation, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void setLocalTransform(Transformation transformation, int time) {
        super.setLocalTransform(transformation, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void moveRelative(Vector3f movement, int time) {
        super.moveRelative(movement, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void moveAbsolute(Vector3f position, int time) {
        super.moveAbsolute(position, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {
        super.LRotateAbsolute(rotation, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {
        super.LRotateRelative(rotation, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {
        super.RRotateAbsolute(rotation, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {
        super.RRotateRelative(rotation, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void scaleAbsolute(Vector3f scale, int time) {
        super.scaleAbsolute(scale, time);
        updateEntity(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void scaleRelative(Vector3f scale, int time) {
        super.scaleRelative(scale, time);
        updateEntity(time);
    }


    /**
     * Pushes this object's current {@link #getFinalTransform() final transform} onto the live
     * entity, with client-side interpolation over {@code time} ticks. A no-op if the entity
     * hasn't been spawned yet or is no longer valid.
     */
    private void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;
        entity.setTransformation(getFinalTransform());
        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(time);
    }

    /** Sets the billboard mode; takes effect on the entity next time it is spawned. */
    public void setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    /**
     * Changes the item this display renders. Applies immediately to the live entity if already
     * spawned and valid.
     */
    public void setItem(ItemStack item) {
        this.item = item;
        if (entity != null && entity.isValid()) {
            entity.setItemStack(item);
        }
    }

    /** The item currently rendered by this display. */
    public ItemStack getItem() {
        return item;
    }

    /**
     * Changes how the item is rendered (e.g. GUI, HEAD, GROUND, FIXED). Applies immediately to
     * the live entity if already spawned and valid.
     */
    public void setItemDisplayTransform(ItemDisplay.ItemDisplayTransform transform) {
        this.itemDisplayTransform = transform;
        if (entity != null && entity.isValid()) {
            entity.setItemDisplayTransform(transform);
        }
    }

    /** The current item display transform (how the item model is positioned). */
    public ItemDisplay.ItemDisplayTransform getItemDisplayTransform() {
        return itemDisplayTransform;
    }

    @Override
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    /** {@inheritDoc} Applies immediately to the live entity if already spawned. */
    @Override
    public IDisplayable hideByDefault(boolean hide) {
        hiddenByDefault = hide;
        if (entity != null) {
            entity.setVisibleByDefault(!hide);
        }
        return this;
    }

    @Override
    public IDisplayable showForPlayer(Player player) {
        if (entity != null) {
            player.showEntity(PluginHolder.getPlugin(), entity);
        }
        return this;
    }

    @Override
    public IDisplayable hideForPlayer(Player player) {
        if (entity != null) {
            player.hideEntity(PluginHolder.getPlugin(), entity);
        }
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not implemented</b> - always returns {@code null}. This is a known gap rather than
     * intentional behaviour.
     */
    @Override
    public IDisplayable clone() {
        return null;
    }

    /** {@inheritDoc} Also removes the backing entity from the world, if spawned. */
    @Override
    public void remove() {
        super.remove();
        if (entity != null) entity.remove();
        entity = null;
    }
}
