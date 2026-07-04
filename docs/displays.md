# Displays

All display types below extend `PositionObject` and follow the same lifecycle: build with a static
`create(...)` factory, call `spawnDisplay()` to create the backing entities, mutate with the inherited
transform/color methods, and call `remove()` when finished. See [Core Concepts](core-concepts.md) if you
haven't read it yet — it explains the transform tree these all share.

## `ColorDisplay`

The atomic building block. Wraps a single Bukkit `TextDisplay` entity, used as a solid colored panel rather
than as text (it's given an empty-line text component and a background color, with the default background
disabled — the standard "text display as a colored plane" trick).

```java
ColorDisplay panel = ColorDisplay.create(
        location,
        new Vector3f(1, 1, 1),      // scale
        new Vector3f(0, 0, 0),      // translation
        new Quaternionf(),          // left rotation
        Color.RED
);
panel.spawnDisplay();
panel.setColor(Color.BLUE);
panel.setSeeThrough(true);
```

The remaining *colored* display types below are all ultimately built out of one or more `ColorDisplay`s
(or, for `LineColorDisplay`, out of `RawLineDisplay`, an internal, non-`PositionObject` helper that manages
four `ColorDisplay` panels directly). The block and item displays in the next two sections are the
exception: each wraps a single real Minecraft entity directly and renders an actual block or item rather
than a solid colour, so they do **not** implement `IColorableDisplay`.

> [!NOTE]
> `ColorDisplay.clone()` is currently unimplemented and returns `null`.

## `BlockDisplayObject`

The block-rendering counterpart to `ColorDisplay`. Wraps a single Bukkit `BlockDisplay` entity and renders
an actual Minecraft `BlockData` (e.g. stone, a log, a custom block) instead of a solid-colour panel. Like
every other display here it extends `PositionObject`, participates in the same transform tree, and supports
all the inherited move/rotate/scale methods (animated or not) — but because it shows a real block, it does
**not** implement `IColorableDisplay`. Use `setBlock(BlockData)` / `getBlock()` to control what is shown.

```java
import me.simoncrafter.CraftersDisplayLibrary.display.panel.BlockDisplayObject;
import org.bukkit.Material;

BlockDisplayObject block = BlockDisplayObject.create(
        location,
        new Vector3f(1, 1, 1),      // scale
        new Vector3f(0, 0, 0),      // translation
        new Quaternionf(),          // left rotation
        Material.DIAMOND_BLOCK.createBlockData()
);
block.spawnDisplay();

block.setBlock(Material.GOLD_BLOCK.createBlockData());   // swap the rendered block live
block.setBillboard(Display.Billboard.VERTICAL);           // takes effect on next spawn
```

Three `create(...)` overloads are provided: a full one taking both rotation quaternions, a billboard, and
the `BlockData`; a fixed-billboard one with no right rotation; and a fully-unrotated one. `spawnDisplay()`
is idempotent — calling it again just returns the existing entity — and `respawnEntity()` tears down and
re-spawns the backing entity for a full resync after a chunk reload.

> [!NOTE]
> `BlockDisplayObject.clone()` is currently unimplemented and returns `null`.

## `ItemDisplayObject`

The item-rendering counterpart to `ColorDisplay` and `BlockDisplayObject`. Wraps a single Bukkit
`ItemDisplay` entity and renders an actual `ItemStack` (a sword, a diamond, a custom player head) instead of
a coloured panel or block. It extends `PositionObject` and shares the same transform tree, but does **not**
implement `IColorableDisplay`. Use `setItem(ItemStack)` / `getItem()` to control what is shown, and
`setItemDisplayTransform(ItemDisplay.ItemDisplayTransform)` to control how the item model is posed
(`FIXED`, `GUI`, `HEAD`, `GROUND`, `FIRST_PERSON_RIGHT_HAND`, ...).

```java
import me.simoncrafter.CraftersDisplayLibrary.display.panel.ItemDisplayObject;
import org.bukkit.Material;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.inventory.ItemStack;

ItemDisplayObject item = ItemDisplayObject.create(
        location,
        new Vector3f(1, 1, 1),      // scale
        new Vector3f(0, 0, 0),      // translation
        new Quaternionf(),          // left rotation
        new ItemStack(Material.DIAMOND_SWORD)
);
item.spawnDisplay();

item.setItem(new ItemStack(Material.NETHER_STAR));                 // swap the rendered item live
item.setItemDisplayTransform(ItemDisplay.ItemDisplayTransform.HEAD); // render as if worn on a head
```

The same three `create(...)` overload shapes as `BlockDisplayObject` are provided (full / fixed-billboard
no-right-rotation / fully-unrotated), and `spawnDisplay()` / `respawnEntity()` behave identically. Item
displays are handy for floating loot indicators, 3D icons, decorative props, or anything where a real item
model reads better than a flat coloured plane.

> [!NOTE]
> `ItemDisplayObject.clone()` is currently unimplemented and returns `null`.

## `CubeColorDisplay`

A solid cube built from six `ColorDisplay` face panels (top/bottom/front/back/left/right), each independently
colorable via `CubeColorInformation`.

```java
CubeColorInformation colors = new CubeColorInformation(Color.WHITE)
        .setTop(Color.RED)
        .setBottom(Color.BLUE);

CubeColorDisplay cube = CubeColorDisplay.create(
        location,
        new Vector3f(1, 1, 1),
        new Vector3f(0, 0, 0),
        new Quaternionf(),
        colors,
        false // see-through
);
cube.spawnDisplay();

cube.getTop().setColor(Color.LIME);      // recolor one face directly
cube.setColor(Color.WHITE);              // recolor all six faces at once
cube.setSeeThrough(true);
```

`CubeColorInformation` is an immutable value holder — `setTop(Color)` and friends return a **new** instance
rather than mutating in place, so chain them or reassign the result.

Because the six faces are automatically kept in sync as children, scaling the cube non-uniformly (e.g.
`Vector3f(2, 1, 1)` to make a rectangular box) stretches all six faces correctly rather than just spacing
them further apart.

`CubeColorDisplay` also exposes a standalone static helper, `makeTransformBetween(p1, p2)`, which builds a
`Transformation` that stretches and rotates a unit cube into a beam connecting two arbitrary points — handy
for building custom point-to-point effects.

## `WireframeCubeColorDisplay`

The edges-only counterpart to `CubeColorDisplay`: 12 `LineColorDisplay` edges, no faces, addressed by the
`CubeEdge` enum (`TOP_FRONT`, `BOTTOM_LEFT`, `FRONT_RIGHT`, ...) and grouped by the `CubeFace` enum (each face
maps to the 4 edges bordering it).

```java
WireframeCubeColorInformation edgeColors = new WireframeCubeColorInformation(Color.WHITE);

WireframeCubeColorDisplay wireframe = WireframeCubeColorDisplay.create(
        location,
        new Vector3f(1, 1, 1),
        new Vector3f(0, 0, 0),
        new Quaternionf(),
        edgeColors,
        false,   // see-through
        0.1f     // edge thickness
);
wireframe.spawnDisplay();

wireframe.setEdgeColor(CubeEdge.TOP_FRONT, Color.YELLOW);
wireframe.setFaceColor(CubeFace.TOP, Color.RED);   // colors all 4 edges bordering the top face
wireframe.setThickness(0.05f, 20);                  // animate edge thickness over 20 ticks
```

Edge thickness stays constant regardless of the cube's own scale — only the edge *length* stretches with
non-uniform scaling.

## `FilledWireframeCubeColorDisplay`

Both of the above at once, kept in permanent sync: an internal `CubeColorDisplay` (faces) and
`WireframeCubeColorDisplay` (edges) are both added as children of this object, so every move/rotate/scale
propagates identically to both.

```java
FilledWireframeCubeColorDisplay both = FilledWireframeCubeColorDisplay.create(
        location,
        new Vector3f(1, 1, 1),
        new Vector3f(0, 0, 0),
        new Quaternionf(),
        new CubeColorInformation(Color.fromARGB(80, 255, 255, 255)), // translucent faces
        new WireframeCubeColorInformation(Color.WHITE)
);
both.spawnDisplay();

both.setFaceColor(CubeFace.TOP, Color.RED);       // face panel only
both.setFaceEdgesColor(CubeFace.TOP, Color.RED);  // the 4 edges bordering that face, not the panel
both.setColor(Color.WHITE);                       // all faces AND all edges at once
```

Faces and edges are independently colorable and independently see-through-able (`setFacesSeeThrough` /
`setEdgesSeeThrough` / `setSeeThrough` for both). This is the display type
[`BlockHighlighter`](block-highlighting.md) uses for its `FILLED_WIREFRAME` highlight shape.

## `LineColorDisplay`

A 3D line/beam between two points, rendered as four thin `ColorDisplay` panels arranged 90° apart around the
line's axis (so it reads as a solid line from any viewing angle, without needing a billboard).

```java
LineColorDisplay line = LineColorDisplay.createFromDirection(
        new Vector3f(0, 0, 0),   // start point, local space
        new Vector3f(0, 3, 0),   // direction (also encodes length)
        Color.AQUA,
        location,                // world-space origin
        0.05f                    // thickness
);
line.spawnDisplay();

line.setEndPoint(new Vector3f(0, 5, 0), 20); // animate the line growing, over 20 ticks
line.setThickness(0.1f);
```

`LineColorDisplay` is what `WireframeCubeColorDisplay` uses internally for each of its 12 edges — the same
type works standalone for arrows, beams, connectors, or debug rays.

Continue to [Animations](animations.md) for registered, easing-curve animations, or to
[Block Highlighting](block-highlighting.md) / [View Tinting](view-tinting.md) for the higher-level utilities
built on top of these display types.
