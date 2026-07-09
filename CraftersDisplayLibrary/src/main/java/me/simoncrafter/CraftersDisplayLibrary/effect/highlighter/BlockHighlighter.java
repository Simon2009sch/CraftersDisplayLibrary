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

    /** Highlights {@code block} with a {@link HighlightDisplayType#CUBE} display until manually removed. */
    public static ICuboidDisplay highlightBlock(Block block, IHighlighterFunction<ICuboidDisplay> function, int duration) {
        return highlightBlock(block, HighlightDisplayType.CUBE, function, duration);
    }

    /**
     * Highlights {@code block} until manually removed with {@link #unhighlightBlock}.
     *
     * @param type     which kind of cuboid display to spawn
     * @param function called every {@code duration} ticks to drive the visual effect; may be
     *                  {@code null} for a static (non-animated) highlight
     * @param duration animation cycle length in ticks, passed to {@link IHighlighterFunction#onAnimationRestart}
     * @return the display backing this highlight
     */
    public static ICuboidDisplay highlightBlock(Block block, HighlightDisplayType type, IHighlighterFunction<ICuboidDisplay> function, int duration) {
        unhighlightBlock(block);
        ICuboidDisplay display = createDisplay(block, type);
        try {
            display.spawnDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        registry.register(block, duration, display, function, -1);
        return display;
    }

    /** Highlights {@code block} with a {@link HighlightDisplayType#CUBE} display that auto-removes itself after {@code lifeTime} ticks. */
    public static ICuboidDisplay highlightBlock(Block block, IHighlighterFunction<ICuboidDisplay> function, int lifeTime, int duration) {
        return highlightBlock(block, HighlightDisplayType.CUBE, function, lifeTime, duration);
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
     * @param duration animation cycle length in ticks, passed to {@link IHighlighterFunction#onAnimationRestart}
     * @return the display backing this highlight
     */
    public static ICuboidDisplay highlightBlock(Block block, HighlightDisplayType type, IHighlighterFunction<ICuboidDisplay> function, int lifeTime, int duration) {
        unhighlightBlock(block);
        ICuboidDisplay display = createDisplay(block, type);
        try {
            display.spawnDisplay();
        } catch (Exception e) {
            e.printStackTrace();
        }
        registry.register(block, duration, display, function, lifeTime);
        return display;
    }

    /** Highlights {@code block} with a plain, non-animated {@link HighlightDisplayType#CUBE} display that expires after {@code lifeTime} ticks. */
    public static ICuboidDisplay highlightBlock(Block block, int lifeTime) {
        return highlightBlock(block, HighlightDisplayType.CUBE, null, lifeTime, 20);
    }

    /** Highlights {@code block} with a plain, non-animated {@link HighlightDisplayType#CUBE} display that never expires on its own. */
    public static ICuboidDisplay highlightBlock(Block block) {
        return highlightBlock(block, -1);
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
