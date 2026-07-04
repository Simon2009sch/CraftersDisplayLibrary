package me.simoncrafter.CraftersDisplayLibrary.display.panel;

import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import org.bukkit.Location;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A single display backed by a Bukkit {@link BlockDisplay}, rendering an actual Minecraft
 * {@link BlockData} block (e.g. stone, diamond_block, a log) instead of a solid-colour panel.
 * <p>
 * This is the {@code BlockDisplay}-based counterpart to {@link ColorDisplay}: it extends
 * {@link AbstractEntityBackedDisplay} and participates in the same scene-graph transform tree, but
 * because the backing entity renders a real block state rather than a coloured rectangle, it does
 * <b>not</b> implement {@code IColorableDisplay}. Use {@link #setBlock(BlockData)} /
 * {@link #getBlock()} to control which block is shown.
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call
 * {@link #spawnDisplay()} once the object's transform/location is set up to actually spawn the
 * backing entity - it is idempotent and simply returns the existing entity on subsequent calls.
 * Transform-mutating methods (move/rotate/scale, animated or not) are inherited from
 * {@link AbstractEntityBackedDisplay}, which pushes every change onto the live entity via
 * {@link #resolveEntityTransform()}. Unlike {@link ColorDisplay} (which fakes a colour panel out of
 * an empty {@code TextDisplay} and therefore needs the {@code scaleToBlock} correction), a
 * {@link BlockDisplay} entity's own scale of 1 already renders as one full block, so no such
 * correction is applied here - a scale of {@code (1,1,1)} is one block, as expected.
 * <p>
 * A vanilla {@link BlockDisplay}'s local block model spans from its translation outward toward
 * the positive axes (the translation is the block's corner, not its center), so scaling it would
 * normally grow the block away from this object's position instead of around it. {@link #centerOrigin}
 * corrects for this by re-deriving the translation so the block's own center lands on this
 * object's resolved position, keeping it centered under scaling and rotation alike.
 */
public class BlockDisplayObject extends AbstractEntityBackedDisplay<BlockDisplay> {

    private BlockData blockData;

    private BlockDisplayObject(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, BlockData blockData, Display.Billboard billboard) {
        super(List.of(), new Transformation(translation, leftRotation, scale, rightRotation), loc);
        this.blockData = blockData;
        this.billboard = billboard;
    }

    /**
     * Spawns the backing {@link BlockDisplay} entity at this object's current location and
     * transform. Safe to call multiple times - if the entity already exists, it is returned
     * unchanged and no new entity is spawned. Tags the entity with {@link Tags#CDL_ENTITY} so
     * it can be identified later (e.g. during chunk/world cleanup) as belonging to this library.
     *
     * @return the (possibly newly-spawned) backing entity
     */
    public BlockDisplay spawnDisplay() {
        if (entity != null) {
            return entity;
        }

        entity = getLocation().getWorld().spawn(getLocation(), BlockDisplay.class);
        entity.setBillboard(billboard);
        entity.setBlock(blockData);
        entity.setVisibleByDefault(!hiddenByDefault);
        entity.setTransformation(centerOrigin(getFinalTransform()));
        entity.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);

        return entity;
    }

    /**
     * Removes the current backing entity (if any) and spawns a fresh one in its place,
     * re-applying the current block, transform and visibility state. Useful to force a full
     * resync after external state (e.g. server chunk reload) may have desynced the entity.
     *
     * @return the newly-spawned entity
     */
    public BlockDisplay respawnEntity() {
        if (entity == null) {
            return spawnDisplay();
        }
        entity.remove();
        entity = null;
        return spawnDisplay();
    }

    /** Creates a block display with full control over both rotation components and billboard mode. */
    public static BlockDisplayObject create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, BlockData blockData, Display.Billboard billboard) {
        return new BlockDisplayObject(loc, scale, translation, leftRotation, rightRotation, blockData, billboard);
    }

    /** Creates a fixed-billboard block display with no right rotation. */
    public static BlockDisplayObject create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, BlockData blockData) {
        return new BlockDisplayObject(loc, scale, translation, leftRotation, new Quaternionf(0, 0, 0, 1), blockData, Display.Billboard.FIXED);
    }

    /** Creates a fixed-billboard block display with no rotation at all. */
    public static BlockDisplayObject create(Location loc, Vector3f scale, Vector3f translation, BlockData blockData) {
        return new BlockDisplayObject(loc, scale, translation, new Quaternionf(0, 0, 0, 1), new Quaternionf(0, 0, 0, 1), blockData, Display.Billboard.FIXED);
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
    public BlockDisplay getEntity() {
        return entity;
    }

    /** {@inheritDoc} Applies the {@link #centerOrigin} correction on top of the resolved final transform. */
    @Override
    protected Transformation resolveEntityTransform() {
        return centerOrigin(getFinalTransform());
    }

    /**
     * Re-derives {@code transformation}'s translation so the block model's own center - rather
     * than its corner - lands on that translation, by subtracting the (rotated, scaled) offset
     * from the model's corner to its center. This keeps the block centered on this object's
     * position under scaling and rotation, instead of growing only toward the positive axes.
     */
    private Transformation centerOrigin(Transformation transformation) {
        Vector3f centerOffset = new Vector3f(0.5f, 0.5f, 0.5f);
        transformation.getRightRotation().transform(centerOffset);
        centerOffset.mul(transformation.getScale());
        transformation.getLeftRotation().transform(centerOffset);

        Vector3f centeredTranslation = new Vector3f(transformation.getTranslation()).sub(centerOffset);

        return new Transformation(centeredTranslation, transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
    }

    /**
     * Changes the block this display renders. Applies immediately to the live entity if already
     * spawned and valid.
     */
    public void setBlock(BlockData blockData) {
        this.blockData = blockData;
        if (entity != null && entity.isValid()) {
            entity.setBlock(blockData);
        }
    }

    /** The block currently rendered by this display. */
    public BlockData getBlock() {
        return blockData;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Produces a new, unspawned {@code BlockDisplayObject} with the same transform, location,
     * children (shallow copy) and block data as this one. The copy's backing entity is always
     * {@code null} - call {@link #spawnDisplay()} on it separately to bring it to life.
     */
    @Override
    public IDisplayable clone() {
        Transformation local = getLocalTransform();
        BlockDisplayObject copy = new BlockDisplayObject(
                getLocation(),
                new Vector3f(local.getScale()),
                new Vector3f(local.getTranslation()),
                new Quaternionf(local.getLeftRotation()),
                new Quaternionf(local.getRightRotation()),
                blockData,
                billboard);
        copy.setChildren(getChildren());
        copy.hiddenByDefault = hiddenByDefault;
        return copy;
    }
}
