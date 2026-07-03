package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.ACustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;
import org.bukkit.Color;

public class RepeatingColorAnimationHighlighter implements IHighliterFunction<CubeColorDisplay> {

    ACustomTypeAnimationInterpolationFunction<Color, IColorableDisplay> function;

    @Override
    public void onAnimationRestart(CubeColorDisplay object) {
        GlobalAnimationTickHandler.registerNewColorAnimation(object, function);
    }
}

