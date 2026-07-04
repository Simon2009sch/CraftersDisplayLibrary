package me.simoncrafter.CraftersDisplayLibrary.core.interfaces;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

/**
 * Per-player visibility control layered on top of Bukkit's
 * {@code Display#setVisibleByDefault}/{@link Player#showEntity}/{@link Player#hideEntity}.
 * <p>
 * {@link #isHiddenByDefault()} and {@link #hideByDefault(boolean)} control whether players who
 * have not been given an explicit override can see this display at all; {@link #showForPlayer}
 * and {@link #hideForPlayer} apply a per-player override on top of that default.
 *
 * @apiNote The {@link Contract} annotations below declare that these methods return a new
 * instance, but implementations are free to (and typically do) mutate their visibility state
 * in place and return {@code this} for call chaining - treat the return value as "this display,
 * for chaining" rather than relying on a fresh instance being produced.
 */
public interface IHidable {

    /** Whether this display is currently hidden from players by default. */
    boolean isHiddenByDefault();

    /**
     * Sets whether this display is hidden from players by default (before any per-player
     * override from {@link #showForPlayer}/{@link #hideForPlayer} is applied).
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    IDisplayable hideByDefault(boolean hide);

    /**
     * Overrides this display's default visibility to make it visible for a specific player.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    IDisplayable showForPlayer(Player player);

    /**
     * Overrides this display's default visibility to hide it from a specific player.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    IDisplayable hideForPlayer(Player player);

}
