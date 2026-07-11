package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter;

import me.simoncrafter.CraftersDisplayLibrary.effect.internal.EffectFunction;

/**
 * Extension point for custom {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter}
 * visual effects. See the {@code prefabs} sub-package for ready-made implementations
 * (e.g. {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.PulsingColorHighlighter}).
 *
 * @param <V> the display type this function drives, typically
 *            {@link me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay}
 */
public interface IHighlighterFunction<V> extends EffectFunction<V> {
}
