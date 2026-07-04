package me.simoncrafter.CraftersDisplayLibrary.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.easing.EasingCurve;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.AnimationInterpolationFunction;
import org.joml.Vector3f;

/**
 * Interpolates a {@link PositionObject}'s local translation from {@code start} to {@code end}
 * over {@code duration} ticks, remapping progress through an {@link EasingCurve} before applying
 * the linear interpolation. Defaults to {@link EasingCurve#LINEAR} (constant-speed translation)
 * when no curve is specified.
 */
public class TranslationAnimation extends AnimationInterpolationFunction<Vector3f> {

    private final EasingCurve easingCurve;

    /** Creates a tween starting at tick 0, using {@link EasingCurve#LINEAR}. */
    public TranslationAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        this(duration, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween resuming at an arbitrary tick, using {@link EasingCurve#LINEAR}. */
    public TranslationAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        this(duration, tick, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween starting at tick 0, eased by {@code easingCurve}. */
    public TranslationAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj, EasingCurve easingCurve) {
        super(duration, start, end, obj);
        this.easingCurve = easingCurve;
    }

    /** Creates a tween resuming at an arbitrary tick, eased by {@code easingCurve}. */
    public TranslationAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj, EasingCurve easingCurve) {
        super(duration, tick, start, end, obj);
        this.easingCurve = easingCurve;
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startTranslation, Vector3f endTranslation, PositionObject obj) {
        float progress = easingCurve.ease((float) tick / duration);
        Vector3f interpolated = new Vector3f(startTranslation).lerp(endTranslation, progress);

        if (tick == 0) {
            // Nudge slightly towards the end position on the very first tick, bypassing the
            // easing curve (which maps tick 0 to progress 0). Without this, Minecraft's
            // transformation interpolation can glitch on restart (teleport or replay the
            // previous animation) instead of starting cleanly.
            Vector3f direction = new Vector3f(endTranslation).sub(startTranslation).normalize();
            interpolated.add(direction.mul(0.001f));
        }

        obj.moveAbsoluteNoUpdate(interpolated);
    }
}
