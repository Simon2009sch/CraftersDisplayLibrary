package me.simoncrafter.CraftersDisplayLibrary.core;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IHidable;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * Shared base for the four "leaf" panel-family displays ({@code ColorDisplay}, {@code TextDisplay},
 * {@code BlockDisplayObject}, {@code ItemDisplayObject}) that are each backed directly by a single
 * live Bukkit {@link Display} entity of type {@code E} and previously each independently
 * re-implemented an identical block of transform-mutator boilerplate.
 * <p>
 * Every inherited transform-mutating method from {@link PositionObject} (move/rotate/scale, animated
 * or not) is overridden here to also push the new {@link Transformation} onto the live entity via
 * {@link #updateEntity(int)}. Subclasses supply {@link #resolveEntityTransform()} to compute the
 * actual transform to push (plain {@link #getFinalTransform()}, a {@code scaleToBlock(...)}-corrected
 * version, or a {@code centerOrigin(...)}-corrected version, depending on how their entity type
 * interprets scale/translation), and may override {@link #afterUpdateEntity()} to run extra resync
 * logic after every transform push (e.g. {@code ColorDisplay} resyncing its see-through flag).
 * <p>
 * {@link #setLocation(Location)} always null-checks the backing entity before teleporting it -
 * previously {@code ColorDisplay} did not, which was a latent NPE risk; that bug is fixed here as a
 * side effect of the deduplication. {@link #setBillboard(Display.Billboard)} applies immediately to
 * the live entity if spawned, for all four subclasses - previously only {@code TextDisplay} did this,
 * while the other three only applied the billboard mode on next spawn; this is an intentional,
 * approved behaviour change made uniform here.
 * <p>
 * <b>Known asymmetry - intentionally not addressed here:</b> {@code moveEntityStatic(Location)} is
 * deliberately NOT part of this base class. {@code ColorDisplay}, {@code BlockDisplayObject} and
 * {@code ItemDisplayObject} each override it identically (teleport the entity directly, bypassing the
 * transform/animation system) and keep that small duplication; {@code TextDisplay} does not override
 * it at all, so moving a {@code TextDisplay} via {@code moveEntityStatic} does not currently teleport
 * its live entity. This is a pre-existing quirk that the restructuring intentionally leaves in place -
 * do not "fix" it by pulling {@code moveEntityStatic} into this base class.
 *
 * @param <E> the concrete Bukkit {@link Display} subtype backing this display
 */
public abstract class AbstractEntityBackedDisplay<E extends Display> extends PositionObject implements IHidable {

    /** The backing entity, or {@code null} if not yet spawned (or after {@link #remove()}). */
    protected E entity = null;

    /** Whether this display is hidden from players by default. See {@link IHidable}. */
    protected boolean hiddenByDefault = false;

    /** The billboard mode; applied immediately to the live entity by {@link #setBillboard(Display.Billboard)}. */
    protected Display.Billboard billboard = Display.Billboard.FIXED;

    protected AbstractEntityBackedDisplay(List<IDisplayable> children, Transformation localTransform, Location location) {
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
     * Pushes {@link #resolveEntityTransform()} onto the live entity, with client-side interpolation
     * over {@code time} ticks, then invokes {@link #afterUpdateEntity()}. A no-op if the entity hasn't
     * been spawned yet or is no longer valid.
     */
    protected void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;
        entity.setTransformation(resolveEntityTransform());
        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(time);
        afterUpdateEntity();
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

    /** Sets the billboard mode; applies immediately to the live entity if already spawned and valid. */
    public void setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
        if (entity != null && entity.isValid()) {
            entity.setBillboard(billboard);
        }
    }

    /** Gets the current billboard mode. */
    public Display.Billboard getBillboard() {
        return billboard;
    }

    @Override
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    /** {@inheritDoc} Applies immediately to the live entity if already spawned and valid. */
    @Override
    public IDisplayable hideByDefault(boolean hide) {
        hiddenByDefault = hide;
        if (entity != null && entity.isValid()) {
            entity.setVisibleByDefault(!hide);
        }
        return this;
    }

    @Override
    public IDisplayable showForPlayer(Player player) {
        if (entity != null && entity.isValid()) {
            player.showEntity(PluginHolder.getPlugin(), entity);
        }
        return this;
    }

    @Override
    public IDisplayable hideForPlayer(Player player) {
        if (entity != null && entity.isValid()) {
            player.hideEntity(PluginHolder.getPlugin(), entity);
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
