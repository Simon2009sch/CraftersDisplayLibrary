package me.simoncrafter.CraftersDisplayLibrary.def;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;

public class Displayable {

    private World world = Bukkit.getWorld("world");
    private Vector location = new Vector(0, 0, 0);
    private Vector scale = new Vector(1, 1, 1);
    private Quaternionf rightRotation = new Quaternionf(0, 0, 0, 1);
    private Quaternionf leftRotation = new Quaternionf(0, 0, 0, 1);

    private void a() {
        TextDisplay display = world.spawn(location.toLocation(world), TextDisplay.class);

        Transformation trans = display.getTransformation();

    }



}