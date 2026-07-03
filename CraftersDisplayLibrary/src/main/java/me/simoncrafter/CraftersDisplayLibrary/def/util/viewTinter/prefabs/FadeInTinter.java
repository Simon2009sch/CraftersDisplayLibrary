package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.IViewTinterFunction;
import org.bukkit.Color;

public class FadeInTinter implements IViewTinterFunction {

    private final Color color;
    private final int fadeDuration;

    public FadeInTinter(Color color, int fadeDuration) {
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

        Color transparentColor = Color.fromARGB(0, r, g, b);
        Color opaqueColor = Color.fromARGB(color.getAlpha(), r, g, b);

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<>(fadeDuration, transparentColor, opaqueColor, (IColorableDisplay) object) {
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
