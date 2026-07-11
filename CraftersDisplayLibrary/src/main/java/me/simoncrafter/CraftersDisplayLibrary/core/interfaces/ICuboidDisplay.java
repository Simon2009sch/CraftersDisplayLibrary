package me.simoncrafter.CraftersDisplayLibrary.core.interfaces;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;

/**
 * A colourable display shaped like a cuboid (a plain solid cube, a wireframe cube, or a filled
 * wireframe cube), spawned around some volume - typically a block. Implemented by
 * {@code CubeColorDisplay}, {@code WireframeCubeColorDisplay} and {@code FilledWireframeCubeColorDisplay}
 * so that utilities like {@code BlockHighlighter} can work with any of them interchangeably.
 */
public interface ICuboidDisplay extends IColorableDisplay {

    void spawnDisplay();

    /** Every current implementor extends PositionObject. Centralizes the cast here (documented,
     * single call site) instead of each caller doing its own unchecked cast. */
    default PositionObject asPositionObject() {
        return (PositionObject) this;
    }
}
