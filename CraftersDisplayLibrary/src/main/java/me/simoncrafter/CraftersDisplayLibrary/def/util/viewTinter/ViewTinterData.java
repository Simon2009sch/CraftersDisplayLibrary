package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;

/**
 * Internal per-player state backing {@link ViewTinter}: owns the tint display, its
 * {@link IViewTinterFunction}, and the repeating task that drives it. Mirrors
 * {@link me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.BlockHighlighterData}, but
 * also honours {@link IViewTinterFunction#isRepeating()} - repeating functions are called every
 * {@code animationDuration} ticks like a highlighter; non-repeating ones fire exactly once
 * (tracked via {@link #transitionAnimationPlayed}) unless {@link #setAnimation} resets them.
 */
@ApiStatus.Internal
public class ViewTinterData {

    private int animationDuration;
    private final int lifeTime;
    private int tickCounter = 0;
    private final CubeColorDisplay display;
    private IViewTinterFunction function;
    private BukkitTask task;
    private boolean transitionAnimationPlayed = false;

    /** Same as {@link #ViewTinterData(int, CubeColorDisplay, IViewTinterFunction, int)} with no lifetime. */
    public ViewTinterData(int animationDuration, CubeColorDisplay display, IViewTinterFunction function) {
        this(animationDuration, display, function, -1);
    }

    public ViewTinterData(int animationDuration, CubeColorDisplay display, IViewTinterFunction function, int lifeTime) {
        this.animationDuration = animationDuration;
        this.display = display;
        this.function = function;
        this.lifeTime = lifeTime;
    }

    /**
     * Starts the repeating tick task that drives {@link #function}, respecting
     * {@link IViewTinterFunction#isRepeating()}. No-ops if there is no function to run, or if already started.
     */
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

    /** Cancels the repeating tick task, if running. */
    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }

    /**
     * Replaces the active {@link IViewTinterFunction}, cancelling any in-flight colour animation on
     * the display, restarting the tick task, and resetting the one-shot-transition-played flag so a
     * new non-repeating function gets to fire again.
     */
    public void setAnimation(IViewTinterFunction newFunction, int newDuration) {
        GlobalAnimationTickHandler.removeColorAnimation(display);
        stop();

        this.function = newFunction;
        this.animationDuration = newDuration;
        this.transitionAnimationPlayed = false;

        start();
    }

    /** Ticks between calls to {@link IViewTinterFunction#onAnimationRestart} when the function is repeating. */
    public int getAnimationDuration() {
        return animationDuration;
    }

    /**
     * Configured lifetime in ticks. Unlike {@link me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.BlockHighlighterData},
     * {@link ViewTinter} has no lifetime-checker task, so this value is currently stored but not
     * read anywhere - a tint never auto-expires on its own.
     */
    public int getLifeTime() {
        return lifeTime;
    }

    /** Ticks elapsed since {@link #start()} was called. */
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
