package me.simoncrafter.CraftersDisplayLibrary.def;

import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class PositionObject implements IDisplayable {

    private List<IDisplayable> children = null;
    private Transformation localTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));

    private Transformation parentTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));

    public PositionObject(List<IDisplayable> children, Transformation localTransform) {
        this.children = children;
        this.localTransform = localTransform;
    }

    @Override
    public void setParentTransform(Transformation transformation) {
        parentTransform = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        updateChildren();
    }

    @Override
    public Transformation getParentTransform() {
        return new Transformation(parentTransform.getTranslation(), parentTransform.getLeftRotation(), parentTransform.getScale(), parentTransform.getRightRotation());
    }

    @Override
    public void addChild(IDisplayable child) {
        children.add(child);
        updateChildren();
    }

    @Override
    public void removeChild(IDisplayable child) {
        children.remove(child);
    }

    @Override
    public void setChildren(List<IDisplayable> children) {
        this.children = new ArrayList<>(children);
        updateChildren();
    }

    @Override
    public List<IDisplayable> getChildren() {
        return children;
    }

    @Override
    public void moveRelative(Vector3f movement, int time) {
        localTransform = new Transformation(localTransform.getTranslation().add(movement), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        updateChildren();
    }

    @Override
    public void moveAbsolute(Vector3f position, int time) {
        localTransform = new Transformation(position, localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        updateChildren();
    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation().mul(rotation), localTransform.getScale(), localTransform.getRightRotation());
        updateChildren();
    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), rotation, localTransform.getScale(), localTransform.getRightRotation());
        updateChildren();
    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation().mul(rotation));
        updateChildren();
    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), rotation);
        updateChildren();
    }

    @Override
    public void scaleAbsolute(Vector3f scale, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale().add(scale), localTransform.getRightRotation());
        updateChildren();
    }

    @Override
    public void scaleRelative(Vector3f scale, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), scale, localTransform.getRightRotation());
        updateChildren();
    }


    @Override
    public Transformation getTransformation() {
        return new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
    }

    @Override
    public IDisplayable setTransformation(Transformation transformation) {
        localTransform = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        return this;
    }

    protected void runForEveryChild(Consumer<IDisplayable> action) {
        for (IDisplayable child : children) {
            action.accept(child);
        }
    }

    protected void updateChildren() {
        runForEveryChild(c -> {
            c.setParentTransform(getFinalTransform());
        });
    }

    @Override
    public IDisplayable clone() {
        return new PositionObject(
                new ArrayList<>(children),
                new Transformation(
                        localTransform.getTranslation(),
                        localTransform.getLeftRotation(),
                        localTransform.getScale(),
                        localTransform.getRightRotation())
        );
    }

    protected Transformation getFinalTransform() {
        //multiplies the offsets and scales of the parent
        return new Transformation(
                localTransform.getTranslation().mul(parentTransform.getScale()).add(parentTransform.getTranslation()),
                parentTransform.getLeftRotation().mul(localTransform.getLeftRotation()),
                localTransform.getScale().mul(parentTransform.getScale()),
                parentTransform.getRightRotation().mul(localTransform.getRightRotation())
        );
    }
}
