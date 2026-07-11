package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction;
import org.bukkit.Color;

/**
 * {@link IHighlighterFunction} prefab that steps a {@link ICuboidDisplay} through a fixed 7-colour
 * rainbow, one colour per animation cycle (no interpolation between colours).
 * <p>
 * Each instance keeps its own position in the cycle ({@code currentColorIndex}); highlighting
 * multiple blocks with the <em>same</em> {@code RainbowHighlighter} instance means they all share
 * and advance that one cycle position rather than each starting fresh at red.
 */
public class RainbowHighlighter implements IHighlighterFunction<ICuboidDisplay> {

    private static final Color[] RAINBOW_COLORS = {
        Color.fromRGB(255, 0, 0),
        Color.fromRGB(255, 127, 0),
        Color.fromRGB(255, 255, 0),
        Color.fromRGB(0, 255, 0),
        Color.fromRGB(0, 0, 255),
        Color.fromRGB(75, 0, 130),
        Color.fromRGB(148, 0, 211)
    };

    private int currentColorIndex = 0;
    private final int cycleDuration;

    /** @param cycleDuration ticks between colour steps */
    public RainbowHighlighter(int cycleDuration) {
        this.cycleDuration = cycleDuration;
    }

    /** Same as the 1-argument constructor with {@code cycleDuration = 20} (the prior implicit cadence). */
    public RainbowHighlighter() {
        this(20);
    }

    @Override
    public void onAnimationRestart(ICuboidDisplay object) {
        object.setColor(RAINBOW_COLORS[currentColorIndex]);
        currentColorIndex = (currentColorIndex + 1) % RAINBOW_COLORS.length;
    }

    @Override
    public int getInherentCycleDuration() {
        return cycleDuration;
    }
}
