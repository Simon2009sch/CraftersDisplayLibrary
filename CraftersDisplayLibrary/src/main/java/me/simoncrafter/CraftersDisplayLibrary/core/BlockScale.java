package me.simoncrafter.CraftersDisplayLibrary.core;

import org.bukkit.Bukkit;
import org.joml.Vector3f;

/**
 * Holds the conversion factor from a logical 1x1x1-unit local transform to the on-screen size of
 * one block, as required by display entities on the currently running server version.
 */
public final class BlockScale {
    private BlockScale() {}

    /**
     * Conversion factor from a logical 1x1x1-unit local transform to the on-screen size of one
     * block, as required by display entities on the currently running server version. Computed
     * once at class-load time from {@link Bukkit#getVersion()} and applied by
     * {@link PositionObject#scaleToBlock(org.bukkit.util.Transformation)}.
     * <p>
     * The Y component differs between 1.20 and 1.21+ because vanilla changed how a display
     * entity's declared scale maps to its rendered block-sized footprint.
     */
    public static final Vector3f VALUE = getBlockScaleForVersion();

    private static Vector3f getBlockScaleForVersion() {
        String version = Bukkit.getVersion();

        if (version.equalsIgnoreCase("1.21.1")) {
            return new Vector3f(40, 1.905f, 40);
        } else if (version.contains("1.20")) {
            return new Vector3f(40, 2f, 40);
        } else {
            return new Vector3f(40, 2f, 40);
        }
    }
}
