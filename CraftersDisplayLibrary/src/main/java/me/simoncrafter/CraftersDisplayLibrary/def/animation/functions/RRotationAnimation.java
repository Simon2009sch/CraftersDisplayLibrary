package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.joml.Quaternionf;

/**
 * Linearly interpolates (via spherical interpolation) a {@link PositionObject}'s right rotation
 * from {@code start} to {@code end} over {@code duration} ticks.
 * <p>
 * See {@link RRotationAnimationSmooth} for the eased equivalent, and {@link RotationAnimation}
 * for the left-rotation counterpart.
 */
public class RRotationAnimation extends AAnimationInterpolationFunction<Quaternionf> {

    /** Creates a tween starting at tick 0. */
    public RRotationAnimation(int duration, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public RRotationAnimation(int duration, int tick, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Quaternionf startRotation, Quaternionf endRotation, PositionObject obj) {
        float progress = (float) tick / duration;
        Quaternionf interpolated = new Quaternionf(startRotation).slerp(endRotation, progress);

        if (tick == 0) {
            // Nudge slightly past the start rotation on the very first tick to avoid Minecraft's
            // transformation interpolation glitching (flashing to the target pose or replaying
            // the previous animation) when it restarts at an unchanged value.
            interpolated = new Quaternionf(startRotation).slerp(endRotation, 0.001f);
        }

        obj.RRotateAbsoluteNoUpdate(interpolated);
    }
}
