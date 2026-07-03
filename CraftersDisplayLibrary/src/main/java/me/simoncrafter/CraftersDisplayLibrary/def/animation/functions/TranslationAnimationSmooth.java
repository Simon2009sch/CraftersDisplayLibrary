package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.joml.Vector3f;

public class TranslationAnimationSmooth extends AAnimationInterpolationFunction<Vector3f> {

    public TranslationAnimationSmooth(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    public TranslationAnimationSmooth(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    private float easeInOutSine(float t) {
        return -(float) (Math.cos(Math.PI * t) - 1) / 2;
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startTranslation, Vector3f endTranslation, PositionObject obj) {
        float progress = (float) tick / duration;
        progress = easeInOutSine(progress);
        Vector3f interpolated = new Vector3f(startTranslation).lerp(endTranslation, progress);

        if (tick == 0) {
            Vector3f direction = new Vector3f(endTranslation).sub(startTranslation).normalize();
            interpolated.add(direction.mul(0.001f));
        }

        obj.moveAbsoluteNoUpdate(interpolated);
    }
}
