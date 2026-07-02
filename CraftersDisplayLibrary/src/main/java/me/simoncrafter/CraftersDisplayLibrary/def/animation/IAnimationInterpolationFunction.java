package me.simoncrafter.CraftersDisplayLibrary.def.animation;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;

public abstract class IAnimationInterpolationFunction<V> extends ICustomTypeAnimationInterpolationFunction<V, PositionObject> {

    public IAnimationInterpolationFunction(int duration, V start, V end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    public IAnimationInterpolationFunction(int duration, int tick, V start, V end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    public V getEndValue() {
        return end;
    }

    abstract public void nextTick(final int duration, int tick, final V startRotation, final V endRotation, final PositionObject obj);
}
