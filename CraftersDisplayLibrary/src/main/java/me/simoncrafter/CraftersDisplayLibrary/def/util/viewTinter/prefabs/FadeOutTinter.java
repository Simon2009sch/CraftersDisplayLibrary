package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.IViewTinterFunction;
import org.bukkit.Color;

/**
 * {@link IViewTinterFunction} prefab that fades a player's tint out from {@code color}'s own alpha
 * to fully transparent, once - a one-shot transition, not a repeating pulse.
 */
public class FadeOutTinter implements IViewTinterFunction {

    private final Color color;
    private final int fadeDuration;

    /**
     * @param color        the tint's RGB and starting alpha
     * @param fadeDuration ticks the fade-out takes
     */
    public FadeOutTinter(Color color, int fadeDuration) {
        this.color = color;
        this.fadeDuration = fadeDuration;
    }

    @Override
    public boolean isRepeating() {
        return false;
    }

    @Override
    public void onAnimationRestart(CubeColorDisplay object) {
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        Color opaqueColor = Color.fromARGB(color.getAlpha(), r, g, b);
        Color transparentColor = Color.fromARGB(0, r, g, b);

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<>(fadeDuration, opaqueColor, transparentColor, (IColorableDisplay) object) {
            @Override
            public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
                try {
                    float progress = (float) tick / duration;
                    int alpha = (int) (startColor.getAlpha() + (endColor.getAlpha() - startColor.getAlpha()) * progress);
                    Color interpolated = Color.fromARGB(alpha, r, g, b);
                    obj.setColor(interpolated);
                } catch (Exception e) {
                    // Display may have been removed, silently ignore
                }
            }
        });
    }
}
