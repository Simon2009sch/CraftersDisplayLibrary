package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.bukkit.Color;

/**
 * An {@link IDisplayable} whose visible color can be changed at runtime. Implemented by
 * {@code ColorDisplay}, {@code CubeColorDisplay}, {@code WireframeCubeColorDisplay},
 * {@code FilledWireframeCubeColorDisplay} and {@code LineColorDisplay}.
 * <p>
 * This is also the type operated on by the color-animation registry in
 * {@code GlobalAnimationTickHandler} and by
 * {@code AnimationFactory#registerColorAnimation}/{@code registerColorAnimationSmooth}.
 */
public interface IColorableDisplay extends IDisplayable{
    /** Sets this display's current color immediately. */
    void setColor(Color color);
}
