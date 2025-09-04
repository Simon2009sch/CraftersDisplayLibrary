package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.Contract;

public interface IHidable {

    boolean isHiddenByDefault();

    @Contract(value = "_ -> new")
    IDisplayable hideByDefault(boolean hide);

    @Contract(value = "_ -> new")
    IDisplayable showForPlayer(Player player);

    @Contract(value = "_ -> new")
    IDisplayable hideForPlayer(Player player);

}
