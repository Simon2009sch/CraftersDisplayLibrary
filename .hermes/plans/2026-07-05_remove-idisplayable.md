# Remove IDisplayable — Consolidate into PositionObject

> **For Hermes:** Execute with 3 parallel subagents: core changes, display classes, docs.

**Goal:** Remove the `IDisplayable` interface (which has exactly one implementor — `PositionObject`) and replace all references with `PositionObject` directly. Keep the role interfaces (`IColorableDisplay`, `ICuboidDisplay`, `IHidable`) that provide genuine polymorphism.

**Architecture:** `IDisplayable` is dead weight — it's a 210-line interface that mirrors `PositionObject` 1:1 with zero alternative implementations. The pattern matches the v1.1.0 simplification (10 animation classes → 5 + EasingCurve): collapse the interface/class pair where the interface adds no polymorphism.

**Tech Stack:** Java 21, Paper API, no build needed (IDE-only compile).

---

## Changes Summary

| File | Action |
|---|---|
| `core/interfaces/IDisplayable.java` | **DELETE** |
| `core/interfaces/IColorableDisplay.java` | Remove `extends IDisplayable` |
| `core/interfaces/ICuboidDisplay.java` | No change (extends IColorableDisplay, chain is fine) |
| `core/interfaces/IHidable.java` | Return types `IDisplayable` → `PositionObject` |
| `core/PositionObject.java` | Remove `implements IDisplayable`, `IDisplayable`→`PositionObject` in fields/params |
| `core/AbstractEntityBackedDisplay.java` | `IDisplayable`→`PositionObject` in fields/params |
| `display/panel/ColorDisplay.java` | Remove import of `IDisplayable` |
| `display/panel/TextDisplay.java` | Remove import of `IDisplayable` |
| `display/panel/BlockDisplayObject.java` | Remove import of `IDisplayable` |
| `display/panel/ItemDisplayObject.java` | Remove import of `IDisplayable` |
| `display/cube/CubeColorDisplay.java` | Remove import of `IDisplayable` |
| `display/wireframecube/WireframeCubeColorDisplay.java` | Remove import of `IDisplayable` |
| `display/filledwireframecube/FilledWireframeCubeColorDisplay.java` | Remove import of `IDisplayable` |
| `display/line/LineColorDisplay.java` | Remove import, `IDisplayable`→`PositionObject` in method signatures |
| `docs/core-concepts.md` | Update references |
| `docs/displays.md` | Update references |
| `docs/api-reference.md` | Update references |
| `docs/development/design-patterns.md` | Update references |
| `docs/getting-started.md` | Update references if needed |

---

## Task 1: Core Changes (interfaces + bases)

### Step 1: Update `IHidable` return types

In `core/interfaces/IHidable.java`, change all return types from `IDisplayable` to `PositionObject`:
- Line 31: `IDisplayable hideByDefault(boolean hide)` → `PositionObject hideByDefault(boolean hide)`
- Line 39: `IDisplayable showForPlayer(Player player)` → `PositionObject showForPlayer(Player player)`
- Line 47: `IDisplayable hideForPlayer(Player player)` → `PositionObject hideForPlayer(Player player)`

Add import: `import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;`

### Step 2: Update `IColorableDisplay`

In `core/interfaces/IColorableDisplay.java`, change:
- Line 14: `public interface IColorableDisplay extends IDisplayable{` → `public interface IColorableDisplay {`
- Remove the `IDisplayable` reference from the Javadoc on line 6.

### Step 3: Update `PositionObject`

In `core/PositionObject.java`:
- Line 64: `public class PositionObject implements IDisplayable {` → `public class PositionObject {`
- Remove `import ...IDisplayable` (line 4)
- Line 68: `List<IDisplayable>` → `List<PositionObject>`
- Line 103: `List<IDisplayable>` → `List<PositionObject>`
- Line 175: `IDisplayable child` → `PositionObject child`
- Line 181: `IDisplayable child` → `PositionObject child`
- Line 186: `List<IDisplayable>` → `List<PositionObject>`
- Line 192: `Consumer<IDisplayable>` → `Consumer<PositionObject>`
- Line 193: `IDisplayable display` → `PositionObject display`
- Line 199: `List<IDisplayable>` → `List<PositionObject>`
- Line 383: `IDisplayable setTransformation` → `PositionObject setTransformation`
- Line 389: `Consumer<IDisplayable>` → `Consumer<PositionObject>`
- Line 390: `IDisplayable child` → `PositionObject child`
- Line 397: `IDisplayable#setParentTransform` → `PositionObject#setParentTransform`
- Line 426: `IDisplayable clone()` → `PositionObject clone()`
- Line 451: `IDisplayable obj` → `PositionObject obj`

Also update Javadoc: line 22 `{@link IDisplayable}` → `{@link PositionObject}`, line 32 `{@link #setParentTransform` link reference.

### Step 4: Update `AbstractEntityBackedDisplay`

In `core/AbstractEntityBackedDisplay.java`:
- Remove `import ...IDisplayable` (line 4)
- Line 62: `List<IDisplayable>` → `List<PositionObject>`
- Line 239: `IDisplayable hideByDefault` → `PositionObject hideByDefault`
- Line 248: `IDisplayable showForPlayer` → `PositionObject showForPlayer`
- Line 256: `IDisplayable hideForPlayer` → `PositionObject hideForPlayer`

### Step 5: Delete `IDisplayable.java`

Delete `core/interfaces/IDisplayable.java`.

---

## Task 2: Concrete Display Classes

### Step 6: Panel displays — remove IDisplayable import

In `display/panel/ColorDisplay.java`, `display/panel/TextDisplay.java`, `display/panel/BlockDisplayObject.java`, `display/panel/ItemDisplayObject.java`:
- Remove the `import ...IDisplayable` line.

### Step 7: Cube displays — remove IDisplayable import

In `display/cube/CubeColorDisplay.java`, `display/wireframecube/WireframeCubeColorDisplay.java`, `display/filledwireframecube/FilledWireframeCubeColorDisplay.java`:
- Remove the `import ...IDisplayable` line.

### Step 8: LineColorDisplay — remove IDisplayable import + type changes

In `display/line/LineColorDisplay.java`:
- Remove `import ...IDisplayable` (line 5)
- Line 31: `IDisplayable}/{@link PositionObject` → `PositionObject`
- Line 255: `IDisplayable child` → `PositionObject child`
- Line 260: `IDisplayable child` → `PositionObject child`
- Line 265: `List<IDisplayable>` → `List<PositionObject>`
- Line 270: `Consumer<IDisplayable>` → `Consumer<PositionObject>`
- Line 275: `List<IDisplayable>` → `List<PositionObject>`
- Line 350: `IDisplayable setTransformation` → `PositionObject setTransformation`
- Line 355: `Consumer<IDisplayable>` → `Consumer<PositionObject>`
- Line 367: `IDisplayable clone()` → `PositionObject clone()`
- Line 405: `IDisplayable hideByDefault` → `PositionObject hideByDefault`
- Line 416: `IDisplayable showForPlayer` → `PositionObject showForPlayer`
- Line 426: `IDisplayable hideForPlayer` → `PositionObject hideForPlayer`

---

## Task 3: Documentation Updates

### Step 9: Update `docs/core-concepts.md`

- Line 8-13: Rewrite section. `IDisplayable` is gone. `PositionObject` is now *the* base class. Change:
  > `IDisplayable` is the interface most of your code should be written against...
  → `PositionObject` is the base class every display extends...

- Line 39: `IDisplayable` → `PositionObject` in "addChild/removeChild/setChildren on any IDisplayable"
- Line 67: "Every `IDisplayable` method" → "Every `PositionObject` method"
- Line 97-103: Update references to `IDisplayable` in IColorableDisplay/Hidable sections

### Step 10: Update `docs/development/design-patterns.md`

- Update any references to `IDisplayable` (search and replace → `PositionObject`)

### Step 11: Update `docs/api-reference.md`

- Remove the `IDisplayable` row from the `core.interfaces` table
- Update `IColorableDisplay` description (no longer `extends IDisplayable`)
- Update `ICuboidDisplay` description
- Update `IHidable` description

### Step 12: Update `docs/displays.md`

- Line 3: "extend PositionObject and follow" stays correct
- Check for any `IDisplayable` references

### Step 13: Update `docs/getting-started.md`

- Check for any `IDisplayable` references (unlikely but verify)
