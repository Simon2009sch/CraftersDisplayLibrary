package me.simoncrafter.CraftersDisplayLibrary.def.Animation;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import net.kyori.adventure.text.BlockNBTComponent;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;

public class GlobalAnimationTickHandler {

    private static Map<PositionObject, IAnimationInterpolationFunction<Quaternionf>> LRotationAnimations = new HashMap<>();
    private static Map<PositionObject, IAnimationInterpolationFunction<Quaternionf>> RRotationAnimations = new HashMap<>();
    private static Map<PositionObject, IAnimationInterpolationFunction<Vector3f>> translationAnimations = new HashMap<>();
    private static Map<PositionObject, IAnimationInterpolationFunction<Vector3f>> scaleAnimations = new HashMap<>();
    private static List<PositionObject> objectsToLoop = new ArrayList<>();


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

    public static void registerNewTranslationAnimation(PositionObject obj, IAnimationInterpolationFunction<Vector3f> animationFunction) {
        translationAnimations.put(obj, animationFunction);
        objectsToLoop.add(obj);
    }

    public static void registerNewScaleAnimation(PositionObject obj, IAnimationInterpolationFunction<Vector3f> animationFunction) {
        scaleAnimations.put(obj, animationFunction);
        objectsToLoop.add(obj);
    }

    public static void registerNewRRotationAnimation(PositionObject obj, IAnimationInterpolationFunction<Quaternionf> animationFunction) {
        RRotationAnimations.put(obj, animationFunction);
        objectsToLoop.add(obj);
    }

    public static void registerNewLRotationAnimation(PositionObject obj, IAnimationInterpolationFunction<Quaternionf> animationFunction) {
        LRotationAnimations.put(obj, animationFunction);
        objectsToLoop.add(obj);
    }



    private static void start() {
        tickTask = new BukkitRunnable(){
            @Override
            public void run() {
                tickFunction();
            }
        }.runTaskTimer(PluginHolder.plugin, 0, 1);
    }

    private static void tickFunction() {
        // Then check if the onTick() method returns true, if so cancel the calling of the next animation frame
        for (PositionObject obj : objectsToLoop) {
            if (scaleAnimations.containsKey(obj) && scaleAnimations.get(obj).onTick()) {
                scaleAnimations.remove(obj);
                objectsToLoop.remove(obj);
            }
            if (translationAnimations.containsKey(obj) && translationAnimations.get(obj).onTick()) {
                translationAnimations.remove(obj);
                objectsToLoop.remove(obj);
            }
            if (RRotationAnimations.containsKey(obj) && RRotationAnimations.get(obj).onTick()) {
                RRotationAnimations.remove(obj);
                objectsToLoop.remove(obj);
            }
            if (LRotationAnimations.containsKey(obj) && LRotationAnimations.get(obj).onTick()) {
                LRotationAnimations.remove(obj);
                objectsToLoop.remove(obj);
            }
        }
    }

}
