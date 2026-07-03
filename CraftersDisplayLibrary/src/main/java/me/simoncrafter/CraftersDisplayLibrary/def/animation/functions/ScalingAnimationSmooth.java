package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

/**
 * Interpolates a {@link PositionObject}'s local scale from {@code start} to {@code end} over
 * {@code duration} ticks, passed through an ease-in-out-sine curve before the linear
 * interpolation.
 * <p>
 * See {@link ScalingAnimation} for the linear equivalent.
 */
public class ScalingAnimationSmooth extends AAnimationInterpolationFunction<Vector3f> {

    /** Creates a tween starting at tick 0. */
    public ScalingAnimationSmooth(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public ScalingAnimationSmooth(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    /** Ease-in-out-sine easing curve: slow start, fast middle, slow end. */
    private float easeInOutSine(float t) {
        return -(float) (Math.cos(Math.PI * t) - 1) / 2;
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startScale, Vector3f endScale, PositionObject obj) {
        float progress = (float) tick / duration;
        progress = easeInOutSine(progress);
        Vector3f interpolated = new Vector3f(startScale).lerp(endScale, progress);

        if (tick == 0) {
            // Nudge slightly towards the end scale on the very first tick, bypassing the easing
            // curve (which maps tick 0 to progress 0). Without this, Minecraft's transformation
            // interpolation can glitch on restart instead of starting cleanly.
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
