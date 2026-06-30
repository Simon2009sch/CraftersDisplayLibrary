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

    public void onTick() {
        animationFunction.onTick(oldTranslation, oldRRotation, oldScale, oldLRotation, newTranslation, newRRotation, newScale, newLRotation, duration, tick, extraArgs);
        tick++;
    }

}
