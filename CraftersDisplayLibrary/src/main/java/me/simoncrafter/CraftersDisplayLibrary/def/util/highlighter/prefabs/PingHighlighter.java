package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;
import org.joml.Vector3f;

public class PingHighlighter implements IHighliterFunction<ICuboidDisplay> {

    private final float minScale;
    private final float maxScale;
    private final int cycleDuration;
    private final Color color;

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

        GlobalAnimationTickHandler.registerNewScaleAnimation(positionObject, new AAnimationInterpolationFunction<>(cycleDuration, startVector, endVector, positionObject) {
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

        GlobalAnimationTickHandler.registerNewColorAnimation(object, new ACustomTypeAnimationInterpolationFunction<>(cycleDuration, color, color.setAlpha(0), object) {
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
