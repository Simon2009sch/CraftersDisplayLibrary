package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;

/**
 * {@link IHighliterFunction} prefab that pulses a {@link ICuboidDisplay} from a dimmed version of
 * a base colour up to the full base colour every cycle, for a "glowing" effect.
 * <p>
 * Note: {@code maxBrightness} is accepted but not actually applied to the animation's end colour
 * (the end colour is always {@code baseColor} at full RGB) - only {@code minBrightness} affects
 * the visible range, scaling the dim starting colour down towards black.
 */
public class GlowingHighlighter implements IHighliterFunction<ICuboidDisplay> {

    private Color baseColor;
    private int cycleDuration;
    private int minBrightness;
    private int maxBrightness;

    /** Same as the 4-argument constructor with {@code minBrightness = 100} and {@code maxBrightness = 255}. */
    public GlowingHighlighter(Color baseColor, int cycleDuration) {
        this(baseColor, cycleDuration, 100, 255);
    }

    /**
     * @param baseColor      the colour to glow towards each cycle
     * @param cycleDuration  ticks per glow cycle
     * @param minBrightness  0-255 brightness of {@code baseColor} at the start of each cycle
     * @param maxBrightness  accepted for symmetry but currently unused - see class docs
     */
    public GlowingHighlighter(Color baseColor, int cycleDuration, int minBrightness, int maxBrightness) {
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

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>(cycleDuration, dimColor, baseColor, object) {
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
}
