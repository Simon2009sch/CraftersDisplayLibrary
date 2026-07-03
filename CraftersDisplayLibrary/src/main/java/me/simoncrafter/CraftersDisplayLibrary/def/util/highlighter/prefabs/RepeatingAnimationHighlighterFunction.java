package me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.prefabs;

import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.AAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.def.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.def.util.highlighter.IHighliterFunction;

public class RepeatingAnimationHighlighterFunction implements IHighliterFunction<CubeColorDisplay> {

    AAnimationInterpolationFunction<CubeColorDisplay> function;

    @Override
    public void onAnimationRestart(CubeColorDisplay object) {
        //GlobalAnimationTickHandler.registerNew;
    }
}
