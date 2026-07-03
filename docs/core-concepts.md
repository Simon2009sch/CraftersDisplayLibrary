# Core Concepts

Every display type in the library is built on the same foundation: a small scene-graph of `PositionObject`s.
Understanding this once means every display type in [Displays](displays.md) works the same way.

## `IDisplayable` and `PositionObject`

`IDisplayable` is the interface most of your code should be written against — "a thing with a transform,
children, and a location that can be moved/rotated/scaled, optionally with an animation duration."
`PositionObject` is the concrete base class that implements it, and every spawnable display
(`ColorDisplay`, `BlockDisplayObject`, `ItemDisplayObject`, `CubeColorDisplay`,
`WireframeCubeColorDisplay`, `FilledWireframeCubeColorDisplay`, `LineColorDisplay`) extends it.

A `PositionObject` has:

- a **`localTransform`** — its own position/rotation/scale, relative to its parent (or to the world, for a
  root object with no parent);
- a **`parentTransform`** — the resolved transform handed down from its parent (identity, for a root);
- a list of **children** (also `IDisplayable`s).

Whenever a `PositionObject`'s own transform changes, it recomputes its combined "final transform" and pushes
it down to every child as *their* `parentTransform`. This is what makes composite displays act like a single
rigid body:

```
CubeColorDisplay          (root, has its own localTransform)
├── top    (ColorDisplay child)
├── bottom (ColorDisplay child)
├── left   (ColorDisplay child)
├── right  (ColorDisplay child)
├── front  (ColorDisplay child)
└── back   (ColorDisplay child)
```

Move, rotate, or scale the `CubeColorDisplay`, and all six face panels update together — you never touch the
faces directly for a rigid-body transform.

You can build your own hierarchies the same way with `addChild`/`removeChild`/`setChildren` on any
`IDisplayable` — for example, parenting a small indicator cube to a larger display so it rides along with it.

## Two rotation channels

Bukkit's `Transformation` (and the underlying Minecraft display entity) has two independent rotation
quaternions, applied on either side of the scale in the transform composition:

- **Left rotation** — `LRotateAbsolute` / `LRotateRelative`. Applied *before* scale.
- **Right rotation** — `RRotateAbsolute` / `RRotateRelative`. Applied *after* scale.

Most of the time you'll only need left rotation (it's what you'd reach for to "rotate this object"). Right
rotation is useful when you need a rotation that shouldn't be distorted by a non-uniform scale, or vice versa
— for instance, `LineColorDisplay` uses left rotation to orient the line along its direction vector while
scale stretches it to length, and reserves right rotation for callers who want to spin the line's cross-
section without affecting its direction.

`Absolute` variants set the rotation outright; `Relative` variants compose (multiply) it onto the current
rotation.

## Two ways to animate

The library has two independent animation mechanisms, and it's easy to mix them up:

### 1. Per-call duration (built into every move/rotate/scale)

Every `IDisplayable` method that changes the transform takes a `time` parameter, in ticks:

```java
cube.moveRelative(new Vector3f(0, 1, 0), 20); // move up 1 block over 1 second
```

Passing `0` applies the change instantly. Passing a positive number starts a short-lived, per-object
`BukkitTask` that linearly interpolates (lerp for position/scale, slerp for rotation) from the current
transform to the target over that many ticks, calling `updateChildren` every tick along the way. This is the
simplest way to animate and needs no other setup.

> [!NOTE]
> Starting a new per-call animation on an object cancels whatever per-call animation was already running on
> it. There's no queueing — the newest call wins.

### 2. Registered animations (`AnimationFactory` / `GlobalAnimationTickHandler`)

For animations with **easing curves**, or that need to be independently created, tracked, and torn down
(this is what [block highlighting](block-highlighting.md) and [view tinting](view-tinting.md) are built on),
use `AnimationFactory.registerXAnimation(Smooth)`. These register an interpolator function with a single
shared `GlobalAnimationTickHandler`, which runs one repeating task for the whole server and drives every
registered animation each tick. See [Animations](animations.md) for the full catalog.

The two systems don't conflict directly, but they can compete: a registered translation animation and a
per-call `moveRelative(..., time)` both ultimately write to the same `localTransform.translation`, so running
both on the same object for the same property at the same time will fight each other. Pick one mechanism per
property per object.

## Coloring and visibility

Two small interfaces sit alongside `IDisplayable`:

- **`IColorableDisplay`** — adds `setColor(Color)`. Implemented by every display type that renders a solid
  color. The two entity-backed displays — `BlockDisplayObject` and `ItemDisplayObject` — intentionally do
  **not** implement it, since they render a real block/item rather than a flat colour; use their
  `setBlock(BlockData)` / `setItem(ItemStack)` methods instead. This is also the type the color-animation
  system operates on.
- **`IHidable`** — per-player visibility control, layered on top of Bukkit's own
  `Display.setVisibleByDefault`/`Player.showEntity`/`hideEntity`:
  - `hideByDefault(true)` makes the display invisible to everyone by default;
  - `showForPlayer(player)` / `hideForPlayer(player)` then override that default for specific players.

  This is exactly how [view tinting](view-tinting.md) works: the tint box is hidden by default and shown only
  to the one player it belongs to.

## Cuboid displays

`ICuboidDisplay` (extended by `IColorableDisplay`) is the common interface for anything box-shaped:
`CubeColorDisplay`, `WireframeCubeColorDisplay`, and `FilledWireframeCubeColorDisplay` all implement it, which
is what lets [`BlockHighlighter`](block-highlighting.md) spawn any of the three interchangeably based on a
`HighlightDisplayType` argument.

Continue to [Displays](displays.md) for a tour of each concrete display type.
