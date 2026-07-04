package me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.IViewTinterFunction;
import org.bukkit.Color;

/**
 * {@link IViewTinterFunction} prefab that repeatedly oscillates a player's tint alpha between
 * {@code minAlpha} and {@code maxAlpha} following a sine wave, mirroring
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.PulsingColorHighlighter}
 * but for {@link me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter} instead of
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter}.
 */
public class PulsingTinter implements IViewTinterFunction {

    private final Color color;
    private final int minAlpha;
    private final int maxAlpha;
    private final int cycleDuration;

    /** Same as the 4-argument constructor with {@code minAlpha = 50} and {@code maxAlpha = color.getAlpha()}. */
    public PulsingTinter(Color color, int cycleDuration) {
        this(color, 50, color.getAlpha(), cycleDuration);
    }

    /**
     * @param color         RGB of the pulse; its own alpha is ignored in favour of {@code minAlpha}/{@code maxAlpha}
     *                       when this constructor is used directly
     * @param minAlpha      alpha at the trough of the sine wave
     * @param maxAlpha      alpha at the peak of the sine wave
     * @param cycleDuration ticks per full sine cycle
     */
    public PulsingTinter(Color color, int minAlpha, int maxAlpha, int cycleDuration) {
        this.color = color;
        this.minAlpha = minAlpha;
        this.maxAlpha = maxAlpha;
        this.cycleDuration = cycleDuration;
    }

    @Override
    public void onAnimationRestart(CubeColorDisplay object) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        Color dummyStart = Color.fromARGB(minAlpha, r, g, b);
        Color dummyEnd = Color.fromARGB(maxAlpha, r, g, b);

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new CustomTypeAnimationInterpolationFunction<>(cycleDuration, dummyStart, dummyEnd, (IColorableDisplay) object) {
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
