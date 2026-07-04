package me.simoncrafter.CraftersDisplayLibrary.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.easing.EasingCurve;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.AnimationInterpolationFunction;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

/**
 * Interpolates a {@link PositionObject}'s local scale from {@code start} to {@code end} over
 * {@code duration} ticks, remapping progress through an {@link EasingCurve} before applying the
 * linear interpolation. Defaults to {@link EasingCurve#LINEAR} (constant-speed scaling) when no
 * curve is specified.
 */
public class ScalingAnimation extends AnimationInterpolationFunction<Vector3f> {

    private final EasingCurve easingCurve;

    /** Creates a tween starting at tick 0, using {@link EasingCurve#LINEAR}. */
    public ScalingAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        this(duration, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween resuming at an arbitrary tick, using {@link EasingCurve#LINEAR}. */
    public ScalingAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        this(duration, tick, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween starting at tick 0, eased by {@code easingCurve}. */
    public ScalingAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj, EasingCurve easingCurve) {
        super(duration, start, end, obj);
        this.easingCurve = easingCurve;
    }

    /** Creates a tween resuming at an arbitrary tick, eased by {@code easingCurve}. */
    public ScalingAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj, EasingCurve easingCurve) {
        super(duration, tick, start, end, obj);
        this.easingCurve = easingCurve;
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startScale, Vector3f endScale, PositionObject obj) {
        float progress = easingCurve.ease((float) tick / duration);
        Vector3f interpolated = new Vector3f(startScale).lerp(endScale, progress);

        if (tick == 0) {
            // Nudge slightly towards the end scale on the very first tick, bypassing the easing
            // curve (which maps tick 0 to progress 0). Restarting a transformation interpolation
            // with an unchanged value can make Minecraft's client glitch (flash to the target
            // scale or replay the previous animation) instead of starting cleanly.
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
