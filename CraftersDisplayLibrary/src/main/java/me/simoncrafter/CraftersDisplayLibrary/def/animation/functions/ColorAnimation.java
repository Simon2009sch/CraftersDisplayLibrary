package me.simoncrafter.CraftersDisplayLibrary.def.animation.functions;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import org.bukkit.Color;

/**
 * Linearly interpolates the RGB channels of a colour from {@code start} to {@code end} over
 * {@code duration} ticks.
 * <p>
 * See {@link ColorAnimationSmooth} for the eased equivalent.
 *
 * @apiNote {@code nextTick} computes the interpolated colour but does not currently call
 * {@link IColorableDisplay#setColor} to apply it - this implementation is a no-op with respect
 * to the display and should not be relied on. Use {@link ColorAnimationSmooth} instead, which
 * does apply the colour.
 */
public class ColorAnimation extends ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> {

    /** Creates a tween starting at tick 0. */
    public ColorAnimation(int duration, Color start, Color end, IColorableDisplay obj) {
        super(duration, start, end, obj);
    }

    /** Creates a tween resuming at an arbitrary tick. */
    public ColorAnimation(int duration, int tick, Color start, Color end, IColorableDisplay obj) {
        super(duration, tick, start, end, obj);
    }

    /**
     * {@inheritDoc}
     *
     * @apiNote does not apply the interpolated colour to {@code obj} - see the class-level note.
     */
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
