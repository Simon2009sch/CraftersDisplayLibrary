package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

/**
 * Linearly interpolates a {@link PositionObject}'s local scale from {@code start} to {@code end}
 * over {@code duration} ticks.
 * <p>
 * See {@link ScalingAnimationSmooth} for the eased equivalent.
 */
public class ScalingAnimation extends AAnimationInterpolationFunction<Vector3f> {

    /** Creates a tween starting at tick 0. */
    public ScalingAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public ScalingAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startScale, Vector3f endScale, PositionObject obj) {
        float progress = (float) tick / duration;
        Vector3f interpolated = new Vector3f(startScale).lerp(endScale, progress);

        if (tick == 0) {
            // Nudge slightly towards the end scale on the very first tick. Restarting a
            // transformation interpolation with an unchanged value can make Minecraft's client
            // glitch (flash to the target scale or replay the previous animation) instead of
            // starting cleanly.
            Vector3f direction = new Vector3f(endScale).sub(startScale).normalize();
            interpolated.add(direction.mul(0.001f));
        }

        Transformation current = obj.getLocalTransform();
        obj.setLocalTransformNoUpdate(new Transformation(
                current.getTranslation(),
                current.getLeftRotation(),
                interpolated,
                current.getRightRotation()
        ));
    }
}
