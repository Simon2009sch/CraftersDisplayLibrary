package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.Contract;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.function.BiFunction;

public interface IDisplayable {

    void addChild(IDisplayable child);
    void removeChild(IDisplayable child);
    void setChildren(List<IDisplayable> children);
    List<IDisplayable> getChildren();

    void setParentTransform(Transformation transformation, int time);
    Transformation getParentTransform();

    void moveRelative(Vector3f movement, int time);
    void moveAbsolute(Vector3f position, int time);
    void moveRelativeToWorld(Vector3f position, int time);
    void LRotateAbsolute(Quaternionf rotation, int time);
    void LRotateRelative(Quaternionf rotation, int time);
    void RRotateAbsolute(Quaternionf rotation, int time);
    void RRotateRelative(Quaternionf rotation, int time);
    void scaleAbsolute(Vector3f scale, int time);
    void scaleRelative(Vector3f scale, int time);

    void setParentApplierFunction(BiFunction<Transformation, Transformation, Transformation> func);
    BiFunction<Transformation, Transformation, Transformation> getParentApplierFunction();

    Transformation getTransformation();

    IDisplayable setTransformation(Transformation transformation);

    IDisplayable clone();

    void setLocationNoUpdate(Location loc);
    void setLocation(Location loc);
    Location getLocation();

    Transformation getLocalTransform();
    void setLocalTransform(Transformation transformation);
    void setLocalTransform(Transformation transformation, int time);

    void moveEntityStatic(Location location);
}
