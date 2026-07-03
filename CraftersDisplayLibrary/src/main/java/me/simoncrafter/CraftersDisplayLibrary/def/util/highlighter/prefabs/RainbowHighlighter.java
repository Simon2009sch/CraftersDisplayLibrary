package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;

public class RainbowHighlighter implements IHighliterFunction<CubeColorDisplay> {

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
    public void onAnimationRestart(CubeColorDisplay object) {
        object.setColor(RAINBOW_COLORS[currentColorIndex]);
        currentColorIndex = (currentColorIndex + 1) % RAINBOW_COLORS.length;
    }
}
