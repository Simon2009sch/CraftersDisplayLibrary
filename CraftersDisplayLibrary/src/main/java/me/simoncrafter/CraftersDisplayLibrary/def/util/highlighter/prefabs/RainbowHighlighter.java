package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;

/**
 * {@link IHighliterFunction} prefab that steps a {@link ICuboidDisplay} through a fixed 7-colour
 * rainbow, one colour per animation cycle (no interpolation between colours).
 * <p>
 * Each instance keeps its own position in the cycle ({@code currentColorIndex}); highlighting
 * multiple blocks with the <em>same</em> {@code RainbowHighlighter} instance means they all share
 * and advance that one cycle position rather than each starting fresh at red.
 */
public class RainbowHighlighter implements IHighliterFunction<ICuboidDisplay> {

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

    @Override
    public void onAnimationRestart(ICuboidDisplay object) {
        object.setColor(RAINBOW_COLORS[currentColorIndex]);
        currentColorIndex = (currentColorIndex + 1) % RAINBOW_COLORS.length;
    }
}
