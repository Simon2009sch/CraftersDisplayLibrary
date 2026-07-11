package me.simoncrafter.CraftersDisplayLibrary.effect.internal;

/**
 * Shared contract behind the two timed-effect extension points in this library:
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction} (block
 * highlights) and {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.IViewTinterFunction}
 * (player screen tints). Both drive a display of type {@code D} on a repeating or one-shot basis.
 *
 * @param <D> the display type this function drives
 */
public interface EffectFunction<D> {
    /**
     * Called to drive the effect: once every {@code animationDuration} ticks if
     * {@link #isRepeating()} is {@code true}, or exactly once if it's {@code false}.
     *
     * @param display the display being driven
     */
    void onAnimationRestart(D display);

    /**
     * This function's own inherent cycle length in ticks - how often {@link #onAnimationRestart}
     * fires. Drives the {@code animationDuration} that used to be passed separately to
     * {@code register}/{@code setAnimation}; now derived from the function itself so it can never
     * drift out of sync with the function's own animation cadence.
     */
    int getInherentCycleDuration();

    /**
     * Whether this effect repeats every animation cycle ({@code true}, the default - e.g. a pulse)
     * or is a one-shot transition that fires only once ({@code false} - e.g. a fade in/out).
     */
    default boolean isRepeating() {
        return true;
    }
}
