package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;

/**
 * Extension point for custom {@link me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.ViewTinter}
 * visual effects - the screen-tint equivalent of
 * {@link me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction}. See the
 * {@code prefabs} sub-package for ready-made implementations
 * (e.g. {@link me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.prefabs.PulsingTinter}).
 */
public interface IViewTinterFunction {
    /**
     * Called to drive the tint effect: once every {@code animationDuration} ticks if
     * {@link #isRepeating()} is {@code true}, or exactly once if it's {@code false}.
     *
     * @param display the tint cube riding the tinted player's head
     */
    void onAnimationRestart(CubeColorDisplay display);

    /**
     * Whether this effect repeats every animation cycle ({@code true}, the default - e.g. a pulse)
     * or is a one-shot transition that fires only once ({@code false} - e.g. a fade in/out).
     */
    default boolean isRepeating() {
        return true;
    }
}
