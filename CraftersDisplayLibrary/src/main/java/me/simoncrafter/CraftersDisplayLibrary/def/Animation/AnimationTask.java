package me.simoncrafter.CraftersDisplayLibrary.def.Animation;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Map;

public class AnimationTask {

    private Vector3f oldTranslation;
    private Quaternionf oldRRotation;
    private Quaternionf oldLRotation;
    private Vector3f oldScale;
    private Vector3f newTranslation;
    private Quaternionf newRRotation;
    private Quaternionf newLRotation;
    private Vector3f newScale;
    private int duration;
    private IAnimationFunction animationFunction;
    private int tick = 0;
    private Map<String, Object> extraArgs;

    public AnimationTask(Vector3f oldTranslation, Quaternionf oldRRotation, Quaternionf oldLRotation, Vector3f oldScale, Vector3f newTranslation, Quaternionf newRRotation, Quaternionf newLRotation, Vector3f newScale, int duration, IAnimationFunction animationFunction, Map<String, Object> extraArgs) {
        this.oldTranslation = oldTranslation;
        this.oldRRotation = oldRRotation;
        this.oldLRotation = oldLRotation;
        this.oldScale = oldScale;
        this.newTranslation = newTranslation;
        this.newRRotation = newRRotation;
        this.newLRotation = newLRotation;
        this.newScale = newScale;
        this.duration = duration;
        this.animationFunction = animationFunction;
        this.extraArgs = extraArgs;
    }

    public void onTick() {
        animationFunction.onTick(oldTranslation, oldRRotation, oldScale, oldLRotation, newTranslation, newRRotation, newScale, newLRotation, duration, tick, extraArgs);
        tick++;
    }

}
