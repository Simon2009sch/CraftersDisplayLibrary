package me.simoncrafter.CraftersDisplayLibrary.def.inactive;

import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IInactiveDisplay;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.TextDisplay;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

public class InactiveColorDisplay implements IInactiveDisplay {

    private final Transformation transformation = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));
    private final Location location = new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
    private final Color color = Color.fromARGB(100, 100, 100, 100);


    public void a() {
        TextDisplay e;
        e.setBackgroundColor(Color.);

    }



}
