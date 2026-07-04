# Design Patterns

The v1.1.0 restructure wasn't just moving files into better-named folders — several classic OOP patterns
were introduced to remove duplication that had built up in the old layout. Each section below explains the
pattern in general terms, then points at exactly where it lives in this codebase.

## Package by feature, not by layer or generic name

**The idea:** name packages after *what the code does*, not after *what kind of thing it is* (`util`,
`helpers`, `common`) or a project-specific abbreviation nobody outside the project would recognize (`def`).
A good package name should tell a reader what's inside before they open it.

**Before:** `def.active.Cube`, `def.util.highlighter`, `def.util.viewTinter` — `def` meant nothing (short for
"definitions"?), and `util` grouped two completely unrelated feature systems (block highlighting and screen
tinting) under a name that suggests generic helper code.

**After:** `display.cube`, `effect.highlighter`, `effect.viewtinter` — `display` is "things you can spawn and
see", `effect` is "timed visual effects layered on top of a display", `animation` is "how values change over
time", `core` is "the shared foundation everything else builds on". See
[Project Setup](project-setup.md#package-tour) for the full tour.

**Why it matters beyond this project:** package-by-feature scales much better than package-by-layer as a
codebase grows, because related code that changes together *lives* together — adding a new display type
touches one folder, not four.

## Template Method — `AbstractEntityBackedDisplay`

**The idea:** put the parts of an algorithm that are always the same in a base class, and defer only the
parts that vary to an abstract method each subclass implements. The base class controls the *order* of
operations; subclasses only fill in the blanks.

**Before:** `ColorDisplay`, `TextDisplay`, `BlockDisplayObject`, and `ItemDisplayObject` each independently
re-implemented an identical ~15-method block — every transform mutator (`moveRelative`, `scaleAbsolute`,
`LRotateAbsolute`, ...) called `super.x(...)` and then pushed the result to a live Bukkit entity. The *only*
thing that actually varied between the four classes was **what transform to compute** before pushing it.

**After:** `AbstractEntityBackedDisplay` implements every mutator once, calling a shared `updateEntity(int)`
after each one:

```java
protected void updateEntity(int time) {
    if (entity == null || !entity.isValid()) return;
    entity.setTransformation(resolveEntityTransform());   // <- the one abstract "hook"
    entity.setInterpolationDelay(0);
    entity.setInterpolationDuration(time);
    afterUpdateEntity();                                   // <- an optional hook, no-op by default
}

protected abstract Transformation resolveEntityTransform();
```

Each subclass implements only `resolveEntityTransform()` — `ColorDisplay` applies a block-scale correction,
`BlockDisplayObject` applies an origin-centering correction, `TextDisplay`/`ItemDisplayObject` return the
transform unchanged — and `ColorDisplay` additionally overrides the optional `afterUpdateEntity()` hook to
resync its see-through flag. That's the whole difference between the four classes now.

**Watch for:** a template method base class only pays off when the *invariant* part (the algorithm's shape)
is genuinely identical across implementers. If you find yourself adding `if (this instanceof X)` branches
inside the base class to handle an exception, that's a sign one implementer doesn't actually fit the
template and should override the whole method instead.

## Strategy — `EasingCurve`

**The idea:** when several classes differ only in *one interchangeable algorithm* (how to compute a value),
extract that algorithm into its own type and pass it in as a parameter, instead of writing a whole extra
class for every combination.

**Before:** every animation kind had a plain class and a `*Smooth` sibling — 10 classes for 5 animation
kinds — differing only in whether progress was passed through an easing curve first. The easing math itself
was duplicated near-verbatim across four of the five `*Smooth` classes.

**After:** one `EasingCurve` enum holds every easing algorithm as an interchangeable strategy:

```java
public enum EasingCurve {
    LINEAR { public float ease(float t) { return t; } },
    EASE_IN_OUT_SINE { public float ease(float t) { return -(float) (Math.cos(Math.PI * t) - 1) / 2; } },
    EASE_IN_OUT_CUBIC { public float ease(float t) { return t < 0.5f ? 4*t*t*t : 1 - (float) Math.pow(-2*t+2, 3) / 2; } };
    public abstract float ease(float t);
}
```

...and each animation class takes one as a constructor parameter instead of being duplicated per curve:

```java
float progress = easingCurve.ease((float) tick / duration);
```

`AnimationFactory.registerXAnimationSmooth(...)` is now just a thin wrapper that passes a non-default curve
into the same class `registerXAnimation(...)` uses with `EasingCurve.LINEAR` — 5 classes instead of 10, and
adding a new easing curve later means adding one enum constant, not five new classes.

**A Java-specific detail worth noting:** an enum with a method body per constant (like above) is Java's way
of doing Strategy without a separate interface + one class per implementation — each constant *is* its own
implementation. This only reads cleanly when the set of strategies is fixed and known in advance (you can't
add a new `EasingCurve` from outside this file); if you needed callers to supply arbitrary custom easing
functions, you'd use a plain `@FunctionalInterface` instead (see below).

## Composition over inheritance — `TimedEffectRegistry`

**The idea:** when two classes share behavior, it's tempting to reach for a common superclass. But
inheritance is a strong, *permanent* coupling — a subclass gets its parent's entire public API whether it
wants it or not, and cannot easily switch to a different implementation later. Composition — holding an
instance of the shared behavior as a field, rather than extending it — keeps that behavior swappable and
keeps the outer class's own public API exactly what it declares, nothing more.

**Before:** `BlockHighlighter`/`BlockHighlighterData` and `ViewTinter`/`ViewTinterData` were two hand-written
copies of the same pattern — a registry keyed by something (`Block` vs. `Player`), a per-key data object
tracking a display/animation-function/tick-task, and a shared "start/stop/tick" lifecycle.

**After:** `TimedEffectRegistry<K, D>` implements that shared registry-and-tick-task machinery once.
`BlockHighlighter` and `ViewTinter` each hold one as a **field**, not as a superclass:

```java
public class BlockHighlighter {
    private static final TimedEffectRegistry<Block, ICuboidDisplay> registry = new TimedEffectRegistry<>() {
        @Override
        protected void onRemove(Block key, ICuboidDisplay display) {
            display.remove();   // the one piece of behavior that's genuinely highlighter-specific
        }
    };

    public static void unhighlightBlock(Block block) {
        registry.remove(block);   // delegates, doesn't inherit
    }
}
```

Because it's composition, `BlockHighlighter`'s public API is still exactly the static methods it declares —
callers never see `TimedEffectRegistry` at all, and `ViewTinter`'s very different teardown logic (removing a
tint cube's passenger entities) lives entirely in *its own* `onRemove` override, not tangled into the shared
class.

**Rule of thumb:** reach for inheritance only for a genuine "is-a" relationship where the subclass should
expose everything the parent does (that's exactly the case for `AbstractEntityBackedDisplay` above — a
`ColorDisplay` really *is* a kind of entity-backed display). Reach for composition when you're sharing
*implementation* between things that aren't conceptually the same kind of object — a block highlight and a
player's screen tint aren't the same kind of thing, they just happen to need the same bookkeeping.

## Generics for infrastructure code

**The idea:** when you write the same class shape twice with only the types swapped, that's what generics
exist to eliminate.

`TimedEffectRegistry<K, D extends IColorableDisplay>` is generic over **what the key is** (`Block` for
highlights, `Player` for tints) and **what kind of display it manages**, so one implementation serves both
features. `EffectFunction<D>` (below) is generic over the display type its callback receives. This is the
same idea as `List<T>` — the container logic doesn't care what's inside, so don't write it once per type.

## Functional interfaces — `EffectFunction<D>`

**The idea:** an interface with exactly one abstract method (a "SAM type") lets callers pass a lambda
instead of writing a whole named class, when all you actually need is "a bit of behavior to call later".

**Before:** `IHighlighterFunction<V>` and `IViewTinterFunction` were separate, unrelated interfaces with the
same single-method shape (`onAnimationRestart(display)`), one of them missing the `@FunctionalInterface`
annotation that documents "this is meant to be a lambda target".

**After:** both extend a shared `EffectFunction<D>`:

```java
@FunctionalInterface
public interface EffectFunction<D> {
    void onAnimationRestart(D display);
    default boolean isRepeating() { return true; }
}
```

This is source-compatible with every existing lambda/anonymous-class caller (the compiler doesn't care that
the abstract method is now inherited rather than declared directly), and gives `IHighlighterFunction`
implementers a free `isRepeating()` override they didn't have before, at no extra cost.

## Facade — `AnimationFactory`

**The idea:** a facade is a simple, friendly entry point that hides a more complex subsystem behind it — not
a "real" design pattern in the polymorphic sense, just a deliberately thin wrapper.

`AnimationFactory.registerTranslationAnimation(obj, duration, start, end)` is a one-line static method that
constructs a `TranslationAnimation` and hands it to `GlobalAnimationTickHandler`. Nothing about it is clever
— that's the point. Callers never need to know that a `GlobalAnimationTickHandler` singleton exists, or how
to construct the underlying animation classes correctly; the facade exists purely to make the common case
short to type. (This is *not* the Gang-of-Four "Factory" pattern in the polymorphic-creation sense — there's
no subclassing decision being made — it's really just a facade, despite the name.)

## Encapsulating internals — `effect.internal` + `@ApiStatus.Internal`

**The idea:** make it obvious, both to readers and to tooling (IDEs flag `@ApiStatus.Internal` usage from
outside a project), which types are genuinely public API versus implementation detail that could change
without notice.

`TimedEffectRegistry` and `EffectFunction` live in `effect.internal` and are annotated `@ApiStatus.Internal`
— a consumer of this library should never construct or reference them directly, only the public
`BlockHighlighter`/`ViewTinter`/`IHighlighterFunction`/`IViewTinterFunction` types that sit in front of them.
Putting internals in their own package (rather than just relying on the annotation) also means a future
`module-info.java` could enforce this at compile time, if the project ever adopts the Java module system.

## Immutable value objects — `CubeColorInformation`

Not new in this restructure, but worth calling out: `CubeColorInformation`/`WireframeCubeColorInformation`
never mutate in place. `setTop(Color)` and friends return a **new** instance with one field changed:

```java
CubeColorInformation colors = new CubeColorInformation(Color.WHITE)
        .setTop(Color.RED)      // returns a new instance
        .setBottom(Color.BLUE); // returns another new instance
```

Immutable value objects are easier to reason about (no "who else is holding a reference to this and might
change it under me?") and safe to share freely — a `CubeColorDisplay` can hand its `CubeColorInformation` out
to a caller without worrying about it being mutated behind its back.

## Idempotent lifecycle methods — `spawnDisplay()` / `respawnEntity()`

Every entity-backed display's `spawnDisplay()` checks `if (entity != null) return entity;` before doing any
work. Calling it twice is safe and cheap — it's **idempotent**. This matters a lot for a Bukkit plugin, where
a display object might be spawned from several different code paths (a command, a scheduled task, a chunk
load event) and you don't want to have to track "did I already spawn this?" at every call site — the method
itself guarantees it's safe to call repeatedly.

## How the restructure was sequenced

A closing, more process-oriented note: the actual restructure was done in **dependency order**, as separate,
reviewable commits — `core/` first (since almost everything else imports `PositionObject`/the core
interfaces), then `display/` and `animation/` in parallel (both depend only on `core/`), then `effect/`
(depends on `display/cube`, `display/wireframecube`, `display/filledwireframecube`, and `animation`), and
finally `DisplayTestPlugin` + the docs (need every final package name settled first). Pure file moves were
kept separate from behavior changes (like the `AbstractEntityBackedDisplay` extraction or the `ColorAnimation`
bug fix) where practical, so each commit is small enough to review and — if something turned out wrong — to
revert independently. When you can't run a compiler to check your work as you go, sequencing a big refactor
this way, plus a final grep sweep for every old name/path, is what stands in for "compile early, compile
often".
