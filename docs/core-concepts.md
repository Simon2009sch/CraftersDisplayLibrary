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

`PositionObject` also exposes `forEveryChild(Consumer<IDisplayable>)` / `runForEveryChild(Consumer<IDisplayable>)`
(two equivalent, public methods — pick whichever name reads better at the call site) to run arbitrary logic
over an object's *immediate* children without needing to call `getChildren()` yourself:

```java
cube.forEveryChild(child -> child.setLocationNoUpdate(child.getLocation()));
```

These are not recursive on their own — see the `recursive` flag on `hideByDefault`/`showForPlayer`/
`hideForPlayer` below (declared directly on `IDisplayable`) for the pattern this library uses when a
whole-subtree walk is needed.

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

- **`IColorableDisplay`** — extends `IDisplayable`; adds `setColor(Color)` plus see-through control
  (`isSeeThrough()`/`setSeeThrough(boolean)`). Implemented by every display type that renders a solid
  color: `ColorDisplay`, `TextDisplay` (panel), `CubeColorDisplay`, `WireframeCubeColorDisplay`,
  `FilledWireframeCubeColorDisplay`, `LineColorDisplay`. The two entity-backed displays —
  `BlockDisplayObject` and `ItemDisplayObject` — intentionally do **not** implement it, since they render a
  real block/item rather than a flat colour (and Bukkit's own `Display` interface only declares
  see-through on `TextDisplay` to begin with); use their `setBlock(BlockData)` / `setItem(ItemStack)`
  methods instead. `IColorableDisplay` is also the type the color-animation system operates on.
- **Hide/show** — every `IDisplayable`, not just colorable ones, carries per-player visibility control
  directly (it's part of the base interface, not a separate opt-in one), layered on top of Bukkit's own
  `Display.setVisibleByDefault`/`Player.showEntity`/`hideEntity`:
  - `hideByDefault(true)` makes the display invisible to everyone by default;
  - `showForPlayer(player)` / `hideForPlayer(player)` then override that default for specific players.

  This is exactly how [view tinting](view-tinting.md) works: the tint box is hidden by default and shown only
  to the one player it belongs to.

  All three methods have a `recursive` overload — `hideByDefault(boolean, boolean)`,
  `showForPlayer(Player, boolean)`, `hideForPlayer(Player, boolean)` — where the extra `boolean` controls
  whether the call cascades down to every descendant (via `getChildren()`, walked recursively) or applies to
  just that one object. The no-flag versions above are convenience wrappers that pass `true`, so a call like
  `cube.hideByDefault(true)` on a `CubeColorDisplay` already hides all six faces (and, for a composite like
  `FilledWireframeCubeColorDisplay`, everything under both the face cube and the wireframe cube). Pass `false`
  explicitly to affect only the object the method was called on:

  ```java
  filledCube.hideByDefault(true, false); // only this object's own bookkeeping flag flips; children untouched
  filledCube.hideByDefault(true);        // == hideByDefault(true, true): hides faces + edges recursively
  ```

  Since hide/show lives on `IDisplayable` itself, every display in the library supports it out of the box —
  there's nothing extra to implement or opt into.

## Tagging entities with persistent data

`IDisplayable` itself exposes `setPersistentData(NamespacedKey, PersistentDataType<T, Z>, Z)`, which writes
one entry directly into the `PersistentDataContainer` of every live Bukkit entity this display owns — the
same mechanism the library uses internally to tag its own entities with `Tags.CDL_ENTITY`.

```java
cube.setPersistentData(myKey, PersistentDataType.STRING, "some-id");
```

Like hide/show above, there's a `recursive` overload — `setPersistentData(key, type, value, boolean)` — but
with the **opposite default**: the no-flag version above only tags this object's own entity/entities, not its
descendants. A composite display with no entity of its own (`CubeColorDisplay`, `WireframeCubeColorDisplay`,
`FilledWireframeCubeColorDisplay`) is therefore a no-op when called without `recursive = true`:

```java
cube.setPersistentData(myKey, PersistentDataType.STRING, "some-id");       // no-op: CubeColorDisplay owns no entity
cube.setPersistentData(myKey, PersistentDataType.STRING, "some-id", true); // tags all 6 face entities
```

> [!NOTE]
> `setPersistentData` is a one-off, low-level way to stamp arbitrary data onto a display's entities. For the
> higher-level concern of making a whole display tree **survive a server restart** — tag it once, read it
> back out of the world as live objects, and clean it up in bulk by plugin-load "iteration" — see
> [Persistence](persistence.md), which is built on this same PDC mechanism plus its own dedicated `Tags` keys.

## Location vs. local transform

Every `PositionObject` tracks two separate things that both affect where it renders:

- its **`location`** — the world-space point its backing entity is actually spawned/teleported to;
- its **`localTransform`**'s translation — an offset from that location, applied client-side via the
  entity's `Transformation` (this is what `moveRelative`/`moveAbsolute` and friends change).

Normal moves (`moveRelative`, `moveAbsolute`, `setLocation`) keep these working together as you'd
expect. Two more specialized methods on the entity-backed panel displays (`ColorDisplay`,
`TextDisplay`, `BlockDisplayObject`, `ItemDisplayObject`) let you manipulate the entity's raw
`location` directly, independently of the transform:

- **`moveEntityStatic(Location)`** teleports the backing entity directly, bypassing the
  transform/animation system entirely — the display **moves** to the new location, instantly, with
  no interpolation.
- **`rebaseEntity(Location)`** does the opposite: it teleports the entity's raw location but
  compensates the local transform translation so the display's rendered position **does not
  change**. This is useful for re-anchoring a long-lived display's coordinates — for example, after
  it has drifted far from its spawn point through many small `moveRelative` calls — without any
  visible jump.

```java
// Panel currently rendering at world (1, 65, 1), reached via moveRelative offsets from its spawn point.
panel.rebaseEntity(panel.getLocation().add(new Vector(0, 0, 5))); // re-anchors 5 blocks over; nothing visibly moves
```

> [!IMPORTANT]
> `rebaseEntity` always updates the object's tracked `location` to match the entity's new raw
> position. This matters if you plan to call `setLocation` afterward with coordinates that happen to
> match an earlier local-space offset you used with `moveRelative`/`moveAbsolute` — without this,
> `setLocation` could compute its move relative to a stale location and re-apply that offset on top
> of the rebase, causing the display to jump.

## Cuboid displays

`ICuboidDisplay` (which *extends* `IColorableDisplay`, not the other way around) is the common interface for
anything box-shaped: `CubeColorDisplay`, `WireframeCubeColorDisplay`, and `FilledWireframeCubeColorDisplay`
all implement it, which is what lets [`BlockHighlighter`](block-highlighting.md) spawn any of the three
interchangeably based on a `HighlightDisplayType` argument. It only adds `spawnDisplay()` and a convenience
`asPositionObject()` cast (every current implementor is also a `PositionObject`) on top of what it inherits —
see-through control (`isSeeThrough()`/`setSeeThrough(boolean)`) lives up on `IColorableDisplay` now, not
declared separately here.

Continue to [Displays](displays.md) for a tour of each concrete display type.
