package me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.AnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction;
import org.bukkit.Color;
import org.joml.Vector3f;

/**
 * {@link IHighlighterFunction} prefab that grows a {@link ICuboidDisplay} from {@code minScale} to
 * {@code maxScale} while simultaneously fading its colour's alpha to 0, like a radar "ping" that
 * expands and disappears. Both animations run concurrently and restart together every cycle.
 */
public class PingHighlighter implements IHighlighterFunction<ICuboidDisplay> {

    private final float minScale;
    private final float maxScale;
    private final int cycleDuration;
    private final Color color;

    /**
     * @param minScale      scale at the start of each ping
     * @param maxScale      scale at the end of each ping
     * @param color         opaque colour the ping starts at before fading to transparent
     * @param cycleDuration ticks per ping cycle
     */
    public PingHighlighter(float minScale, float maxScale, Color color, int cycleDuration) {
        this.minScale = minScale;
        this.maxScale = maxScale;
        this.cycleDuration = cycleDuration;
        this.color = color;
    }

    @Override
    public void onAnimationRestart(ICuboidDisplay object) {
        Vector3f startVector = new Vector3f(minScale, minScale, minScale);
        Vector3f endVector = new Vector3f(maxScale, maxScale, maxScale);

        // Every ICuboidDisplay (CubeColorDisplay/WireframeCubeColorDisplay/FilledWireframeCubeColorDisplay)
        // is also a PositionObject, which is what the scale-animation registry operates on.
        PositionObject positionObject = (PositionObject) object;

        GlobalAnimationTickHandler.registerNewScaleAnimation(positionObject, new AnimationInterpolationFunction<>(cycleDuration, startVector, endVector, positionObject) {
            @Override
            public void nextTick(int duration, int tick, Vector3f startScale, Vector3f endScale, PositionObject obj) {
                if (tick >= duration) {
                    obj.scaleAbsoluteNoUpdate(new Vector3f(minScale, minScale, minScale));
                    return;
                }

                float progress = (float) tick / duration;
                float scale = minScale + (maxScale - minScale) * progress;

                obj.scaleAbsoluteNoUpdate(new Vector3f(scale, scale, scale));
            }
        });

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new CustomTypeAnimationInterpolationFunction<>(cycleDuration, color, color.setAlpha(0), object) {
            @Override
            public void nextTick(int duration, int tick, Color startColor, Color endColor, IColorableDisplay obj) {
                int startAlpha = startColor.getAlpha();

                if (tick >= duration || tick <= 0) {
                    obj.setColor(startColor.setAlpha(0));
                    return;
                }

                float progress = (float) tick / duration;
                int currentAlpha = Math.round(startAlpha - startAlpha * progress);
                obj.setColor(startColor.setAlpha(currentAlpha));
            }
        });
    }
}
