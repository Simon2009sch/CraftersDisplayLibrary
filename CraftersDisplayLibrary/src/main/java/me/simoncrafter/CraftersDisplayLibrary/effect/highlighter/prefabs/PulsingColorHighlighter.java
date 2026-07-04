package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction;
import org.bukkit.Color;

/**
 * {@link IHighlighterFunction} prefab that oscillates a {@link ICuboidDisplay}'s alpha between
 * {@code minAlpha} and {@code maxAlpha} following a sine wave, for a smooth "breathing" pulse
 * rather than a linear fade back and forth.
 */
public class PulsingColorHighlighter implements IHighlighterFunction<ICuboidDisplay> {

    private final Color baseColor;
    private final int minAlpha;
    private final int maxAlpha;
    private final int cycleDuration;

    /** Same as the 4-argument constructor with {@code minAlpha = 50} and {@code maxAlpha = color.getAlpha()}. */
    public PulsingColorHighlighter(Color color, int cycleDuration) {
        this(color, 50, color.getAlpha(), cycleDuration);
    }

    /**
     * @param color         RGB of the pulse; its own alpha is ignored in favour of {@code minAlpha}/{@code maxAlpha}
     *                       when this constructor is used directly
     * @param minAlpha      alpha at the trough of the sine wave
     * @param maxAlpha      alpha at the peak of the sine wave
     * @param cycleDuration ticks per full sine cycle
     */
    public PulsingColorHighlighter(Color color, int minAlpha, int maxAlpha, int cycleDuration) {
        this.baseColor = color;
        this.minAlpha = minAlpha;
        this.maxAlpha = maxAlpha;
        this.cycleDuration = cycleDuration;
    }

    @Override
    public void onAnimationRestart(ICuboidDisplay object) {
        int r = baseColor.getRed();
        int g = baseColor.getGreen();
        int b = baseColor.getBlue();

        Color dummyStart = Color.fromARGB(minAlpha, r, g, b);
        Color dummyEnd = Color.fromARGB(maxAlpha, r, g, b);

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new CustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>(cycleDuration, dummyStart, dummyEnd, object) {
            @Override
            public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
                try {
                    float progress = (float) tick / duration;
                    float sineWave = (float) Math.sin(progress * Math.PI * 2);
                    float normalizedSine = (sineWave + 1) / 2;

                    int alpha = (int) (minAlpha + (maxAlpha - minAlpha) * normalizedSine);
                    Color interpolated = Color.fromARGB(alpha, r, g, b);
                    obj.setColor(interpolated);
                } catch (Exception e) {
                    // Display may have been removed, silently ignore
                }
            }
        });
    }
}
