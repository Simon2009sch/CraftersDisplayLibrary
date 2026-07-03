package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class BlockHighlighterData {

    private int animationDuration = 20;
    private int lifeTime = -1;
    private int tickCounter = 0;
    private ICuboidDisplay display;
    private IHighliterFunction<ICuboidDisplay> function;
    private BukkitTask task;

    public BlockHighlighterData(int animationDuration, ICuboidDisplay display, IHighliterFunction<ICuboidDisplay> function) {
        this(animationDuration, display, function, -1);
    }

    public BlockHighlighterData(int animationDuration, ICuboidDisplay display, IHighliterFunction<ICuboidDisplay> function, int lifeTime) {
        this.animationDuration = animationDuration;
        this.display = display;
        this.function = function;
        this.lifeTime = lifeTime;
    }

    public void start() {
        if (animationDuration > 0 && function == null) return;
        if (task != null) return;
        task = new BukkitRunnable(){
            int ticksSinceLastAnimation = animationDuration;
            @Override
            public void run() {
                tickCounter++;
                ticksSinceLastAnimation++;

                if (ticksSinceLastAnimation >= animationDuration) {
                    if (function != null) {
                        function.onAnimationRestart(display);
                    }
                    ticksSinceLastAnimation = 0;
                }
            }
        }.runTaskTimer(PluginHolder.getPlugin(), 0, 1);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
        }
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public int getLifeTime() {
        return lifeTime;
    }

    public void setLifeTime(int lifeTime) {
        this.lifeTime = lifeTime;
    }

    public int getTickCounter() {
        return tickCounter;
    }

    public IHighliterFunction<ICuboidDisplay> getFunction() {
        return function;
    }

    public void setFunction(IHighliterFunction<ICuboidDisplay> function) {
        this.function = function;
    }

    public ICuboidDisplay getDisplay() {
        return display;
    }

    public void setDisplay(ICuboidDisplay display) {
        this.display = display;
    }
}
