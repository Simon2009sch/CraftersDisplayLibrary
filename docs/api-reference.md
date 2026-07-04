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
| `PluginHolder` | Holds the `JavaPlugin` instance every other class needs. Call `setPlugin(this)` in `onEnable()` before anything else. |
| `Tags` | The `NamespacedKey` used to tag entities this library spawns. |

### `core` — the transform tree

| Type | Purpose |
|---|---|
| `PositionObject` | Base class for every display: local/parent transform, child propagation, per-call animation, the two rotation channels. See [Core Concepts](core-concepts.md). |
| `BlockScale` | Holds the version-dependent `VALUE` constant — the conversion factor from a logical 1×1×1 local transform to the on-screen size of one block, used by `PositionObject.scaleToBlock(...)`. |
| `AbstractEntityBackedDisplay<E>` | Shared base for the four entity-backed leaf displays (`ColorDisplay`, `TextDisplay`, `BlockDisplayObject`, `ItemDisplayObject`); pushes transform changes to the live entity and handles billboard/visibility/removal. |

### `core.interfaces` — contracts

| Type | Purpose |
|---|---|
| `IDisplayable` | The core contract: transform, children, move/rotate/scale, location, lifecycle. |
| `IColorableDisplay` | Extends `IDisplayable`; adds `setColor(Color)`. |
| `ICuboidDisplay` | Extends `IColorableDisplay`; the common contract for box-shaped displays (`spawnDisplay`, `isSeeThrough`/`setSeeThrough`). |
| `IHidable` | Per-player visibility control on top of Bukkit's default-visibility API. |

### `display.panel` — single-entity panel displays

See [Displays](displays.md) for usage examples of each.

| Type | Purpose |
|---|---|
| `ColorDisplay` | A single colored panel, backed by one `TextDisplay` entity. |
| `TextDisplay` | A single text label display. |
| `BlockDisplayObject` | A single block, backed by one `BlockDisplay` rendering real `BlockData`. Not `IColorableDisplay`. |
| `ItemDisplayObject` | A single item, backed by one `ItemDisplay` rendering a real `ItemStack`. Not `IColorableDisplay`. |

### `display.line` — lines and beams

| Type | Purpose |
|---|---|
| `LineColorDisplay` | A colored line/beam between two points. |
| `RawLineDisplay` | Internal geometry engine behind `LineColorDisplay`. |

### `display.cube` — solid cubes

| Type | Purpose |
|---|---|
| `CubeColorDisplay` | Six-panel solid cube. |
| `CubeColorInformation` | Immutable per-face color holder for `CubeColorDisplay`. |

### `display.wireframecube` — wireframe cubes

| Type | Purpose |
|---|---|
| `WireframeCubeColorDisplay` | Twelve-edge wireframe cube. |
| `WireframeCubeColorInformation` | Immutable per-edge color holder. |
| `CubeEdge` | The 12 named edges of a unit cube. |
| `CubeFace` | The 6 named faces, each mapping to its 4 bordering edges. |

### `display.filledwireframecube` — combined cubes

| Type | Purpose |
|---|---|
| `FilledWireframeCubeColorDisplay` | Faces + edges combined, kept in sync. |

### `animation` — the registered-animation system

See [Animations](animations.md).

| Type | Purpose |
|---|---|
| `AnimationFactory` | Public entry point — registers a plain or smooth interpolator for translation/rotation/scale/color. |
| `GlobalAnimationTickHandler` | The single shared tick loop that drives every registered animation. |

### `animation.spi` — interpolator base classes

| Type | Purpose |
|---|---|
| `AnimationInterpolationFunction<V>` | Base class for `PositionObject`-targeted interpolators. |
| `CustomTypeAnimationInterpolationFunction<V, K>` | Fully generic base (used for color, targeting `IColorableDisplay`). |

### `animation.easing` — easing curves

| Type | Purpose |
|---|---|
| `EasingCurve` | Enum (`LINEAR`, `EASE_IN_OUT_SINE`, `EASE_IN_OUT_CUBIC`) remapping an animation's linear progress before interpolation; see [Animations](animations.md#easingcurve). |

### `animation.functions` — concrete interpolators

| Type | Purpose |
|---|---|
| `animation.functions.*` | Five concrete interpolators — `TranslationAnimation`, `RotationAnimation`, `RRotationAnimation`, `ScalingAnimation`, `ColorAnimation` — each with a plain (`EasingCurve.LINEAR`) constructor and an overload taking an explicit `EasingCurve`, replacing the old separate plain/`*Smooth` class pairs. |

### `effect.internal` — shared effect plumbing

| Type | Purpose |
|---|---|
| `EffectFunction<D>` | Shared functional interface behind `IHighlighterFunction` and `IViewTinterFunction`: `onAnimationRestart(D)` plus `default isRepeating() = true`. |
| `TimedEffectRegistry<K, D>` | *(internal)* shared registry/tick-task base generalizing the highlighter/view-tinter timed-effect pattern. |

### `effect.highlighter` — block highlighting

See [Block Highlighting](block-highlighting.md).

| Type | Purpose |
|---|---|
| `BlockHighlighter` | Static registry: highlight/unhighlight a `Block`. |
| `HighlightDisplayType` | `CUBE` / `WIREFRAME` / `FILLED_WIREFRAME` — which `ICuboidDisplay` to use. |
| `IHighlighterFunction<V>` | Functional interface for pluggable highlight animations; extends `EffectFunction<V>`. |
| `prefabs.*` | `GlowingHighlighter`, `PingHighlighter`, `PulsingColorHighlighter`, `RainbowHighlighter`, `ScalingPulseHighlighter`. |

### `effect.viewtinter` — player view tinting

See [View Tinting](view-tinting.md).

| Type | Purpose |
|---|---|
| `ViewTinter` | Static registry: tint/untint a `Player`. |
| `IViewTinterFunction` | Interface for pluggable tint animations (repeating or one-shot); extends `EffectFunction<CubeColorDisplay>`. |
| `prefabs.*` | `ColorShiftTinter`, `FadeInTinter`, `FadeOutTinter`, `PulsingTinter`. |

## Internal-only types

A couple of types are marked `@ApiStatus.Internal` and aren't meant to be used directly by consumers:
`effect.internal.TimedEffectRegistry` and `PositionObject.updateChildrenNow`. They're documented in the
source for maintainers, but treat them as implementation detail subject to change without notice.
