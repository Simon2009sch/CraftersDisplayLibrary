package me.simoncrafter.CraftersDisplayLibrary.def.Animation;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import org.bukkit.scheduler.BukkitRunnable;
import org.joml.Quaternionf;

public abstract class IAnimationInterpolationFunction<V> {

    public IAnimationInterpolationFunction(int duration, V start, V end, PositionObject obj) {
        this.duration = duration;
        this.tick = 0;
        this.start = start;
        this.end = end;
        this.obj = obj;
    }

    public IAnimationInterpolationFunction(int duration, int tick, V start, V end, PositionObject obj) {
        this.duration = duration;
        this.tick = tick;
        this.start = start;
        this.end = end;
        this.obj = obj;
    }

    public boolean onTick() {
        nextTick(duration, tick, start, end, obj);
        tick++;
        if (tick >= duration) {
            return true;
        }
        return false;
    }

    final PositionObject obj;
    final int duration;
    int tick;
    final V start;
    final V end;
    abstract void nextTick(final int duration, int tick, final V startRotation, final V endRotation, final PositionObject obj);
}
