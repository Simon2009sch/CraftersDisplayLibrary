package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;

public interface IViewTinterFunction {
    /**
     * Called every animation cycle for the tint effect
     */
    void onAnimationRestart(CubeColorDisplay display);

    /**
     * Returns whether this animation repeats (true) or is a one-time transition (false)
     */
    default boolean isRepeating() {
        return true;
    }
}
