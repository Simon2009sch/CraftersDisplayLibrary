package me.simoncrafter.CraftersDisplayLibrary.def.animation;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.functions.*;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import org.bukkit.Color;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Convenience entry point for starting animations without constructing interpolation function
 * instances by hand. Each {@code registerX} method builds the matching
 * {@link me.simoncrafter.CraftersDisplayLibrary.def.animation.functions} interpolator and hands
 * it to {@link GlobalAnimationTickHandler}, which then drives it once per tick until it finishes.
 * <p>
 * Every animation type comes in two flavours: a plain linear version, and a "Smooth" version
 * that applies an easing curve before interpolating. Registering a new animation of the same
 * type (translation, scale, left-rotation, right-rotation or colour) on the same object replaces
 * any animation of that type already running on it - only one animation per type can run on an
 * object at a time.
 * <p>
 * Usage: {@code AnimationFactory.registerLRotationAnimation(object, 20, startQuat, endQuat);}
 */
public class AnimationFactory {

    /** Linearly interpolates {@code obj}'s left rotation from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerLRotationAnimation(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewLRotationAnimation(obj, new RotationAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link RotationAnimationSmooth}) version of {@link #registerLRotationAnimation}. */
    public static void registerLRotationAnimationSmooth(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewLRotationAnimation(obj, new RotationAnimationSmooth(durationTicks, start, end, obj));
    }

    /** Linearly interpolates {@code obj}'s right rotation from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerRRotationAnimation(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewRRotationAnimation(obj, new RRotationAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link RRotationAnimationSmooth}) version of {@link #registerRRotationAnimation}. */
    public static void registerRRotationAnimationSmooth(PositionObject obj, int durationTicks, Quaternionf start, Quaternionf end) {
        GlobalAnimationTickHandler.registerNewRRotationAnimation(obj, new RRotationAnimationSmooth(durationTicks, start, end, obj));
    }

    /** Linearly interpolates {@code obj}'s translation from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerTranslationAnimation(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewTranslationAnimation(obj, new TranslationAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link TranslationAnimationSmooth}) version of {@link #registerTranslationAnimation}. */
    public static void registerTranslationAnimationSmooth(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewTranslationAnimation(obj, new TranslationAnimationSmooth(durationTicks, start, end, obj));
    }

    /** Linearly interpolates {@code obj}'s scale from {@code start} to {@code end} over {@code durationTicks}. */
    public static void registerScalingAnimation(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewScaleAnimation(obj, new ScalingAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link ScalingAnimationSmooth}) version of {@link #registerScalingAnimation}. */
    public static void registerScalingAnimationSmooth(PositionObject obj, int durationTicks, Vector3f start, Vector3f end) {
        GlobalAnimationTickHandler.registerNewScaleAnimation(obj, new ScalingAnimationSmooth(durationTicks, start, end, obj));
    }

    /**
     * Linearly interpolates {@code obj}'s colour from {@code start} to {@code end} over
     * {@code durationTicks}.
     *
     * @apiNote {@link ColorAnimation}, the interpolator backing this method, currently computes
     * the interpolated colour but never applies it to {@code obj} - prefer
     * {@link #registerColorAnimationSmooth} until that is fixed.
     */
    public static void registerColorAnimation(IColorableDisplay obj, int durationTicks, Color start, Color end) {
        GlobalAnimationTickHandler.registerNewColorAnimation(obj, new ColorAnimation(durationTicks, start, end, obj));
    }

    /** Eased ({@link ColorAnimationSmooth}) version of {@link #registerColorAnimation}. Unlike the plain version, this one does apply the interpolated colour on every tick. */
    public static void registerColorAnimationSmooth(IColorableDisplay obj, int durationTicks, Color start, Color end) {
        GlobalAnimationTickHandler.registerNewColorAnimation(obj, new ColorAnimationSmooth(durationTicks, start, end, obj));
    }
}
