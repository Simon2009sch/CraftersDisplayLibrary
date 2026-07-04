package me.simoncrafter.CraftersDisplayLibrary.display.panel;

import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
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
 * idempotent and simply returns the existing entity on subsequent calls. Transform-mutating methods
 * (move/rotate/scale, animated or not) are inherited from {@link AbstractEntityBackedDisplay}, which
 * pushes every change onto the live entity via {@link #resolveEntityTransform()}, applying the
 * block-scale correction from {@code scaleToBlock}.
 */
public class ColorDisplay extends AbstractEntityBackedDisplay<TextDisplay> implements IColorableDisplay {

    private boolean seeThrough = false;
    private Color color = Color.fromARGB(0, 0, 0, 0);

    private ColorDisplay(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, Color color, boolean seeThrough, Display.Billboard billboard) {
        super(List.of(), new Transformation(translation, leftRotation, scale, rightRotation), loc);
        this.color = color;
        this.seeThrough = seeThrough;
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
        entity.setSeeThrough(seeThrough);
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
    public static ColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, Color color, boolean seeThrough, Display.Billboard billboard) {
        return new ColorDisplay(loc, scale, translation, leftRotation, rightRotation, color, seeThrough, billboard);
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
        if (entity != null) {
            entity.teleport(location);
        }

        super.moveEntityStatic(location);
    }

    /** The backing entity, or {@code null} if {@link #spawnDisplay()} has not been called yet. */
    public TextDisplay getEntity() {
        return entity;
    }

    /** {@inheritDoc} Applies the block-scale correction on top of the resolved final transform. */
    @Override
    protected Transformation resolveEntityTransform() {
        return scaleToBlock(getFinalTransform());
    }

    /** {@inheritDoc} Resyncs the live entity's see-through flag if it has drifted from {@link #isSeeThrough()}. */
    @Override
    protected void afterUpdateEntity() {
        if (entity.isSeeThrough() != seeThrough) {
            entity.setSeeThrough(seeThrough);
        }
    }

    /** Toggles see-through rendering, applying it to the live entity immediately if spawned. */
    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        if (entity != null) {
            entity.setSeeThrough(seeThrough);
        }
    }

    public boolean isSeeThrough() {
        return seeThrough;
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
     * Produces a new, unspawned {@code ColorDisplay} with the same transform, location, children
     * (shallow copy) and colour/see-through state as this one. The copy's backing entity is always
     * {@code null} - call {@link #spawnDisplay()} on it separately to bring it to life.
     */
    @Override
    public IDisplayable clone() {
        Transformation local = getLocalTransform();
        ColorDisplay copy = new ColorDisplay(
                getLocation(),
                new Vector3f(local.getScale()),
                new Vector3f(local.getTranslation()),
                new Quaternionf(local.getLeftRotation()),
                new Quaternionf(local.getRightRotation()),
                color,
                seeThrough,
                billboard);
        copy.setChildren(getChildren());
        copy.hiddenByDefault = hiddenByDefault;
        return copy;
    }
}
