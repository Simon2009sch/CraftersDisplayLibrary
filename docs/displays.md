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
panel.setSeeTrough(true);
```

Every other display type in this library is ultimately built out of one or more `ColorDisplay`s (or, for
`LineColorDisplay`, out of `RawLineDisplay`, an internal, non-`PositionObject` helper that manages four
`ColorDisplay` panels directly).

> [!NOTE]
> `ColorDisplay.clone()` is currently unimplemented and returns `null`.

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
cube.setSeeTrough(true);
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

Faces and edges are independently colorable and independently see-through-able (`setFacesSeeTrough` /
`setEdgesSeeTrough` / `setSeeTrough` for both). This is the display type
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
