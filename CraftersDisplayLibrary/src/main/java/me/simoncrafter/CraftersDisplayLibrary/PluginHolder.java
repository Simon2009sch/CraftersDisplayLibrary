package me.simoncrafter.CraftersDisplayLibrary;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector3f;

/**
 * Library-wide bootstrap holding the {@link JavaPlugin} instance CraftersDisplayLibrary uses to
 * talk to the Bukkit API (scheduling tasks, building {@link org.bukkit.NamespacedKey}s, etc.).
 * <p>
 * A consuming plugin <strong>must</strong> call {@link #setPlugin(JavaPlugin)} exactly once, from
 * its {@code onEnable()}, before touching any other part of this library:
 * <pre>{@code
 * @Override
 * public void onEnable() {
 *     PluginHolder.setPlugin(this);
 * }
 * }</pre>
 * Everything that schedules a {@code BukkitTask} (e.g. {@link me.simoncrafter.CraftersDisplayLibrary.def.PositionObject},
 * {@link GlobalAnimationTickHandler}) reads {@link #getPlugin()} on demand, so this only needs to run once at startup.
 */
public class PluginHolder {
    private static JavaPlugin plugin;
    private static GlobalAnimationTickHandler animationTickHandler;

    /**
     * Registers the owning plugin instance for the whole library. Must be called once, before any
     * other CraftersDisplayLibrary API is used, typically from {@code onEnable()}.
     *
     * @param plugin the plugin that owns this instance of the library
     */
    public static void setPlugin(JavaPlugin plugin) {
        PluginHolder.plugin = plugin;
        // animationTickHandler is never assigned elsewhere in this class, so it is always null
        // here and this branch currently never runs; GlobalAnimationTickHandler instead
        // lazily initializes itself the first time an animation is registered.
        if (animationTickHandler != null) {
            animationTickHandler = GlobalAnimationTickHandler.getInstance();
        }
    }

    /**
     * The plugin registered via {@link #setPlugin(JavaPlugin)}, used internally to schedule
     * Bukkit tasks and access other plugin-scoped APIs.
     *
     * @return the registered plugin, or {@code null} if {@link #setPlugin(JavaPlugin)} has not
     * been called yet
     */
    public static JavaPlugin getPlugin() {
        return plugin;
    }

    /**
     * Conversion factor from a logical 1x1x1-unit local transform to the on-screen size of one
     * block, as required by display entities on the currently running server version. Computed
     * once at class-load time from {@link Bukkit#getVersion()} and applied by
     * {@link me.simoncrafter.CraftersDisplayLibrary.def.PositionObject#scaleToBlock(org.bukkit.util.Transformation)}.
     * <p>
     * The Y component differs between 1.20 and 1.21+ because vanilla changed how a display
     * entity's declared scale maps to its rendered block-sized footprint.
     */
    public static Vector3f BLOCK_SCALE = getBlockScaleForVersion();

    private static Vector3f getBlockScaleForVersion() {
        String version = Bukkit.getVersion();

        if (version.contains("1.21")) {
            return new Vector3f(40, 1.905f, 40);
        } else if (version.contains("1.20")) {
            return new Vector3f(40, 2f, 40);
        } else {
            return new Vector3f(40, 2f, 40);
        }
    }
}
