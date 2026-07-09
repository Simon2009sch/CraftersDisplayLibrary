package me.simoncrafter.CraftersDisplayLibrary.core;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IHidable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Generic base for a {@link PositionObject} backed by a single live Bukkit {@link Entity} of type
 * {@code E}, for entity types that are not necessarily a {@link org.bukkit.entity.Display}.
 * <p>
 * Every inherited transform-mutating method from {@link PositionObject} (move/rotate/scale, animated
 * or not) is overridden here to also resync the live entity via {@link #updateEntity(int)}, mirroring
 * {@link AbstractEntityBackedDisplay}'s approach. On top of that, {@link #updateChildren(int)} itself
 * is also overridden to call {@link #updateEntity(int)} - this is the path that matters for animated
 * rotations specifically: {@link PositionObject}'s per-object animation task's per-tick
 * {@code updateAnimation()} step mutates the local transform directly and calls
 * {@link #updateChildren(int)} without going through any of the individual mutators above, so without
 * this second hook a plain entity (which, unlike a {@link org.bukkit.entity.Display}, has no
 * client-side interpolation to lean on) would only resync once at the start of the animation rather
 * than on every tick. No separate ticking mechanism is introduced for entity-backed objects.
 * <p>
 * {@link #updateEntity(int)}'s default implementation (a) syncs a {@link LivingEntity}'s
 * {@code GENERIC_SCALE} attribute from {@link #resolveEntityTransform()}'s scale, and (b) converts
 * this object's resolved left-rotation channel into entity yaw/pitch via {@link #applyEntityRotation()}
 * and pushes it to the entity - the right-rotation channel has no equivalent on a plain entity and is
 * not applied. Anything else an entity type needs (e.g. actually pushing a {@link Transformation})
 * must come from an {@code @Override} in a subclass, or - for the four {@code Display}-backed panel
 * types - from {@link AbstractEntityBackedDisplay}, which extends this class to add the
 * {@code Display.setTransformation}/interpolation push and billboard handling instead (a
 * {@link org.bukkit.entity.Display} has no yaw/pitch of its own, so it does not go through
 * {@link #applyEntityRotation()}).
 * <p>
 * Also provides the {@link IHidable} delegation (default-hidden flag, per-player show/hide),
 * {@link #setLocation(Location)} (teleports the entity by the same delta, null-checked), and
 * {@link #rebaseEntity(Location)} (re-anchors the entity's raw coordinates without moving the
 * visible display) shared by every concrete subclass.
 *
 * @param <E> the concrete Bukkit {@link Entity} subtype backing this object
 */
public abstract class AbstractEntityBackedPositionObject<E extends Entity> extends PositionObject implements IHidable {


    /** The backing entity, or {@code null} if not yet spawned (or after {@link #remove()}). */
    protected E entity = null;

    /** Whether this display is hidden from players by default. See {@link IHidable}. */
    protected boolean hiddenByDefault = false;

    protected AbstractEntityBackedPositionObject(List<IDisplayable> children, Transformation localTransform, Location location) {
        super(children, localTransform, location);
    }

    /**
     * Computes the {@link Transformation} that should be pushed onto the live entity for the current
     * {@link #getFinalTransform() final transform}, applying whatever per-entity-type correction
     * (block-scale, origin-centering, or none) this subclass' entity type needs.
     */
    protected abstract Transformation resolveEntityTransform();

    /**
     * Extension point invoked at the end of {@link #updateEntity(int)}, after the transform and
     * interpolation timing have been pushed to the live entity. No-op by default; overridden by
     * subclasses that need to resync additional per-entity state immediately after every transform
     * push (e.g. {@code ColorDisplay} resyncing its see-through flag).
     */
    protected void afterUpdateEntity() {
    }

    /**
     * Converts this object's resolved left-rotation channel into a Minecraft yaw/pitch facing and
     * applies it to the backing entity via {@code Entity.setRotation}. A no-op if the entity hasn't
     * been spawned yet or is no longer valid.
     * <p>
     * Only the left-rotation channel maps onto an entity's yaw/pitch; the right-rotation channel
     * (which only makes sense as a {@code Display}'s independent post-scale rotation) has no
     * equivalent for a plain entity and is ignored.
     */
    protected void applyEntityRotation() {
        if (entity == null || !entity.isValid()) return;

        Vector3f direction = getFinalTransform().getLeftRotation().transform(new Vector3f(0, 0, 1));
        Location facing = new Location(null, 0, 0, 0);
        facing.setDirection(new Vector(direction.x, direction.y, direction.z));

        entity.setRotation(facing.getYaw(), facing.getPitch());
    }

    /**
     * Syncs the live entity to {@link #resolveEntityTransform()}: the entity's world position to
     * {@link #getLocation()} plus the resolved transform's translation, a {@link LivingEntity}'s
     * {@code GENERIC_SCALE} attribute from its scale, and the entity's yaw/pitch from its left
     * rotation via {@link #applyEntityRotation()} - then invokes {@link #afterUpdateEntity()}. A
     * no-op if the entity hasn't been spawned yet or is no longer valid. Invoked from
     * {@link #updateChildren(int)}, so it re-runs on every tick of an in-flight per-object animation,
     * not just once per mutator call - see the class Javadoc.
     * <p>
     * Unlike a {@link org.bukkit.entity.Display}, a plain entity's world location <i>is</i> its
     * render position - there is no separate client-side-relative {@link Transformation} to push -
     * so the entity is teleported outright every time this runs, keeping it at
     * {@code location + finalTransform.translation} regardless of whether that translation comes
     * from this object's own local transform or from a parent's pushed-down transform.
     */
    protected void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;
        Transformation trans = resolveEntityTransform();


        Vector3f translation = trans.getTranslation();
        entity.teleport(getLocation().add(translation.x, translation.y, translation.z));

        if (entity instanceof LivingEntity) {
            Vector3f scale = trans.getScale();
            AttributeInstance attr = ((LivingEntity) entity).getAttribute(Attribute.GENERIC_SCALE);
            if (attr != null) attr.setBaseValue((scale.x + scale.y + scale.z)/3);
        }
        applyEntityRotation();
        afterUpdateEntity();
    }

    /**
     * {@inheritDoc} Also resyncs the backing entity via {@link #updateEntity(int)} - see the class
     * Javadoc for why hooking this single choke point (rather than every individual mutator) is what
     * makes entity resync keep up with an in-flight per-object animation, not just the initial call.
     */
    @Override
    protected void updateChildren(int time) {
        updateEntity(time);
        super.updateChildren(time);
    }

    /** {@inheritDoc} Also resyncs the backing entity's transform. */
    @Override
    public void setParentTransform(Transformation transformation, int time) {
        super.setParentTransform(transformation, time);
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

    /** {@inheritDoc} Also teleports the backing entity by the same delta, if spawned. */
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

    /**
     * Teleports the backing entity's raw location to {@code newLocation}, compensating this
     * object's local transform translation so the display's rendered position does not change -
     * a "rebase" that moves the entity's coordinate anchor without any visible jump.
     * <p>
     * Contrast with {@code moveEntityStatic(Location)} (declared per-subclass rather than here),
     * which teleports the entity <i>and</i> moves the visible display to match. This method is for
     * the opposite case: re-anchoring a long-lived display's raw coordinates (e.g. after it has
     * drifted far from its spawn point through many small {@link #moveRelative} calls, or before a
     * change of world) while it keeps rendering exactly where it already was.
     * <p>
     * Correctly updates this object's tracked {@link #getLocation() location} to
     * {@code newLocation} as part of the rebase. This matters: it means a later
     * {@link #setLocation(Location)} call - even one passing the exact same coordinates that were
     * previously used as a local-space {@link #moveRelative} offset - computes its move relative
     * to the entity's actual current position rather than a stale pre-rebase one, so it will not
     * unexpectedly re-apply that offset on top of the rebase.
     * <p>
     * If the entity hasn't been spawned yet, this simply updates the tracked location without
     * touching the local transform, since there is nothing rendered yet whose position needs
     * preserving.
     *
     * @param newLocation the world-space location to move the backing entity's raw position to
     */
    public void rebaseEntity(Location newLocation) {
        Location originalLocation = getLocation();

        if (entity == null) {
            setLocationNoUpdate(newLocation);
            return;
        }

        Vector3f delta = newLocation.toVector().subtract(originalLocation.toVector()).toVector3f();

        entity.teleport(newLocation);
        setLocationNoUpdate(newLocation);
        moveRelative(delta.negate(), 0);
    }

    @Override
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    /** {@inheritDoc} Applies immediately to the live entity if already spawned and valid; if {@code recursive}, also to every child. */
    @Override
    public IDisplayable hideByDefault(boolean hide, boolean recursive) {
        hiddenByDefault = hide;
        if (entity != null && entity.isValid()) {
            entity.setVisibleByDefault(!hide);
        }
        if (recursive) {
            forEveryChild(child -> {
                if (child instanceof IHidable hidable) hidable.hideByDefault(hide, true);
            });
        }
        return this;
    }

    /** {@inheritDoc} If {@code recursive}, also applies to every child. */
    @Override
    public IDisplayable showForPlayer(Player player, boolean recursive) {
        if (entity != null && entity.isValid()) {
            player.showEntity(PluginHolder.getPlugin(), entity);
        }
        if (recursive) {
            forEveryChild(child -> {
                if (child instanceof IHidable hidable) hidable.showForPlayer(player, true);
            });
        }
        return this;
    }

    /** {@inheritDoc} If {@code recursive}, also applies to every child. */
    @Override
    public IDisplayable hideForPlayer(Player player, boolean recursive) {
        if (entity != null && entity.isValid()) {
            player.hideEntity(PluginHolder.getPlugin(), entity);
        }
        if (recursive) {
            forEveryChild(child -> {
                if (child instanceof IHidable hidable) hidable.hideForPlayer(player, true);
            });
        }
        return this;
    }

    /** {@inheritDoc} Also removes the backing entity from the world, if spawned. */
    @Override
    public void remove() {
        super.remove();
        if (entity != null) entity.remove();
        entity = null;
    }

}
