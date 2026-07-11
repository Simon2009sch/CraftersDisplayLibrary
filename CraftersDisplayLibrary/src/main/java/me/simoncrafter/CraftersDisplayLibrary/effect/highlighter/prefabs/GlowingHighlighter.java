package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction;
import org.bukkit.Color;

/**
 * {@link IHighlighterFunction} prefab that pulses a {@link ICuboidDisplay} from a dimmed version of
 * a base colour up to the full base colour every cycle, for a "glowing" effect.
 * <p>
 * Note: {@code maxBrightness} is accepted but not actually applied to the animation's end colour
 * (the end colour is always {@code baseColor} at full RGB) - only {@code minBrightness} affects
 * the visible range, scaling the dim starting colour down towards black.
 */
public class GlowingHighlighter implements IHighlighterFunction<ICuboidDisplay> {

    private Color baseColor;
    private int cycleDuration;
    private int minBrightness;
    private int maxBrightness;

    /** Same as the 4-argument constructor with {@code minBrightness = 100} and {@code maxBrightness = 255}. */
    public GlowingHighlighter(Color baseColor, int cycleDuration) {
        this(baseColor, 100, 255, cycleDuration);
    }

    /**
     * @param baseColor      the colour to glow towards each cycle
     * @param minBrightness  0-255 brightness of {@code baseColor} at the start of each cycle
     * @param maxBrightness  accepted for symmetry but currently unused - see class docs
     * @param cycleDuration  ticks per glow cycle
     */
    public GlowingHighlighter(Color baseColor, int minBrightness, int maxBrightness, int cycleDuration) {
        this.baseColor = baseColor;
        this.cycleDuration = cycleDuration;
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
    }

    @Override
    public void onAnimationRestart(ICuboidDisplay object) {
        Color dimColor = Color.fromRGB(
            (baseColor.getRed() * minBrightness) / 255,
            (baseColor.getGreen() * minBrightness) / 255,
            (baseColor.getBlue() * minBrightness) / 255
        );

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new CustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>(cycleDuration, dimColor, baseColor, object) {
            @Override
            public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
                float progress = (float) tick / duration;

                int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
                int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
                int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);

                Color interpolated = Color.fromRGB(r, g, b);
                obj.setColor(interpolated);
            }
        });
    }

    @Override
    public int getInherentCycleDuration() {
        return cycleDuration;
    }
}
