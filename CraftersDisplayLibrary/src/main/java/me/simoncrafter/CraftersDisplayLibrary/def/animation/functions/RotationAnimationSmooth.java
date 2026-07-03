package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.joml.Quaternionf;

/**
 * Interpolates a {@link PositionObject}'s left rotation from {@code start} to {@code end} over
 * {@code duration} ticks, passed through an ease-in-out-sine curve before the spherical
 * interpolation.
 * <p>
 * See {@link RotationAnimation} for the linear equivalent, and {@link RRotationAnimationSmooth}
 * for the right-rotation counterpart.
 */
public class RotationAnimationSmooth extends AAnimationInterpolationFunction<Quaternionf> {

    /** Creates a tween starting at tick 0. */
    public RotationAnimationSmooth(int duration, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public RotationAnimationSmooth(int duration, int tick, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    /** Ease-in-out-sine easing curve: slow start, fast middle, slow end. */
    private float easeInOutSine(float t) {
        return -(float) (Math.cos(Math.PI * t) - 1) / 2;
    }

    @Override
    public void nextTick(int duration, int tick, Quaternionf startRotation, Quaternionf endRotation, PositionObject obj) {
        float progress = (float) tick / duration;
        progress = easeInOutSine(progress);
        Quaternionf interpolated = new Quaternionf(startRotation).slerp(endRotation, progress);

        if (tick == 0) {
            // Nudge slightly past the start rotation on the very first tick, bypassing the
            // easing curve (which would otherwise map tick 0 to progress 0, i.e. no movement at
            // all). Without this, Minecraft's transformation interpolation can glitch on restart
            // (flash to the target pose or replay the previous animation) instead of starting
            // cleanly.
            interpolated = new Quaternionf(startRotation).slerp(endRotation, 0.001f);
        }

        obj.LRotateAbsoluteNoUpdate(interpolated);
    }
}
