# CraftersDisplayLibrary

CraftersDisplayLibrary is a small Java library for [Paper](https://papermc.io) plugins that makes it easy to
build, animate, and manage composite [display entities](https://minecraft.wiki/w/Display) — the
block/item/text display entities Minecraft has used for client-side visual effects since 1.19.4.

Rather than juggling raw `TextDisplay`/`BlockDisplay` entities and `Transformation` matrices by hand, you
build a tree of `PositionObject`s. Moving, rotating, or scaling a parent object automatically propagates to
every child, so a "cube" made of six colored panels, or a "line" made of four thin panels, behaves and
animates as a single rigid object.

On top of that transform tree, the library ships two ready-to-use gameplay utilities: **block highlighting**
(outline/fill a block with a pulsing, glowing, or color-cycling effect) and **view tinting** (fade the
player's whole screen to a color, like a damage flash or a status effect).

## What's here

| Page | Covers |
|---|---|
| [Getting Started](getting-started.md) | Adding the dependency, the one required setup call, a first display |
| [Core Concepts](core-concepts.md) | `IDisplayable`, `PositionObject`, the transform tree, the two rotation channels, per-call vs. registered animation |
| [Displays](displays.md) | `ColorDisplay`, `BlockDisplayObject`, `ItemDisplayObject`, `CubeColorDisplay`, `WireframeCubeColorDisplay`, `FilledWireframeCubeColorDisplay`, `LineColorDisplay` |
| [Animations](animations.md) | `AnimationFactory`, the linear/smooth interpolators, and how the global tick handler drives them |
| [Block Highlighting](block-highlighting.md) | `BlockHighlighter` and its built-in pulse/glow/rainbow/ping effects |
| [View Tinting](view-tinting.md) | `ViewTinter` and its built-in fade/pulse/color-shift effects |
| [Persistence](persistence.md) | `DisplayPersistence` — tag a display tree so it survives a restart, read it back as live objects, clean up by plugin-load "iteration" |
| [API Reference](api-reference.md) | Package-by-package index of every public type, and how to generate full Javadoc HTML |
| [Development](development/README.md) | Module/build setup and the design patterns behind the package layout, for anyone working on the library itself rather than consuming it |

> [!NOTE]
> This documentation covers the `CraftersDisplayLibrary` module. The `DisplayTestPlugin` module in the same
> repository is a standalone in-game testing harness (the `/cdl` command) used while developing the library —
> it isn't part of the public API, but reading its source alongside these guides is a good way to see every
> feature invoked with real arguments.

## Requirements

- Java 21
- A [Paper](https://papermc.io) server, API version 1.20–1.21
- The library is added as a **shaded dependency** of your plugin, not installed as its own plugin — see
  [Getting Started](getting-started.md).

> [!NOTE]
> This project was inspired by [Cymaera](https://www.youtube.com/@heledron). Specifically
> [this Video of his](https://www.youtube.com/@heledron). <br>
> Also because I currently don't possess the Mathematical knowledge thanks a lot to Claude
> for helping me out on that one. (Also for making my code a bit more readable and organized into sensible packages)
