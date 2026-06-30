package me.simoncrafter.CraftersDisplayLibrary.def.Animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;


// this is the distance a object moves in one frame
@FunctionalInterface
public interface IAnimationFunction {
    AnimationFrameOffset onTick(Vector3f oldTranslation, Quaternionf oldRRotation, Vector3f oldScale, Quaternionf oldLRotation, Vector3f newTranslation, Quaternionf newRRotation, Vector3f newScale, Quaternionf newLRotation, int duration, int tick, Map<String, Object> extraArgs);
}
