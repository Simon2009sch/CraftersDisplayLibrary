package me.simoncrafter.CraftersDisplayLibrary;

import org.bukkit.plugin.java.JavaPlugin;

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
 * Everything that schedules a {@code BukkitTask} (e.g. {@link me.simoncrafter.CraftersDisplayLibrary.core.PositionObject},
 * {@link me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler}) reads {@link #getPlugin()} on demand, so this only needs to run once at startup.
 */
public class PluginHolder {
    private static JavaPlugin plugin;

    /**
     * Registers the owning plugin instance for the whole library. Must be called once, before any
     * other CraftersDisplayLibrary API is used, typically from {@code onEnable()}.
     *
     * @param plugin the plugin that owns this instance of the library
     */
    public static void setPlugin(JavaPlugin plugin) {
        PluginHolder.plugin = plugin;
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
}
