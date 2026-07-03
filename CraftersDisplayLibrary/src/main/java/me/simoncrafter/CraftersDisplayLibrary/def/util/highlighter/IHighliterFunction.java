package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

/**
 * Extension point for custom {@link me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.BlockHighlighter}
 * visual effects. See the {@code prefabs} sub-package for ready-made implementations
 * (e.g. {@link me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs.PulsingColorHighlighter}).
 *
 * @param <V> the display type this function drives, typically
 *            {@link me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay}
 */
@FunctionalInterface
public interface IHighliterFunction<V> {
    /**
     * Called every time the highlight's animation cycle elapses, i.e. once every
     * {@code animationDuration} ticks passed to {@link BlockHighlighter#highlightBlock}.
     *
     * @param object the display being highlighted
     */
    void onAnimationRestart(V object);
}
