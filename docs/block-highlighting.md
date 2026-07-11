# Block Highlighting

`BlockHighlighter` outlines or fills a block with a colored [cuboid display](displays.md), optionally driven
by a repeating pulse effect and/or an automatic expiry time. It's a static registry — one highlight per
`Block` at a time.

## Basic usage

```java
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.HighlightDisplayType;

// Highlight indefinitely, no pulse animation, fully transparent until you set a color
BlockHighlighter.highlightBlock(block);

// Highlight for 100 ticks (5 seconds), then auto-remove
BlockHighlighter.highlightBlock(block, 100);

// Solid color, no animation, until manually removed
BlockHighlighter.highlightBlock(block, Color.RED);

// Solid color of a chosen shape, expires after 100 ticks
BlockHighlighter.highlightBlock(block, HighlightDisplayType.WIREFRAME, Color.RED, 100);

// Animated, chosen shape, no expiry — the function's own cycle duration drives its cadence
BlockHighlighter.highlightBlock(block, HighlightDisplayType.WIREFRAME, new RainbowHighlighter());

// Animated, chosen shape, expires after 100 ticks
BlockHighlighter.highlightBlock(block, HighlightDisplayType.CUBE, new PulsingColorHighlighter(Color.RED, 20), 100);

// Swap the running animation on an already-highlighted block, in place (no respawn)
BlockHighlighter.setBlockAnimation(block, new GlowingHighlighter(Color.AQUA, 20));

// Set the color directly (works with the function-driven overloads too, if you want a starting color)
BlockHighlighter.getHighlightDisplay(block).setColor(Color.RED);

// Remove early
BlockHighlighter.unhighlightBlock(block);
BlockHighlighter.unhighlightAllBlocks();
```

There's no `animationDuration`-style parameter anywhere in this API anymore — each animation function
carries its own inherent cycle length (see below), so it can never drift out of sync with what the function
actually draws. The function-driven and no-color overloads spawn the display **fully transparent**, centered
on the block at 1.01× block scale (to avoid z-fighting with the block's own faces); the new `Color`-only
overloads skip that step and apply the color immediately instead.

## Shapes

`HighlightDisplayType` picks which [cuboid display](displays.md) backs the highlight:

| Value | Display used | Look |
|---|---|---|
| `CUBE` (default) | `CubeColorDisplay` | Solid colored faces, no edges |
| `WIREFRAME` | `WireframeCubeColorDisplay` | Edges only |
| `FILLED_WIREFRAME` | `FilledWireframeCubeColorDisplay` | Both |

## Built-in animations (`IHighlighterFunction`)

Pass any `IHighlighterFunction<ICuboidDisplay>` as the animation argument. There's no separate duration
parameter to pass alongside it: every `IHighlighterFunction` (via `EffectFunction`) declares its own
`getInherentCycleDuration()`, backed by whatever `cycleDuration` (or equivalent) you gave its constructor,
and that value alone now drives how often `onAnimationRestart` fires — it can no longer drift out of sync
with `BlockHighlighter` the way a separately-passed duration could. Five ready-made ones live in
`effect.highlighter.prefabs`:

| Class | Effect |
|---|---|
| `PulsingColorHighlighter(color, cycleDuration)` / `(color, minAlpha, maxAlpha, cycleDuration)` | Smooth sine-wave alpha breathing between two alpha values |
| `GlowingHighlighter(baseColor, cycleDuration)` / `(baseColor, minBrightness, maxBrightness, cycleDuration)` | Fades from a dimmed version of the color up to full brightness each cycle |
| `RainbowHighlighter()` / `RainbowHighlighter(cycleDuration)` | Steps through a fixed 7-color rainbow, one color per cycle (no-arg default: 20 ticks) |
| `ScalingPulseHighlighter(minScale, maxScale, cycleDuration)` | Repeating linear grow/shrink pulse |
| `PingHighlighter(minScale, maxScale, color, cycleDuration)` | Grows while fading out — a radar-style "ping" |

```java
BlockHighlighter.highlightBlock(block, new GlowingHighlighter(Color.AQUA, 20));
```

Write your own by implementing `IHighlighterFunction<ICuboidDisplay>` — it now has two required methods,
`onAnimationRestart(ICuboidDisplay)` and `getInherentCycleDuration()` (plus the existing `default
isRepeating()`), so it's no longer a valid lambda target (no highlighter/tinter in this library ever was
constructed as one). Most prefabs just register a small custom color/scale interpolator (see
[Animations](animations.md)) each time `onAnimationRestart` fires.

> [!IMPORTANT]
> `GlowingHighlighter`'s 4-argument constructor is `(baseColor, minBrightness, maxBrightness, cycleDuration)`
> — duration last, matching every other prefab's convention. If you have older call sites using
> `(baseColor, cycleDuration, minBrightness, maxBrightness)`, update the argument order; the 2-argument
> `(baseColor, cycleDuration)` convenience constructor is unaffected.

> [!NOTE]
> `RainbowHighlighter` keeps its color-cycle position as instance state. Reuse one instance across multiple
> highlighted blocks and they'll share (and desync from each other's expectations of) that single cycle
> position — construct a new instance per block if you want independent rainbow cycles.

## Auto-expiry

The `lifeTime` overloads schedule automatic removal after a fixed number of ticks, backed by one shared
lazily-started "lifetime checker" task that self-cancels once nothing has a pending expiry:

```java
BlockHighlighter.highlightBlock(block, HighlightDisplayType.CUBE, new RainbowHighlighter(), 200); // lifeTime = 200 ticks
```

Pass `-1` (or omit it, via the shorter overloads) for a highlight that lasts until you explicitly call
`unhighlightBlock`. A highlight with a `lifeTime` but **no** animation function now reliably expires too —
previously the internal checker task never started ticking for a purely static, timed highlight, so its
lifetime silently never counted down.

Continue to [View Tinting](view-tinting.md) for the equivalent full-screen effect on a player.
