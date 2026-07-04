package me.simoncrafter.CraftersDisplayLibrary.display.wireframecube;

import org.joml.Vector3f;

/**
 * The 12 edges of a unit cube (corners at ±0.5), each named by the two faces that meet at it.
 * <p>
 * Coordinate convention (matching the rest of the library): +x = right, +y = top, +z = front.
 */
public enum CubeEdge {
    // Top face edges (y = +0.5)
    TOP_FRONT(-0.5f, 0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
    TOP_BACK(-0.5f, 0.5f, -0.5f, 0.5f, 0.5f, -0.5f),
    TOP_LEFT(-0.5f, 0.5f, -0.5f, -0.5f, 0.5f, 0.5f),
    TOP_RIGHT(0.5f, 0.5f, -0.5f, 0.5f, 0.5f, 0.5f),

    // Bottom face edges (y = -0.5)
    BOTTOM_FRONT(-0.5f, -0.5f, 0.5f, 0.5f, -0.5f, 0.5f),
    BOTTOM_BACK(-0.5f, -0.5f, -0.5f, 0.5f, -0.5f, -0.5f),
    BOTTOM_LEFT(-0.5f, -0.5f, -0.5f, -0.5f, -0.5f, 0.5f),
    BOTTOM_RIGHT(0.5f, -0.5f, -0.5f, 0.5f, -0.5f, 0.5f),

    // Vertical side edges (running along y)
    FRONT_LEFT(-0.5f, -0.5f, 0.5f, -0.5f, 0.5f, 0.5f),
    FRONT_RIGHT(0.5f, -0.5f, 0.5f, 0.5f, 0.5f, 0.5f),
    BACK_LEFT(-0.5f, -0.5f, -0.5f, -0.5f, 0.5f, -0.5f),
    BACK_RIGHT(0.5f, -0.5f, -0.5f, 0.5f, 0.5f, -0.5f);

    private final float sx, sy, sz;
    private final float ex, ey, ez;

    CubeEdge(float sx, float sy, float sz, float ex, float ey, float ez) {
        this.sx = sx;
        this.sy = sy;
        this.sz = sz;
        this.ex = ex;
        this.ey = ey;
        this.ez = ez;
    }

    /** Start corner of this edge, in unit-cube local space (a fresh vector each call). */
    public Vector3f getStart() {
        return new Vector3f(sx, sy, sz);
    }

    /** End corner of this edge, in unit-cube local space (a fresh vector each call). */
    public Vector3f getEnd() {
        return new Vector3f(ex, ey, ez);
    }

    /** Direction (end - start) of this edge, in unit-cube local space (a fresh vector each call). */
    public Vector3f getDirection() {
        return new Vector3f(ex - sx, ey - sy, ez - sz);
    }

    /**
     * Resolves an edge from a case-insensitive name, accepting both {@code TOP_FRONT} and {@code topFront}.
     *
     * @return the matching edge, or {@code null} if none matches
     */
    public static CubeEdge fromString(String name) {
        if (name == null) return null;
        String normalized = name.trim().replace("_", "").replace("-", "").toLowerCase();
        for (CubeEdge edge : values()) {
            if (edge.name().replace("_", "").toLowerCase().equals(normalized)) {
                return edge;
            }
        }
        return null;
    }
}
