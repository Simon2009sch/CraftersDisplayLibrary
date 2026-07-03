# API Reference

Full method-level documentation lives as Javadoc in the source under `CraftersDisplayLibrary/src/main/java`.
This page is a package-by-package map of what's there, so you know where to look.

## Generating HTML Javadoc

The library doesn't currently bundle the `maven-javadoc-plugin`, but since every class has proper Javadoc
comments, you can generate a browsable HTML site on demand from the `CraftersDisplayLibrary` module:

```bash
mvn javadoc:javadoc -pl CraftersDisplayLibrary
```

Output lands in `CraftersDisplayLibrary/target/site/apidocs/index.html`.

## Package map

### `me.simoncrafter.CraftersDisplayLibrary` — bootstrap

| Type | Purpose |
|---|---|
| `PluginHolder` | Holds the `JavaPlugin` instance every other class needs. Call `setPlugin(this)` in `onEnable()` before anything else. Also exposes the version-dependent `BLOCK_SCALE` constant. |
| `Tags` | The `NamespacedKey` used to tag entities this library spawns. |

### `def` — the transform tree

| Type | Purpose |
|---|---|
| `PositionObject` | Base class for every display: local/parent transform, child propagation, per-call animation, the two rotation channels. See [Core Concepts](core-concepts.md). |

### `def.interfaces` — contracts

| Type | Purpose |
|---|---|
| `IDisplayable` | The core contract: transform, children, move/rotate/scale, location, lifecycle. |
| `IColorableDisplay` | Extends `IDisplayable`; adds `setColor(Color)`. |
| `ICuboidDisplay` | Extends `IColorableDisplay`; the common contract for box-shaped displays (`spawnDisplay`, `isSeeTrough`/`setSeeTrough`). |
| `IHidable` | Per-player visibility control on top of Bukkit's default-visibility API. |

### `def.active` — concrete display types

See [Displays](displays.md) for usage examples of each.

| Type | Purpose |
|---|---|
| `ColorDisplay` | A single colored panel, backed by one `TextDisplay` entity. |
| `Cube.CubeColorDisplay` | Six-panel solid cube. |
| `Cube.CubeColorInformation` | Immutable per-face color holder for `CubeColorDisplay`. |
| `WireframeCube.WireframeCubeColorDisplay` | Twelve-edge wireframe cube. |
| `WireframeCube.WireframeCubeColorInformation` | Immutable per-edge color holder. |
| `WireframeCube.CubeEdge` | The 12 named edges of a unit cube. |
| `WireframeCube.CubeFace` | The 6 named faces, each mapping to its 4 bordering edges. |
| `FilledWireframeCube.FilledWireframeCubeColorDisplay` | Faces + edges combined, kept in sync. |
| `Line.LineColorDisplay` | A colored line/beam between two points. |
| `Line.RawLineDisplay` | Internal geometry engine behind `LineColorDisplay`. |

### `def.animation` — the registered-animation system

See [Animations](animations.md).

| Type | Purpose |
|---|---|
| `AnimationFactory` | Public entry point — registers a plain or smooth interpolator for translation/rotation/scale/color. |
| `GlobalAnimationTickHandler` | The single shared tick loop that drives every registered animation. |
| `AAnimationInterpolationFunction<V>` | Base class for `PositionObject`-targeted interpolators. |
| `ACustomTypeAnimationInterpolationFunction<V, K>` | Fully generic base (used for color, targeting `IColorableDisplay`). |
| `animation.functions.*` | The 10 concrete interpolators (translation/scale/L-rotation/R-rotation × plain/smooth, plus color × plain/smooth). |

### `def.util.highlighter` — block highlighting

See [Block Highlighting](block-highlighting.md).

| Type | Purpose |
|---|---|
| `BlockHighlighter` | Static registry: highlight/unhighlight a `Block`. |
| `HighlightDisplayType` | `CUBE` / `WIREFRAME` / `FILLED_WIREFRAME` — which `ICuboidDisplay` to use. |
| `IHighliterFunction<V>` | Functional interface for pluggable highlight animations. |
| `BlockHighlighterData` | *(internal)* per-block animation/lifetime state. |
| `prefabs.*` | `GlowingHighlighter`, `PingHighlighter`, `PulsingColorHighlighter`, `RainbowHighlighter`, `ScalingPulseHighlighter`. |

### `def.util.viewTinter` — player view tinting

See [View Tinting](view-tinting.md).

| Type | Purpose |
|---|---|
| `ViewTinter` | Static registry: tint/untint a `Player`. |
| `IViewTinterFunction` | Interface for pluggable tint animations (repeating or one-shot). |
| `ViewTinterData` | *(internal)* per-player animation/lifetime state. |
| `prefabs.*` | `ColorShiftTinter`, `FadeInTinter`, `FadeOutTinter`, `PulsingTinter`. |

## Internal-only types

A few types are marked `@ApiStatus.Internal` and aren't meant to be used directly by consumers:
`BlockHighlighterData`, `ViewTinterData`, and `PositionObject.updateChildrenNow`. They're documented in the
source for maintainers, but treat them as implementation detail subject to change without notice.
