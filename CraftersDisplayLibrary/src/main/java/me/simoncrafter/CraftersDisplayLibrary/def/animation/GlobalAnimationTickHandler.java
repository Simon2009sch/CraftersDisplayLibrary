package me.simoncrafter.CraftersDisplayLibrary.def.animation;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import org.bukkit.Color;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class GlobalAnimationTickHandler {

    private static Map<PositionObject, AAnimationInterpolationFunction<Quaternionf>> LRotationAnimations = new HashMap<>();
    private static Map<PositionObject, AAnimationInterpolationFunction<Quaternionf>> RRotationAnimations = new HashMap<>();
    private static Map<PositionObject, AAnimationInterpolationFunction<Vector3f>> translationAnimations = new HashMap<>();
    private static Map<PositionObject, AAnimationInterpolationFunction<Vector3f>> scaleAnimations = new HashMap<>();
    private static Map<IColorableDisplay, ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>> colorAnimations = new HashMap<>();


    private static BukkitTask tickTask;
    private static GlobalAnimationTickHandler instance;

    private GlobalAnimationTickHandler() {
        start();
    }

    public static GlobalAnimationTickHandler getInstance() {
        if (instance == null) {
            instance = new GlobalAnimationTickHandler();
        }
        return instance;
    }

    public static void registerNewTranslationAnimation(PositionObject obj, AAnimationInterpolationFunction<Vector3f> animationFunction) {
        translationAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    public static void registerNewScaleAnimation(PositionObject obj, AAnimationInterpolationFunction<Vector3f> animationFunction) {
        scaleAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    public static void registerNewRRotationAnimation(PositionObject obj, AAnimationInterpolationFunction<Quaternionf> animationFunction) {
        RRotationAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    public static void registerNewLRotationAnimation(PositionObject obj, AAnimationInterpolationFunction<Quaternionf> animationFunction) {
        LRotationAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

    public static void registerNewColorAnimation(IColorableDisplay obj, ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> animationFunction) {
        colorAnimations.put(obj, animationFunction);
        checkIfStarted();
    }

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

        for (PositionObject obj : objectsToLoop) {
            if (scaleAnimations.containsKey(obj) && scaleAnimations.get(obj).onTick()) {
                AAnimationInterpolationFunction<Vector3f> anim = scaleAnimations.get(obj);
                obj.scaleAbsoluteNoUpdate(anim.getEndValue());
                scaleAnimations.remove(obj);
            }
            if (translationAnimations.containsKey(obj) && translationAnimations.get(obj).onTick()) {
                AAnimationInterpolationFunction<Vector3f> anim = translationAnimations.get(obj);
                obj.moveAbsoluteNoUpdate(anim.getEndValue());
                translationAnimations.remove(obj);
            }
            if (RRotationAnimations.containsKey(obj) && RRotationAnimations.get(obj).onTick()) {
                AAnimationInterpolationFunction<Quaternionf> anim = RRotationAnimations.get(obj);
                obj.RRotateAbsoluteNoUpdate(anim.getEndValue());
                RRotationAnimations.remove(obj);
            }
            if (LRotationAnimations.containsKey(obj) && LRotationAnimations.get(obj).onTick()) {
                AAnimationInterpolationFunction<Quaternionf> anim = LRotationAnimations.get(obj);
                obj.LRotateAbsoluteNoUpdate(anim.getEndValue());
                LRotationAnimations.remove(obj);
            }
            obj.updateChildrenNow(1);
        }
    }

}
