package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;

public class GlowingHighlighter implements IHighliterFunction<CubeColorDisplay> {

    private Color baseColor;
    private int cycleDuration;
    private int minBrightness;
    private int maxBrightness;

    public GlowingHighlighter(Color baseColor, int cycleDuration) {
        this(baseColor, cycleDuration, 100, 255);
    }

    public GlowingHighlighter(Color baseColor, int cycleDuration, int minBrightness, int maxBrightness) {
        this.baseColor = baseColor;
        this.cycleDuration = cycleDuration;
        this.minBrightness = minBrightness;
        this.maxBrightness = maxBrightness;
    }

    @Override
    public void onAnimationRestart(CubeColorDisplay object) {
        Color dimColor = Color.fromRGB(
            (baseColor.getRed() * minBrightness) / 255,
            (baseColor.getGreen() * minBrightness) / 255,
            (baseColor.getBlue() * minBrightness) / 255
        );

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>(cycleDuration, dimColor, baseColor, (IColorableDisplay) object) {
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
