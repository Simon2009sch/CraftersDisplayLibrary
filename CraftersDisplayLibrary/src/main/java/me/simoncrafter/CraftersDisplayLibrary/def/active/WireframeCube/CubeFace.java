package me.simoncrafter.CraftersDisplayLibrary.def.active.WireframeCube;

/**
 * The 6 faces of a cube. Each face maps to the 4 {@link CubeEdge}s that border it,
 * so colouring a face colours all of its edges.
 */
public enum CubeFace {
    TOP,
    BOTTOM,
    FRONT,
    BACK,
    LEFT,
    RIGHT;

    /** The four edges that make up this face. */
    public CubeEdge[] getEdges() {
        return switch (this) {
            case TOP -> new CubeEdge[]{CubeEdge.TOP_FRONT, CubeEdge.TOP_BACK, CubeEdge.TOP_LEFT, CubeEdge.TOP_RIGHT};
            case BOTTOM -> new CubeEdge[]{CubeEdge.BOTTOM_FRONT, CubeEdge.BOTTOM_BACK, CubeEdge.BOTTOM_LEFT, CubeEdge.BOTTOM_RIGHT};
            case FRONT -> new CubeEdge[]{CubeEdge.TOP_FRONT, CubeEdge.BOTTOM_FRONT, CubeEdge.FRONT_LEFT, CubeEdge.FRONT_RIGHT};
            case BACK -> new CubeEdge[]{CubeEdge.TOP_BACK, CubeEdge.BOTTOM_BACK, CubeEdge.BACK_LEFT, CubeEdge.BACK_RIGHT};
            case LEFT -> new CubeEdge[]{CubeEdge.TOP_LEFT, CubeEdge.BOTTOM_LEFT, CubeEdge.FRONT_LEFT, CubeEdge.BACK_LEFT};
            case RIGHT -> new CubeEdge[]{CubeEdge.TOP_RIGHT, CubeEdge.BOTTOM_RIGHT, CubeEdge.FRONT_RIGHT, CubeEdge.BACK_RIGHT};
        };
    }

    /**
     * Resolves a face from a case-insensitive name.
     *
     * @return the matching face, or {@code null} if none matches
     */
    public static CubeFace fromString(String name) {
        if (name == null) return null;
        for (CubeFace face : values()) {
            if (face.name().equalsIgnoreCase(name.trim())) {
                return face;
            }
        }
        return null;
    }
}
