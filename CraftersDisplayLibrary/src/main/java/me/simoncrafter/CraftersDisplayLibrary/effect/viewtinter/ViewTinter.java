package me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter;

import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.effect.internal.TimedEffectRegistry;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Static registry that tints a player's screen by wrapping their head in a negatively-scaled
 * {@link CubeColorDisplay} ridden as a vehicle passenger, so its inward-facing faces surround the
 * player's own viewpoint like a full-screen colour overlay. Only the tinted player is shown their
 * own tint cube ({@code hideByDefault(true)} + {@code showForPlayer(player)}); other players never see it.
 * <p>
 * At most one tint is tracked per player; calling {@link #tintPlayer} again for an already-tinted
 * player implicitly removes the old tint first. This mirrors
 * {@link me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter}, which is
 * the same pattern applied to blocks instead of a player's screen.
 */
public class ViewTinter {

    /**
     * Backing {@link TimedEffectRegistry}, held via composition rather than inheritance so this
     * class's own public static API stays unchanged. {@link #onRemove} keeps the passenger-removal
     * dance that is specific to tint cubes; that logic is intentionally not generalized into the
     * shared base.
     */
    private static final TimedEffectRegistry<Player, CubeColorDisplay> registry = new TimedEffectRegistry<>() {
        @Override
        protected void onRemove(Player player, CubeColorDisplay display) {
            try {
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
        }
    };

    /**
     * Tints {@code player}'s screen with {@code color}, driven by the optional {@code function}, and
     * automatically calling {@link #untintPlayer} once {@code lifeTime} ticks have elapsed (a value
     * {@code <= 0} disables auto-removal).
     * <p>
     * If {@code player} is already tinted, the existing tint cube is reused in place instead of being
     * respawned - {@code color} is applied via {@link CubeColorDisplay#setColor}, {@code function} is
     * swapped in via {@link TimedEffectRegistry#setAnimation}, and {@code lifeTime} is updated via
     * {@link TimedEffectRegistry#updateLifeTime} - so switching e.g. a fade-in to a fade-out on an
     * already-tinted player is a seamless in-place swap with no flicker/respawn.
     *
     * @param color    the tint colour (its alpha controls opacity)
     * @param function called to animate the tint; may be {@code null} for a static tint
     * @param lifeTime ticks until this tint is automatically removed; a value {@code <= 0} disables
     *                 auto-removal for this tint (but still starts the checker task)
     * @return the tint cube backing this tint, or {@code null} if spawning it failed
     */
    public static CubeColorDisplay tintPlayer(Player player, Color color, IViewTinterFunction function, int lifeTime) {
        if (registry.isActive(player)) {
            CubeColorDisplay existing = registry.getDisplay(player);
            existing.setColor(color);
            registry.setAnimation(player, function);
            registry.updateLifeTime(player, lifeTime);
            return existing;
        }

        Location playerHead = player.getEyeLocation().toVector().toLocation(player.getWorld());
        // Negative scale on X/Y/Z turns the cube "inside-out" so its faces point inward, wrapping
        // the player's own viewpoint instead of presenting an outward-facing box to onlookers.
        CubeColorDisplay display = CubeColorDisplay.create(
                playerHead,
                new Vector3f(-0.4f, -1.1f, -0.4f),
                new Vector3f(0, -0.3f, 0),
                new Quaternionf(),
                new CubeColorInformation(color)
        );

        //display.hideByDefault(true);
        //display.showForPlayer(player);

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
            return null;
        }

        registry.register(player, display, function, lifeTime);
        return display;
    }

    /**
     * Tints {@code player}'s screen with {@code color}, driven by {@code function}, until manually
     * removed. See {@link #tintPlayer(Player, Color, IViewTinterFunction, int)} for the reuse-in-place
     * behaviour when {@code player} is already tinted.
     */
    public static CubeColorDisplay tintPlayer(Player player, Color color, IViewTinterFunction function) {
        return tintPlayer(player, color, function, -1);
    }

    /** Tints {@code player}'s screen with a static (non-animated) {@code color} that expires after {@code lifeTime} ticks. */
    public static CubeColorDisplay tintPlayer(Player player, Color color, int lifeTime) {
        return tintPlayer(player, color, null, lifeTime);
    }

    /** Tints {@code player}'s screen with a static (non-animated) {@code color} until manually removed. */
    public static CubeColorDisplay tintPlayer(Player player, Color color) {
        return tintPlayer(player, color, null, -1);
    }

    /** Removes {@code player}'s tint, if any, stopping its animation and despawning the tint cube. */
    public static void untintPlayer(Player player) {
        registry.remove(player);
    }

    /** Removes every currently active player tint. */
    public static void untintAllPlayers() {
        registry.removeAll();
    }

    /** The tint cube currently applied to {@code player}, or {@code null} if they aren't tinted. */
    public static CubeColorDisplay getTintDisplay(Player player) {
        return registry.getDisplay(player);
    }

    public static boolean isPlayerTinted(Player player) {
        return registry.isActive(player);
    }

    /**
     * Swaps the active {@link IViewTinterFunction} on an already-tinted player without removing
     * and recreating the tint cube. No-ops if {@code player} isn't currently tinted.
     */
    public static void setPlayerAnimation(Player player, IViewTinterFunction animation) {
        registry.setAnimation(player, animation);
    }

}
