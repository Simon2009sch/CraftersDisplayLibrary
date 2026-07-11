package me.simoncrafter.CraftersDisplayLibrary.core;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import org.bukkit.NamespacedKey;

/**
 * Persistent-data keys used to mark entities spawned by this library.
 * <p>
 * {@link #CDL_ENTITY} alone has always been enough to identify "an entity this library spawned."
 * The remaining keys extend that into a full persistence round-trip (see
 * {@code me.simoncrafter.CraftersDisplayLibrary.persistence.DisplayPersistence}): together they let
 * a live scene-graph tree be tagged onto its backing entities' PDCs, scanned back out of the world
 * after a restart, and bulk-removed by plugin-load "iteration."
 *
 * @apiNote Every field here is a {@code static final} initialized eagerly at class-load time using
 * {@link PluginHolder#getPlugin()}. This class must not be loaded before
 * {@link PluginHolder#setPlugin(org.bukkit.plugin.java.JavaPlugin)} has been called, otherwise
 * {@link NamespacedKey}'s constructor receives a {@code null} plugin.
 */
public class Tags {
    /**
     * Key set to {@code true} in the {@link org.bukkit.persistence.PersistentDataContainer} of
     * every entity this library spawns (e.g. by
     * {@code ColorDisplay.spawnDisplay()}), so such entities can be identified and
     * distinguished from unrelated entities in the world.
     */
    public static final NamespacedKey CDL_ENTITY = new NamespacedKey(PluginHolder.getPlugin(), "ENTITY");

    /** STRING: the name of the plugin that owns the {@link PluginHolder} instance which spawned this entity. */
    public static final NamespacedKey CDL_PLUGIN = new NamespacedKey(PluginHolder.getPlugin(), "PLUGIN");

    /** LONG: {@link PluginHolder#getRegisterTimestamp()} at the time this entity was tagged - one value per plugin-load "iteration." */
    public static final NamespacedKey CDL_ITERATION = new NamespacedKey(PluginHolder.getPlugin(), "ITERATION");

    /** STRING (a {@link java.util.UUID}): shared by every physical entity belonging to ONE logical display node. */
    public static final NamespacedKey CDL_DISPLAY_UUID = new NamespacedKey(PluginHolder.getPlugin(), "DISPLAY_UUID");

    /** STRING (a {@link java.util.UUID}): the {@link #CDL_DISPLAY_UUID} of the immediate parent node in the scene graph; absent for a root. */
    public static final NamespacedKey CDL_PARENT_UUID = new NamespacedKey(PluginHolder.getPlugin(), "PARENT_UUID");

    /** STRING: which concrete display class this display-uuid group reconstructs into. */
    public static final NamespacedKey CDL_DISPLAY_TYPE = new NamespacedKey(PluginHolder.getPlugin(), "DISPLAY_TYPE");

    /** STRING: this entity's role within its display-uuid group (e.g. {@code "SELF"}, {@code "MARKER"}/{@code "SHULKER"}, {@code "PANEL_0"}-{@code "PANEL_3"}). */
    public static final NamespacedKey CDL_ENTITY_ROLE = new NamespacedKey(PluginHolder.getPlugin(), "ENTITY_ROLE");

    /** STRING: the serialized state blob for this display-uuid group, redundant across every entity sharing one {@link #CDL_DISPLAY_UUID}. */
    public static final NamespacedKey CDL_DISPLAY_DATA = new NamespacedKey(PluginHolder.getPlugin(), "DISPLAY_DATA");

    /**
     * STRING: an encoded list of {@code (uuid, type, data)} triples for every entity-less "pure
     * grouping" ancestor (e.g. {@code CubeColorDisplay}, which owns no entity of its own) between
     * this entity's display-uuid group and the nearest ancestor that does have (or itself needs) a
     * physical entity, ordered furthest-ancestor-first. Absent if there is no such ancestor. See
     * {@code DisplayPersistence} for how this is produced and consumed.
     */
    public static final NamespacedKey CDL_ANCESTOR_CHAIN = new NamespacedKey(PluginHolder.getPlugin(), "ANCESTOR_CHAIN");
}
