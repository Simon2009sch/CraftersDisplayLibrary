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

/**
 * Static registry that tints a player's screen by wrapping their head in a negatively-scaled
 * {@link CubeColorDisplay} ridden as a vehicle passenger, so its inward-facing faces surround the
 * player's own viewpoint like a full-screen colour overlay. Only the tinted player is shown their
 * own tint cube ({@code hideByDefault(true)} + {@code showForPlayer(player)}); other players never see it.
 * <p>
 * At most one tint is tracked per player; calling {@link #tintPlayer} again for an already-tinted
 * player implicitly removes the old tint first. This mirrors
 * {@link me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.BlockHighlighter}, which is
 * the same pattern applied to blocks instead of a player's screen.
 */
public class ViewTinter {

    private static Map<Player, ViewTinterData> tintedPlayers = new HashMap<>();

    /**
     * Tints {@code player}'s screen with {@code color}, driven by the optional {@code function}.
     *
     * @param color    the tint colour (its alpha controls opacity)
     * @param duration ticks per animation cycle, passed to {@link IViewTinterFunction#onAnimationRestart}
     * @param function called to animate the tint; may be {@code null} for a static tint
     */
    public static void tintPlayer(Player player, Color color, int duration, IViewTinterFunction function) {
        untintPlayer(player);

        Location playerHead = player.getEyeLocation().toVector().toLocation(player.getWorld());
        // Negative scale on X/Y/Z turns the cube "inside-out" so its faces point inward, wrapping
        // the player's own viewpoint instead of presenting an outward-facing box to onlookers.
        CubeColorDisplay display = CubeColorDisplay.create(
                playerHead,
                new Vector3f(-0.4f, -1.2f, -0.4f),
                new Vector3f(0, -0.3f, 0),
                new Quaternionf(),
                new CubeColorInformation(color)
        );

        display.hideByDefault(true);
        display.showForPlayer(player);

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

    /** Tints {@code player}'s screen with a static (non-animated) {@code color}. */
    public static void tintPlayer(Player player, Color color, int duration) {
        tintPlayer(player, color, duration, null);
    }

    /** Tints {@code player}'s screen with a static (non-animated) {@code color} using a default 20-tick cycle length. */
    public static void tintPlayer(Player player, Color color) {
        tintPlayer(player, color, 20, null);
    }

    /** Removes {@code player}'s tint, if any, stopping its animation and despawning the tint cube. */
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

    /** Removes every currently active player tint. */
    public static void untintAllPlayers() {
        for (Player player : new ArrayList<>(tintedPlayers.keySet())) {
            untintPlayer(player);
        }
    }

    /** The tint cube currently applied to {@code player}, or {@code null} if they aren't tinted. */
    public static CubeColorDisplay getTintDisplay(Player player) {
        ViewTinterData style = tintedPlayers.get(player);
        return style != null ? style.getDisplay() : null;
    }

    public static boolean isPlayerTinted(Player player) {
        return tintedPlayers.containsKey(player);
    }

    /**
     * Swaps the active {@link IViewTinterFunction} on an already-tinted player without removing
     * and recreating the tint cube. No-ops if {@code player} isn't currently tinted.
     */
    public static void setPlayerAnimation(Player player, IViewTinterFunction animation, int duration) {
        ViewTinterData style = tintedPlayers.get(player);
        if (style != null) {
            style.setAnimation(animation, duration);
        }
    }

}
