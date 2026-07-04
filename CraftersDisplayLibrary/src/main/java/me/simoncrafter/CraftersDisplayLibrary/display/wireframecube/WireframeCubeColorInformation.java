package me.simoncrafter.CraftersDisplayLibrary.display.wireframecube;

import org.bukkit.Color;
import org.jetbrains.annotations.Contract;

import java.util.EnumMap;
import java.util.Map;

/**
 * Immutable holder for the colours of a wireframe cube's 12 edges.
 * <p>
 * Mirrors {@link me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorInformation},
 * but keyed by {@link CubeEdge}. The {@code with...} methods return new instances.
 */
public class WireframeCubeColorInformation {

    private final EnumMap<CubeEdge, Color> colors;

    private WireframeCubeColorInformation(EnumMap<CubeEdge, Color> colors) {
        this.colors = colors;
    }

    /** Every edge painted the given colour. */
    public WireframeCubeColorInformation(Color color) {
        this.colors = new EnumMap<>(CubeEdge.class);
        for (CubeEdge edge : CubeEdge.values()) {
            this.colors.put(edge, color);
        }
    }

    /** Every edge white. */
    public WireframeCubeColorInformation() {
        this(Color.WHITE);
    }

    public Color getEdge(CubeEdge edge) {
        return colors.get(edge);
    }

    /** A copy with a single edge recoloured. */
    @Contract("_, _ -> new")
    public WireframeCubeColorInformation withEdge(CubeEdge edge, Color color) {
        EnumMap<CubeEdge, Color> copy = new EnumMap<>(colors);
        copy.put(edge, color);
        return new WireframeCubeColorInformation(copy);
    }

    /** A copy with all four edges of a face recoloured. */
    @Contract("_, _ -> new")
    public WireframeCubeColorInformation withFace(CubeFace face, Color color) {
        EnumMap<CubeEdge, Color> copy = new EnumMap<>(colors);
        for (CubeEdge edge : face.getEdges()) {
            copy.put(edge, color);
        }
        return new WireframeCubeColorInformation(copy);
    }

    /** A copy with the given per-edge overrides applied. */
    @Contract("_ -> new")
    public WireframeCubeColorInformation withEdges(Map<CubeEdge, Color> overrides) {
        EnumMap<CubeEdge, Color> copy = new EnumMap<>(colors);
        copy.putAll(overrides);
        return new WireframeCubeColorInformation(copy);
    }
}
