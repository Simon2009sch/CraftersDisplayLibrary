package me.simoncrafter.CraftersDisplayLibrary.def;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Base implementation of {@link IDisplayable} and the foundational scene-graph node that nearly
 * every display type in this library extends (e.g. {@code ColorDisplay}, {@code CubeColorDisplay},
 * {@code LineColorDisplay}, {@code WireframeCubeColorDisplay}).
 * <p>
 * Each {@code PositionObject} holds a {@code localTransform} (its own position/rotation/scale
 * relative to its parent) and a {@code parentTransform} (the resolved transform pushed down from
 * its parent, or identity for a root object). {@link #getFinalTransform()} combines the two via a
 * pluggable {@link #getParentApplierFunction() parent applier function} - by default the standard
 * scale-then-rotate-then-translate composition. Whenever this object's own transform changes,
 * {@link #updateChildren(int)} recomputes the final transform and pushes it to every child's
 * {@link #setParentTransform}, which is how composite displays (e.g. a cube built from six face
 * children, or a wireframe cube built from twelve edge children) stay rigidly in sync as one unit.
 *
 * <h2>Rotation channels</h2>
 * Per Bukkit's {@link Transformation} model, rotation has two independent channels: left rotation
 * (applied before scale - {@code LRotate*} methods) and right rotation (applied after scale -
 * {@code RRotate*} methods), each with Absolute and Relative variants.
 *
 * <h2>Two animation mechanisms</h2>
 * Any move/rotate/scale method that takes a {@code time} in ticks animates the change - but this
 * happens through two entirely separate mechanisms depending on the call path:
 * <ul>
 *     <li>Calling e.g. {@link #moveRelative}, {@link #LRotateAbsolute(Quaternionf, int)} etc.
 *     directly with a positive {@code time} starts a lightweight, per-object Bukkit repeating task
 *     ({@link #startAnimation}/{@link #updateAnimation()}/{@link #cancelAnimation()}) that
 *     lerps/slerps this object's local transform from its current value to the target over
 *     {@code time} ticks. This is the implicit animation baked into every transform mutator.</li>
 *     <li>The {@code GlobalAnimationTickHandler}/{@code AnimationFactory} system is a separate,
 *     more explicit mechanism built on registered {@code AAnimationInterpolationFunction}s that
 *     support easing curves. Its tick loop updates many objects' local transforms via the
 *     {@code *NoUpdate} methods below (skipping this class's own animation machinery and child
 *     propagation) and then performs one batched {@link #updateChildrenNow(int)} pass afterward.</li>
 * </ul>
 *
 * <h2>{@code *NoUpdate} variants</h2>
 * Methods such as {@link #moveRelativeNoUpdate}, {@link #scaleAbsoluteNoUpdate} and
 * {@link #LRotateAbsoluteNoUpdate} mutate the local transform directly, without calling
 * {@link #updateChildren(int)}. They exist so external tick loops (namely
 * {@code GlobalAnimationTickHandler}) can update many objects' local transforms first and defer
 * child propagation to a single later pass, instead of recomputing every child's transform on
 * every intermediate mutation.
 */
public class PositionObject implements IDisplayable {
    private static final Vector3f BLOCK_TEXT_DIFFERENCE = new Vector3f(0f, 0f, 0);


    private List<IDisplayable> children = new ArrayList<>();
    private Transformation localTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));
    private Location location;

    /** Default parent/local transform composition: scale, then rotate, then translate. */
    private BiFunction<Transformation, Transformation, Transformation> parentAplerFunction = (parent, local) -> {
        // Apply transformation in correct order: scale → rotate → translate
        Vector3f scaledTranslation = new Vector3f(local.getTranslation()).mul(parent.getScale());
        Vector3f rotatedTranslation = new Vector3f(scaledTranslation)
                .rotate(parent.getLeftRotation())
                .rotate(parent.getRightRotation());
        Vector3f finalTranslation = rotatedTranslation.add(parent.getTranslation());

        return new Transformation(
                finalTranslation,
                parent.getLeftRotation().mul(local.getLeftRotation(), new Quaternionf()).normalize(),
                local.getScale().mul(parent.getScale(), new Vector3f()),
                parent.getRightRotation().mul(local.getRightRotation(), new Quaternionf()).normalize()
        );
    };

    private Transformation parentTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));

    // Animation tracking
    private BukkitTask animationTask = null;
    private Transformation animationStartTransform = null;
    private Transformation animationEndTransform = null;
    private int animationDurationTicks = 0;
    private int animationCurrentTick = 0;

    /**
     * @param children      initial children, copied into a new list
     * @param localTransform initial local transform (position/rotation/scale relative to the parent)
     * @param location      world-space origin the local transform is relative to
     */
    public PositionObject(List<IDisplayable> children, Transformation localTransform, Location location) {
        this.children = new ArrayList<>(children);
        this.localTransform = localTransform;
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location loc) {
        Vector oldLoc = location.toVector();
        Vector newLoc = loc.toVector();

        Vector diff = newLoc.subtract(oldLoc);

        runForEveryChild(child -> child.setLocation(child.getLocation().add(diff)));

        this.location = loc;
    }

    @Override
    public void setLocationNoUpdate(Location loc) {
        this.location = loc;
    }

    @Override
    public Transformation getLocalTransform() {
        return localTransform;
    }

    @Override
    public void setLocalTransform(Transformation transformation) {
        setLocalTransform(transformation, 0);
    }
    @Override
    public void setLocalTransform(Transformation transformation, int time) {
        localTransform = transformation;
        updateChildren(time);
    }

    /** Sets the local transform directly, without propagating the change to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void setLocalTransformNoUpdate(Transformation transformation) {
        localTransform = transformation;
    }


    /** {@inheritDoc} */
    public void moveEntityStatic(Location location) {
        Vector oldLoc = this.location.toVector();
        Vector newLoc = location.toVector();

        Vector diff = newLoc.subtract(oldLoc);

        runForEveryChild((child) -> child.moveEntityStatic(diff.toLocation(location.getWorld())));
        this.location = location;
    }

    @Override
    public void setParentTransform(Transformation transformation, int time) {
        parentTransform = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        updateChildren(time);
    }

    @Override
    public Transformation getParentTransform() {
        return new Transformation(parentTransform.getTranslation(), parentTransform.getLeftRotation(), parentTransform.getScale(), parentTransform.getRightRotation());
    }

    @Override
    public void addChild(IDisplayable child) {
        children.add(child);
        updateChildren(0);
    }

    @Override
    public void removeChild(IDisplayable child) {
        children.remove(child);
    }

    @Override
    public void setChildren(List<IDisplayable> children) {
        this.children = new ArrayList<>(children);
        updateChildren(0);
    }

    /** Runs {@code consumer} for every current child. */
    protected void forEveryChild(Consumer<IDisplayable> consumer) {
        for (IDisplayable display : children) {
            consumer.accept(display);
        }
    }

    @Override
    public List<IDisplayable> getChildren() {
        return children;
    }

    @Override
    public void moveRelative(Vector3f movement, int time) {
        try {
            localTransform = new Transformation(localTransform.getTranslation()
                    .add(movement), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        } catch (NullPointerException ignored) {}
        updateChildren(time);
    }

    /** Translates the local transform relative to its current value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void moveRelativeNoUpdate(Vector3f movement) {
        try {
            localTransform = new Transformation(localTransform.getTranslation()
                    .add(movement), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        } catch (NullPointerException ignored) {}
    }

    @Override
    public void moveAbsolute(Vector3f position, int time) {
        localTransform = new Transformation(position, localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        updateChildren(time);
    }

    /** Sets the local translation to an absolute value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void moveAbsoluteNoUpdate(Vector3f position) {
        localTransform = new Transformation(position, localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
    }

    @Override
    public void moveRelativeToWorld(Vector3f position, int time) {
        Vector3f currentPos = location.toVector().toVector3f().add(getFinalTransform().getTranslation());
        Vector3f diff = position.sub(currentPos);
        moveRelative(diff, time);
    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {
        LRotateAbsolute(rotation, time, true);
    }

    /**
     * Sets the local left rotation to an absolute value.
     *
     * @param time          ticks over which to animate the rotation
     * @param useAnimation  if {@code true}, animates via this object's per-object animation task
     *                      (see the class Javadoc); if {@code false}, applies the change
     *                      immediately and still propagates it to children over {@code time} ticks
     */
    public void LRotateAbsolute(Quaternionf rotation, int time, boolean useAnimation) {
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), rotation, localTransform.getScale(), localTransform.getRightRotation());
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), rotation, localTransform.getScale(), localTransform.getRightRotation());
            updateChildren(time);
        }
    }

    /** Sets the local left rotation to an absolute value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void LRotateAbsoluteNoUpdate(Quaternionf rotation) {
        localTransform = new Transformation(localTransform.getTranslation(), rotation, localTransform.getScale(), localTransform.getRightRotation());
    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {
        LRotateRelative(rotation, time, true);
    }

    /**
     * Rotates the local left rotation relative to its current value.
     *
     * @param time          ticks over which to animate the rotation
     * @param useAnimation  if {@code true}, animates via this object's per-object animation task
     *                      (see the class Javadoc); if {@code false}, applies the change
     *                      immediately and still propagates it to children over {@code time} ticks
     */
    public void LRotateRelative(Quaternionf rotation, int time, boolean useAnimation) {
        Quaternionf newRotation = new Quaternionf(localTransform.getLeftRotation()).mul(rotation);
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), newRotation, localTransform.getScale(), localTransform.getRightRotation());
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), newRotation, localTransform.getScale(), localTransform.getRightRotation());
            updateChildren(time);
        }
    }

    /** Rotates the local left rotation relative to its current value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void LRotateRelativeNoUpdate(Quaternionf rotation) {
        Quaternionf newRotation = new Quaternionf(localTransform.getLeftRotation()).mul(rotation);
        localTransform = new Transformation(localTransform.getTranslation(), newRotation, localTransform.getScale(), localTransform.getRightRotation());
    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {
        RRotateAbsolute(rotation, time, true);
    }

    /**
     * Sets the local right rotation to an absolute value.
     *
     * @param time          ticks over which to animate the rotation
     * @param useAnimation  if {@code true}, animates via this object's per-object animation task
     *                      (see the class Javadoc); if {@code false}, applies the change
     *                      immediately and still propagates it to children over {@code time} ticks
     */
    public void RRotateAbsolute(Quaternionf rotation, int time, boolean useAnimation) {
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), rotation);
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), rotation);
            updateChildren(time);
        }
    }

    /** Sets the local right rotation to an absolute value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void RRotateAbsoluteNoUpdate(Quaternionf rotation) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), rotation);
    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {
        RRotateRelative(rotation, time, true);
    }

    /**
     * Rotates the local right rotation relative to its current value.
     *
     * @param time          ticks over which to animate the rotation
     * @param useAnimation  if {@code true}, animates via this object's per-object animation task
     *                      (see the class Javadoc); if {@code false}, applies the change
     *                      immediately and still propagates it to children over {@code time} ticks
     */
    public void RRotateRelative(Quaternionf rotation, int time, boolean useAnimation) {
        Quaternionf newRotation = new Quaternionf(localTransform.getRightRotation()).mul(rotation);
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), newRotation);
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), newRotation);
            updateChildren(time);
        }
    }

    /** Rotates the local right rotation relative to its current value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void RRotateRelativeNoUpdate(Quaternionf rotation) {
        Quaternionf newRotation = new Quaternionf(localTransform.getRightRotation()).mul(rotation);
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), newRotation);
    }

    @Override
    public void scaleAbsolute(Vector3f scale, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), scale, localTransform.getRightRotation());
        updateChildren(time);
    }

    /** Sets the local scale to an absolute value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void scaleAbsoluteNoUpdate(Vector3f scale) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), scale, localTransform.getRightRotation());
    }

    @Override
    public void scaleRelative(Vector3f scale, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale().add(scale), localTransform.getRightRotation());
        updateChildren(time);
    }

    /** Adds to the local scale relative to its current value, without propagating to children. See the {@code *NoUpdate} note in the class Javadoc. */
    public void scaleRelativeNoUpdate(Vector3f scale) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale().add(scale), localTransform.getRightRotation());
    }


    @Override
    public Transformation getTransformation() {
        return new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
    }

    @Override
    public IDisplayable setTransformation(Transformation transformation) {
        localTransform = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        return this;
    }

    /** Runs {@code action} for every current child. */
    protected void runForEveryChild(Consumer<IDisplayable> action) {
        for (IDisplayable child : children) {
            action.accept(child);
        }
    }

    /**
     * Recomputes {@link #getFinalTransform()} and pushes it to every child via
     * {@link IDisplayable#setParentTransform}, keeping composite displays rigidly in sync.
     *
     * @param time ticks over which children should animate into the new transform
     */
    protected void updateChildren(int time) {
        runForEveryChild(c -> {
            c.setParentTransform(getFinalTransform(), time);
        });
    }

    /**
     * Forces an immediate {@link #updateChildren(int)} pass. Used internally by
     * {@code GlobalAnimationTickHandler} after batch-updating local transforms via the
     * {@code *NoUpdate} methods, to defer child propagation to a single pass per tick rather than
     * one per intermediate mutation. Not intended for typical API consumers.
     *
     * @param duration ticks over which children should animate into the new transform
     */
    @ApiStatus.Internal
    public void updateChildrenNow(int duration) {
        updateChildren(duration);
    }

    /**
     * Copies this object's transform state: a fresh {@link Transformation} and children list
     * wrapper, but the same child elements and {@link Location} reference. Does not deep-clone
     * children or spawn new backing entities.
     */
    @Override
    public IDisplayable clone() {
        return new PositionObject(
                new ArrayList<>(children),
                new Transformation(
                        localTransform.getTranslation(),
                        localTransform.getLeftRotation(),
                        localTransform.getScale(),
                        localTransform.getRightRotation()),
                location
        );
    }

    @Override
    public void setParentApplierFunction(BiFunction<Transformation, Transformation, Transformation> func) {
        parentAplerFunction = func;
    }

    @Override
    public BiFunction<Transformation, Transformation, Transformation> getParentApplierFunction() {
        return parentAplerFunction;
    }

    /** Recursively removes every child. Subclasses with a backing entity should also remove it and call {@code super.remove()}. */
    @Override
    public void remove() {
        for (IDisplayable obj : getChildren()) {
            obj.remove();
        }
    }

    /** Combines {@link #getParentTransform()} and {@link #getLocalTransform()} via the current {@link #getParentApplierFunction() applier function} into this object's resolved transform. */
    protected Transformation getFinalTransform() {
        return parentAplerFunction.apply(parentTransform, localTransform);
    }

    /**
     * Starts (replacing any in-flight animation) this object's per-object animation task, which
     * interpolates the local transform from its current value to {@code endTransform} over
     * {@code durationTicks} ticks. See the "Two animation mechanisms" section of the class
     * Javadoc for how this differs from the {@code GlobalAnimationTickHandler} system.
     */
    private void startAnimation(Transformation endTransform, int durationTicks) {
        cancelAnimation();
        animationStartTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        animationEndTransform = endTransform;
        animationDurationTicks = durationTicks;
        animationCurrentTick = 0;

        if (durationTicks <= 0) {
            setLocalTransform(endTransform, 0);
            return;
        }

        animationTask = Bukkit.getScheduler().runTaskTimer(PluginHolder.getPlugin(), this::updateAnimation, 0L, 1L);
    }

    /** Advances the in-flight per-object animation by one tick, interpolating the local transform toward {@code animationEndTransform}. */
    private void updateAnimation() {
        if (animationStartTransform == null || animationEndTransform == null) {
            cancelAnimation();
            return;
        }

        float progress = (float) animationCurrentTick / animationDurationTicks;
        progress = Math.min(progress, 1.0f);

        Vector3f interpolatedPos = new Vector3f(animationStartTransform.getTranslation())
                .lerp(animationEndTransform.getTranslation(), progress);

        Quaternionf interpolatedLeftRot = new Quaternionf(animationStartTransform.getLeftRotation())
                .slerp(animationEndTransform.getLeftRotation(), progress);

        Quaternionf interpolatedRightRot = new Quaternionf(animationStartTransform.getRightRotation())
                .slerp(animationEndTransform.getRightRotation(), progress);

        Vector3f interpolatedScale = new Vector3f(animationStartTransform.getScale())
                .lerp(animationEndTransform.getScale(), progress);

        localTransform = new Transformation(interpolatedPos, interpolatedLeftRot, interpolatedScale, interpolatedRightRot);
        updateChildren(1);

        animationCurrentTick++;
        if (animationCurrentTick > animationDurationTicks) {
            cancelAnimation();
        }
    }

    /** Cancels any in-flight per-object animation task started by {@link #startAnimation}. */
    private void cancelAnimation() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        animationStartTransform = null;
        animationEndTransform = null;
    }

    /**
     * Converts a logical 1x1x1-unit local transform into the correct on-screen block size using
     * {@link PluginHolder#BLOCK_SCALE}. Used by entity-backed subclasses (e.g. {@code ColorDisplay})
     * when pushing a transform to a live Bukkit display entity.
     */
    protected Transformation scaleToBlock(Transformation transformation) {
        return new Transformation(transformation.getTranslation().add(BLOCK_TEXT_DIFFERENCE), transformation.getLeftRotation(), transformation.getScale().mul(PluginHolder.BLOCK_SCALE), transformation.getRightRotation());
    }

}
