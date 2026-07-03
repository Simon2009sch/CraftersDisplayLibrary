package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class ViewTinterData {

    private int animationDuration;
    private final int lifeTime;
    private int tickCounter = 0;
    private final CubeColorDisplay display;
    private IViewTinterFunction function;
    private BukkitTask task;
    private boolean transitionAnimationPlayed = false;

    public ViewTinterData(int animationDuration, CubeColorDisplay display, IViewTinterFunction function) {
        this(animationDuration, display, function, -1);
    }

    public ViewTinterData(int animationDuration, CubeColorDisplay display, IViewTinterFunction function, int lifeTime) {
        this.animationDuration = animationDuration;
        this.display = display;
        this.function = function;
        this.lifeTime = lifeTime;
    }

    public void start() {
        if (task != null) return;
        if (function == null && animationDuration > 0) return;

        task = new BukkitRunnable() {
            int ticksSinceLastAnimation = 0;

            @Override
            public void run() {
                tickCounter++;
                ticksSinceLastAnimation++;

                if (function != null) {
                    boolean isRepeating = function.isRepeating();

                    if (isRepeating) {
                        if (ticksSinceLastAnimation >= animationDuration) {
                            function.onAnimationRestart(display);
                            ticksSinceLastAnimation = 0;
                        }
                    } else {
                        if (!transitionAnimationPlayed) {
                            function.onAnimationRestart(display);
                            transitionAnimationPlayed = true;
                        }
                    }
                }
            }
        }.runTaskTimer(PluginHolder.getPlugin(), 0, 1);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    public void setAnimation(IViewTinterFunction newFunction, int newDuration) {
        GlobalAnimationTickHandler.removeColorAnimation(display);
        stop();

        this.function = newFunction;
        this.animationDuration = newDuration;
        this.transitionAnimationPlayed = false;

        start();
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public IViewTinterFunction getFunction() {
        return function;
    }

    public CubeColorDisplay getDisplay() {
        return display;
    }
}
