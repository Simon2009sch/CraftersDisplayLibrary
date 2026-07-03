package me.simoncrafter.CraftersDisplayLibrary.def.animation;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;

/**
 * {@link ACustomTypeAnimationInterpolationFunction} specialised for {@link PositionObject}
 * targets - i.e. every built-in translation, scale and rotation animation in
 * {@link me.simoncrafter.CraftersDisplayLibrary.def.animation.functions}.
 *
 * @param <V> the type of value being interpolated (e.g. {@code Vector3f}, {@code Quaternionf})
 */
public abstract class AAnimationInterpolationFunction<V> extends ACustomTypeAnimationInterpolationFunction<V, PositionObject> {

    /** Creates a tween starting at tick 0. */
    public AAnimationInterpolationFunction(int duration, V start, V end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public AAnimationInterpolationFunction(int duration, int tick, V start, V end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    /** The target value this animation interpolates towards. */
    public V getEndValue() {
        return end;
    }

    /**
     * Computes the value for the given tick and applies it to {@code obj}, typically via a
     * {@code *NoUpdate} setter on {@link PositionObject} so that parent/child transform
     * propagation can be batched once per tick by the caller (see
     * {@link GlobalAnimationTickHandler}).
     */
    abstract public void nextTick(final int duration, int tick, final V startRotation, final V endRotation, final PositionObject obj);
}
