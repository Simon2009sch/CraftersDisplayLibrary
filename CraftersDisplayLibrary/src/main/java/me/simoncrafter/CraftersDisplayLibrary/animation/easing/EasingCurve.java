package me.simoncrafter.CraftersDisplayLibrary.animation.easing;

/**
 * Strategy for remapping an animation's linear {@code tick / duration} progress (in
 * {@code [0, 1]}) before it is used to interpolate a value, allowing an animation to accelerate
 * or decelerate instead of moving at a constant rate.
 * <p>
 * Used by every class in {@link me.simoncrafter.CraftersDisplayLibrary.animation.functions} to
 * collapse what used to be separate "plain" and "Smooth" implementations into a single
 * easing-parameterised class.
 */
public enum EasingCurve {

    /** No remapping: {@code ease(t) == t}. Constant-speed interpolation. */
    LINEAR {
        public float ease(float t) {
            return t;
        }
    },

    /** Slow start, fast middle, slow end, based on a cosine curve. */
    EASE_IN_OUT_SINE {
        public float ease(float t) {
            return -(float) (Math.cos(Math.PI * t) - 1) / 2;
        }
    },

    /** Slow start, fast middle, slow end, based on a cubic curve (steeper than {@link #EASE_IN_OUT_SINE}). */
    EASE_IN_OUT_CUBIC {
        public float ease(float t) {
            return t < 0.5f ? 4 * t * t * t : 1 - (float) Math.pow(-2 * t + 2, 3) / 2;
        }
    };

    /**
     * Remaps linear progress {@code t} (expected in {@code [0, 1]}) according to this curve.
     *
     * @param t linear progress, where {@code 0} is the start of the animation and {@code 1} is the end
     * @return the eased progress value to use for interpolation
     */
    public abstract float ease(float t);
}
