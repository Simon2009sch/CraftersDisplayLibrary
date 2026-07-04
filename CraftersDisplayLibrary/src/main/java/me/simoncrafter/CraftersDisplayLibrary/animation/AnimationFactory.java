package me.simoncrafter.CraftersDisplayLibrary.animation;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.easing.EasingCurve;
import me.simoncrafter.CraftersDisplayLibrary.animation.functions.*;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import org.bukkit.Color;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Convenience entry point for starting animations without constructing interpolation function
 * instances by hand. Each {@code registerX} method builds the matching
 * {@link me.simoncrafter.CraftersDisplayLibrary.animation.functions} interpolator and hands
 * it to {@link GlobalAnimationTickHandler}, which then drives it once per tick until it finishes.
 * <p>
 * Every animation type comes in two flavours: a plain linear version (using
 * {@link EasingCurve#LINEAR}), and a "Smooth" version that applies an easing curve before
 * interpolating. Registering a new animation of the same type (translation, scale, left-rotation,
 * right-rotation or colour) on the same object replaces any animation of that type already
 * running on it - only one animation per type can run on an object at a time.
 * <p>
 * Usage: {@code AnimationFactory.registerLRotationAnimation(object, 20, startQuat, endQuat);}
 */
public class AnimationFactory {

    /** Linearly interpolates {@code obj}'s left rotation from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerLRotationAnimation(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewLRotationAnimation(obj, new RotationAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link EasingCurve#EASE_IN_OUT_SINE}) version of {@link #registerLRotationAnimation}. */
    public static void registerLRotationAnimationSmooth(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewLRotationAnimation(obj, new RotationAnimation(durationTicks, start, end, obj, EasingCurve.EASE_IN_OUT_SINE));
    }

    /** Linearly interpolates {@code obj}'s right rotation from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerRRotationAnimation(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewRRotationAnimation(obj, new RRotationAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link EasingCurve#EASE_IN_OUT_SINE}) version of {@link #registerRRotationAnimation}. */
    public static void registerRRotationAnimationSmooth(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewRRotationAnimation(obj, new RRotationAnimation(durationTicks, start, end, obj, EasingCurve.EASE_IN_OUT_SINE));
    }

    /** Linearly interpolates {@code obj}'s translation from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerTranslationAnimation(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewTranslationAnimation(obj, new TranslationAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link EasingCurve#EASE_IN_OUT_SINE}) version of {@link #registerTranslationAnimation}. */
    public static void registerTranslationAnimationSmooth(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewTranslationAnimation(obj, new TranslationAnimation(durationTicks, start, end, obj, EasingCurve.EASE_IN_OUT_SINE));
    }

    /** Linearly interpolates {@code obj}'s scale from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerScalingAnimation(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewScaleAnimation(obj, new ScalingAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link EasingCurve#EASE_IN_OUT_SINE}) version of {@link #registerScalingAnimation}. */
    public static void registerScalingAnimationSmooth(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewScaleAnimation(obj, new ScalingAnimation(durationTicks, start, end, obj, EasingCurve.EASE_IN_OUT_SINE));
    }

    /** Linearly interpolates {@code obj}'s colour from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerColorAnimation(IColorableDisplay obj, int durationTicks, Color start, Color end) {
        GlobalAnimationTickHandler.registerNewColorAnimation(obj, new ColorAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link EasingCurve#EASE_IN_OUT_CUBIC}) version of {@link #registerColorAnimation}. */
    public static void registerColorAnimationSmooth(IColorableDisplay obj, int durationTicks, Color start, Color end) {
        GlobalAnimationTickHandler.registerNewColorAnimation(obj, new ColorAnimation(durationTicks, start, end, obj, EasingCurve.EASE_IN_OUT_CUBIC));
    }
}
