package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter;

import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.display.filledwireframecube.FilledWireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.effect.internal.TimedEffectRegistry;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Static registry that outlines {@link Block}s with a temporary, invisible-by-default
 * {@link ICuboidDisplay} and pulses it via a pluggable {@link IHighlighterFunction}.
 * <p>
 * At most one highlight is tracked per block; starting a new highlight on an already-highlighted
 * block implicitly removes the old one first. The display spawns fully transparent
 * (see {@link #createDisplay}) so nothing is visible until the highlighter function - or the
 * caller - sets a real color on it.
 * <p>
 * See also {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter}, which
 * is the same pattern applied to a player's screen instead of a block.
 */
public class BlockHighlighter {

    /**
     * Backing {@link TimedEffectRegistry}, held via composition rather than inheritance so this
     * class's own public static API stays unchanged. Overrides {@link
     * TimedEffectRegistry#initialTicksSinceLastAnimation} so a highlight's driving function fires on
     * the very first tick after registration, matching this class's historical behaviour.
     */
    private static final TimedEffectRegistry<Block, ICuboidDisplay> registry = new TimedEffectRegistry<>() {
        @Override
        protected void onRemove(Block key, ICuboidDisplay display) {
            display.remove();
        }

        @Override
        protected int initialTicksSinceLastAnimation(int animationDuration) {
            return animationDuration;
        }
    };

    /** Highlights {@code block} with a plain, non-animated {@link HighlightDisplayType#CUBE} display that never expires on its own. */
    public static ICuboidDisplay highlightBlock(Block block) {
        return highlightBlock(block, -1);
    }

    /** Highlights {@code block} with a plain, non-animated {@link HighlightDisplayType#CUBE} display that expires after {@code lifeTime} ticks. */
    public static ICuboidDisplay highlightBlock(Block block, int lifeTime) {
        return highlightBlock(block, HighlightDisplayType.CUBE, (IHighlighterFunction<ICuboidDisplay>) null, lifeTime);
    }

    /** Highlights {@code block} with a solid {@code color}, {@link HighlightDisplayType#CUBE} display until manually removed. */
    public static ICuboidDisplay highlightBlock(Block block, Color color) {
        return highlightBlock(block, HighlightDisplayType.CUBE, color);
    }

    /** Highlights {@code block} with a solid {@code color} display of the given {@code type} until manually removed. */
    public static ICuboidDisplay highlightBlock(Block block, HighlightDisplayType type, Color color) {
        return highlightBlock(block, type, color, -1);
    }

    /** Highlights {@code block} with a solid {@code color}, {@link HighlightDisplayType#CUBE} display that expires after {@code lifeTime} ticks. */
    public static ICuboidDisplay highlightBlock(Block block, Color color, int lifeTime) {
        return highlightBlock(block, HighlightDisplayType.CUBE, color, lifeTime);
    }

    /**
     * Highlights {@code block} with a solid, non-animated {@code color} display of the given
     * {@code type}, automatically calling {@link #unhighlightBlock} once {@code lifeTime} ticks have
     * elapsed (a value {@code <= 0} disables auto-removal).
     *
     * @param type     which kind of cuboid display to spawn
     * @param color    the solid color applied to the display once spawned
     * @param lifeTime ticks until this highlight is automatically removed; a value {@code <= 0}
     *                  disables auto-removal for this highlight (but still starts the checker task)
     * @return the display backing this highlight
     */
    public static ICuboidDisplay highlightBlock(Block block, HighlightDisplayType type, Color color, int lifeTime) {
        unhighlightBlock(block);
        ICuboidDisplay display = createDisplay(block, type);
        try {
            display.spawnDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        display.setColor(color);
        registry.register(block, display, null, lifeTime);
        return display;
    }

    /** Highlights {@code block} with a {@link HighlightDisplayType#CUBE} display until manually removed. */
    public static ICuboidDisplay highlightBlock(Block block, IHighlighterFunction<ICuboidDisplay> function) {
        return highlightBlock(block, HighlightDisplayType.CUBE, function);
    }

    /**
     * Highlights {@code block} until manually removed with {@link #unhighlightBlock}.
     *
     * @param type     which kind of cuboid display to spawn
     * @param function called every {@link IHighlighterFunction#getInherentCycleDuration()} ticks to
     *                  drive the visual effect; may be {@code null} for a static (non-animated) highlight
     * @return the display backing this highlight
     */
    public static ICuboidDisplay highlightBlock(Block block, HighlightDisplayType type, IHighlighterFunction<ICuboidDisplay> function) {
        return highlightBlock(block, type, function, -1);
    }

    /** Highlights {@code block} with a {@link HighlightDisplayType#CUBE} display that auto-removes itself after {@code lifeTime} ticks. */
    public static ICuboidDisplay highlightBlock(Block block, IHighlighterFunction<ICuboidDisplay> function, int lifeTime) {
        return highlightBlock(block, HighlightDisplayType.CUBE, function, lifeTime);
    }

    /**
     * Highlights {@code block}, automatically calling {@link #unhighlightBlock} once {@code lifeTime}
     * ticks have elapsed. Starts (or reuses) a single shared background task that checks every
     * highlight's remaining lifetime once per tick; that task self-cancels once no highlight is left.
     *
     * @param type     which kind of cuboid display to spawn
     * @param function called every {@link IHighlighterFunction#getInherentCycleDuration()} ticks to
     *                  drive the visual effect; may be {@code null} for a static (non-animated) highlight
     * @param lifeTime ticks until this highlight is automatically removed; a value {@code <= 0}
     *                  disables auto-removal for this highlight (but still starts the checker task)
     * @return the display backing this highlight
     */
    public static ICuboidDisplay highlightBlock(Block block, HighlightDisplayType type, IHighlighterFunction<ICuboidDisplay> function, int lifeTime) {
        unhighlightBlock(block);
        ICuboidDisplay display = createDisplay(block, type);
        try {
            display.spawnDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        registry.register(block, display, function, lifeTime);
        return display;
    }

    /**
     * Swaps the driving {@link IHighlighterFunction} on an already-highlighted block without
     * removing and recreating its display. No-ops if {@code block} isn't currently highlighted.
     */
    public static void setBlockAnimation(Block block, IHighlighterFunction<ICuboidDisplay> function) {
        registry.setAnimation(block, function);
    }

    /** Removes the highlight from {@code block}, if any, stopping its animation and despawning its display. */
    public static void unhighlightBlock(Block block) {
        registry.remove(block);
    }

    /** Removes every currently active block highlight. */
    public static void unhighlightAllBlocks() {
        registry.removeAll();
    }

    /** The display currently highlighting {@code block}, or {@code null} if it isn't highlighted. */
    public static ICuboidDisplay getHighlightDisplay(Block block) {
        return registry.getDisplay(block);
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

}
