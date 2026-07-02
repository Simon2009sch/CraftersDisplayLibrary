package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.IAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ICustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorable;
import org.bukkit.Color;

public class ColorAnimationSmooth extends ICustomTypeAnimationInterpolationFunction<Color, IColorable> {

    public ColorAnimationSmooth(int duration, Color start, Color end, IColorable obj) {
        super(duration, start, end, obj);
    }

    public ColorAnimationSmooth(int duration, int tick, Color start, Color end, IColorable obj) {
        super(duration, tick, start, end, obj);
    }

    private float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    @Override
    public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorable obj) {
        float progress = (float) tick / duration;
        progress = easeInOutCubic(progress);

        int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
        int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
        int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);

        Color interpolated = Color.fromRGB(r, g, b);
        obj.setColor(interpolated);
    }
}
