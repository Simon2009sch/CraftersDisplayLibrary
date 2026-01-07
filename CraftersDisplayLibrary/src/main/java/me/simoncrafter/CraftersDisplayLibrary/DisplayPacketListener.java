package me.simoncrafter.CraftersDisplayLibrary;

import com.comphenix.protocol.PacketType;
import com.comphenix.protocol.ProtocolLibrary;
import com.comphenix.protocol.ProtocolManager;
import com.comphenix.protocol.events.PacketAdapter;
import com.comphenix.protocol.events.PacketContainer;
import com.comphenix.protocol.events.PacketEvent;
import com.comphenix.protocol.wrappers.WrappedDataWatcher;
import com.comphenix.protocol.wrappers.WrappedWatchableObject;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.UUID;

public class DisplayPacketListener {

    private static DisplayPacketListener instance;
    private final ProtocolManager protocolManager;
    public boolean enabled = true;

    // Private constructor ensures singleton
    private DisplayPacketListener() {
        if (PluginHolder.plugin == null) {
            protocolManager = null;
            return;
        }

        if (Bukkit.getServer().getPluginManager().isPluginEnabled("ProtocolLib")) {
            protocolManager = ProtocolLibrary.getProtocolManager();
            PluginHolder.plugin.getLogger().info("[CraftersDisplayLibrary] ProtocolLib found! Registering packet listeners...");
            registerPacketListeners();
        } else {
            protocolManager = null;
            PluginHolder.plugin.getLogger().warning("[CraftersDisplayLibrary] ProtocolLib not found — packet features disabled.");
        }
    }

    // Public accessor for singleton instance
    public static DisplayPacketListener getInstance() {
        if (instance == null) {
            instance = new DisplayPacketListener();
        }
        return instance;
    }

    // Register the ENTITY_METADATA listener
    private void registerPacketListeners() {
        if (protocolManager == null) return;

        protocolManager.addPacketListener(new PacketAdapter(PluginHolder.plugin, PacketType.Play.Server.ENTITY_METADATA) {
            @Override
            public void onPacketSending(PacketEvent event) {

                PacketContainer packet = event.getPacket();

                // 1️⃣ Get the entity ID
                int entityId = packet.getIntegers().read(0);

                // 2️⃣ Convert to Bukkit entity
                Entity entity = packet.getEntityModifier(event.getPlayer().getWorld()).read(0);

                // 3️⃣ Check if this is our custom entity
                if (entity.getPersistentDataContainer().getOrDefault(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, false)) {
                    event.setCancelled(enabled);
                }

                /*
                // Example: modify metadata (invisible flag)
                WrappedDataWatcher watcher = packet.getWatchableCollectionModifier().read(0);
                for (WrappedWatchableObject obj : watcher.getWatchableObjects()) {
                    if (obj.getIndex() == 0) {
                        byte flags = (Byte) obj.getValue();
                        flags |= 0x20; // Set invisible
                        obj.setValue(flags);
                    }
                }
                packet.getWatchableCollectionModifier().write(0, watcher.getWatchableObjects());
                */
            }
        });
    }

}
