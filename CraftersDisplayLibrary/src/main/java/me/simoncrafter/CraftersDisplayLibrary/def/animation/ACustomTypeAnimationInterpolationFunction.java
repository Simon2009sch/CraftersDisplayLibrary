package me.simoncrafter.CraftersDisplayLibrary.def.animation;

public abstract class ACustomTypeAnimationInterpolationFunction<V, K> {

    public ACustomTypeAnimationInterpolationFunction(int duration, V start, V end, K obj) {
        this.duration = duration;
        this.tick = 0;
        this.start = start;
        this.end = end;
        this.obj = obj;
    }

    public ACustomTypeAnimationInterpolationFunction(int duration, int tick, V start, V end, K obj) {
        this.duration = duration;
        this.tick = tick;
        this.start = start;
        this.end = end;
        this.obj = obj;
    }

    public boolean onTick() {
        nextTick(duration, tick, start, end, obj);
        tick++;
        if (tick > duration) {
            return true;
        }
        return false;
    }

    public V getEndValue() {
        return end;
    }

    final K obj;
    final int duration;
    int tick;
    final V start;
    final V end;
    abstract public void nextTick(final int duration, int tick, final V startRotation, final V endRotation, final K obj);
}
