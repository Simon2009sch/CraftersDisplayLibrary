package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import org.bukkit.util.Transformation;
import org.joml.Vector3f;

public class ScalingAnimation extends AAnimationInterpolationFunction<Vector3f> {

    public ScalingAnimation(int duration, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, start, end, obj);
    }

    public ScalingAnimation(int duration, int tick, Vector3f start, Vector3f end, PositionObject obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Vector3f startScale, Vector3f endScale, PositionObject obj) {
        float progress = (float) tick / duration;
        Vector3f interpolated = new Vector3f(startScale).lerp(endScale, progress);

        if (tick == 0) {
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
