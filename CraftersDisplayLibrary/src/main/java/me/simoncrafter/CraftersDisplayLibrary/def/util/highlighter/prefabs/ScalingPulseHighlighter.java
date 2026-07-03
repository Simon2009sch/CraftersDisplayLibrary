package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.joml.Vector3f;

/**
 * {@link IHighliterFunction} prefab that linearly scales a {@link ICuboidDisplay} from
 * {@code minScale} to {@code maxScale} every cycle - a plain grow pulse, with no colour change and
 * no shrink-back. Unlike {@link PingHighlighter}, which resets to {@code minScale} once a cycle
 * finishes, this prefab's scale animation simply stops advancing once it reaches {@code maxScale}
 * and holds there until the next {@link #onAnimationRestart} call restarts the grow from {@code minScale}.
 */
public class ScalingPulseHighlighter implements IHighliterFunction<ICuboidDisplay> {

    private final float minScale;
    private final float maxScale;
    private final int cycleDuration;

    /**
     * @param minScale      scale at the start of each pulse
     * @param maxScale      scale the display grows to and holds at for the remainder of the cycle
     * @param cycleDuration ticks to grow from {@code minScale} to {@code maxScale}
     */
    public ScalingPulseHighlighter(float minScale, float maxScale, int cycleDuration) {
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.cycleDuration = cycleDuration;
    }

    @Override
    public void onAnimationRestart(ICuboidDisplay object) {
        Vector3f start = new Vector3f(minScale, minScale, minScale);
        Vector3f end = new Vector3f(maxScale, maxScale, maxScale);

        // Every ICuboidDisplay (CubeColorDisplay/WireframeCubeColorDisplay/FilledWireframeCubeColorDisplay)
        // is also a PositionObject, which is what the scale-animation registry operates on.
        PositionObject positionObject = (PositionObject) object;

        GlobalAnimationTickHandler.registerNewScaleAnimation(positionObject, new AAnimationInterpolationFunction<>(cycleDuration, start, end, positionObject) {
            @Override
            public void nextTick(int duration, int tick, Vector3f startScale, Vector3f endScale, PositionObject obj) {
                float progress = (float) tick / duration;

                float x = startScale.x + (endScale.x - startScale.x) * progress;
                float y = startScale.y + (endScale.y - startScale.y) * progress;
                float z = startScale.z + (endScale.z - startScale.z) * progress;

                obj.scaleAbsoluteNoUpdate(new Vector3f(x, y, z));
            }
        });
    }
}
