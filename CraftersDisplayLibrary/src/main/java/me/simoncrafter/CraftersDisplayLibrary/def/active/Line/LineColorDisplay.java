package me.simoncrafter.CraftersDisplayLibrary.def.active.Line;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.active.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class LineColorDisplay extends PositionObject implements IHidable, IColorableDisplay {

    private boolean seeTrough = false;
    private Color color;
    private float thickness = 0.1f;
    private Vector3f direction = new Vector3f(0,1,0);
    private ColorDisplay frontDisply;
    private ColorDisplay backDisplay;
    private ColorDisplay rightDisply;
    private ColorDisplay leftDisplay;

    private LineColorDisplay(Vector3f translation, Color color, Location location, Vector3f direction, float thickness) {
        super(List.of(), new Transformation(translation, vectorToQuaternion(direction), new Vector3f(), new Quaternionf()), location);
        this.thickness = thickness;
        this.direction = direction;
        this.color = color;
    }

    public static LineColorDisplay create(Vector3f translation, Color color, Location origin, Vector3f direction) {
        return create(translation, color, origin, direction, 0.01f);
    }

    public static LineColorDisplay create(Vector3f translation, Color color, Location origin, Vector3f direction, float thickness) {
        return new LineColorDisplay(translation, color, origin, direction, thickness);
    }

    public void spawnDisplay() {
        frontDisply = ColorDisplay.create(getLocation(), new Vector3f(thickness, direction.length(), thickness), new Vector3f(-(thickness/2), 0, 0).rotate(getTransformation().getLeftRotation()).rotate(getTransformation().getRightRotation()), getTransformation().getLeftRotation(), color);

        frontDisply.spawnDisplay();
        updateChildren(0);
    }

    public float getThickness() {
        return thickness;
    }

    public void setThickness(float thickness, int duration) {
        this.thickness = thickness;
        if (frontDisply != null && frontDisply.getEntity() != null) {
            frontDisply.scaleAbsolute(new Vector3f(thickness, direction.length(), thickness), duration);
        }
    }

    @Override
    public void setColor(Color color) {

    }

    public boolean isSeeTrough() {
        return seeTrough;
    }

    public void setSeeTrough(boolean seeTrough) {
        this.seeTrough = seeTrough;
    }

    public void setThickness(float thickness) {
        setThickness(thickness, 0);
    }

    public Vector3f getDirection() {
        return new Vector3f(direction);
    }

    public void setDirection(Vector3f newDirection, int duration) {
        this.direction = new Vector3f(newDirection);
        Quaternionf newRotation = vectorToQuaternion(newDirection);
        LRotateAbsolute(newRotation, duration, false);

        if (frontDisply != null && frontDisply.getEntity() != null) {
            Vector3f offsetPos = new Vector3f(-(thickness/2), 0, 0).rotate(newRotation);
            frontDisply.moveAbsolute(offsetPos, duration);
            frontDisply.scaleAbsolute(new Vector3f(thickness, direction.length(), thickness), duration);
            frontDisply.LRotateAbsolute(newRotation, duration, false);
        }
    }

    public void setDirection(Vector3f newDirection) {
        setDirection(newDirection, 0);
    }

    @Override
    public Location getLocation() {
        return super.getLocation();
    }

    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);
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

    @Override
    public void moveEntityStatic(Location location) {
        super.moveEntityStatic(location);
    }

    @Override
    public void setParentTransform(Transformation transformation, int time) {
        super.setParentTransform(transformation, time);
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
    protected void forEveryChild(Consumer<IDisplayable> consumer) {
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
    protected void runForEveryChild(Consumer<IDisplayable> action) {
        super.runForEveryChild(action);
    }

    @Override
    protected void updateChildren(int time) {
        super.updateChildren(time);
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

    @Override
    public void remove() {
        super.remove();
        frontDisply.remove();
        backDisplay.remove();
        leftDisplay.remove();
        rightDisply.remove();
    }

    @Override
    protected Transformation getFinalTransform() {
        return super.getFinalTransform();
    }

    @Override
    protected Transformation scaleToBlock(Transformation transformation) {
        return super.scaleToBlock(transformation);
    }

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

    //claude
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

    //claude
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
