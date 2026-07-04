package me.simoncrafter.CraftersDisplayLibrary.display.panel;

import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A single display backed by a Bukkit {@link ItemDisplay}, rendering an actual Minecraft
 * {@link ItemStack} (e.g. a sword, a diamond, a custom head) instead of a solid-colour panel
 * or a block.
 * <p>
 * This is the {@code ItemDisplay}-based counterpart to {@link ColorDisplay} and
 * {@link BlockDisplayObject}: it extends {@link AbstractEntityBackedDisplay} and participates in the
 * same scene-graph transform tree, but because the backing entity renders a real item rather than a
 * coloured rectangle, it does <b>not</b> implement {@code IColorableDisplay}. Use
 * {@link #setItem(ItemStack)} / {@link #getItem()} to control which item is shown, and
 * {@link #setItemDisplayTransform(ItemDisplay.ItemDisplayTransform)} /
 * {@link #getItemDisplayTransform()} to control how the item is rendered (e.g. GUI, HEAD,
 * GROUND, FIXED).
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call
 * {@link #spawnDisplay()} once the object's transform/location is set up to actually spawn the
 * backing entity - it is idempotent and simply returns the existing entity on subsequent calls.
 * Transform-mutating methods (move/rotate/scale, animated or not) are inherited from
 * {@link AbstractEntityBackedDisplay}, which pushes every change onto the live entity via
 * {@link #resolveEntityTransform()}. Unlike {@link ColorDisplay} (which fakes a colour panel out of
 * an empty {@code TextDisplay} and therefore needs the {@code scaleToBlock} correction), an
 * {@link ItemDisplay} entity's own scale of 1 already renders at its natural item size, so no such
 * correction is applied here.
 */
public class ItemDisplayObject extends AbstractEntityBackedDisplay<ItemDisplay> {

    private ItemStack item;
    private ItemDisplay.ItemDisplayTransform itemDisplayTransform = ItemDisplay.ItemDisplayTransform.FIXED;

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

    /**
     * {@inheritDoc} Teleports the backing entity directly, bypassing the transform/animation
     * system. See {@link me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedDisplay#rebaseEntity}
     * for the opposite operation: teleporting the entity's raw location while keeping the display
     * rendered exactly where it already was.
     */
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

    /**
     * {@inheritDoc} No correction is applied - an {@link ItemDisplay}'s own scale of 1 already
     * renders at its natural item size.
     */
    @Override
    protected Transformation resolveEntityTransform() {
        return getFinalTransform();
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

    /**
     * {@inheritDoc}
     * <p>
     * Produces a new, unspawned {@code ItemDisplayObject} with the same transform, location,
     * children (shallow copy), item and item-transform state as this one. The copy's backing
     * entity is always {@code null} - call {@link #spawnDisplay()} on it separately to bring it
     * to life.
     */
    @Override
    public IDisplayable clone() {
        Transformation local = getLocalTransform();
        ItemDisplayObject copy = new ItemDisplayObject(
                getLocation(),
                new Vector3f(local.getScale()),
                new Vector3f(local.getTranslation()),
                new Quaternionf(local.getLeftRotation()),
                new Quaternionf(local.getRightRotation()),
                item,
                itemDisplayTransform,
                billboard);
        copy.setChildren(getChildren());
        copy.hiddenByDefault = hiddenByDefault;
        return copy;
    }
}
