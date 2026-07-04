package me.simoncrafter.CraftersDisplayLibrary.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.easing.EasingCurve;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.AnimationInterpolationFunction;
import org.joml.Quaternionf;

/**
 * Interpolates (via spherical interpolation) a {@link PositionObject}'s left rotation from
 * {@code start} to {@code end} over {@code duration} ticks, remapping progress through an
 * {@link EasingCurve} before applying the slerp. Defaults to {@link EasingCurve#LINEAR}
 * (constant-speed rotation) when no curve is specified.
 * <p>
 * See {@link RRotationAnimation} for the right-rotation counterpart.
 */
public class RotationAnimation extends AnimationInterpolationFunction<Quaternionf> {

    private final EasingCurve easingCurve;

    /** Creates a tween starting at tick 0, using {@link EasingCurve#LINEAR}. */
    public RotationAnimation(int duration, Quaternionf start, Quaternionf end, PositionObject obj) {
        this(duration, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween resuming at an arbitrary tick, using {@link EasingCurve#LINEAR}. */
    public RotationAnimation(int duration, int tick, Quaternionf start, Quaternionf end, PositionObject obj) {
        this(duration, tick, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween starting at tick 0, eased by {@code easingCurve}. */
    public RotationAnimation(int duration, Quaternionf start, Quaternionf end, PositionObject obj, EasingCurve easingCurve) {
        super(duration, start, end, obj);
        this.easingCurve = easingCurve;
    }

    /** Creates a tween resuming at an arbitrary tick, eased by {@code easingCurve}. */
    public RotationAnimation(int duration, int tick, Quaternionf start, Quaternionf end, PositionObject obj, EasingCurve easingCurve) {
        super(duration, tick, start, end, obj);
        this.easingCurve = easingCurve;
    }

    @Override
    public void nextTick(int duration, int tick, Quaternionf startRotation, Quaternionf endRotation, PositionObject obj) {
        float progress = easingCurve.ease((float) tick / duration);
        Quaternionf interpolated = new Quaternionf(startRotation).slerp(endRotation, progress);

        if (tick == 0) {
            // Nudge slightly past the start rotation on the very first tick, bypassing the
            // easing curve (which maps tick 0 to progress 0, i.e. no movement at all). Applying
            // an interpolation that starts and ends at the exact same value the client already
            // has can make Minecraft's transformation interpolation glitch (flash to the target
            // pose or replay the previous animation) instead of restarting cleanly.
            interpolated = new Quaternionf(startRotation).slerp(endRotation, 0.001f);
        }

        obj.LRotateAbsoluteNoUpdate(interpolated);
    }
}
