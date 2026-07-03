package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.joml.Vector3f;

/**
 * Interpolates a {@link PositionObject}'s local translation from {@code start} to {@code end}
 * over {@code duration} ticks, passed through an ease-in-out-sine curve before the linear
 * interpolation.
 * <p>
 * See {@link TranslationAnimation} for the linear equivalent.
 */
public class TranslationAnimationSmooth extends AAnimationInterpolationFunction<Vector3f> {

    /** Creates a tween starting at tick 0. */
    public TranslationAnimationSmooth(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public TranslationAnimationSmooth(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    /** Ease-in-out-sine easing curve: slow start, fast middle, slow end. */
    private float easeInOutSine(float t) {
        return -(float) (Math.cos(Math.PI * t) - 1) / 2;
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startTranslation, Vector3f endTranslation, PositionObject obj) {
        float progress = (float) tick / duration;
        progress = easeInOutSine(progress);
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
