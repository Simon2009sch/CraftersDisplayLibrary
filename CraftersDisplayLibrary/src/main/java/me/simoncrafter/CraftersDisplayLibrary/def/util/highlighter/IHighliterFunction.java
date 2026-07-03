package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

@FunctionalInterface
public interface IHighliterFunction<V> {
    /**
     * Pulses every time the animation duration elapses
     * @param object
     */
    void onAnimationRestart(V object);
}
