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

/**
 * Static registry that outlines {@link Block}s with a temporary, invisible-by-default
 * {@link ICuboidDisplay} and pulses it via a pluggable {@link IHighliterFunction}.
 * <p>
 * At most one highlight is tracked per block; starting a new highlight on an already-highlighted
 * block implicitly removes the old one first. The display spawns fully transparent
 * (see {@link #createDisplay}) so nothing is visible until the highlighter function - or the
 * caller - sets a real color on it.
 * <p>
 * See also {@link me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.ViewTinter}, which
 * is the same pattern applied to a player's screen instead of a block.
 */
public class BlockHighlighter {

    private static Map<Block, BlockHighlighterData> highlightedBlock = new HashMap<>();
    private static Runnable lifetimeCheckTask;

    /** Highlights {@code block} with a {@link HighlightDisplayType#CUBE} display until manually removed. */
    public static void highlightBlock(Block block, IHighliterFunction<ICuboidDisplay> function, int duration) {
        highlightBlock(block, HighlightDisplayType.CUBE, function, duration);
    }

    /**
     * Highlights {@code block} until manually removed with {@link #unhighlightBlock}.
     *
     * @param type     which kind of cuboid display to spawn
     * @param function called every {@code duration} ticks to drive the visual effect; may be
     *                  {@code null} for a static (non-animated) highlight
     * @param duration animation cycle length in ticks, passed to {@link IHighliterFunction#onAnimationRestart}
     */
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

    /** Highlights {@code block} with a {@link HighlightDisplayType#CUBE} display that auto-removes itself after {@code lifeTime} ticks. */
    public static void highlightBlock(Block block, IHighliterFunction<ICuboidDisplay> function, int lifeTime, int duration) {
        highlightBlock(block, HighlightDisplayType.CUBE, function, lifeTime, duration);
    }

    /**
     * Highlights {@code block}, automatically calling {@link #unhighlightBlock} once {@code lifeTime}
     * ticks have elapsed. Starts (or reuses) a single shared background task that checks every
     * highlight's remaining lifetime once per tick; that task self-cancels once no highlight is left.
     *
     * @param type     which kind of cuboid display to spawn
     * @param function called every {@code duration} ticks to drive the visual effect; may be
     *                  {@code null} for a static (non-animated) highlight
     * @param lifeTime ticks until this highlight is automatically removed; a value {@code <= 0}
     *                  disables auto-removal for this highlight (but still starts the checker task)
     * @param duration animation cycle length in ticks, passed to {@link IHighliterFunction#onAnimationRestart}
     */
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

    /** Highlights {@code block} with a plain, non-animated {@link HighlightDisplayType#CUBE} display that expires after {@code lifeTime} ticks. */
    public static void highlightBlock(Block block, int lifeTime) {
        highlightBlock(block, HighlightDisplayType.CUBE, null, lifeTime, 20);
    }

    /** Highlights {@code block} with a plain, non-animated {@link HighlightDisplayType#CUBE} display that never expires on its own. */
    public static void highlightBlock(Block block) {
        highlightBlock(block, -1);
    }

    /** Removes the highlight from {@code block}, if any, stopping its animation and despawning its display. */
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

    /** Removes every currently active block highlight. */
    public static void unhighlightAllBlocks() {
        for (Block block : new java.util.ArrayList<>(highlightedBlock.keySet())) {
            unhighlightBlock(block);
        }
    }

    /** The display currently highlighting {@code block}, or {@code null} if it isn't highlighted. */
    public static ICuboidDisplay getHighlightDisplay(Block block) {
        BlockHighlighterData style = highlightedBlock.get(block);
        return style != null ? style.getDisplay() : null;
    }

    /**
     * Builds a fully transparent {@link ICuboidDisplay} of the given {@code type}, centered on and
     * scaled slightly larger (1.01x) than {@code block} so its faces don't z-fight with the block's own.
     */
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

    /**
     * Lazily starts the single shared repeating task that expires highlights whose {@code lifeTime}
     * has elapsed. No-ops if already running; cancels itself once {@link #highlightedBlock} is empty.
     */
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
