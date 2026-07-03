# View Tinting

`ViewTinter` tints a single player's entire view with a color — useful for damage flashes, status effect
overlays, or any full-screen feedback. It works by wrapping the player's head in an inward-facing
`CubeColorDisplay` and riding it as a vehicle passenger of the player, so it moves and turns with their
camera. Only the tinted player sees their own box (it's hidden by default and shown only to them).

## Basic usage

```java
import me.simoncrafter.CraftersDisplayLibrary.def.util.viewTinter.ViewTinter;

// Solid red tint, applied over 20 ticks
ViewTinter.tintPlayer(player, Color.RED, 20);

// With a built-in animation
ViewTinter.tintPlayer(player, Color.RED, 20, new FadeInTinter(Color.RED, 20));

// Query / clear
ViewTinter.isPlayerTinted(player);
ViewTinter.untintPlayer(player);
ViewTinter.untintAllPlayers();

// Get the underlying display, e.g. to change color directly
ViewTinter.getTintDisplay(player).setColor(Color.fromARGB(120, 255, 0, 0));

// Swap the running animation on an already-tinted player, without re-tinting
ViewTinter.setPlayerAnimation(player, new PulsingTinter(Color.RED, 20), 20);
```

A player can only have one active tint at a time — calling `tintPlayer` again replaces the previous one.

## Built-in animations (`IViewTinterFunction`)

`IViewTinterFunction` has one required method, `onAnimationRestart(CubeColorDisplay)`, plus a
`default boolean isRepeating() { return true; }`:

- **Repeating** functions (`isRepeating() == true`) fire every `animationDuration` ticks, indefinitely — like
  a pulsing status overlay.
- **One-shot** functions (`isRepeating() == false`) fire exactly once and never again unless you call
  `setPlayerAnimation` to reset them — like a transition that plays once and holds.

| Class | Repeating? | Effect |
|---|---|---|
| `FadeInTinter(color, fadeDuration)` | No | Alpha 0 → `color`'s own alpha |
| `FadeOutTinter(color, fadeDuration)` | No | `color`'s own alpha → 0 |
| `ColorShiftTinter(startColor, endColor, cycleDuration)` | No | Full ARGB transition between two colors |
| `PulsingTinter(color, cycleDuration)` | Yes | Smooth sine-wave alpha breathing |

```java
ViewTinter.tintPlayer(player, Color.BLACK, 0, new FadeInTinter(Color.fromARGB(180, 0, 0, 0), 30));
```

Write your own by implementing `IViewTinterFunction` directly — see [Animations](animations.md) for the
underlying interpolator mechanism these prefabs are built from.

## Relationship to Block Highlighting

`ViewTinter` and [`BlockHighlighter`](block-highlighting.md) are structurally the same pattern (a static
per-target registry, a hidden invisible-until-colored `CubeColorDisplay`, a repeating task that calls a
pluggable animation function) applied to two different targets — a player's view versus a world block. If
you understand one, you understand the other.
