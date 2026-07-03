package me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class ViewTinter {

    private static Map<Player, ViewTinterData> tintedPlayers = new HashMap<>();

    public static void tintPlayer(Player player, Color color, int duration, IViewTinterFunction function) {
        untintPlayer(player);

        Location playerHead = player.getEyeLocation().toVector().toLocation(player.getWorld());
        CubeColorDisplay display = CubeColorDisplay.create(
                playerHead,
                new Vector3f(-0.4f, -1.2f, -0.4f),
                new Vector3f(0, -0.3f, 0),
                new Quaternionf(),
                new CubeColorInformation(color)
        );

        try {
            display.spawnDisplay();
            if (display.getTop() != null) {
                player.addPassenger(display.getTop().getEntity());
            }
            if (display.getBottom() != null) {
                player.addPassenger(display.getBottom().getEntity());
            }
            if (display.getLeft() != null) {
                player.addPassenger(display.getLeft().getEntity());
            }
            if (display.getRight() != null) {
                player.addPassenger(display.getRight().getEntity());
            }
            if (display.getFront() != null) {
                player.addPassenger(display.getFront().getEntity());
            }
            if (display.getBack() != null) {
                player.addPassenger(display.getBack().getEntity());
            }
        } catch (Exception e) {
            e.printStackTrace();
            return;
        }

        ViewTinterData style = new ViewTinterData(duration, display, function);
        tintedPlayers.put(player, style);
        style.start();
    }

    public static void tintPlayer(Player player, Color color, int duration) {
        tintPlayer(player, color, duration, null);
    }

    public static void tintPlayer(Player player, Color color) {
        tintPlayer(player, color, 20, null);
    }

    public static void untintPlayer(Player player) {
        ViewTinterData style = tintedPlayers.get(player);
        if (style != null) {
            style.stop();
            try {
                CubeColorDisplay display = style.getDisplay();
                if (display.getTop() != null) {
                    player.removePassenger(display.getTop().getEntity());
                }
                if (display.getBottom() != null) {
                    player.removePassenger(display.getBottom().getEntity());
                }
                if (display.getLeft() != null) {
                    player.removePassenger(display.getLeft().getEntity());
                }
                if (display.getRight() != null) {
                    player.removePassenger(display.getRight().getEntity());
                }
                if (display.getFront() != null) {
                    player.removePassenger(display.getFront().getEntity());
                }
                if (display.getBack() != null) {
                    player.removePassenger(display.getBack().getEntity());
                }
                display.remove();
            } catch (Exception e) {
                e.printStackTrace();
            }
            GlobalAnimationTickHandler.removeColorAnimation(style.getDisplay());
            tintedPlayers.remove(player);
        }
    }

    public static void untintAllPlayers() {
        for (Player player : new ArrayList<>(tintedPlayers.keySet())) {
            untintPlayer(player);
        }
    }

    public static CubeColorDisplay getTintDisplay(Player player) {
        ViewTinterData style = tintedPlayers.get(player);
        return style != null ? style.getDisplay() : null;
    }

    public static boolean isPlayerTinted(Player player) {
        return tintedPlayers.containsKey(player);
    }

    public static void setPlayerAnimation(Player player, IViewTinterFunction animation, int duration) {
        ViewTinterData style = tintedPlayers.get(player);
        if (style != null) {
            style.setAnimation(animation, duration);
        }
    }

}
