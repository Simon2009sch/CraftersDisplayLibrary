package me.simoncrafter.CraftersDisplayLibrary.def.active.Cube;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.active.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class CubeColorDisplay extends PositionObject implements IHidable {

    private boolean seeThrough = false;

    private CubeColorInformation colorInformation = new CubeColorInformation();
    private ColorDisplay top;
    private ColorDisplay bottom;
    private ColorDisplay front;
    private ColorDisplay back;
    private ColorDisplay left;
    private ColorDisplay right;


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

    public static CubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, CubeColorInformation colorInformation, boolean seeThrough) {
        return new CubeColorDisplay(new Transformation(translation, leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc, colorInformation, seeThrough);
    }
    public static CubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, CubeColorInformation colorInformation, boolean seeThrough) {
        return new CubeColorDisplay(new Transformation(translation, leftRotation, scale, rightRotation), loc, colorInformation, seeThrough);
    }
    public static CubeColorDisplay create(Location loc, Vector3f scale, Quaternionf leftRotation, CubeColorInformation colorInformation, boolean seeThrough) {
        return new CubeColorDisplay(new Transformation(new Vector3f(0, 0, 0), leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc, colorInformation, seeThrough);
    }

    public void spawnDisplay() {
        // Get cube's rotation to apply to face positions
        Quaternionf cubeLeftRotation = getLocalTransform().getLeftRotation();
        Quaternionf cubeRightRotation = getLocalTransform().getRightRotation();

        // Fixed unit positions for each face center
        Vector3f topPos = new Vector3f(0, 0.5f, 0).rotate(cubeLeftRotation).rotate(cubeRightRotation); // RED
        Vector3f bottomPos = new Vector3f(0, -0.5f, 0).rotate(cubeLeftRotation).rotate(cubeRightRotation); // BLUE
        Vector3f leftPos = new Vector3f(-0.5f, 0, 0).rotate(cubeLeftRotation).rotate(cubeRightRotation); // PURPLE
        Vector3f rightPos = new Vector3f(0.5f, 0, 0).rotate(cubeLeftRotation).rotate(cubeRightRotation); // ORANGE
        Vector3f frontPos = new Vector3f(0, 0, 0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // GREEN
        Vector3f backPos = new Vector3f(0, 0, -0.5f).rotate(cubeLeftRotation).rotate(cubeRightRotation); // YELLOW

        // Create all faces with rotation-adjusted positions
        top = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), topPos, new Quaternionf(-0.707f, 0, 0, 0.707f), colorInformation.getTop());
        bottom = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), bottomPos, new Quaternionf(0.707f, 0, 0, 0.707f), colorInformation.getBottom());
        left = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), leftPos, new Quaternionf(0, 0, 0, 1), colorInformation.getLeft());
        right = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), rightPos, new Quaternionf(0, 1, 0, 0), colorInformation.getRight());
        front = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), frontPos, new Quaternionf(0, 0.707f, 0, 0.707f), colorInformation.getFront());
        back = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), backPos, new Quaternionf(0, -0.707f, 0, 0.707f), colorInformation.getBack());

        // Spawn all displays
        top.spawnDisplay();
        bottom.spawnDisplay();
        left.spawnDisplay();
        right.spawnDisplay();
        front.spawnDisplay();
        back.spawnDisplay();

        // Apply initial transformation to all faces
        updateChildren(0);
    }

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

    public ColorDisplay getTop() {
        return top;
    }

    public ColorDisplay getBottom() {
        return bottom;
    }

    public ColorDisplay getFront() {
        return front;
    }

    public ColorDisplay getBack() {
        return back;
    }

    public ColorDisplay getLeft() {
        return left;
    }

    public ColorDisplay getRight() {
        return right;
    }

    //TODO: Implement hideable
    @Override
    public boolean isHiddenByDefault() {
        return false;
    }

    @Override
    public IDisplayable hideByDefault(boolean hide) {
        return null;
    }

    @Override
    public IDisplayable showForPlayer(Player player) {
        return null;
    }

    @Override
    public IDisplayable hideForPlayer(Player player) {
        return null;
    }

    @Override
    protected void updateChildren(int time) {
        super.updateChildren(time);

        Transformation finalTransform = getFinalTransform();
        Vector3f scale = finalTransform.getScale();

        top.setParentTransform(finalTransform, time);
        top.setParentApplierFunction(topBottomApplier);

        bottom.setParentTransform(finalTransform, time);
        bottom.setParentApplierFunction(topBottomApplier);

        //left right follows XYZ rules (default applier)
        left.setParentTransform(finalTransform, time);
        right.setParentTransform(finalTransform, time);

        front.setParentTransform(finalTransform, time);
        front.setParentApplierFunction(frontBackApplier);

        back.setParentTransform(finalTransform, time);
        back.setParentApplierFunction(frontBackApplier);

    }
}
