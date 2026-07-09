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
 * <p>
 * Every mutator has a {@code recursive} overload: {@code true} (the default used by the
 * no-flag convenience method) also applies the change to every descendant reachable via
 * {@link IDisplayable#getChildren()}; {@code false} applies it to this display alone, leaving
 * children untouched.
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
     * override from {@link #showForPlayer}/{@link #hideForPlayer} is applied). Equivalent to
     * {@link #hideByDefault(boolean, boolean) hideByDefault(hide, true)} - applies recursively
     * to every descendant.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    default IDisplayable hideByDefault(boolean hide) {
        return hideByDefault(hide, true);
    }

    /**
     * Sets whether this display is hidden from players by default (before any per-player
     * override from {@link #showForPlayer}/{@link #hideForPlayer} is applied).
     *
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link IDisplayable#getChildren()}; if {@code false}, applies to this
     *                  display alone
     * @return this display, for chaining
     */
    @Contract(value = "_, _ -> new")
    IDisplayable hideByDefault(boolean hide, boolean recursive);

    /**
     * Overrides this display's default visibility to make it visible for a specific player.
     * Equivalent to {@link #showForPlayer(Player, boolean) showForPlayer(player, true)} -
     * applies recursively to every descendant.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    default IDisplayable showForPlayer(Player player) {
        return showForPlayer(player, true);
    }

    /**
     * Overrides this display's default visibility to make it visible for a specific player.
     *
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link IDisplayable#getChildren()}; if {@code false}, applies to this
     *                  display alone
     * @return this display, for chaining
     */
    @Contract(value = "_, _ -> new")
    IDisplayable showForPlayer(Player player, boolean recursive);

    /**
     * Overrides this display's default visibility to hide it from a specific player. Equivalent
     * to {@link #hideForPlayer(Player, boolean) hideForPlayer(player, true)} - applies
     * recursively to every descendant.
     *
     * @return this display, for chaining
     */
    @Contract(value = "_ -> new")
    default IDisplayable hideForPlayer(Player player) {
        return hideForPlayer(player, true);
    }

    /**
     * Overrides this display's default visibility to hide it from a specific player.
     *
     * @param recursive if {@code true}, also applies to every descendant reachable via
     *                  {@link IDisplayable#getChildren()}; if {@code false}, applies to this
     *                  display alone
     * @return this display, for chaining
     */
    @Contract(value = "_, _ -> new")
    IDisplayable hideForPlayer(Player player, boolean recursive);

}
