package me.simoncrafter.CraftersDisplayLibrary.display.line;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IHidable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * A 3D line/beam between two points, rendered via a {@link RawLineDisplay} (4 thin
 * {@link me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay} panels arranged around the
 * line's axis). Used as the edge type for
 * {@link me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorDisplay}.
 * <p>
 * This is a {@link PositionObject} wrapper: the start point and direction passed to
 * {@link #create}/{@link #createFromDirection} are kept as {@code baseStartPoint}/{@code baseDirection}
 * in <em>local</em> space, and re-derived into world-space endpoints on every transform change (own
 * or inherited from a parent) via {@link #updateRawLineTransform}. This is what lets a line be
 * scaled/rotated/moved as part of a larger composite object while still being described in simple
 * local start/direction terms.
 * <p>
 * Most of the {@link IDisplayable}/{@link PositionObject} method overrides below are pure delegation
 * to {@code super} with no added behaviour - present only because this class needs to intercept a
 * few of them ({@link #setLocation}, {@link #moveEntityStatic}, {@link #updateChildren}) and Java
 * doesn't allow overriding just some members of an inherited API surface without listing the rest.
 */
public class LineColorDisplay extends PositionObject implements IHidable, IColorableDisplay {

    private RawLineDisplay rawLine;
    private Vector3f baseStartPoint;
    private Vector3f baseDirection;
    private boolean hiddenByDefault = false;
    private Transformation lastAppliedTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf());

    private LineColorDisplay(Vector3f startPoint, Vector3f direction, Color color, Location location, float thickness) {
        super(List.of(), new Transformation(new Vector3f(0, 0, 0), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf()), location);
        this.baseStartPoint = new Vector3f(startPoint);
        this.baseDirection = new Vector3f(direction);
        this.rawLine = new RawLineDisplay(startPoint, direction, color, location, thickness);
    }

    /** Creates a line from {@code startPoint} extending by {@code direction}, with a default thin thickness. */
    public static LineColorDisplay create(Vector3f startPoint, Vector3f direction, Color color, Location origin) {
        return create(startPoint, direction, color, origin, 0.01f);
    }

    /** Creates a line from {@code startPoint} extending by {@code direction}. */
    public static LineColorDisplay create(Vector3f startPoint, Vector3f direction, Color color, Location origin, float thickness) {
        return new LineColorDisplay(startPoint, direction, color, origin, thickness);
    }

    /**
     * Alias for {@link #create}, with a default thin thickness. Named for readability at call sites
     * that build the line from a direction vector rather than an explicit end point.
     */
    public static LineColorDisplay createFromDirection(Vector3f startPoint, Vector3f direction, Color color, Location origin) {
        return createFromDirection(startPoint, direction, color, origin, 0.01f);
    }

    /** Alias for {@link #create}. */
    public static LineColorDisplay createFromDirection(Vector3f startPoint, Vector3f direction, Color color, Location origin, float thickness) {
        return new LineColorDisplay(startPoint, direction, color, origin, thickness);
    }

    /** Spawns the 4 backing panel entities and applies any {@link #hideByDefault} state set before this call. */
    public void spawnDisplay() {
        rawLine.spawn();
        updateRawLineTransform(0);
        // Apply visibility state in case hideByDefault() was called before spawning
        for (int i = 0; i < 4; i++) {
            var display = rawLine.getDisplay(i);
            if (display != null) display.hideByDefault(hiddenByDefault);
        }
    }

    public float getThickness() {
        return rawLine.getThickness();
    }

    public void setThickness(float thickness) {
        setThickness(thickness, 0);
    }

    /** Sets line thickness, interpolated over {@code duration} ticks on the client. */
    public void setThickness(float thickness, int duration) {
        rawLine.setThickness(thickness, duration);
    }

    @Override
    public void setColor(Color color) {
        rawLine.setColor(color);
    }

    public Color getColor() {
        return rawLine.getColor();
    }

    public boolean isSeeThrough() {
        return rawLine.isSeeThrough();
    }

    public void setSeeThrough(boolean seeThrough) {
        rawLine.setSeeThrough(seeThrough);
    }

    /** The line's start point, in this object's local space. */
    public Vector3f getStartPoint() {
        return rawLine.getStartPoint();
    }

    /** The line's end point ({@code startPoint + direction}), in this object's local space. */
    public Vector3f getEndPoint() {
        return rawLine.getEndPoint();
    }

    /** The line's direction vector (end minus start), in this object's local space. */
    public Vector3f getDirection() {
        return rawLine.getDirection();
    }

    /** The line's length, i.e. the magnitude of {@link #getDirection()}. */
    public float getLength() {
        return rawLine.getLength();
    }

    public void setStartPoint(Vector3f newStart) {
        setStartPoint(newStart, 0);
    }

    /** Moves the start point, keeping the end point fixed (direction is recomputed), interpolated over {@code duration} ticks. */
    public void setStartPoint(Vector3f newStart, int duration) {
        this.baseStartPoint = new Vector3f(newStart);
        updateRawLineTransform(duration);
    }

    public void setEndPoint(Vector3f newEnd) {
        setEndPoint(newEnd, 0);
    }

    /** Moves the end point, keeping the start point fixed, interpolated over {@code duration} ticks. */
    public void setEndPoint(Vector3f newEnd, int duration) {
        Vector3f newDirection = new Vector3f(newEnd).sub(baseStartPoint);
        setDirection(newDirection, duration);
    }

    public void setDirection(Vector3f newDirection) {
        setDirection(newDirection, 0);
    }

    /** Sets the direction vector, keeping the start point fixed, interpolated over {@code duration} ticks. */
    public void setDirection(Vector3f newDirection, int duration) {
        this.baseDirection = new Vector3f(newDirection);
        updateRawLineTransform(duration);
    }

    /**
     * Re-derives the line's world-space start point and direction from the local
     * {@code baseStartPoint}/{@code baseDirection} by applying this object's current
     * {@linkplain #getFinalTransform() final transform} (parent * local): positions are scaled,
     * rotated, then translated; direction vectors are scaled and rotated but not translated.
     */
    private void updateRawLineTransform(int duration) {
        Transformation currentTransform = getFinalTransform();
        Vector3f scale = currentTransform.getScale();
        Quaternionf leftRotation = currentTransform.getLeftRotation();
        Quaternionf rightRotation = currentTransform.getRightRotation();

        // Apply the full (parent * local) transform to the line's endpoints.
        // A position is scaled (in local space), then rotated, then translated;
        // a direction vector is scaled and rotated but NOT translated.
        Vector3f transformedStart = new Vector3f(baseStartPoint)
                .mul(scale)
                .rotate(leftRotation)
                .rotate(rightRotation)
                .add(currentTransform.getTranslation());
        Vector3f transformedDir = new Vector3f(baseDirection)
                .mul(scale)
                .rotate(leftRotation)
                .rotate(rightRotation);

        rawLine.setStartPoint(transformedStart, duration);
        rawLine.setDirection(transformedDir, duration);

        lastAppliedTransform = new Transformation(currentTransform.getTranslation(), currentTransform.getLeftRotation(), currentTransform.getScale(), currentTransform.getRightRotation());
    }

    // --- The overrides below are largely pure delegation to PositionObject, present because this
    // --- class needs to intercept a few of them (setLocation, moveEntityStatic, updateChildren) to
    // --- keep the RawLineDisplay in sync, and Java requires overriding the whole method if any of it
    // --- changes visibility/behaviour relative to the supertype in a subclass-specific way here.

    @Override
    public void setParentTransform(Transformation transformation, int time) {
        super.setParentTransform(transformation, time);
    }

    @Override
    public Location getLocation() {
        return super.getLocation();
    }

    /** {@inheritDoc} Also updates the {@link RawLineDisplay}'s world-space origin. */
    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);
        rawLine.setWorldLocation(loc);
    }

    @Override
    public void setLocationNoUpdate(Location loc) {
        super.setLocationNoUpdate(loc);
    }

    @Override
    public Transformation getLocalTransform() {
        return super.getLocalTransform();
    }

    @Override
    public void setLocalTransform(Transformation transformation) {
        super.setLocalTransform(transformation);
    }

    @Override
    public void setLocalTransform(Transformation transformation, int time) {
        super.setLocalTransform(transformation, time);
    }

    /** {@inheritDoc} Also teleports the 4 backing panel entities directly and updates the {@link RawLineDisplay}'s world-space origin. */
    @Override
    public void moveEntityStatic(Location location) {
        super.moveEntityStatic(location);
        for (int i = 0; i < 4; i++) {
            var display = rawLine.getDisplay(i);
            if (display != null) display.moveEntityStatic(location);
        }
        rawLine.setWorldLocation(location);
    }

    @Override
    public Transformation getParentTransform() {
        return super.getParentTransform();
    }

    @Override
    public void addChild(IDisplayable child) {
        super.addChild(child);
    }

    @Override
    public void removeChild(IDisplayable child) {
        super.removeChild(child);
    }

    @Override
    public void setChildren(List<IDisplayable> children) {
        super.setChildren(children);
    }

    @Override
    public void forEveryChild(Consumer<IDisplayable> consumer) {
        super.forEveryChild(consumer);
    }

    @Override
    public List<IDisplayable> getChildren() {
        return super.getChildren();
    }

    @Override
    public void moveRelative(Vector3f movement, int time) {
        super.moveRelative(movement, time);
    }

    @Override
    public void moveAbsolute(Vector3f position, int time) {
        super.moveAbsolute(position, time);
    }

    @Override
    public void moveRelativeToWorld(Vector3f position, int time) {
        super.moveRelativeToWorld(position, time);
    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {
        super.LRotateAbsolute(rotation, time);
    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time, boolean useAnimation) {
        super.LRotateAbsolute(rotation, time, useAnimation);
    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {
        super.LRotateRelative(rotation, time);
    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time, boolean useAnimation) {
        super.LRotateRelative(rotation, time, useAnimation);
    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {
        super.RRotateAbsolute(rotation, time);
    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time, boolean useAnimation) {
        super.RRotateAbsolute(rotation, time, useAnimation);
    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {
        super.RRotateRelative(rotation, time);
    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time, boolean useAnimation) {
        super.RRotateRelative(rotation, time, useAnimation);
    }

    @Override
    public void scaleAbsolute(Vector3f scale, int time) {
        super.scaleAbsolute(scale, time);
    }

    @Override
    public void scaleRelative(Vector3f scale, int time) {
        super.scaleRelative(scale, time);
    }

    @Override
    public Transformation getTransformation() {
        return super.getTransformation();
    }

    @Override
    public IDisplayable setTransformation(Transformation transformation) {
        return super.setTransformation(transformation);
    }

    @Override
    public void runForEveryChild(Consumer<IDisplayable> action) {
        super.runForEveryChild(action);
    }

    /** {@inheritDoc} Also re-derives and pushes the world-space start/direction onto the {@link RawLineDisplay}. */
    @Override
    protected void updateChildren(int time) {
        super.updateChildren(time);
        updateRawLineTransform(time);
    }

    @Override
    public IDisplayable clone() {
        return super.clone();
    }

    @Override
    public void setParentApplierFunction(BiFunction<Transformation, Transformation, Transformation> func) {
        super.setParentApplierFunction(func);
    }

    @Override
    public BiFunction<Transformation, Transformation, Transformation> getParentApplierFunction() {
        return super.getParentApplierFunction();
    }

    /** {@inheritDoc} Also removes the 4 backing panel entities. */
    @Override
    public void remove() {
        super.remove();
        rawLine.remove();
    }

    @Override
    public Transformation getFinalTransform() {
        return super.getFinalTransform();
    }

    @Override
    protected Transformation scaleToBlock(Transformation transformation) {
        return super.scaleToBlock(transformation);
    }

    @Override
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    /**
     * {@inheritDoc} Always applies to all 4 backing panels (panels not yet spawned are skipped) -
     * they are this line's own rendering, not "children" in the {@link #getChildren()} sense. If
     * {@code recursive}, also applies to any actual children attached via {@link #addChild}.
     */
    @Override
    public IDisplayable hideByDefault(boolean hide, boolean recursive) {
        hiddenByDefault = hide;
        for (int i = 0; i < 4; i++) {
            var display = rawLine.getDisplay(i);
            if (display != null) display.hideByDefault(hide, true);
        }
        if (recursive) {
            forEveryChild(child -> {
                if (child instanceof IHidable hidable) hidable.hideByDefault(hide, true);
            });
        }
        return this;
    }

    /**
     * {@inheritDoc} Always applies to all 4 backing panels - they are this line's own rendering,
     * not "children" in the {@link #getChildren()} sense. If {@code recursive}, also applies to
     * any actual children attached via {@link #addChild}.
     */
    @Override
    public IDisplayable showForPlayer(Player player, boolean recursive) {
        for (int i = 0; i < 4; i++) {
            var display = rawLine.getDisplay(i);
            if (display != null) display.showForPlayer(player, true);
        }
        if (recursive) {
            forEveryChild(child -> {
                if (child instanceof IHidable hidable) hidable.showForPlayer(player, true);
            });
        }
        return this;
    }

    /**
     * {@inheritDoc} Always applies to all 4 backing panels - they are this line's own rendering,
     * not "children" in the {@link #getChildren()} sense. If {@code recursive}, also applies to
     * any actual children attached via {@link #addChild}.
     */
    @Override
    public IDisplayable hideForPlayer(Player player, boolean recursive) {
        for (int i = 0; i < 4; i++) {
            var display = rawLine.getDisplay(i);
            if (display != null) display.hideForPlayer(player, true);
        }
        if (recursive) {
            forEveryChild(child -> {
                if (child instanceof IHidable hidable) hidable.hideForPlayer(player, true);
            });
        }
        return this;
    }

    /**
     * Computes the rotation that aligns the reference direction {@code (0, 1, 0)} to {@code direction},
     * handling the parallel (identity) and antiparallel (180° about an arbitrary perpendicular axis)
     * degenerate cases.
     */
    public static Quaternionf vectorToQuaternion(Vector3f direction) {
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

    /**
     * Computes the rotation that aligns {@code referenceDirection} to {@code direction}, handling the
     * parallel (identity) and antiparallel (180° about an arbitrary perpendicular axis) degenerate
     * cases.
     */
    public static Quaternionf vectorToQuaternion(Vector3f direction, Vector3f referenceDirection) {
        Vector3f normalizedDir = new Vector3f(direction).normalize();
        Vector3f normalizedRef = new Vector3f(referenceDirection).normalize();

        float dot = normalizedRef.dot(normalizedDir);
        dot = Math.max(-1.0f, Math.min(1.0f, dot));

        if (Math.abs(dot - 1.0f) < 1e-6f) {
            return new Quaternionf();
        }

        if (Math.abs(dot + 1.0f) < 1e-6f) {
            Vector3f perpendicular = new Vector3f(1, 0, 0);
            if (Math.abs(normalizedRef.dot(perpendicular)) > 0.9f) {
                perpendicular.set(0, 1, 0);
            }
            return new Quaternionf().fromAxisAngleRad(perpendicular, (float) Math.PI);
        }

        Vector3f axis = normalizedRef.cross(normalizedDir, new Vector3f());
        axis.normalize();

        float theta = (float) Math.acos(dot);
        Quaternionf quat = new Quaternionf();
        quat.fromAxisAngleRad(axis, theta);

        return quat.normalize();
    }
}
