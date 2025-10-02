package me.simoncrafter.CraftersDisplayLibrary.def.active;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.QuaterniondInterpolator;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class ColorDisplay extends PositionObject implements IHidable {

    private TextDisplay entity = null;
    private boolean seeTrough = false;
    private Color color = Color.fromARGB(0, 0, 0, 0);
    private Display.Billboard billboard = Display.Billboard.FIXED;

    private ColorDisplay(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, Color color, boolean seeTrough, Display.Billboard billboard) {
        super(List.of(), new Transformation(translation, leftRotation, scale, rightRotation), loc);
        this.color = color;
        this.seeTrough = seeTrough;
        this.billboard = billboard;
    }

    public TextDisplay spawnDisplay() {
        if (entity != null) {
            return entity;
        }

        entity = getLocation().getWorld().spawn(getLocation(), TextDisplay.class);
        entity.setBillboard(billboard);
        entity.setBackgroundColor(color);
        entity.setSeeThrough(seeTrough);
        Transformation transform = scaleToBlock(getFinalTransform());
        entity.setTransformation(transform);

        return entity;
    }

    public static ColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, Color color, boolean seeTrough, Display.Billboard billboard) {
        return new ColorDisplay(loc, scale, translation, leftRotation, rightRotation, color, seeTrough, billboard);
    }

    public static ColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Color color) {
        return new ColorDisplay(loc, scale, translation, leftRotation, new Quaternionf(0, 0, 0, 1), color, false, Display.Billboard.FIXED);
    }

    public void killDisplay() {
        entity.remove();
    }

    @Override
    public void moveEntityStatic(Location location) {

        Vector oldLoc = getLocation().toVector();
        Vector newLoc = location.toVector();

        Vector diff = newLoc.subtract(oldLoc);
        Vector3f diff3f = diff.toVector3f().div(getLocalTransform().getScale());

        moveRelative(diff3f.mul(-1f), 0);
        entity.teleport(location);

        super.moveEntityStatic(location);
    }

    public TextDisplay getEntity() {
        return entity;
    }

    @Override
    public void setLocation(Location loc) {
        super.setLocation(loc);

        Vector oldLocation = entity.getLocation().toVector();
        Vector newLocation = loc.toVector();
        Vector difference = newLocation.subtract(oldLocation);

        entity.teleport(entity.getLocation().clone().add(difference));
    }

    @Override
    public void setParentTransform(Transformation transformation, int time) {
        super.setParentTransform(transformation, time);
        updateEntity(time);
    }

    @Override
    public void moveRelative(Vector3f movement, int time) {
        super.moveRelative(movement, time);
        updateEntity(time);
    }

    @Override
    public void moveAbsolute(Vector3f position, int time) {
        super.moveAbsolute(position, time);
        updateEntity(time);
    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {
        super.LRotateAbsolute(rotation, time);
        updateEntity(time);
    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {
        super.LRotateRelative(rotation, time);
        updateEntity(time);
    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {
        super.RRotateAbsolute(rotation, time);
        updateEntity(time);
    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {
        super.RRotateRelative(rotation, time);
        updateEntity(time);
    }

    @Override
    public void scaleAbsolute(Vector3f scale, int time) {
        super.scaleAbsolute(scale, time);
        updateEntity(time);
    }

    @Override
    public void scaleRelative(Vector3f scale, int time) {
        super.scaleRelative(scale, time);
        updateEntity(time);
    }

    private void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;
        entity.setTransformation(scaleToBlock(getFinalTransform()));
        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(time);
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


    @Override
    public IDisplayable clone() {
        return null;
    }





}
