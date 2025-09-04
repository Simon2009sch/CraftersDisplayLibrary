package me.simoncrafter.CraftersDisplayLibrary.def.active;

import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

public class ColorDisplay implements IDisplayable, IHidable {

    private TextDisplay entity = null;
    private boolean seeTrough = false;
    private Color color = Color.fromARGB(0, 0, 0, 0);
    private Display.Billboard billboard = Display.Billboard.FIXED;

    //transform after parent applied values
    private Transformation parentTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));

    public TextDisplay spawnDisplay() {
        if (entity != null) {
            return entity;
        }

        entity = location.getWorld().spawn(location, TextDisplay.class);
        entity.setBillboard(billboard);
        entity.setBackgroundColor(color);
        entity.setSeeThrough(seeTrough);
        entity.setTransformation(getUpdatedGlobalTransformation());

        return entity;
    }

    public void killDisplay() {
        entity.remove();
    }


    @Override
    public void moveRelative(Vector3f movement, int time) {

    }

    @Override
    public void moveAbsolute(Vector3f position, int time) {

    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {

    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {

    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {

    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {

    }

    @Override
    public void scaleAbsolute(Vector3f scale, int time) {

    }

    @Override
    public void scaleRelative(Vector3f scale, int time) {

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

    //continue implementing the parent child propogation.
    // when doing in world entity locations, do make them always follow the parent, event if the entity location changes.
    // so when the location changes, the translation should accommodate for that (doesn't have to be beautiful)

    
    @Override
    public Location getLocation() {
        return null;
    }

    @Override
    public IDisplayable setLocation(Location location) {
        return null;
    }

    @Override
    public Transformation getTransformation() {
        return null;
    }

    @Override
    public IDisplayable setTransformation(Transformation transformation) {
        return null;
    }

    @Override
    public IDisplayable clone() {
        return null;
    }





}
