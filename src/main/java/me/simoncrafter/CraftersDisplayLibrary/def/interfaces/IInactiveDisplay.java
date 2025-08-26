package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.Contract;

public interface IInactiveDisplay {

    void display();
    void remove();
    Location getLocation();
    @Contract(value = "_ -> new")
    IInactiveDisplay setLocation(Location location);
    Transformation getTransformation();

    boolean isHiddenByDefault();

    @Contract(value = "_ -> new")
    IInactiveDisplay setTransformation(Transformation transformation);

    @Contract(value = "_ -> new")
    IInactiveDisplay hideByDefault(boolean hide);

    @Contract(value = "_ -> new")
    IInactiveDisplay showForPlayer(Player player);

    @Contract(value = "_ -> new")
    IInactiveDisplay hideForPlayer(Player player);

}
