package me.simoncrafter.CraftersDisplayLibrary.def.active;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.Tags;
import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A single flat, solid-colour panel rendered with a Bukkit {@link TextDisplay}.
 * <p>
 * The "colour panel" effect is a text-display trick: the entity is given empty text, a background
 * colour, and {@code defaultBackground(false)} so the coloured background box renders on its own as
 * a plain rectangle. This is the smallest building block in the library - {@code CubeColorDisplay}
 * composes six of these into a cube face-set, and {@code LineColorDisplay}/{@code RawLineDisplay}
 * arrange four of them around an axis to fake a thick 3D line.
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call {@link #spawnDisplay()}
 * once the object's transform/location is set up to actually spawn the backing entity - it is
 * idempotent and simply returns the existing entity on subsequent calls. Every inherited
 * transform-mutating method from {@link PositionObject} (move/rotate/scale, animated or not) is
 * overridden here to also push the new {@link Transformation} onto the live entity via
 * {@link #updateEntity(int)}, applying the block-scale correction from {@code scaleToBlock}.
 */
public class ColorDisplay extends PositionObject implements IHidable, IColorableDisplay {

    private TextDisplay entity = null;
    private boolean seeTrough = false;
    private boolean hiddenByDefault = false;
    private Color color = Color.fromARGB(0, 0, 0, 0);
    private Display.Billboard billboard = Display.Billboard.FIXED;

    private ColorDisplay(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, Color color, boolean seeTrough, Display.Billboard billboard) {
        super(List.of(), new Transformation(translation, leftRotation, scale, rightRotation), loc);
        this.color = color;
        this.seeTrough = seeTrough;
        this.billboard = billboard;
    }

    /**
     * Spawns the backing {@link TextDisplay} entity at this object's current location and transform.
     * Safe to call multiple times - if the entity already exists, it is returned unchanged and no new
     * entity is spawned. Tags the entity with {@link Tags#CDL_ENTITY} so it can be identified later
     * (e.g. during chunk/world cleanup) as belonging to this library.
     *
     * @return the (possibly newly-spawned) backing entity
     */
    public TextDisplay spawnDisplay() {
        if (entity != null) {
            return entity;
        }

        entity = getLocation().getWorld().spawn(getLocation(), TextDisplay.class);
        entity.setBillboard(billboard);
        entity.setBackgroundColor(color);
        entity.setDefaultBackground(false);
        entity.setSeeThrough(seeTrough);
        entity.setVisibleByDefault(!hiddenByDefault);
        entity.text(Component.text("\n"));
        Transformation transform = scaleToBlock(getFinalTransform());
        entity.setTransformation(transform);
        entity.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);

        return entity;
    }

    /**
     * Removes the current backing entity (if any) and spawns a fresh one in its place, re-applying
     * the current colour, transform and visibility state. Useful to force a full resync after
     * external state (e.g. server chunk reload) may have desynced the entity.
     *
     * @return the newly-spawned entity
     */
    public TextDisplay respawnEntity() {
        if (entity == null) {
            return spawnDisplay();
        }
        entity.remove();
        entity = null;
        return spawnDisplay();
    }

    /** Creates a colour panel with full control over both rotation components and billboard mode. */
    public static ColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, Color color, boolean seeTrough, Display.Billboard billboard) {
        return new ColorDisplay(loc, scale, translation, leftRotation, rightRotation, color, seeTrough, billboard);
    }

    /** Creates a fixed-billboard, non-see-through colour panel with no right rotation. */
    public static ColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Color color) {
        return new ColorDisplay(loc, scale, translation, leftRotation, new Quaternionf(0, 0, 0, 1), color, false, Display.Billboard.FIXED);
    }

    /** {@inheritDoc} Teleports the backing entity directly, bypassing the transform/animation system. */
    @Override
    public void moveEntityStatic(Location location) {

        /*Vector oldLoc = getLocation().toVector();
        Vector newLoc = location.toVector();

        Vector diff = newLoc.subtract(oldLoc);
        Vector3f diff3f = diff.toVector3f().div(getLocalTransform().getScale());

        moveRelative(diff3f, 0);*/
        entity.teleport(location);

        super.moveEntityStatic(location);
    }

    /** The backing entity, or {@code null} if {@link #spawnDisplay()} has not been called yet. */
    public TextDisplay getEntity() {
        return entity;
    }

    /** {@inheritDoc} Also teleports the backing entity by the same delta. */
    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);

        Vector oldLocation = entity.getLocation().toVector();
        Vector newLocation = loc.toVector();
        Vector difference = newLocation.subtract(oldLocation);

        entity.teleport(entity.getLocation().clone().add(difference));
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
     * Pushes this object's current {@link #getFinalTransform() final transform} (block-scale corrected)
     * onto the live entity, with client-side interpolation over {@code time} ticks. A no-op if the
     * entity hasn't been spawned yet or is no longer valid.
     */
    private void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;
        entity.setTransformation(scaleToBlock(getFinalTransform()));
        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(time);
        if (entity.isSeeThrough() != seeTrough) {
            entity.setSeeThrough(seeTrough);
        }
    }

    /** Sets the billboard mode; takes effect on the entity next time it is spawned. */
    public void setBillboard(Display.Billboard billboard) {
        this.billboard = billboard;
    }

    public Display.Billboard getBillboard() {
        return billboard;
    }

    /** Toggles see-through rendering, applying it to the live entity immediately if spawned. */
    public void setSeeTrough(boolean seeTrough) {
        this.seeTrough = seeTrough;
        if (entity != null) {
            entity.setSeeThrough(seeTrough);
        }
    }

    public boolean isSeeTrough() {
        return seeTrough;
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

    /** {@inheritDoc} Applies immediately to the live entity if already spawned and valid. */
    @Override
    public void setColor(Color newColor) {
        color = newColor;
        if (entity != null && entity.isValid()) {
            entity.setBackgroundColor(color);
        }
    }

    public Color getColor() {
        return color;
    }

    /**
     * {@inheritDoc}
     * <p>
     * <b>Not implemented</b> - always returns {@code null} regardless of this object's state. Callers
     * relying on {@link IDisplayable#clone()} to duplicate a {@code ColorDisplay} will get a null
     * reference rather than a copy; this is a known gap rather than intentional behaviour.
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
