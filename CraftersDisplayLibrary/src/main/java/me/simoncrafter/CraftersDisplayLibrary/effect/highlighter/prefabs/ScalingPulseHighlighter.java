package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.AnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction;
import org.joml.Vector3f;

/**
 * {@link IHighlighterFunction} prefab that linearly scales a {@link ICuboidDisplay} from
 * {@code minScale} to {@code maxScale} every cycle - a plain grow pulse, with no colour change and
 * no shrink-back. Unlike {@link PingHighlighter}, which resets to {@code minScale} once a cycle
 * finishes, this prefab's scale animation simply stops advancing once it reaches {@code maxScale}
 * and holds there until the next {@link #onAnimationRestart} call restarts the grow from {@code minScale}.
 */
public class ScalingPulseHighlighter implements IHighlighterFunction<ICuboidDisplay> {

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
        PositionObject positionObject = object.asPositionObject();

        GlobalAnimationTickHandler.registerNewScaleAnimation(positionObject, new AnimationInterpolationFunction<>(cycleDuration, start, end, positionObject) {
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

    @Override
    public int getInherentCycleDuration() {
        return cycleDuration;
    }
}
