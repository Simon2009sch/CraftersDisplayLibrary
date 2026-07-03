package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;

public class PulsingColorHighlighter implements IHighliterFunction<ICuboidDisplay> {

    private final Color baseColor;
    private final int minAlpha;
    private final int maxAlpha;
    private final int cycleDuration;

    public PulsingColorHighlighter(Color color, int cycleDuration) {
        this(color, 50, color.getAlpha(), cycleDuration);
    }

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

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>(cycleDuration, dummyStart, dummyEnd, object) {
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
