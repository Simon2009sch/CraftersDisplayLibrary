package me.simoncrafter.CraftersDisplayLibrary.core.interfaces;

import org.bukkit.Color;

/**
 * An {@link IDisplayable} whose visible color can be changed at runtime. Implemented by
 * {@code ColorDisplay}, {@code CubeColorDisplay}, {@code WireframeCubeColorDisplay},
 * {@code FilledWireframeCubeColorDisplay} and {@code LineColorDisplay}.
 * <p>
 * This is also the type operated on by the color-animation registry in
 * {@code GlobalAnimationTickHandler} and by
 * {@code AnimationFactory#registerColorAnimation}/{@code registerColorAnimationSmooth}.
 * <p>
 * Also declares see-through control ({@link #isSeeThrough()}/{@link #setSeeThrough(boolean)}): every
 * current implementor already supports it, so this makes the existing duplication a declared
 * contract instead of an ad-hoc convention.
 */
public interface IColorableDisplay extends IDisplayable{
    /** Sets this display's current color immediately. */
    void setColor(Color color);

    /** Whether this display currently renders as see-through. */
    boolean isSeeThrough();

    /** Sets whether this display renders as see-through. */
    void setSeeThrough(boolean seeThrough);
}
