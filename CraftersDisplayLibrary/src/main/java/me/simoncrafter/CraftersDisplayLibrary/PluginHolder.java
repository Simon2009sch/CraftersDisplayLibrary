package me.simoncrafter.CraftersDisplayLibrary;

import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.joml.Vector3f;

public class PluginHolder {
    private static JavaPlugin plugin;
    private static GlobalAnimationTickHandler animationTickHandler;

    public static void setPlugin(JavaPlugin plugin) {
        PluginHolder.plugin = plugin;
        if (animationTickHandler != null) {
            animationTickHandler = GlobalAnimationTickHandler.getInstance();
        }
    }

    public static JavaPlugin getPlugin() {
        return plugin;
    }

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
