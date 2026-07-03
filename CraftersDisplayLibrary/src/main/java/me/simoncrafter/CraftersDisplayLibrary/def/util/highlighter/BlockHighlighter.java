package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class BlockHighlighter {

    private static Map<Block, BlockHighlighterStyle> highlightedBlock = new HashMap<>();

    public static void highlightBlock(Block block, IHighliterFunction<CubeColorDisplay> function, int duration) {
        unhighlightBlock(block);
        CubeColorDisplay display = CubeColorDisplay.create(
                block.getLocation().add(new Vector(0.5f, 0.5f, 0.5f)),
                new Vector3f(1.01f, 1.01f, 1.01f),
                new Vector3f(0, 0, 0),
                new Quaternionf(),
                new CubeColorInformation(Color.fromARGB(0,0,0,0))
        );
        BlockHighlighterStyle style = new BlockHighlighterStyle(duration, display, function);
        highlightedBlock.put(block, style);
        style.start();
    }

    public static void unhighlightBlock(Block block) {
        BlockHighlighterStyle style = highlightedBlock.get(block);
        if (style != null) {
            style.stop();
            style.getDisplay().remove();
            highlightedBlock.remove(block);
        }
    }

}
