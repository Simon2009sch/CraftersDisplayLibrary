package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.joml.Quaternionf;

/**
 * Linearly interpolates (via spherical interpolation) a {@link PositionObject}'s left rotation
 * from {@code start} to {@code end} over {@code duration} ticks.
 * <p>
 * See {@link RotationAnimationSmooth} for the eased equivalent, and {@link RRotationAnimation}
 * for the right-rotation counterpart.
 */
public class RotationAnimation extends AAnimationInterpolationFunction<Quaternionf> {

    /** Creates a tween starting at tick 0. */
    public RotationAnimation(int duration, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public RotationAnimation(int duration, int tick, Quaternionf start, Quaternionf end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Quaternionf startRotation, Quaternionf endRotation, PositionObject obj) {
        float progress = (float) tick / duration;
        Quaternionf interpolated = new Quaternionf(startRotation).slerp(endRotation, progress);

        if (tick == 0) {
            // Nudge slightly past the start rotation on the very first tick. Applying an
            // interpolation that starts and ends at the exact same value the client already has
            // can make Minecraft's transformation interpolation glitch (flash to the target pose
            // or replay the previous animation) instead of restarting cleanly.
            interpolated = new Quaternionf(startRotation).slerp(endRotation, 0.001f);
        }

        obj.LRotateAbsoluteNoUpdate(interpolated);
    }
}
