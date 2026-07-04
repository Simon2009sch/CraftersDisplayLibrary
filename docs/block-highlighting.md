# Block Highlighting

`BlockHighlighter` outlines or fills a block with a colored [cuboid display](displays.md), optionally driven
by a repeating pulse effect and/or an automatic expiry time. It's a static registry — one highlight per
`Block` at a time.

## Basic usage

```java
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.HighlightDisplayType;

// Highlight indefinitely, no pulse animation
BlockHighlighter.highlightBlock(block);

// Highlight for 100 ticks (5 seconds), then auto-remove
BlockHighlighter.highlightBlock(block, 100);

// Full control: shape, animation, no expiry
BlockHighlighter.highlightBlock(block, HighlightDisplayType.WIREFRAME, new RainbowHighlighter(), 20);

// Full control with an expiry time (lifeTime) and a pulse duration (animationDuration)
BlockHighlighter.highlightBlock(block, HighlightDisplayType.CUBE, new PulsingColorHighlighter(Color.RED, 20), 100, 20);

// Set the color/see-through after highlighting (the display starts fully transparent)
BlockHighlighter.getHighlightDisplay(block).setColor(Color.RED);

// Remove early
BlockHighlighter.unhighlightBlock(block);
BlockHighlighter.unhighlightAllBlocks();
```

The display always spawns **fully transparent** and centered on the block at 1.01× block scale (to avoid
z-fighting with the block's own faces) — you set a visible color yourself, either directly via
`getHighlightDisplay(block).setColor(...)` or through the animation function you pass in.

## Shapes

`HighlightDisplayType` picks which [cuboid display](displays.md) backs the highlight:

| Value | Display used | Look |
|---|---|---|
| `CUBE` (default) | `CubeColorDisplay` | Solid colored faces, no edges |
| `WIREFRAME` | `WireframeCubeColorDisplay` | Edges only |
| `FILLED_WIREFRAME` | `FilledWireframeCubeColorDisplay` | Both |

## Built-in animations (`IHighlighterFunction`)

Pass any `IHighlighterFunction<ICuboidDisplay>` as the animation argument — it's called every
`animationDuration` ticks (default 20, i.e. once per second) with the live display. Five ready-made ones
live in `effect.highlighter.prefabs`:

| Class | Effect |
|---|---|
| `PulsingColorHighlighter(color, cycleDuration)` | Smooth sine-wave alpha breathing between two alpha values |
| `GlowingHighlighter(baseColor, cycleDuration)` | Fades from a dimmed version of the color up to full brightness each cycle |
| `RainbowHighlighter()` | Steps through a fixed 7-color rainbow, one color per cycle |
| `ScalingPulseHighlighter(minScale, maxScale, cycleDuration)` | Repeating linear grow/shrink pulse |
| `PingHighlighter(minScale, maxScale, color, cycleDuration)` | Grows while fading out — a radar-style "ping" |

```java
BlockHighlighter.highlightBlock(block, new GlowingHighlighter(Color.AQUA, 20), -1);
```

Write your own by implementing the single-method `IHighlighterFunction<ICuboidDisplay>` interface — most
prefabs just register a small custom color/scale interpolator (see [Animations](animations.md)) each time
`onAnimationRestart` fires.

> [!NOTE]
> `RainbowHighlighter` keeps its color-cycle position as instance state. Reuse one instance across multiple
> highlighted blocks and they'll share (and desync from each other's expectations of) that single cycle
> position — construct a new instance per block if you want independent rainbow cycles.

## Auto-expiry

The `lifeTime` overloads schedule automatic removal after a fixed number of ticks, backed by one shared
lazily-started "lifetime checker" task that self-cancels once nothing has a pending expiry:

```java
BlockHighlighter.highlightBlock(block, HighlightDisplayType.CUBE, new RainbowHighlighter(), 200, 10);
//                                                                                     ^lifeTime  ^animationDuration
```

Pass `-1` (or omit it, via the shorter overloads) for a highlight that lasts until you explicitly call
`unhighlightBlock`.

Continue to [View Tinting](view-tinting.md) for the equivalent full-screen effect on a player.
