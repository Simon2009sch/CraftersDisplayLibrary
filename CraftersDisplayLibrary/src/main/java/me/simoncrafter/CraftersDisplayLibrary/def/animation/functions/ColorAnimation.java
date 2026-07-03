package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import org.bukkit.Color;

public class ColorAnimation extends ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> {

    public ColorAnimation(int duration, Color start, Color end, IColorableDisplay obj) {
        super(duration, start, end, obj);
    }

    public ColorAnimation(int duration, int tick, Color start, Color end, IColorableDisplay obj) {
        super(duration, tick, start, end, obj);
    }

    @Override
    public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
        float progress = (float) tick / duration;

        int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
        int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
        int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);

        Color interpolated = Color.fromRGB(r, g, b);
        // Apply color to object - this would need a method on PositionObject or be done through a wrapper
        // For now, we store it but implementation depends on how colors are applied in the system
    }
}
