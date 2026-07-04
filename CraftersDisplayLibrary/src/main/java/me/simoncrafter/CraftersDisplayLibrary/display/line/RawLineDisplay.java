package me.simoncrafter.CraftersDisplayLibrary.display.line;

import me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

/**
 * Internal geometry engine behind {@link LineColorDisplay}. Given a start point, direction vector,
 * and a {@code worldLocation} origin, renders a line as 4 thin
 * {@link me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay} "walls": each offset
 * perpendicular to the line's axis by {@code thickness / 2} and rotated 90° apart around that axis.
 * This billboard-less arrangement means that from any horizontal viewing angle at least one wall
 * renders edge-on (thin) and one renders broadside (visible as a flat line), faking a line that looks
 * roughly line-like from every direction without needing an actual billboard entity type.
 * <p>
 * Unlike {@link LineColorDisplay}, this class is not a {@link me.simoncrafter.CraftersDisplayLibrary.core.PositionObject}
 * - it operates directly in the coordinate space it's given and has no parent/child transform
 * propagation of its own.
 */
public class RawLineDisplay {

    private Vector3f startPoint;
    private Vector3f direction;
    private float thickness;
    private Color color;
    private boolean seeThrough;

    private ColorDisplay display0;
    private ColorDisplay display1;
    private ColorDisplay display2;
    private ColorDisplay display3;

    private Location worldLocation;

    /** Creates the geometry engine for a line; call {@link #spawn()} to actually create the panel entities. */
    public RawLineDisplay(Vector3f startPoint, Vector3f direction, Color color, Location worldLocation, float thickness) {
        this.startPoint = new Vector3f(startPoint);
        this.direction = new Vector3f(direction);
        this.color = color;
        this.worldLocation = worldLocation;
        this.thickness = thickness;
        this.seeThrough = false;
    }

    /** Spawns the 4 backing panel entities at their initial positions. */
    public void spawn() {
        float lineLength = direction.length();
        Quaternionf directionRotation = LineColorDisplay.vectorToQuaternion(direction);

        // Create 4 displays arranged 90° apart
        // Each display i is rotated by i*90° around Y axis, then by direction rotation
        for (int i = 0; i < 4; i++) {
            Quaternionf localRotation = new Quaternionf().rotateY((float) (i * Math.PI / 2f));
            Quaternionf finalRotation = directionRotation.mul(localRotation, new Quaternionf());

            Vector3f offset = getOffsetForDisplay(i);

            ColorDisplay display = ColorDisplay.create(worldLocation, new Vector3f(thickness, lineLength, thickness), offset.add(startPoint), finalRotation, color);
            display.setSeeThrough(seeThrough);
            display.spawnDisplay();

            switch (i) {
                case 0 -> display0 = display;
                case 1 -> display1 = display;
                case 2 -> display2 = display;
                case 3 -> display3 = display;
            }
        }
    }

    /** Removes all 4 backing panel entities that have been spawned. */
    public void remove() {
        if (display0 != null) display0.remove();
        if (display1 != null) display1.remove();
        if (display2 != null) display2.remove();
        if (display3 != null) display3.remove();
    }

    public void setStartPoint(Vector3f newStart) {
        this.startPoint = new Vector3f(newStart);
        updateDisplays(0);
    }

    public void setDirection(Vector3f newDirection) {
        this.direction = new Vector3f(newDirection);
        updateDisplays(0);
    }

    /** Moves the start point, interpolated over {@code duration} ticks. A no-op until {@link #spawn()} has been called. */
    public void setStartPoint(Vector3f newStart, int duration) {
        this.startPoint = new Vector3f(newStart);
        updateDisplays(duration);
    }

    /** Sets the direction vector, interpolated over {@code duration} ticks. A no-op until {@link #spawn()} has been called. */
    public void setDirection(Vector3f newDirection, int duration) {
        this.direction = new Vector3f(newDirection);
        updateDisplays(duration);
    }

    public void setThickness(float newThickness) {
        setThickness(newThickness, 0);
    }

    /** Sets line thickness, interpolated over {@code duration} ticks. A no-op until {@link #spawn()} has been called. */
    public void setThickness(float newThickness, int duration) {
        this.thickness = newThickness;
        updateDisplays(duration);
    }

    /** Colours all 4 backing panels (panels not yet spawned are skipped). */
    public void setColor(Color newColor) {
        this.color = newColor;
        if (display0 != null) display0.setColor(newColor);
        if (display1 != null) display1.setColor(newColor);
        if (display2 != null) display2.setColor(newColor);
        if (display3 != null) display3.setColor(newColor);
    }

    /** Sets see-through on all 4 backing panels (panels not yet spawned are skipped). */
    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        if (display0 != null) display0.setSeeThrough(seeThrough);
        if (display1 != null) display1.setSeeThrough(seeThrough);
        if (display2 != null) display2.setSeeThrough(seeThrough);
        if (display3 != null) display3.setSeeThrough(seeThrough);
    }

    public Vector3f getStartPoint() {
        return new Vector3f(startPoint);
    }

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }

    public Vector3f getEndPoint() {
        return new Vector3f(startPoint).add(direction);
    }

    /** The line's length, i.e. the magnitude of the direction vector. */
    public float getLength() {
        return direction.length();
    }

    public float getThickness() {
        return thickness;
    }

    public Color getColor() {
        return color;
    }

    public boolean isSeeThrough() {
        return seeThrough;
    }

    /**
     * One of the 4 backing panel displays, indexed 0-3 (each 90° apart around the line's axis), or
     * {@code null} if {@code index} is out of range or {@link #spawn()} hasn't been called yet.
     */
    public ColorDisplay getDisplay(int index) {
        return switch (index) {
            case 0 -> display0;
            case 1 -> display1;
            case 2 -> display2;
            case 3 -> display3;
            default -> null;
        };
    }

    /** Moves the line's world-space origin without changing its local start point/direction. */
    public void setWorldLocation(Location newLocation) {
        this.worldLocation = newLocation;
        updateDisplays(0);
    }

    /**
     * Recomputes each of the 4 panels' offset, rotation and scale from the current
     * {@code startPoint}/{@code direction}/{@code thickness} and pushes it to the panel, interpolated
     * over {@code duration} ticks. A no-op if {@link #spawn()} hasn't been called yet.
     */
    private void updateDisplays(int duration) {
        if (display0 == null || display1 == null || display2 == null || display3 == null) return;

        float lineLength = direction.length();
        Quaternionf directionRotation = LineColorDisplay.vectorToQuaternion(direction);

        for (int i = 0; i < 4; i++) {
            Quaternionf localRotation = new Quaternionf().rotateY((float) (i * Math.PI / 2f));
            Quaternionf finalRotation = directionRotation.mul(localRotation, new Quaternionf());

            Vector3f offset = getOffsetForDisplay(i).add(startPoint);
            Vector3f scale = new Vector3f(thickness, lineLength, thickness);

            ColorDisplay display = getDisplay(i);
            if (display != null) {
                Transformation transform = new Transformation(offset, finalRotation, scale, new Quaternionf());
                display.setLocalTransform(transform, duration);
            }
        }
    }

    /** The perpendicular offset (before rotation) of panel {@code displayIndex}, {@code thickness / 2} out along ±X or ±Z. */
    private Vector3f getOffsetForDisplay(int displayIndex) {
        float offset = thickness / 2f;
        return switch (displayIndex) {
            case 0 -> new Vector3f(-offset, 0, 0);      // +X direction
            case 1 -> new Vector3f(0, 0, offset);      // +Z direction
            case 2 -> new Vector3f(offset, 0, 0);     // -X direction
            case 3 -> new Vector3f(0, 0, -offset);     // -Z direction
            default -> new Vector3f();
        };
    }
}
