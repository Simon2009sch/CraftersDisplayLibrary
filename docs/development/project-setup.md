# Project Setup

## Modules

This repo is a Maven **reactor** — a parent `pom.xml` (packaging `pom`, no code of its own) that aggregates
two child modules:

```
CraftersDisplay (root pom.xml, groupId me.simoncrafter, version 1.1.0)
├── CraftersDisplayLibrary/   the publishable library — everything in docs/ documents this module
└── DisplayTestPlugin/        an in-game Paper plugin used to manually exercise the library while developing it
```

`DisplayTestPlugin` isn't part of the public API. It's a thin `/cdl` command (`testCommand.java`) that spawns
every display type, animation, highlight, and tint with real arguments — reading its source alongside these
docs is a good way to see a feature invoked end-to-end. It depends on `CraftersDisplayLibrary` like any other
consumer would, pinned to the current reactor version (`1.1.0`) in its own `pom.xml`.

### Why two Maven profiles

JitPack (which builds the library when a consumer adds it as a Gradle/Maven dependency) should only ever
build `CraftersDisplayLibrary` — it has no reason to compile a whole Paper test plugin. Rather than a second
reactor or a submodule flag, the root `pom.xml` uses two profiles that control which modules are included:

```xml
<profiles>
    <profile>
        <id>library</id>
        <activation><activeByDefault>true</activeByDefault></activation>
        <modules><module>CraftersDisplayLibrary</module></modules>
    </profile>
    <profile>
        <id>dev</id>
        <modules>
            <module>CraftersDisplayLibrary</module>
            <module>DisplayTestPlugin</module>
        </modules>
    </profile>
</profiles>
```

`library` is active by default, so `jitpack.yml`'s `mvn install -DskipTests` (see below) only ever touches
the library module. Building both modules locally means explicitly requesting `-Pdev`.

## Versioning

The reactor version (`pom.xml`, currently `1.1.0`) is the single source of truth `DisplayTestPlugin/pom.xml`
inherits from (`<parent><version>`) and separately pins as its `CraftersDisplayLibrary` dependency version —
both must be bumped together, or the `dev` profile build breaks. `DisplayTestPlugin`'s *own* artifact version
(`v1.0.8`) is unrelated and versioned independently, since it's never published anywhere.

There's no `CHANGELOG.md` or `VERSION` file. JitPack and the GitHub release workflow
(`.github/workflows/release.yml`) both derive the published version from **git tags** (`v*`), not from the
`pom.xml` content — bumping the `pom.xml` version is about keeping the reactor's own module versions
consistent with each other, not about what a consumer sees on JitPack.

## Build constraints

Maven here is only available bundled inside IntelliJ IDEA — there's no standalone `mvn` on this machine's
`PATH` (see `AGENTS.md` at the repo root). That means:

- Build and run the project from IntelliJ's Maven tool window, not a terminal.
- If you're using an AI coding assistant on this repo, it should never attempt to invoke `mvn` directly — it
  should verify changes by reading/grepping the source and ask you to confirm compilation in the IDE instead.

## Package tour

`CraftersDisplayLibrary`'s source root is `me.simoncrafter.CraftersDisplayLibrary`. As of the v1.1.0
restructure, everything below the root is organized **by feature**, not by layer or by an arbitrary name:

```
me.simoncrafter.CraftersDisplayLibrary/
├── PluginHolder.java          bootstrap: holds the JavaPlugin instance every internal Bukkit call needs
├── core/                      the scene-graph foundation every display type builds on
│   ├── PositionObject.java        base class: transform tree, two rotation channels, two animation mechanisms
│   ├── AbstractEntityBackedDisplay.java   shared base for the four single-entity display types
│   ├── BlockScale.java             the one version-dependent constant the whole library depends on
│   ├── Tags.java                   the one NamespacedKey used to tag every entity this library spawns
│   └── interfaces/                 IDisplayable (now also owns hide/show), IColorableDisplay (now also owns see-through), ICuboidDisplay
├── display/                   every concrete display type, grouped by what it renders
│   ├── panel/                      ColorDisplay, TextDisplay, BlockDisplayObject, ItemDisplayObject
│   ├── line/                       LineColorDisplay, RawLineDisplay
│   ├── cube/                       CubeColorDisplay, CubeColorInformation
│   ├── wireframecube/              WireframeCubeColorDisplay, WireframeCubeColorInformation, CubeEdge, CubeFace
│   └── filledwireframecube/        FilledWireframeCubeColorDisplay (composes cube/ + wireframecube/)
├── animation/                 the registered-animation system (see core-concepts.md's "two ways to animate")
│   ├── AnimationFactory.java       thin static facade for registering animations
│   ├── GlobalAnimationTickHandler.java   the one shared per-server tick loop that drives them
│   ├── spi/                        the generic interpolation-function base classes
│   ├── easing/                     EasingCurve — the easing strategies, shared by every animation kind
│   └── functions/                  one concrete animation class per kind (translation, rotation, scale, color)
├── effect/                    higher-level, ready-to-use gameplay utilities built on the above
│   ├── highlighter/                BlockHighlighter and its prefab pulse/glow/rainbow effects
│   ├── viewtinter/                 ViewTinter and its prefab fade/pulse effects
│   └── internal/                   TimedEffectRegistry, EffectFunction — shared plumbing behind both
└── persistence/                DisplayPersistence — tag a display tree, read it back after a restart (see persistence.md)
```

`IHidable` (per-player visibility) no longer exists as its own interface — its methods were folded directly
into `IDisplayable`, since every display in the library ended up implementing it anyway. Same story for
see-through, folded into `IColorableDisplay`.

This replaces the old `def`/`def.active`/`def.util` layout, where package names described nothing (`def` of
*what*?) and several subpackages were mixed-case (`Cube`, `WireframeCube`, `viewTinter`), which is invalid
Java package-naming convention. See [Design Patterns](design-patterns.md) for the patterns that came out of
untangling the duplication that layout had accumulated.

Continue to [Design Patterns](design-patterns.md) for the "why" behind the shapes of `core/` and `effect/` in
particular.
