package me.simoncrafter.CraftersDisplayLibrary.animation.spi;

/**
 * Base building block for a single tween: interpolates a value of type {@code V} from
 * {@code start} to {@code end} over {@code duration} ticks and applies each intermediate
 * value to a target object of type {@code K}.
 * <p>
 * A tick loop (normally {@link me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler})
 * calls {@link #onTick()} once per game tick. Each call delegates to {@link #nextTick} to compute
 * and apply the value for the current tick, then advances the internal tick counter.
 * {@link #onTick()} returns {@code true} once the animation has run past its duration, signalling
 * to the caller that it is finished and can be discarded.
 * <p>
 * This class is generic over the target type so it can drive both
 * {@link me.simoncrafter.CraftersDisplayLibrary.core.PositionObject PositionObject}-based
 * transform animations (see {@link AnimationInterpolationFunction}) and other targets, such as
 * {@link me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay} for colour
 * animations.
 *
 * @param <V> the type of value being interpolated (e.g. {@code Vector3f}, {@code Quaternionf}, {@code Color})
 * @param <K> the type of object the interpolated value is applied to
 */
public abstract class CustomTypeAnimationInterpolationFunction<V, K> {

    /** Creates a tween starting at tick 0. */
    public CustomTypeAnimationInterpolationFunction(int duration, V start, V end, K obj) {
        this.duration = duration;
        this.tick = 0;
        this.start = start;
        this.end = end;
        this.obj = obj;
    }

    /** Creates a tween resuming at an arbitrary tick, e.g. when reconstructing an in-progress animation. */
    public CustomTypeAnimationInterpolationFunction(int duration, int tick, V start, V end, K obj) {
        this.duration = duration;
        this.tick = tick;
        this.start = start;
        this.end = end;
        this.obj = obj;
    }

    /**
     * Advances the animation by one tick: computes the interpolated value via {@link #nextTick}
     * and applies it, then increments the internal tick counter.
     *
     * @return {@code true} once the tick counter has advanced past {@code duration}, meaning
     *         this animation is complete and should be removed from whatever registry is driving it
     */
    public boolean onTick() {
        nextTick(duration, tick, start, end, obj);
        tick++;
        if (tick > duration) {
            return true;
        }
        return false;
    }

    /** The target value this animation interpolates towards. */
    public V getEndValue() {
        return end;
    }

    protected final K obj;
    protected final int duration;
    protected int tick;
    protected final V start;
    protected final V end;

    /**
     * Computes the value for the given tick and applies it to {@code obj}. Implementations
     * typically interpolate between {@code startRotation} and {@code endRotation} (despite the
     * parameter names, these represent the generic start/end value, not necessarily a rotation)
     * according to {@code tick / duration} progress, optionally passed through an easing curve,
     * and push the result onto {@code obj} via a {@code *NoUpdate} setter so that parent/child
     * propagation can be batched by the caller.
     */
    abstract public void nextTick(final int duration, int tick, final V startRotation, final V endRotation, final K obj);
}
