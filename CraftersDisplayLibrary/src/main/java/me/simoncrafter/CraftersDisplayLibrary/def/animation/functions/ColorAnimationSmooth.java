package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import org.bukkit.Color;

/**
 * Interpolates the RGB channels of a colour from {@code start} to {@code end} over
 * {@code duration} ticks, passed through an ease-in-out-cubic curve, and applies the result to
 * the target display every tick.
 * <p>
 * Unlike {@link ColorAnimation}, this implementation actually calls
 * {@link IColorableDisplay#setColor} with the interpolated colour, so it is the recommended
 * colour interpolator to use (see {@link me.simoncrafter.CraftersDisplayLibrary.def.animation.AnimationFactory#registerColorAnimationSmooth}).
 */
public class ColorAnimationSmooth extends ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> {

    /** Creates a tween starting at tick 0. */
    public ColorAnimationSmooth(int duration, Color start, Color end, IColorableDisplay obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public ColorAnimationSmooth(int duration, int tick, Color start, Color end, IColorableDisplay obj) {
        super(duration, tick, start, end, obj);
    }

    /** Ease-in-out-cubic easing curve: slow start, fast middle, slow end. */
    private float easeInOutCubic(float t) {
        return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
    }

    @Override
    public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
        float progress = (float) tick / duration;
        progress = easeInOutCubic(progress);

        int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
        int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
        int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);

        Color interpolated = Color.fromRGB(r, g, b);
        obj.setColor(interpolated);
    }
}
