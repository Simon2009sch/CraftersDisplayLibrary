package me.simoncrafter.CraftersDisplayLibrary.animation;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.AnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.IViewTinterFunction;
import org.bukkit.Color;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

/**
 * Static, per-JVM registry and scheduler that drives every in-flight animation created via
 * {@link AnimationFactory} (or registered directly). A single repeating Bukkit task, running
 * once per game tick, ticks every registered animation, applies finished animations' end values
 * exactly, and propagates the result to each animated object's children.
 * <p>
 * Animations are tracked in one map per animation type (translation, scale, left-rotation,
 * right-rotation, colour), keyed by the target object. Because each map holds at most one entry
 * per object, registering a new animation of a given type on an object that already has one
 * running of that type silently replaces it - there is no built-in queueing or additive
 * combination of same-type animations on the same object. Different animation types (e.g. a
 * translation and a scale) can run concurrently on the same object without conflict.
 * <p>
 * The task is started lazily the first time an animation is registered, and is not currently
 * ever stopped once started.
 */
public class GlobalAnimationTickHandler {

    private static Map<PositionObject, AnimationInterpolationFunction<Quaternionf>> LRotationAnimations = new HashMap<>();
    private static Map<PositionObject, AnimationInterpolationFunction<Quaternionf>> RRotationAnimations = new HashMap<>();
    private static Map<PositionObject, AnimationInterpolationFunction<Vector3f>> translationAnimations = new HashMap<>();
    private static Map<PositionObject, AnimationInterpolationFunction<Vector3f>> scaleAnimations = new HashMap<>();
    private static Map<PositionObject, CustomTypeAnimationInterpolationFunction<?, ?>> generalAnimation = new HashMap<>();
    private static Map<IColorableDisplay, CustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>> colorAnimations = new HashMap<>();

    private static BukkitTask tickTask;
    private static GlobalAnimationTickHandler instance;

    private GlobalAnimationTickHandler() {
        start();
    }

    /**
     * Returns the (lazily-created) singleton instance, starting the shared tick task if it
     * hasn't been already.
     *
     * @apiNote The animation registries and tick task are all static, so this instance carries
     * no state of its own; most callers should use {@link AnimationFactory} or the
     * {@code registerNew*Animation} methods directly instead of calling this.
     */
    public static GlobalAnimationTickHandler getInstance() {
        if (instance == null) {
            instance = new GlobalAnimationTickHandler();
        }
        return instance;
    }

    /** Starts (or replaces) the translation animation running on {@code obj}. */
    public static void registerNewTranslationAnimation(PositionObject obj, AnimationInterpolationFunction<Vector3f> animationFunction) {
        translationAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    /** Starts (or replaces) the scale animation running on {@code obj}. */
    public static void registerNewScaleAnimation(PositionObject obj, AnimationInterpolationFunction<Vector3f> animationFunction) {
        scaleAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    /** Starts (or replaces) the right-rotation animation running on {@code obj}. */
    public static void registerNewRRotationAnimation(PositionObject obj, AnimationInterpolationFunction<Quaternionf> animationFunction) {
        RRotationAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    /** Starts (or replaces) the left-rotation animation running on {@code obj}. */
    public static void registerNewLRotationAnimation(PositionObject obj, AnimationInterpolationFunction<Quaternionf> animationFunction) {
        LRotationAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    /** Starts (or replaces) the colour animation running on {@code obj}. */
    public static void registerNewColorAnimation(IColorableDisplay obj, CustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> animationFunction) {
        colorAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    public static void registerNewGeneralAnimation(PositionObject obj, CustomTypeAnimationInterpolationFunction<?, ?> animationFunction) {
        generalAnimation.put(obj, animationFunction);
        checkIfStarted();
    }

    /**
     * Cancels any in-progress colour animation on {@code obj} without applying its end value.
     * Used e.g. when a display is removed or its colour animation is being replaced by
     * higher-level code that manages its own transition (see
     * {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter#setPlayerAnimation(Player, IViewTinterFunction)}).
     */
    public static void removeColorAnimation(IColorableDisplay obj) {
        colorAnimations.remove(obj);
    }

    private static void checkIfStarted() {
        if (tickTask == null) {
            start();
        }
    }

    private static void start() {
        tickTask = new BukkitRunnable(){
            @Override
            public void run() {
                tickFunction();
            }
        }.runTaskTimer(PluginHolder.getPlugin(), 0, 1);
    }

    /**
     * Advances every registered animation by one tick. Colour animations are ticked in their
     * own pass (they're keyed by {@link IColorableDisplay} rather than {@link PositionObject});
     * finished ones are removed afterwards. For the {@link PositionObject}-keyed animation
     * types, each object appearing in any of the four maps is ticked once for each animation
     * type it has running; a finished animation snaps the object to its exact end value via the
     * matching {@code *NoUpdate} setter (avoiding any last-tick rounding error from the
     * interpolation) before being removed. Every touched object then has
     * {@link PositionObject#updateChildrenNow} called once, propagating the tick's changes to
     * its children/entities exactly once regardless of how many animation types fired.
     */
    private static void tickFunction() {
        Collection<PositionObject> objectsToLoop = new HashSet<>();
        objectsToLoop.addAll(scaleAnimations.keySet());
        objectsToLoop.addAll(translationAnimations.keySet());
        objectsToLoop.addAll(RRotationAnimations.keySet());
        objectsToLoop.addAll(LRotationAnimations.keySet());

        List<IColorableDisplay> listOfColorAnimationsToRemove = new ArrayList<>();
        // have to handle in seperate loop becaues of type difference
        for (IColorableDisplay colorable : colorAnimations.keySet()) {
            if (colorAnimations.containsKey(colorable) && colorAnimations.get(colorable).onTick()) {
                listOfColorAnimationsToRemove.add(colorable);
            }
        }
        // remove finished animations
        for (IColorableDisplay colorable : listOfColorAnimationsToRemove) {
            colorAnimations.remove(colorable);
        }

        List<Object> listOfGeneralAnimationsToRemove = new ArrayList<>();
        // have to handle in seperate loop becaues of type difference
        for (Object anim : generalAnimation.keySet()) {
            if (generalAnimation.containsKey(anim) && generalAnimation.get(anim).onTick()) {
                listOfGeneralAnimationsToRemove.add(anim);
            }
        }
        // remove finished animations
        for (Object anim : listOfGeneralAnimationsToRemove) {
            generalAnimation.remove(anim);
        }

        for (PositionObject obj : objectsToLoop) {
            if (scaleAnimations.containsKey(obj) && scaleAnimations.get(obj).onTick()) {
                AnimationInterpolationFunction<Vector3f> anim = scaleAnimations.get(obj);
                obj.scaleAbsoluteNoUpdate(anim.getEndValue());
                scaleAnimations.remove(obj);
            }
            if (translationAnimations.containsKey(obj) && translationAnimations.get(obj).onTick()) {
                AnimationInterpolationFunction<Vector3f> anim = translationAnimations.get(obj);
                obj.moveAbsoluteNoUpdate(anim.getEndValue());
                translationAnimations.remove(obj);
            }
            if (RRotationAnimations.containsKey(obj) && RRotationAnimations.get(obj).onTick()) {
                AnimationInterpolationFunction<Quaternionf> anim = RRotationAnimations.get(obj);
                obj.RRotateAbsoluteNoUpdate(anim.getEndValue());
                RRotationAnimations.remove(obj);
            }
            if (LRotationAnimations.containsKey(obj) && LRotationAnimations.get(obj).onTick()) {
                AnimationInterpolationFunction<Quaternionf> anim = LRotationAnimations.get(obj);
                obj.LRotateAbsoluteNoUpdate(anim.getEndValue());
                LRotationAnimations.remove(obj);
            }
            obj.updateChildrenNow(1);
        }
    }

}
