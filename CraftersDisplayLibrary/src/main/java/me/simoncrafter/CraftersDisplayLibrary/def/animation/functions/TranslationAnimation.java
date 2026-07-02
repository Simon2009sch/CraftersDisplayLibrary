package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.IAnimationInterpolationFunction;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.joml.Vector3f;

public class TranslationAnimation extends IAnimationInterpolationFunction<Vector3f> {

    public TranslationAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    public TranslationAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startTranslation, Vector3f endTranslation, PositionObject obj) {
        float progress = (float) tick / duration;
        Vector3f interpolated = new Vector3f(startTranslation).lerp(endTranslation, progress);

        if (tick == 0) {
            Vector3f direction = new Vector3f(endTranslation).sub(startTranslation).normalize();
            interpolated.add(direction.mul(0.001f)); // add minute value to stop wired teleporting and replaying of last animation
        }

        obj.moveAbsoluteNoUpdate(interpolated);
    }
}
