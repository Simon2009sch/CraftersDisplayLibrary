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
        // Rotate the local translation by the parent's rotation
        Vector3f rotatedTranslation = new Vector3f(local.getTranslation())
                .rotate(parent.getLeftRotation());

        // Combine with parent translation and scale
        Vector3f finalTranslation = rotatedTranslation.mul(parent.getScale()).add(parent.getTranslation());

        return new Transformation(
                new Vector3f(finalTranslation.x, finalTranslation.y, finalTranslation.z),
                parent.getLeftRotation().mul(local.getLeftRotation(), new Quaternionf()).normalize(),
                local.getScale().mul(new Vector3f(parent.getScale().x, parent.getScale().z, parent.getScale().y), new Vector3f()),
                parent.getRightRotation().mul(local.getRightRotation(), new Quaternionf()).normalize()
        );
    };
    private static BiFunction<Transformation, Transformation, Transformation> frontBackApplier = (parent, local) -> {
        // Rotate the local translation by the parent's rotation
        Vector3f rotatedTranslation = new Vector3f(local.getTranslation())
                .rotate(parent.getLeftRotation());

        // Combine with parent translation and scale
        Vector3f finalTranslation = rotatedTranslation.mul(parent.getScale()).add(parent.getTranslation());

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

        top = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), new Vector3f(-0.5f, 0.5f, 0.5f), new Quaternionf(-0.707, 0, 0, 0.707), colorInformation.getTop());
        bottom = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), new Vector3f(-0.5f, -0.5f, -0.5f), new Quaternionf(0.707, 0, 0, 0.707), colorInformation.getBottom());
        left = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), new Vector3f(-0.5f, -0.5f, 0.5f), new Quaternionf(0, 0, 0, 1), colorInformation.getLeft());
        right = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), new Vector3f(0.5f, -0.5f, -0.5f), new Quaternionf(0, 1, 0, 0), colorInformation.getRight());
        front = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), new Vector3f(0.5f, -0.5f, 0.5f), new Quaternionf(0, 0.707, 0, 0.707), colorInformation.getFront());
        back = ColorDisplay.create(getLocation(), new Vector3f(1, 1, 1), new Vector3f(-0.5f, -0.5f, -0.5f), new Quaternionf(0, -0.707, 0, 0.707), colorInformation.getBack());

        top.spawnDisplay();
        bottom.spawnDisplay();
        left.spawnDisplay();
        right.spawnDisplay();
        front.spawnDisplay();
        back.spawnDisplay();
    }

    @Override
    public void moveEntityStatic(Location location) {
        Vector oldLoc = getLocation().toVector();
        Vector newLoc = location.toVector();

        Vector diff = newLoc.subtract(oldLoc);
        Vector3f diff3f = diff.toVector3f().div(getLocalTransform().getScale());

        moveRelative(diff3f.mul(-1f), 0);

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
