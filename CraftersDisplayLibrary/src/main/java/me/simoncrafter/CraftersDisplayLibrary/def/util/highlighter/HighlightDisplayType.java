package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter;

/** Which kind of {@link me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay} to use for a block highlight. */
public enum HighlightDisplayType {
    /** Solid coloured faces, no edges. */
    CUBE,
    /** Edges only, no faces. */
    WIREFRAME,
    /** Both solid faces and edges. */
    FILLED_WIREFRAME
}
