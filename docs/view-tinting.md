# View Tinting

`ViewTinter` tints a single player's entire view with a color — useful for damage flashes, status effect
overlays, or any full-screen feedback. It works by wrapping the player's head in an inward-facing
`CubeColorDisplay` and riding it as a vehicle passenger of the player, so it moves and turns with their
camera. Only the tinted player sees their own box (it's hidden by default and shown only to them).

## Basic usage

```java
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter;

// Static red tint, no animation, until manually removed
ViewTinter.tintPlayer(player, Color.RED);

// Static red tint, expires after 100 ticks
ViewTinter.tintPlayer(player, Color.RED, 100);

// With a built-in animation, until manually removed
ViewTinter.tintPlayer(player, Color.RED, new FadeInTinter(Color.RED, 20));

// With a built-in animation, expires after 100 ticks
ViewTinter.tintPlayer(player, Color.RED, new FadeInTinter(Color.RED, 20), 100);

// Query / clear
ViewTinter.isPlayerTinted(player);
ViewTinter.untintPlayer(player);
ViewTinter.untintAllPlayers();

// Get the underlying display, e.g. to change color directly
ViewTinter.getTintDisplay(player).setColor(Color.fromARGB(120, 255, 0, 0));

// Swap the running animation on an already-tinted player, without re-tinting
ViewTinter.setPlayerAnimation(player, new PulsingTinter(Color.RED, 20));
```

A player can only have one active tint at a time. Calling `tintPlayer` again on an already-tinted player no
longer means "remove the old tint and spawn a new one" — the **existing** tint cube is now reused in place:
the new `color` is applied via `setColor`, the driving function is swapped the same way `setPlayerAnimation`
does it, and `lifeTime` (if given) is updated on the existing entry. In practice this means switching e.g. a
running fade-in to a fade-out on an already-tinted player is a seamless in-place color/animation swap — no
flicker, no despawn/respawn pop. `setPlayerAnimation` remains the narrower entry point for "just swap the
function, leave the current color alone."

## Built-in animations (`IViewTinterFunction`)

`IViewTinterFunction` requires two methods: `onAnimationRestart(CubeColorDisplay)` and
`getInherentCycleDuration()` (the number of ticks between restarts — this is now the *only* place a
duration is specified; `ViewTinter` no longer takes a separate duration argument, it just asks the function
for its own), plus a `default boolean isRepeating() { return true; }`:

- **Repeating** functions (`isRepeating() == true`) fire every `getInherentCycleDuration()` ticks,
  indefinitely — like a pulsing status overlay.
- **One-shot** functions (`isRepeating() == false`) fire exactly once and never again unless you call
  `setPlayerAnimation` to reset them — like a transition that plays once and holds.

| Class | Repeating? | Effect |
|---|---|---|
| `FadeInTinter(color, cycleDuration)` | No | Alpha 0 → `color`'s own alpha |
| `FadeOutTinter(color, cycleDuration)` | No | `color`'s own alpha → 0 |
| `ColorShiftTinter(startColor, endColor, cycleDuration)` | No | Full ARGB transition between two colors |
| `PulsingTinter(color, cycleDuration)` | Yes | Smooth sine-wave alpha breathing |

```java
ViewTinter.tintPlayer(player, Color.fromARGB(180, 0, 0, 0), new FadeInTinter(Color.fromARGB(180, 0, 0, 0), 30));
```

Write your own by implementing `IViewTinterFunction` directly — see [Animations](animations.md) for the
underlying interpolator mechanism these prefabs are built from.

## Relationship to Block Highlighting

`ViewTinter` and [`BlockHighlighter`](block-highlighting.md) are structurally the same pattern (a static
per-target registry, a hidden invisible-until-colored `CubeColorDisplay`, a repeating task that calls a
pluggable animation function) applied to two different targets — a player's view versus a world block. If
you understand one, you understand the other.
