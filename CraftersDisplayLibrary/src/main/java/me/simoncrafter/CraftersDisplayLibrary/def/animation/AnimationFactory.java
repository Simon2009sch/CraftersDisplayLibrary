package me.simoncrafter.CraftersDisplayLibrary.def.animation;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.functions.*;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorable;
import org.bukkit.Color;
import org.joml.Quaternionf;
import org.joml.Vector3f;

// Convenience factory for registering all animation types
// Usage: AnimationFactory.registerLRotationAnimation(object, 20, startQuat, endQuat);

public class AnimationFactory {

    public static void registerLRotationAnimation(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewLRotationAnimation(obj, new RotationAnimation(durationTicks, start, end, obj));
    }

    public static void registerLRotationAnimationSmooth(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewLRotationAnimation(obj, new RotationAnimationSmooth(durationTicks, start, end, obj));
    }

    public static void registerRRotationAnimation(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewRRotationAnimation(obj, new RRotationAnimation(durationTicks, start, end, obj));
    }

    public static void registerRRotationAnimationSmooth(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewRRotationAnimation(obj, new RRotationAnimationSmooth(durationTicks, start, end, obj));
    }

    public static void registerTranslationAnimation(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewTranslationAnimation(obj, new TranslationAnimation(durationTicks, start, end, obj));
    }

    public static void registerTranslationAnimationSmooth(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewTranslationAnimation(obj, new TranslationAnimationSmooth(durationTicks, start, end, obj));
    }

    public static void registerScalingAnimation(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewScaleAnimation(obj, new ScalingAnimation(durationTicks, start, end, obj));
    }

    public static void registerScalingAnimationSmooth(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewScaleAnimation(obj, new ScalingAnimationSmooth(durationTicks, start, end, obj));
    }

    public static void registerColorAnimation(IColorable obj, int durationTicks, Color start, Color end) {
        GlobalAnimationTickHandler.registerNewColorAnimation(obj, new ColorAnimation(durationTicks, start, end, obj));
    }

    public static void registerColorAnimationSmooth(IColorable obj, int durationTicks, Color start, Color end) {
        GlobalAnimationTickHandler.registerNewColorAnimation(obj, new ColorAnimationSmooth(durationTicks, start, end, obj));
    }
}
