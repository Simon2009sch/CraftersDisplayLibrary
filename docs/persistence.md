# Persistence

Every entity this library spawns is already tagged with `Tags.CDL_ENTITY`. `persistence.DisplayPersistence`
builds on that to solve a different problem: **surviving a restart**. Displays are normal Bukkit entities —
a server restart, or even a chunk unload, doesn't destroy them, but the Java `PositionObject` instances that
knew how to move/color/animate them are gone. `DisplayPersistence` lets you tag a tree once, then read it
back out of the world later as live, fully-functional objects.

## Basic usage

```java
import me.simoncrafter.CraftersDisplayLibrary.persistence.DisplayPersistence;

// After building and spawning something you want to survive a restart:
CubeColorDisplay cube = CubeColorDisplay.create(loc, scale, translation, rotation, colors);
cube.spawnDisplay();
DisplayPersistence.tag(cube);

// Later - e.g. in onEnable(), or per-chunk via a ChunkLoadEvent listener:
List<PositionObject> recovered = DisplayPersistence.readWorld(world);
// or, scoped to one chunk as it loads:
List<PositionObject> recoveredHere = DisplayPersistence.readChunk(chunk);
```

`readWorld`/`readChunk` return fully live objects wrapping the *existing* Bukkit entities — no new entities
are spawned. You can immediately call `moveRelative`, `setColor`, `hideByDefault`, etc. on them exactly like
any other display. Only the roots of each tagged tree are returned (a child's parent gets re-attached via
the ordinary `addChild` mechanism during reconstruction, so you get back one entry per independent structure,
not one per leaf entity).

## "Iteration" — grouping by plugin load

Every entity gets tagged with a timestamp captured once per `PluginHolder.setPlugin(...)` call — i.e. one
value per plugin enable/reload cycle, exposed as `PluginHolder.getRegisterTimestamp()`. This is what the
plan and the API call an **iteration**: everything spawned during one run of your plugin shares one
timestamp, distinct from what a previous run left behind.

```java
// What iterations currently have tagged displays loaded?
List<Long> iterations = DisplayPersistence.listIterationTimestamps(world);

// Remove everything from one specific old run:
DisplayPersistence.removeIteration(world, oldTimestamp);

// Nuke every CDL-tagged display in the world, from any run:
DisplayPersistence.removeAllIterations(world);
```

Each of the four cleanup/listing methods also has a `Chunk`-scoped overload
(`listIterationTimestamps(Chunk)`, `removeIteration(Chunk, long)`, `removeAllIterations(Chunk)`) —
useful for hooking a `ChunkLoadEvent` and reading back or cleaning up just that chunk's displays without
scanning the whole world.

## What's preserved, what isn't

Preserved: transform (translation/rotation/scale), the original creation `Location` (so `moveEntityStatic`/
`rebaseEntity` semantics still make sense after reconstruction), color and see-through state, block/item
data, text content and formatting, billboard mode, the 6 generic `Display` properties (teleport duration,
view range, shadow radius/strength, glow color override, brightness), and parent/child structure.

**Animation state is never persisted.** A display mid-pulse when the server stops comes back on the next
load in whatever "settled" state its last non-animated field values were — no in-flight
`GlobalAnimationTickHandler` tween or per-object animation task survives a restart. If you want a
reconstructed display to resume animating, restart the animation yourself after reading it back.

## How it works, briefly

Every node in the scene graph — whether a library type like `CubeColorDisplay` or a plain grouping
`PositionObject` you built yourself with `addChild` — gets its own UUID when `tag(...)` walks the tree. A
node that owns a real Bukkit entity (or several — `ShulkerBasedCollisionBox`'s marker+shulker pair,
`LineColorDisplay`'s 4 internal panels) tags that entity directly with its own uuid, its parent's uuid, its
type, and a serialized blob of its own state. A "pure grouping" node with no entity of its own
(`CubeColorDisplay`, `WireframeCubeColorDisplay`, `FilledWireframeCubeColorDisplay`) has no physical entity
to carry its tags, so its identity travels as an extra "ancestor record" embedded on every entity in its
subtree instead — which is what lets the whole tree be reconstructed from nothing but the physically-present
entities sitting in the world, with no phantom/unspawnable placeholder entities involved.

> [!NOTE]
> `tag(...)` is an explicit, one-off call — it is **not** run automatically by `spawnDisplay()`. Most
> temporary effects (a highlight, a tint) have no reason to survive a restart and shouldn't pay this cost;
> reach for `DisplayPersistence.tag(...)` specifically for structures you build once and want to persist.

Continue to [API Reference](api-reference.md) for the full `persistence` package listing.
