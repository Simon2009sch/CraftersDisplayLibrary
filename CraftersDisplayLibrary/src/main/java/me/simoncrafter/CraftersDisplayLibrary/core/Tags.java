package me.simoncrafter.CraftersDisplayLibrary.core;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import org.bukkit.NamespacedKey;

/**
 * Persistent-data keys used to mark entities spawned by this library.
 */
public class Tags {
    /**
     * Key set to {@code true} in the {@link org.bukkit.persistence.PersistentDataContainer} of
     * every entity this library spawns (e.g. by
     * {@code ColorDisplay.spawnDisplay()}), so such entities can be identified and
     * distinguished from unrelated entities in the world.
     *
     * @apiNote This field is a {@code static final} initialized eagerly at class-load time using
     * {@link PluginHolder#getPlugin()}. It must not be referenced (and this class must not be
     * loaded) before {@link PluginHolder#setPlugin(org.bukkit.plugin.java.JavaPlugin)} has been
     * called, otherwise {@link NamespacedKey}'s constructor receives a {@code null} plugin.
     */
    public static final NamespacedKey CDL_ENTITY = new NamespacedKey(PluginHolder.getPlugin(), "ENTITY");
}
