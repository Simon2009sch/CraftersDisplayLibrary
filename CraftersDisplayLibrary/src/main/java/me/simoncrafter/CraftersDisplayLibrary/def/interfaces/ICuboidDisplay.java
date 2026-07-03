package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

/**
 * A colourable display shaped like a cuboid (a plain solid cube, a wireframe cube, or a filled
 * wireframe cube), spawned around some volume - typically a block. Implemented by
 * {@code CubeColorDisplay}, {@code WireframeCubeColorDisplay} and {@code FilledWireframeCubeColorDisplay}
 * so that utilities like {@code BlockHighlighter} can work with any of them interchangeably.
 */
public interface ICuboidDisplay extends IColorableDisplay {

    void spawnDisplay();

    boolean isSeeTrough();

    void setSeeTrough(boolean seeTrough);
}
