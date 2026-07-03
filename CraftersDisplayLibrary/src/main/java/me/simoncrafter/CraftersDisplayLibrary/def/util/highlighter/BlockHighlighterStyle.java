package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.jetbrains.annotations.ApiStatus;

@ApiStatus.Internal
public class BlockHighlighterStyle {

    private int animationDuration = 20;
    private CubeColorDisplay display;
    private IHighliterFunction<CubeColorDisplay> function;
    private BukkitTask task;

    public BlockHighlighterStyle(int animationDuration, CubeColorDisplay display, IHighliterFunction<CubeColorDisplay> function) {
        this.animationDuration = animationDuration;
        this.display = display;
        this.function = function;
    }

    public void start() {
        if (animationDuration > 0) return;
        if (task != null) return;
        task = new BukkitRunnable(){
            @Override
            public void run() {
                function.onAnimationRestart(display);
            }
        }.runTaskTimer(PluginHolder.getPlugin(), 0, animationDuration);
    }

    public void stop() {
        task.cancel();
    }

    public int getAnimationDuration() {
        return animationDuration;
    }

    public void setAnimationDuration(int animationDuration) {
        this.animationDuration = animationDuration;
    }

    public IHighliterFunction<CubeColorDisplay> getFunction() {
        return function;
    }

    public void setFunction(IHighliterFunction<CubeColorDisplay> function) {
        this.function = function;
    }

    public CubeColorDisplay getDisplay() {
        return display;
    }

    public void setDisplay(CubeColorDisplay display) {
        this.display = display;
    }
}
