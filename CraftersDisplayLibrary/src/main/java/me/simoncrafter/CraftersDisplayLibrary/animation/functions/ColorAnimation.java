package me.simoncrafter.CraftersDisplayLibrary.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.animation.easing.EasingCurve;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import org.bukkit.Color;

/**
 * Interpolates the RGB channels of a colour from {@code start} to {@code end} over
 * {@code duration} ticks, remapping progress through an {@link EasingCurve} before applying the
 * linear interpolation, and applies the interpolated colour to the target display via
 * {@link IColorableDisplay#setColor} every tick. Defaults to {@link EasingCurve#LINEAR}
 * (constant-speed colour change) when no curve is specified.
 */
public class ColorAnimation extends CustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> {

    private final EasingCurve easingCurve;

    /** Creates a tween starting at tick 0, using {@link EasingCurve#LINEAR}. */
    public ColorAnimation(int duration, Color start, Color end, IColorableDisplay obj) {
        this(duration, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween resuming at an arbitrary tick, using {@link EasingCurve#LINEAR}. */
    public ColorAnimation(int duration, int tick, Color start, Color end, IColorableDisplay obj) {
        this(duration, tick, start, end, obj, EasingCurve.LINEAR);
    }

    /** Creates a tween starting at tick 0, eased by {@code easingCurve}. */
    public ColorAnimation(int duration, Color start, Color end, IColorableDisplay obj, EasingCurve easingCurve) {
        super(duration, start, end, obj);
        this.easingCurve = easingCurve;
    }

    /** Creates a tween resuming at an arbitrary tick, eased by {@code easingCurve}. */
    public ColorAnimation(int duration, int tick, Color start, Color end, IColorableDisplay obj, EasingCurve easingCurve) {
        super(duration, tick, start, end, obj);
        this.easingCurve = easingCurve;
    }

    @Override
    public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
        float progress = easingCurve.ease((float) tick / duration);

        int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * progress);
        int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * progress);
        int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * progress);

        Color interpolated = Color.fromRGB(r, g, b);
        obj.setColor(interpolated);
    }
}
