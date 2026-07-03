package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.IViewTinterFunction;
import org.bukkit.Color;

public class PulsingTinter implements IViewTinterFunction {

    private final Color color;
    private final int minAlpha;
    private final int maxAlpha;
    private final int cycleDuration;

    public PulsingTinter(Color color, int cycleDuration) {
        this(color, 50, color.getAlpha(), cycleDuration);
    }

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

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<>(cycleDuration, dummyStart, dummyEnd, (IColorableDisplay) object) {
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
