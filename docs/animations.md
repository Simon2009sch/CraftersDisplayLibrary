# Animations

For most cases, the `time` parameter on `PositionObject`'s move/rotate/scale methods (see
[Core Concepts](core-concepts.md)) is all you need. This page covers the second, lower-level animation
system: registered interpolators driven by a single shared `GlobalAnimationTickHandler`. This is what
[Block Highlighting](block-highlighting.md) and [View Tinting](view-tinting.md) use internally, and it's
available directly for your own effects too.

## `AnimationFactory`

The public entry point. Each method registers the right interpolator for you:

```java
AnimationFactory.registerTranslationAnimation(obj, 20, startPos, endPos);
AnimationFactory.registerTranslationAnimationSmooth(obj, 20, startPos, endPos);

AnimationFactory.registerLRotationAnimation(obj, 20, startQuat, endQuat);
AnimationFactory.registerLRotationAnimationSmooth(obj, 20, startQuat, endQuat);
AnimationFactory.registerRRotationAnimation(obj, 20, startQuat, endQuat);
AnimationFactory.registerRRotationAnimationSmooth(obj, 20, startQuat, endQuat);

AnimationFactory.registerScalingAnimation(obj, 20, startScale, endScale);
AnimationFactory.registerScalingAnimationSmooth(obj, 20, startScale, endScale);

AnimationFactory.registerColorAnimation(colorableDisplay, 20, startColor, endColor);
AnimationFactory.registerColorAnimationSmooth(colorableDisplay, 20, startColor, endColor);
```

| Property | Plain | Smooth (eased) |
|---|---|---|
| Translation | `registerTranslationAnimation` | `registerTranslationAnimationSmooth` |
| Left rotation | `registerLRotationAnimation` | `registerLRotationAnimationSmooth` |
| Right rotation | `registerRRotationAnimation` | `registerRRotationAnimationSmooth` |
| Scale | `registerScalingAnimation` | `registerScalingAnimationSmooth` |
| Color | `registerColorAnimation` | `registerColorAnimationSmooth` |

- **Plain** variants interpolate linearly (lerp for vectors, slerp for rotation) from start to end over the
  given duration in ticks — this is `EasingCurve.LINEAR` under the hood.
- **Smooth** variants apply an easing curve first — `EasingCurve.EASE_IN_OUT_SINE` for
  translation/rotation/scale, `EasingCurve.EASE_IN_OUT_CUBIC` for color — for a gentler start and end
  instead of constant speed. See [`EasingCurve`](#easingcurve) below.

## `EasingCurve`

`EasingCurve` is the enum that unifies what used to be separate "plain" and "Smooth" animation classes into
a single class per animation kind (translation, left rotation, right rotation, scale, color) with an
optional easing curve constructor argument. It has three values:

| Value | Behavior |
|---|---|
| `LINEAR` | No remapping — constant-speed interpolation. Used by the plain `registerXAnimation` methods. |
| `EASE_IN_OUT_SINE` | Slow start, fast middle, slow end, based on a cosine curve. Used by `registerXAnimationSmooth` for translation, rotation and scale. |
| `EASE_IN_OUT_CUBIC` | Slow start, fast middle, slow end, based on a cubic curve (steeper than `EASE_IN_OUT_SINE`). Used by `registerColorAnimationSmooth`. |

Each animation class (`TranslationAnimation`, `RotationAnimation`, `RRotationAnimation`, `ScalingAnimation`,
`ColorAnimation`) has a 3-arg constructor that defaults to `EasingCurve.LINEAR`, and an overload taking an
explicit `EasingCurve`. `AnimationFactory.registerXAnimationSmooth` is just a thin wrapper that passes a
non-default `EasingCurve` into the same constructor `registerXAnimation` (plain) uses with `LINEAR` — there
are no longer separate `*Smooth` classes, only one class per animation kind with an optional easing curve.
If you're writing a custom interpolator (see below) and want the same acceleration behavior, call
`easingCurve.ease(t)` on your own progress fraction before interpolating.

## How it's driven

`GlobalAnimationTickHandler` is a static registry keyed by target object, one map per animation type
(translation / scale / left rotation / right rotation / color). Registering a new animation of a given type
on an object **replaces** any animation of that same type already running on it — there's no queueing or
additive blending of same-type animations on one object.

Once registered, a single shared repeating `BukkitTask` (1 tick / 20 tps) calls every animation's `nextTick`
each server tick. When an animation finishes (`tick > duration`), the handler snaps the object to the exact
end value and removes it from the registry, then propagates the change to children once per object per tick.

You generally don't need to interact with `GlobalAnimationTickHandler` directly — `AnimationFactory` is the
intended entry point — but `GlobalAnimationTickHandler.removeColorAnimation(display)` is occasionally useful
to cancel an in-flight color animation early (both `BlockHighlighter.unhighlightBlock` and
`ViewTinter.untintPlayer` do this to make sure a highlight/tint doesn't keep animating after it's supposed to
be gone).

## Writing your own interpolator

Both `IHighlighterFunction` prefabs (see [Block Highlighting](block-highlighting.md)) and `IViewTinterFunction`
prefabs (see [View Tinting](view-tinting.md)) are built by registering small anonymous
`CustomTypeAnimationInterpolationFunction`/`AnimationInterpolationFunction` subclasses inline. The general
shape:

```java
GlobalAnimationTickHandler.registerNewColorAnimation(display,
        new CustomTypeAnimationInterpolationFunction<Color, IColorableDisplay>(20, startColor, endColor, display) {
            @Override
            public void nextTick(int duration, int tick, Color start, Color end, IColorableDisplay obj) {
                float progress = (float) tick / duration;
                // compute and apply an interpolated value to obj
            }
        });
```

`AnimationInterpolationFunction<V>` is the same idea specialized for `PositionObject` targets (translation,
scale, rotation); `CustomTypeAnimationInterpolationFunction<V, K>` is the fully generic base used for color
(`K = IColorableDisplay`).

> [!NOTE]
> Every plain and smooth interpolator nudges the interpolated value by a tiny epsilon at `tick == 0` before
> writing it. This works around a Minecraft client quirk where restarting a transformation-interpolation at
> an exact, unchanged value at t=0 can cause a visible flash or replay the previous animation frame. If
> you're writing a custom interpolator that snaps to a value at tick 0, consider doing the same.
