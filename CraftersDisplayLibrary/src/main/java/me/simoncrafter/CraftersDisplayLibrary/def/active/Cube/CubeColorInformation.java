package me.simoncrafter.CraftersDisplayLibrary.def.active.Cube;

import org.bukkit.Color;
import org.jetbrains.annotations.Contract;

/**
 * Immutable holder for the colours of a {@link CubeColorDisplay}'s 6 faces.
 * <p>
 * Mirrors {@link me.simoncrafter.CraftersDisplayLibrary.def.active.WireframeCube.WireframeCubeColorInformation}
 * for the solid-face cube. Despite the name, the {@code setX(Color)} methods are not mutators: each
 * returns a new instance with one face recoloured, leaving the original unchanged
 * ({@code @Contract("_ -> new")}), builder-style.
 */
public class CubeColorInformation {
    private final Color top;
    private final Color bottom;
    private final Color front;
    private final Color back;
    private final Color left;
    private final Color right;

    /** All 6 faces given independent colours. */
    public CubeColorInformation(Color top, Color bottom, Color front, Color back, Color left, Color right) {
        this.top = top;
        this.bottom = bottom;
        this.front = front;
        this.back = back;
        this.left = left;
        this.right = right;
    }

    /** Every face painted the same colour. */
    public CubeColorInformation(Color color) {
        this.top = color;
        this.bottom = color;
        this.front = color;
        this.back = color;
        this.left = color;
        this.right = color;
    }

    /** Every face white. */
    public CubeColorInformation() {
        /*
        this.top = Color.RED;
        this.bottom = Color.BLUE;
        this.front = Color.GREEN;
        this.back = Color.YELLOW;
        this.left = Color.PURPLE;
        this.right = Color.ORANGE;
        */
        this.top = Color.WHITE;
        this.bottom = Color.WHITE;
        this.front = Color.WHITE;
        this.back = Color.WHITE;
        this.left = Color.WHITE;
        this.right = Color.WHITE;
    }

    /** A copy with the top face recoloured. */
    @Contract("_ -> new")
    public CubeColorInformation setTop(Color top) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    /** A copy with the bottom face recoloured. */
    @Contract("_ -> new")
    public CubeColorInformation setBottom(Color bottom) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }
    /** A copy with the front face recoloured. */
    @Contract("_ -> new")
    public CubeColorInformation setFront(Color front) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }

    /** A copy with the back face recoloured. */
    @Contract("_ -> new")
    public CubeColorInformation setBack(Color back) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }

    /** A copy with the left face recoloured. */
    @Contract("_ -> new")
    public CubeColorInformation setLeft(Color left) {
        return new CubeColorInformation(top, bottom, front, back, left, right);
    }

    /** A copy with the right face recoloured. */
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
