package me.simoncrafter.mods.displayTestPlugin;

import me.simoncrafter.CraftersDisplayLibrary.DisplayPacketListener;
import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

public final class DisplayTestPlugin extends JavaPlugin {



    @Override
    public void onEnable() {
        Bukkit.getPluginCommand("cdl").setExecutor(new testCommand());
        PluginHolder.plugin = this;
        DisplayPacketListener.getInstance();

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
