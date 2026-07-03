package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.def.active.FilledWireframeCube.FilledWireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.WireframeCube.WireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.WireframeCube.WireframeCubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.HashMap;
import java.util.Map;

public class BlockHighlighter {

    private static Map<Block, BlockHighlighterData> highlightedBlock = new HashMap<>();
    private static Runnable lifetimeCheckTask;

    public static void highlightBlock(Block block, IHighliterFunction<ICuboidDisplay> function, int duration) {
        highlightBlock(block, HighlightDisplayType.CUBE, function, duration);
    }

    public static void highlightBlock(Block block, HighlightDisplayType type, IHighliterFunction<ICuboidDisplay> function, int duration) {
        unhighlightBlock(block);
        ICuboidDisplay display = createDisplay(block, type);
        try {
            display.spawnDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BlockHighlighterData style = new BlockHighlighterData(duration, display, function);
        highlightedBlock.put(block, style);
        style.start();
    }

    public static void highlightBlock(Block block, IHighliterFunction<ICuboidDisplay> function, int lifeTime, int duration) {
        highlightBlock(block, HighlightDisplayType.CUBE, function, lifeTime, duration);
    }

    public static void highlightBlock(Block block, HighlightDisplayType type, IHighliterFunction<ICuboidDisplay> function, int lifeTime, int duration) {
        unhighlightBlock(block);
        ICuboidDisplay display = createDisplay(block, type);
        try {
            display.spawnDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        BlockHighlighterData style = new BlockHighlighterData(duration, display, function, lifeTime);
        highlightedBlock.put(block, style);
        style.start();
        startLifetimeChecker();
    }

    public static void highlightBlock(Block block, int lifeTime) {
        highlightBlock(block, HighlightDisplayType.CUBE, null, lifeTime, 20);
    }

    public static void highlightBlock(Block block) {
        highlightBlock(block, -1);
    }

    public static void unhighlightBlock(Block block) {
        BlockHighlighterData style = highlightedBlock.get(block);
        if (style != null) {
            style.stop();
            ICuboidDisplay display = style.getDisplay();
            GlobalAnimationTickHandler.removeColorAnimation(display);
            display.remove();
            highlightedBlock.remove(block);
        }
    }

    public static void unhighlightAllBlocks() {
        for (Block block : new java.util.ArrayList<>(highlightedBlock.keySet())) {
            unhighlightBlock(block);
        }
    }

    public static ICuboidDisplay getHighlightDisplay(Block block) {
        BlockHighlighterData style = highlightedBlock.get(block);
        return style != null ? style.getDisplay() : null;
    }

    private static ICuboidDisplay createDisplay(Block block, HighlightDisplayType type) {
        Location center = block.getLocation().add(new Vector(0.5f, 0.5f, 0.5f));
        Vector3f scale = new Vector3f(1.01f, 1.01f, 1.01f);
        Vector3f origin = new Vector3f(0, 0, 0);
        Color invisible = Color.fromARGB(0, 0, 0, 0);

        return switch (type) {
            case CUBE -> CubeColorDisplay.create(
                    center,
                    scale,
                    origin,
                    new Quaternionf(),
                    new CubeColorInformation(invisible)
            );
            case WIREFRAME -> WireframeCubeColorDisplay.create(
                    center,
                    scale,
                    origin,
                    new Quaternionf(),
                    new WireframeCubeColorInformation(invisible),
                    false,
                    0.02f
            );
            case FILLED_WIREFRAME -> FilledWireframeCubeColorDisplay.create(
                    center,
                    scale,
                    origin,
                    new Quaternionf(),
                    new CubeColorInformation(invisible),
                    new WireframeCubeColorInformation(invisible)
            );
        };
    }

    private static void startLifetimeChecker() {
        if (lifetimeCheckTask != null) return;

        org.bukkit.scheduler.BukkitRunnable checker = new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                java.util.ArrayList<Block> blocksToRemove = new java.util.ArrayList<>();
                for (Map.Entry<Block, BlockHighlighterData> entry : highlightedBlock.entrySet()) {
                    if (entry.getValue().getLifeTime() > 0 && entry.getValue().getTickCounter() >= entry.getValue().getLifeTime()) {
                        blocksToRemove.add(entry.getKey());
                    }
                }
                for (Block block : blocksToRemove) {
                    unhighlightBlock(block);
                }
                if (highlightedBlock.isEmpty()) {
                    cancel();
                    lifetimeCheckTask = null;
                }
            }
        };
        lifetimeCheckTask = checker::run;
        checker.runTaskTimer(PluginHolder.getPlugin(), 0, 1);
    }

}
