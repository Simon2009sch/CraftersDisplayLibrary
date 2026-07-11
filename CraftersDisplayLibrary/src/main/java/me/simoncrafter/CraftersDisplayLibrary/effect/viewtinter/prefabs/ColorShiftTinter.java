package me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.IViewTinterFunction;
import org.bukkit.Color;

/**
 * {@link IViewTinterFunction} prefab that transitions a player's tint from {@code startColor} to
 * {@code endColor} (including alpha) once, then stops - a one-shot transition, not a repeating pulse.
 */
public class ColorShiftTinter implements IViewTinterFunction {

    private final Color startColor;
    private final Color endColor;
    private final int cycleDuration;

    /**
     * @param startColor    ARGB colour at the start of the transition
     * @param endColor      ARGB colour at the end of the transition
     * @param cycleDuration ticks the transition takes
     */
    public ColorShiftTinter(Color startColor, Color endColor, int cycleDuration) {
        this.startColor = startColor;
        this.endColor = endColor;
        this.cycleDuration = cycleDuration;
    }

    @Override
    public boolean isRepeating() {
        return false;
    }

    @Override
    public void onAnimationRestart(CubeColorDisplay object) {
        GlobalAnimationTickHandler.registerNewColorAnimation(object, new CustomTypeAnimationInterpolationFunction<>(cycleDuration, startColor, endColor, (IColorableDisplay) object) {
            @Override
            public void nextTick(int duration, int tick, Color color1, Color color2, IColorableDisplay obj) {
                try {
                    float progress = (float) tick / duration;

                    int r = (int) (color1.getRed() + (color2.getRed() - color1.getRed()) * progress);
                    int g = (int) (color1.getGreen() + (color2.getGreen() - color1.getGreen()) * progress);
                    int b = (int) (color1.getBlue() + (color2.getBlue() - color1.getBlue()) * progress);
                    int a = (int) (color1.getAlpha() + (color2.getAlpha() - color1.getAlpha()) * progress);

                    Color interpolated = Color.fromARGB(a, r, g, b);
                    obj.setColor(interpolated);
                } catch (Exception e) {
                    // Display may have been removed, silently ignore
                }
            }
        });
    }

    @Override
    public int getInherentCycleDuration() {
        return cycleDuration;
    }
}
