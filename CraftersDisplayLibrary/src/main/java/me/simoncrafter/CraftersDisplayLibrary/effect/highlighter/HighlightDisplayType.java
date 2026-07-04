package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter;

/** Which kind of {@link me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay} to use for a block highlight. */
public enum HighlightDisplayType {
    /** Solid coloured faces, no edges. */
    CUBE,
    /** Edges only, no faces. */
    WIREFRAME,
    /** Both solid faces and edges. */
    FILLED_WIREFRAME
}
