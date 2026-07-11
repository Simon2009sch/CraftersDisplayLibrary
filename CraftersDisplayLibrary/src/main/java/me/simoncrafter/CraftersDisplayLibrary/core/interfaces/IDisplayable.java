package me.simoncrafter.CraftersDisplayLibrary.core.interfaces;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.PropertyLock;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;

/**
 * A node in the library's scene-graph-like display hierarchy: something that has a location and a
 * local transform (position, rotation, scale relative to its parent), can own child
 * {@code IDisplayable}s that follow it, and can be moved/rotated/scaled with or without animation.
 * <p>
 * {@link PositionObject} is the base implementation of this interface and is what nearly every
 * concrete display type in the library extends; code that just needs "any transformable,
 * hierarchical display object" should generally depend on this interface rather than on a
 * specific display class.
 *
 * <h2>Child hierarchy</h2>
 * An {@code IDisplayable} can own children via {@link #addChild}, {@link #removeChild} and
 * {@link #setChildren}; {@link #getChildren()} returns the current list. Whenever this object's
 * own transform changes, its resolved final transform is pushed down to every child via
 * {@link #setParentTransform}, which is how composite displays (e.g. a cube built from six face
 * children) stay rigidly in sync as a single unit. {@link #remove()} tears the object down,
 * cascading to its children.
 *
 * <h2>Transform and parent transform</h2>
 * {@link #getTransformation()}/{@link #setTransformation} and
 * {@link #getLocalTransform()}/{@link #setLocalTransform} read and write this object's own local
 * {@link Transformation} (position/rotation/scale relative to its parent). {@link #getParentTransform()}
 * exposes the transform inherited from the parent (identity for a root object).
 * {@link #getParentApplierFunction()}/{@link #setParentApplierFunction} control how the local and
 * parent transforms are combined into a final transform - the default combines them in the usual
 * scale-then-rotate-then-translate order, but this can be overridden for special-cased children
 * (e.g. a cube face that should not inherit non-uniform parent scale on every axis).
 *
 * <h2>Movement, rotation and scaling</h2>
 * {@link #moveRelative}, {@link #moveAbsolute} and {@link #moveRelativeToWorld} translate the
 * object; {@link #LRotateAbsolute}/{@link #LRotateRelative} and
 * {@link #RRotateAbsolute}/{@link #RRotateRelative} rotate it using Minecraft's two independent
 * rotation channels (left rotation is applied before scale, right rotation after); and
 * {@link #scaleAbsolute}/{@link #scaleRelative} resize it. Every one of these methods takes a
 * {@code time} in ticks: {@code 0} applies the change immediately, while a positive value animates
 * the transition over that many ticks.
 *
 * <h2>Location and lifecycle</h2>
 * {@link #getLocation()}/{@link #setLocation} control the world-space origin the local transform
 * is relative to; {@link #setLocationNoUpdate} sets it without touching children or entities, and
 * {@link #moveEntityStatic(Location)} instantly relocates the object (and its children) without
 * going through the transform/animation system at all. {@link #clone()} produces a copy of this
 * object's transform state.
 *
 * <h2>Per-player visibility</h2>
 * Every {@code IDisplayable} also carries per-player visibility control, layered on top of Bukkit's
 * {@code Display#setVisibleByDefault}/{@link Player#showEntity}/{@link Player#hideEntity}.
 * {@link #isHiddenByDefault()} and {@link #hideByDefault(boolean)} control whether players who have
 * not been given an explicit override can see this display at all; {@link #showForPlayer} and
 * {@link #hideForPlayer} apply a per-player override on top of that default. Every mutator has a
 * {@code recursive} overload: {@code true} (the default used by the no-flag convenience method)
 * also applies the change to every descendant reachable via {@link #getChildren()}; {@code false}
 * applies it to this display alone, leaving children untouched.
 */
public interface IDisplayable {

    /** Adds a child that will inherit this object's final transform from now on. */
    void addChild(IDisplayable child);

    /** Removes a previously added child; the child stops receiving parent-transform updates. */
    void removeChild(IDisplayable child);

    /** Replaces the full list of children, immediately pushing this object's current transform to all of them. */
    void setChildren(List<IDisplayable> children);

    /** Tears this display down (removing any backing entities) and recursively removes all children. */
    void remove();

    /** The children currently attached to this object. */
    List<IDisplayable> getChildren();

    /**
     * Sets the transform inherited from this object's parent and re-propagates the resulting
     * final transform to this object's own children.
     *
     * @param transformation the parent's resolved final transform
     * @param time           ticks over which to animate the change; {@code 0} for immediate
     */
    void setParentTransform(Transformation transformation, int time);

    /** The transform currently inherited from this object's parent (identity if it has none). */
    Transformation getParentTransform();

    /** The lock defining which of this object's properties should not inherit from its parent ({@code null} if none). */
    PropertyLock getPropertyLock();

    /** Sets the lock defining which of this object's properties should not inherit from its parent. */
    void setPropertyLock(PropertyLock propertyLock);

    /** Sets the property lock on this object and recursively on every descendant. */
    void setPropertyLockRecursive(PropertyLock propertyLock);

    /** Sets the property lock recursively on every descendant, leaving this object's own lock untouched. */
    void setChildrenPropertyLockRecursive(PropertyLock propertyLock);

    /**
     * Translates this object relative to its current local position.
     *
     * @param movement offset to add to the current translation
     * @param time     ticks over which to animate the movement; {@code 0} for immediate
     */
    void moveRelative(Vector3f movement, int time);

    /**
     * Sets this object's local translation to an absolute value.
     *
     * @param position new local translation
     * @param time     ticks over which to animate the movement; {@code 0} for immediate
     */
    void moveAbsolute(Vector3f position, int time);

    /**
     * Moves this object so that its resolved world-space position matches {@code position},
     * accounting for any inherited parent transform.
     *
     * @param position target position, in the same space as this object's resolved final transform
     * @param time     ticks over which to animate the movement; {@code 0} for immediate
     */
    void moveRelativeToWorld(Vector3f position, int time);

    /**
     * Sets the local left rotation (applied before scale) to an absolute value.
     *
     * @param time ticks over which to animate the rotation; {@code 0} for immediate
     */
    void LRotateAbsolute(Quaternionf rotation, int time);

    /**
     * Rotates the local left rotation (applied before scale) relative to its current value.
     *
     * @param time ticks over which to animate the rotation; {@code 0} for immediate
     */
    void LRotateRelative(Quaternionf rotation, int time);

    /**
     * Sets the local right rotation (applied after scale) to an absolute value.
     *
     * @param time ticks over which to animate the rotation; {@code 0} for immediate
     */
    void RRotateAbsolute(Quaternionf rotation, int time);

    /**
     * Rotates the local right rotation (applied after scale) relative to its current value.
     *
     * @param time ticks over which to animate the rotation; {@code 0} for immediate
     */
    void RRotateRelative(Quaternionf rotation, int time);

    /**
     * Sets the local scale to an absolute value.
     *
     * @param time ticks over which to animate the scaling; {@code 0} for immediate
     */
    void scaleAbsolute(Vector3f scale, int time);

    /**
     * Adds to the local scale relative to its current value.
     *
     * @param time ticks over which to animate the scaling; {@code 0} for immediate
     */
    void scaleRelative(Vector3f scale, int time);

    /**
     * Overrides how this object combines its parent's transform with its own local transform
     * into a final transform (see {@link #getParentApplierFunction()}). Used by composite
     * displays whose children should not inherit the parent's scale/rotation in the default way.
     */
    void setParentApplierFunction(BiFunction<Transformation, Transformation, Transformation> func);

    /**
     * The function combining {@link #getParentTransform() parent} and
     * {@link #getLocalTransform() local} transforms into this object's final, resolved transform.
     */
    BiFunction<Transformation, Transformation, Transformation> getParentApplierFunction();

    /** This object's local transform (position, rotation, scale relative to its parent). */
    Transformation getTransformation();

    /**
     * Replaces this object's local transform outright, without animating and without
     * propagating the change to children.
     *
     * @return this object, for chaining
     */
    IDisplayable setTransformation(Transformation transformation);

    /**
     * Creates a copy of this object's transform state (local transform, children list, location).
     * Implementations are not required to deep-clone children or spawn new backing entities.
     */
    IDisplayable clone();

    /** Sets the world-space origin this object's local transform is relative to, without notifying children. */
    void setLocationNoUpdate(Location loc);

    /** Sets the world-space origin this object's local transform is relative to, moving any children by the same offset. */
    void setLocation(Location loc);

    /** The world-space origin this object's local transform is relative to. */
    Location getLocation();

    /** This object's local transform (position, rotation, scale relative to its parent). */
    Transformation getLocalTransform();

    /** Sets the local transform immediately, propagating it to children with no animation. */
    void setLocalTransform(Transformation transformation);

    /**
     * Sets the local transform and propagates it to children.
     *
     * @param time ticks over which to animate the change; {@code 0} for immediate
     */
    void setLocalTransform(Transformation transformation, int time);

    /**
     * Instantly relocates this object (and recursively its children) to {@code location},
     * bypassing the transform/animation system entirely - a direct teleport rather than a move.
     */
    void moveEntityStatic(Location location);

    /**
     * Applies a single persistent-data entry directly to every live Bukkit entity this display
     * owns. Equivalent to {@link #setPersistentData(NamespacedKey, PersistentDataType, Object,
     * boolean) setPersistentData(key, type, value, false)} - applies only to this object's own
     * entity/entities, not its descendants.
     *
     * @param key   the persistent-data key
     * @param type  the persistent-data type describing how {@code value} is (de)serialized
     * @param value the value to store
     */
    default <T, Z> void setPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        setPersistentData(key, type, value, false);
    }

    /**
     * Applies a single persistent-data entry directly to every live Bukkit entity this display
     * owns. A no-op for a composite display that owns no entity of its own (e.g. {@code
     * CubeColorDisplay}), unless {@code recursive} reaches an entity-backed descendant.
     *
     * @param key       the persistent-data key
     * @param type      the persistent-data type describing how {@code value} is (de)serialized
     * @param value     the value to store
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link #getChildren()}; if {@code false} (the default used by the
     *                  no-flag overload), applies only to this object's own entity/entities
     */
    <T, Z> void setPersistentData(NamespacedKey key, PersistentDataType<T, Z> type, Z value, boolean recursive);

    /** Whether this display is currently hidden from players by default. */
    boolean isHiddenByDefault();

    /**
     * Sets whether this display is hidden from players by default (before any per-player
     * override from {@link #showForPlayer}/{@link #hideForPlayer} is applied). Equivalent to
     * {@link #hideByDefault(boolean, boolean) hideByDefault(hide, true)} - applies recursively
     * to every descendant.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    default IDisplayable hideByDefault(boolean hide) {
        return hideByDefault(hide, true);
    }

    /**
     * Sets whether this display is hidden from players by default (before any per-player
     * override from {@link #showForPlayer}/{@link #hideForPlayer} is applied).
     *
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link #getChildren()}; if {@code false}, applies to this
     *                  display alone
     * @return this display, for chaining
     */
    @Contract(value = "_, _ -> new")
    IDisplayable hideByDefault(boolean hide, boolean recursive);

    /**
     * Overrides this display's default visibility to make it visible for a specific player.
     * Equivalent to {@link #showForPlayer(Player, boolean) showForPlayer(player, true)} -
     * applies recursively to every descendant.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    default IDisplayable showForPlayer(Player player) {
        return showForPlayer(player, true);
    }

    /**
     * Overrides this display's default visibility to make it visible for a specific player.
     *
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link #getChildren()}; if {@code false}, applies to this
     *                  display alone
     * @return this display, for chaining
     */
    @Contract(value = "_, _ -> new")
    IDisplayable showForPlayer(Player player, boolean recursive);

    /**
     * Overrides this display's default visibility to hide it from a specific player. Equivalent
     * to {@link #hideForPlayer(Player, boolean) hideForPlayer(player, true)} - applies
     * recursively to every descendant.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    default IDisplayable hideForPlayer(Player player) {
        return hideForPlayer(player, true);
    }

    /**
     * Overrides this display's default visibility to hide it from a specific player.
     *
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link #getChildren()}; if {@code false}, applies to this
     *                  display alone
     * @return this display, for chaining
     */
    @Contract(value = "_, _ -> new")
    IDisplayable hideForPlayer(Player player, boolean recursive);
}
