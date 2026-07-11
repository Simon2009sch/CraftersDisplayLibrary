package me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter;

import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.internal.EffectFunction;

/**
 * Extension point for custom {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter}
 * visual effects - the screen-tint equivalent of
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction}. See the
 * {@code prefabs} sub-package for ready-made implementations
 * (e.g. {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs.PulsingTinter}).
 * <p>
 * {@link #onAnimationRestart(CubeColorDisplay)} is called: once every {@code animationDuration}
 * ticks if {@link #isRepeating()} is {@code true}, or exactly once if it's {@code false}. Both
 * members are inherited unchanged from {@link EffectFunction}.
 */
public interface IViewTinterFunction extends EffectFunction<CubeColorDisplay> {
}
