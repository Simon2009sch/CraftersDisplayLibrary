package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.bukkit.Location;

public interface ILocatableDisplay {

    /**
     * Sets the location of the object to inputted one
     * @param location
     * @return
     */
    ILocatableDisplay setLocation(Location location);

    /**
     * Adds the inputted location onto the existing location without changing the world
     * @param location
     * @return
     */
    ILocatableDisplay addLocation(Location location);

    /**
     * Returns the current location
     * @return
     */
    Location getLocation();

}
