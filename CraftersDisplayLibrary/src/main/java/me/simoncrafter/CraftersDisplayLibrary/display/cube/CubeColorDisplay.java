package me.simoncrafter.CraftersDisplayLibrary.display.cube;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IHidable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;

/**
 * A cube with 6 independently colourable, solid faces, each rendered as a
 * {@link me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay}. This is the solid-face
 * counterpart to {@link me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorDisplay}
 * (edges only) and is combined with it inside
 * {@link me.simoncrafter.CraftersDisplayLibrary.display.filledwireframecube.FilledWireframeCubeColorDisplay}.
 * <p>
 * The 6 faces are added as children of this object, so all inherited transform controls (move /
 * rotate / scale, animated or not) propagate to them. Because a face on the Y axis (top/bottom) and
 * a face on the Z axis (front/back) need different remappings of the parent's non-uniform scale to
 * stay flush against the cube body, {@link #topBottomApplier} and {@link #frontBackApplier} are
 * installed as custom {@linkplain IDisplayable#setParentApplierFunction parent-transform appliers}
 * on those faces; left/right use the default XYZ applier.
 */
public class CubeColorDisplay extends PositionObject implements IHidable, IColorableDisplay, ICuboidDisplay {

    private boolean seeThrough = false;
    private boolean hiddenByDefault = false;

    private CubeColorInformation colorInformation = new CubeColorInformation();
    private ColorDisplay top;
    private ColorDisplay bottom;
    private ColorDisplay front;
    private ColorDisplay back;
    private ColorDisplay left;
    private ColorDisplay right;


    /** Remaps the parent's Y/Z scale components before applying it to a top/bottom face's local scale. */
    private static BiFunction<Transformation, Transformation, Transformation> topBottomApplier = (parent, local) -> {
        // Apply transformation in correct order: scale → rotate → translate
        Vector3f scaledTranslation = new Vector3f(local.getTranslation()).mul(parent.getScale());
        Vector3f rotatedTranslation = new Vector3f(scaledTranslation)
                .rotate(parent.getLeftRotation()).rotate(parent.getRightRotation());
        Vector3f finalTranslation = rotatedTranslation.add(parent.getTranslation());

        return new Transformation(
                new Vector3f(finalTranslation.x, finalTranslation.y, finalTranslation.z),
                parent.getLeftRotation().mul(local.getLeftRotation(), new Quaternionf()).normalize(),
                local.getScale().mul(new Vector3f(parent.getScale().x, parent.getScale().z, parent.getScale().y), new Vector3f()),
                parent.getRightRotation().mul(local.getRightRotation(), new Quaternionf()).normalize()
        );
    };
    /** Remaps the parent's X/Z scale components before applying it to a front/back face's local scale. */
    private static BiFunction<Transformation, Transformation, Transformation> frontBackApplier = (parent, local) -> {
        // Apply transformation in correct order: scale → rotate → translate
        Vector3f scaledTranslation = new Vector3f(local.getTranslation()).mul(parent.getScale());
        Vector3f rotatedTranslation = new Vector3f(scaledTranslation)
                .rotate(parent.getLeftRotation()).rotate(parent.getRightRotation());
        Vector3f finalTranslation = rotatedTranslation.add(parent.getTranslation());

        return new Transformation(
                new Vector3f(finalTranslation.x, finalTranslation.y, finalTranslation.z),
                parent.getLeftRotation().mul(local.getLeftRotation(), new Quaternionf()).normalize(),
                local.getScale().mul(new Vector3f(parent.getScale().z, parent.getScale().y, parent.getScale().x), new Vector3f()),
                parent.getRightRotation().mul(local.getRightRotation(), new Quaternionf()).normalize()
        );
    };


    private CubeColorDisplay(Transformation localTransform, Location location, CubeColorInformation colorInformation, boolean seeThrough) {
        super(List.of(), new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation()), location);
        this.colorInformation = colorInformation;
        this.seeThrough = seeThrough;
    }

    /** Creates a cube with no right rotation and the given see-through state. */
    public static CubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, CubeColorInformation colorInformation, boolean seeThrough) {
        return new CubeColorDisplay(new Transformation(translation, leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc, colorInformation, seeThrough);
    }
    /** Creates a non-see-through cube with no right rotation. */
    public static CubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, CubeColorInformation colorInformation) {
        return new CubeColorDisplay(new Transformation(translation, leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc, colorInformation, false);
    }
    /** Creates a cube with full control over both rotation components and see-through state. */
    public static CubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, CubeColorInformation colorInformation, boolean seeThrough) {
        return new CubeColorDisplay(new Transformation(translation, leftRotation, scale, rightRotation), loc, colorInformation, seeThrough);
    }
    /** Creates a cube centred at the origin (no translation) with no right rotation. */
    public static CubeColorDisplay create(Location loc, Vector3f scale, Quaternionf leftRotation, CubeColorInformation colorInformation, boolean seeThrough) {
        return new CubeColorDisplay(new Transformation(new Vector3f(0, 0, 0), leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc, colorInformation, seeThrough);
    }

    /**
     * Computes the 6 face positions/rotations from unit-cube geometry (rotated by this cube's own
     * local rotation), spawns a {@link ColorDisplay} for each face lazily (existing faces are left
     * alone, so this is safe to call again after adding children), adds them all as children, spawns
     * their entities, and re-applies any {@link #hideByDefault} state set before this call.
     */
    public void spawnDisplay() {
        try {
            // Get cube's rotation to apply to face positions
            Quaternionf cubeLeftRotation = getLocalTransform().getLeftRotation();
            Quaternionf cubeRightRotation = getLocalTransform().getRightRotation();

            // Fixed unit positions for each face center
            // colors beside are debug colors used when trying to find errors with the faces
            Vector3f topPos = new Vector3f(-0.5f, 0.5f, 0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // RED
            Vector3f bottomPos = new Vector3f(-0.5f, -0.5f, -0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // BLUE
            Vector3f leftPos = new Vector3f(-0.5f, -0.5f, 0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // PURPLE
            Vector3f rightPos = new Vector3f(0.5f, -0.5f, -0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // ORANGE
            Vector3f frontPos = new Vector3f(0.5f, -0.5f, 0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // GREEN
            Vector3f backPos = new Vector3f(-0.5f, -0.5f, -0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // YELLOW



            // Create all faces with rotation-adjusted positions
            if (top == null) top = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), topPos, new Quaternionf(-0.707f, 0, 0, 0.707f), new Quaternionf(), colorInformation.getTop(), seeThrough, Display.Billboard.FIXED);
            if (bottom == null) bottom = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), bottomPos, new Quaternionf(0.707f, 0, 0, 0.707f), new Quaternionf(), colorInformation.getBottom(), seeThrough, Display.Billboard.FIXED);
            if (left == null) left = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), leftPos, new Quaternionf(0, 0, 0, 1), new Quaternionf(), colorInformation.getLeft(), seeThrough, Display.Billboard.FIXED);
            if (right == null) right = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), rightPos, new Quaternionf(0, 1, 0, 0), new Quaternionf(), colorInformation.getRight(), seeThrough, Display.Billboard.FIXED);
            if (front == null) front = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), frontPos, new Quaternionf(0, 0.707f, 0, 0.707f), new Quaternionf(), colorInformation.getFront(), seeThrough, Display.Billboard.FIXED);
            if (back == null) back = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), backPos, new Quaternionf(0, -0.707f, 0, 0.707f), new Quaternionf(), colorInformation.getBack(), seeThrough, Display.Billboard.FIXED);

            // Add faces as children and spawn
            addChild(top);
            addChild(bottom);
            addChild(left);
            addChild(right);
            addChild(front);
            addChild(back);

            // Spawn all displays
            top.spawnDisplay();
            bottom.spawnDisplay();
            left.spawnDisplay();
            right.spawnDisplay();
            front.spawnDisplay();
            back.spawnDisplay();

            // Apply visibility state in case hideByDefault() was called before spawning
            top.hideByDefault(hiddenByDefault);
            bottom.hideByDefault(hiddenByDefault);
            left.hideByDefault(hiddenByDefault);
            right.hideByDefault(hiddenByDefault);
            front.hideByDefault(hiddenByDefault);
            back.hideByDefault(hiddenByDefault);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * Builds a {@link Transformation} that stretches and rotates a unit cube so it forms a beam
     * connecting {@code p1} to {@code p2}: scaled to the distance between the points along the local
     * X axis, rotated so local +X points from {@code p1} to {@code p2}, and translated to the
     * midpoint. Handles the degenerate zero-length case (returns a zero-X-scale, unrotated transform
     * at {@code p1}) and the antiparallel-direction edge case in the rotation maths (falls back to an
     * arbitrary perpendicular axis for the 180° rotation). Useful for building custom beam/line-like
     * effects out of a cube instead of {@code LineColorDisplay}.
     *
     * @return a transform that, applied to a unit cube, produces a beam from {@code p1} to {@code p2}
     */
    public static Transformation makeTransformBetween(Vector3f p1, Vector3f p2) {
        // 1. Compute direction vector from p1 → p2
        Vector3f diff = new Vector3f(p2).sub(p1);
        float length = diff.length();

        // Handle degenerate (zero-length) case
        if (length < 1e-6f) {
            return new Transformation(
                    new Vector3f(p1),               // translation
                    new Quaternionf(),              // left rotation (identity)
                    new Vector3f(0f, 1f, 1f),       // zero X scale
                    new Quaternionf()               // right rotation (identity)
            );
        }

        // 2. Normalize direction
        Vector3f dir = new Vector3f(diff).normalize();

        // 3. Compute rotation quaternion to align +X → dir
        Vector3f from = new Vector3f(1, 0, 0);
        Vector3f axis = from.cross(dir, new Vector3f());
        float axisLen = axis.length();
        float dot = from.dot(dir);
        dot = Math.max(-1.0f, Math.min(1.0f, dot)); // Clamp to avoid NaN

        Quaternionf qRot = new Quaternionf();

        if (axisLen < 1e-6f) {
            // Parallel or anti-parallel
            if (dot > 0f) {
                qRot.identity(); // same direction
            } else {
                // opposite direction (180°)
                axis.set(0, 1, 0); // arbitrary perpendicular axis
                qRot.fromAxisAngleRad(axis, (float) Math.PI);
            }
        } else {
            axis.normalize();
            float theta = (float) Math.acos(dot);
            qRot.fromAxisAngleRad(axis, theta);
        }

        // Ensure it’s a unit quaternion (pure rotation)
        qRot.normalize();

        // 4. Scale along X (length of line)
        Vector3f scale = new Vector3f(length, 1.0f, 1.0f);

        // 5. Translation — center the cube/beam between p1 and p2
        Vector3f translation = new Vector3f(p1).add(p2).mul(0.5f);

        // 6. Build and return the transformation
        return new Transformation(
                translation,    // center position
                qRot,           // left rotation (unit quaternion)
                scale,          // stretch along local X axis
                new Quaternionf() // right rotation = identity
        );
    }

    /** {@inheritDoc} Also teleports all 6 faces' backing entities directly. */
    @Override
    public void moveEntityStatic(Location location) {
        Vector oldLoc = getLocation().toVector();
        Vector newLoc = location.toVector();

        Vector diff = newLoc.subtract(oldLoc);

        moveRelative(diff.toVector3f().mul(-1), 0);

        super.moveEntityStatic(location);
        top.moveEntityStatic(location);
        bottom.moveEntityStatic(location);
        left.moveEntityStatic(location);
        right.moveEntityStatic(location);
        front.moveEntityStatic(location);
        back.moveEntityStatic(location);
    }

    /** The top face's display, or {@code null} before {@link #spawnDisplay()} has been called. */
    public ColorDisplay getTop() {
        return top;
    }

    /** The bottom face's display, or {@code null} before {@link #spawnDisplay()} has been called. */
    public ColorDisplay getBottom() {
        return bottom;
    }

    /** The front face's display, or {@code null} before {@link #spawnDisplay()} has been called. */
    public ColorDisplay getFront() {
        return front;
    }

    /** The back face's display, or {@code null} before {@link #spawnDisplay()} has been called. */
    public ColorDisplay getBack() {
        return back;
    }

    /** The left face's display, or {@code null} before {@link #spawnDisplay()} has been called. */
    public ColorDisplay getLeft() {
        return left;
    }

    /** The right face's display, or {@code null} before {@link #spawnDisplay()} has been called. */
    public ColorDisplay getRight() {
        return right;
    }

    public boolean isSeeThrough() {
        return seeThrough;
    }

    /** Sets see-through on all 6 faces at once (faces not yet spawned are skipped). */
    public void setSeeThrough(boolean seeThrough) {
        this.seeThrough = seeThrough;
        if (top != null) { top.setSeeThrough(seeThrough); }
        if (bottom != null) { bottom.setSeeThrough(seeThrough); }
        if (left != null) { left.setSeeThrough(seeThrough); }
        if (right != null) { right.setSeeThrough(seeThrough); }
        if (front != null) { front.setSeeThrough(seeThrough); }
        if (back != null) { back.setSeeThrough(seeThrough); }
    }

    /** Colours all 6 faces the same colour. */
    @Override
    public void setColor(Color color) {
        top.setColor(color);
        bottom.setColor(color);
        left.setColor(color);
        right.setColor(color);
        front.setColor(color);
        back.setColor(color);
    }

    @Override
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    /** {@inheritDoc} Applies to all 6 faces (faces not yet spawned are skipped, then picked up in {@link #spawnDisplay()}). */
    @Override
    public IDisplayable hideByDefault(boolean hide) {
        hiddenByDefault = hide;
        if (top != null) top.hideByDefault(hide);
        if (bottom != null) bottom.hideByDefault(hide);
        if (left != null) left.hideByDefault(hide);
        if (right != null) right.hideByDefault(hide);
        if (front != null) front.hideByDefault(hide);
        if (back != null) back.hideByDefault(hide);
        return this;
    }

    /** {@inheritDoc} Applies to all 6 faces. */
    @Override
    public IDisplayable showForPlayer(Player player) {
        if (top != null) top.showForPlayer(player);
        if (bottom != null) bottom.showForPlayer(player);
        if (left != null) left.showForPlayer(player);
        if (right != null) right.showForPlayer(player);
        if (front != null) front.showForPlayer(player);
        if (back != null) back.showForPlayer(player);
        return this;
    }

    /** {@inheritDoc} Applies to all 6 faces. */
    @Override
    public IDisplayable hideForPlayer(Player player) {
        if (top != null) top.hideForPlayer(player);
        if (bottom != null) bottom.hideForPlayer(player);
        if (left != null) left.hideForPlayer(player);
        if (right != null) right.hideForPlayer(player);
        if (front != null) front.hideForPlayer(player);
        if (back != null) back.hideForPlayer(player);
        return this;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Also propagates this cube's transform to all 6 faces and (re-)installs
     * {@link #topBottomApplier} / {@link #frontBackApplier} on the top/bottom and front/back faces
     * respectively, so non-uniform scaling of the cube stretches those faces correctly.
     */
    @Override
    protected void updateChildren(int time) {
        super.updateChildren(time);

        Transformation finalTransform = getFinalTransform();
        Vector3f scale = finalTransform.getScale();

        if (top != null) top.setParentTransform(finalTransform, time);
        if (top != null) top.setParentApplierFunction(topBottomApplier);

        if (bottom != null) bottom.setParentTransform(finalTransform, time);
        if (bottom != null) bottom.setParentApplierFunction(topBottomApplier);

        //left right follows XYZ rules (default applier)
        if (left != null) left.setParentTransform(finalTransform, time);
        if (right != null) right.setParentTransform(finalTransform, time);

        if (front != null) front.setParentTransform(finalTransform, time);
        if (front != null) front.setParentApplierFunction(frontBackApplier);

        if (back != null) back.setParentTransform(finalTransform, time);
        if (back != null) back.setParentApplierFunction(frontBackApplier);

    }

    /** {@inheritDoc} Also removes and clears all 6 face displays. */
    @Override
    public void remove() {
        super.remove();
        top.remove();
        top = null;
        bottom.remove();
        bottom = null;
        left.remove();
        left = null;
        right.remove();
        right = null;
        front.remove();
        front = null;
        back.remove();
        back = null;
    }
}
