package me.simoncrafter.CraftersDisplayLibrary.def.active.Line;

import me.simoncrafter.CraftersDisplayLibrary.def.active.ColorDisplay;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class RawLineDisplay {

    private Vector3f startPoint;
    private Vector3f direction;
    private float thickness;
    private Color color;
    private boolean seeTrough;

    private ColorDisplay display0;
    private ColorDisplay display1;
    private ColorDisplay display2;
    private ColorDisplay display3;

    private Location worldLocation;

    public RawLineDisplay(Vector3f startPoint, Vector3f direction, Color color, Location worldLocation, float thickness) {
        this.startPoint = new Vector3f(startPoint);
        this.direction = new Vector3f(direction);
        this.color = color;
        this.worldLocation = worldLocation;
        this.thickness = thickness;
        this.seeTrough = false;
    }

    public void spawn() {
        float lineLength = direction.length();
        Quaternionf directionRotation = vectorToQuaternion(direction);

        // Create 4 displays arranged 90° apart
        // Each display i is rotated by i*90° around Y axis, then by direction rotation
        for (int i = 0; i < 4; i++) {
            Quaternionf localRotation = new Quaternionf().rotateY((float) (i * Math.PI / 2f));
            Quaternionf finalRotation = directionRotation.mul(localRotation, new Quaternionf());

            Vector3f offset = getOffsetForDisplay(i);

            ColorDisplay display = ColorDisplay.create(worldLocation, new Vector3f(thickness, lineLength, thickness), offset.add(startPoint), finalRotation, color);
            display.setSeeTrough(seeTrough);
            display.spawnDisplay();

            switch (i) {
                case 0 -> display0 = display;
                case 1 -> display1 = display;
                case 2 -> display2 = display;
                case 3 -> display3 = display;
            }
        }
    }

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

    public void setStartPoint(Vector3f newStart, int duration) {
        this.startPoint = new Vector3f(newStart);
        updateDisplays(duration);
    }

    public void setDirection(Vector3f newDirection, int duration) {
        this.direction = new Vector3f(newDirection);
        updateDisplays(duration);
    }

    public void setThickness(float newThickness) {
        setThickness(newThickness, 0);
    }

    public void setThickness(float newThickness, int duration) {
        this.thickness = newThickness;
        updateDisplays(duration);
    }

    public void setColor(Color newColor) {
        this.color = newColor;
        if (display0 != null) display0.setColor(newColor);
        if (display1 != null) display1.setColor(newColor);
        if (display2 != null) display2.setColor(newColor);
        if (display3 != null) display3.setColor(newColor);
    }

    public void setSeeTrough(boolean seeTrough) {
        this.seeTrough = seeTrough;
        if (display0 != null) display0.setSeeTrough(seeTrough);
        if (display1 != null) display1.setSeeTrough(seeTrough);
        if (display2 != null) display2.setSeeTrough(seeTrough);
        if (display3 != null) display3.setSeeTrough(seeTrough);
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

    public float getLength() {
        return direction.length();
    }

    public float getThickness() {
        return thickness;
    }

    public Color getColor() {
        return color;
    }

    public boolean isSeeTrough() {
        return seeTrough;
    }

    public ColorDisplay getDisplay(int index) {
        return switch (index) {
            case 0 -> display0;
            case 1 -> display1;
            case 2 -> display2;
            case 3 -> display3;
            default -> null;
        };
    }

    public void setWorldLocation(Location newLocation) {
        this.worldLocation = newLocation;
        updateDisplays(0);
    }

    private void updateDisplays(int duration) {
        if (display0 == null || display1 == null || display2 == null || display3 == null) return;

        float lineLength = direction.length();
        Quaternionf directionRotation = vectorToQuaternion(direction);

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

    private static Quaternionf vectorToQuaternion(Vector3f direction) {
        Vector3f normalizedDir = new Vector3f(direction).normalize();
        Vector3f referenceDir = new Vector3f(0, 1, 0);

        float dot = referenceDir.dot(normalizedDir);
        dot = Math.max(-1.0f, Math.min(1.0f, dot));

        if (Math.abs(dot - 1.0f) < 1e-6f) {
            return new Quaternionf();
        }

        if (Math.abs(dot + 1.0f) < 1e-6f) {
            return new Quaternionf().fromAxisAngleRad(new Vector3f(1, 0, 0), (float) Math.PI);
        }

        Vector3f axis = referenceDir.cross(normalizedDir, new Vector3f());
        axis.normalize();

        float theta = (float) Math.acos(dot);
        Quaternionf quat = new Quaternionf();
        quat.fromAxisAngleRad(axis, theta);

        return quat.normalize();
    }
}
