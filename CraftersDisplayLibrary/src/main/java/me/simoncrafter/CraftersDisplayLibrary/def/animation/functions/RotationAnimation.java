package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.joml.Quaternionf;

public class RotationAnimation extends AAnimationInterpolationFunction<Quaternionf> {

    public RotationAnimation(int duration, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    public RotationAnimation(int duration, int tick, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Quaternionf startRotation, Quaternionf endRotation, PositionObject obj) {
        float progress = (float) tick / duration;
        Quaternionf interpolated = new Quaternionf(startRotation).slerp(endRotation, progress);

        if (tick == 0) {
            interpolated = new Quaternionf(startRotation).slerp(endRotation, 0.001f);
        }

        obj.LRotateAbsoluteNoUpdate(interpolated);
    }
}
