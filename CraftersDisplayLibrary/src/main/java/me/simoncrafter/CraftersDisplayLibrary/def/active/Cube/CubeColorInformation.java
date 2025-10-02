package me.simoncrafter.CraftersDisplayLibrary.def.active.Cube;

import org.bukkit.Color;
import org.jetbrains.annotations.Contract;

public class CubeColorInformation {
    private final Color top;
    private final Color bottom;
    private final Color front;
    private final Color back;
    private final Color left;
    private final Color right;

    public CubeColorInformation(Color top, Color bottom, Color front, Color back, Color left, Color right) {
        this.top = top;
        this.bottom = bottom;
        this.front = front;
        this.back = back;
        this.left = left;
        this.right = right;
    }

    public CubeColorInformation(Color color) {
        this.top = color;
        this.bottom = color;
        this.front = color;
        this.back = color;
        this.left = color;
        this.right = color;
    }

    public CubeColorInformation() {
        this.top = Color.RED;
        this.bottom = Color.BLUE;
        this.front = Color.GREEN;
        this.back = Color.YELLOW;
        this.left = Color.PURPLE;
        this.right = Color.ORANGE;
    }

    @Contract("_ -> new")
    public CubeColorInformation setTop(Color top) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    @Contract("_ -> new")
    public CubeColorInformation setBottom(Color bottom) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    @Contract("_ -> new")
    public CubeColorInformation setFront(Color front) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    
    @Contract("_ -> new")
    public CubeColorInformation setBack(Color back) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    
    @Contract("_ -> new")
    public CubeColorInformation setLeft(Color left) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    
    @Contract("_ -> new")
    public CubeColorInformation setRight(Color right) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }

    public Color getTop() {
        return top;
    }

    public Color getBottom() {
        return bottom;
    }

    public Color getFront() {
        return front;
    }

    public Color getBack() {
        return back;
    }

    public Color getLeft() {
        return left;
    }

    public Color getRight() {
        return right;
    }
}
