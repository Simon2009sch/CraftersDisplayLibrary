package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.joml.Vector3f;

public interface ITranslatableDisplay {

    /**
     * Sets the objects translation vector to the inputted one
     * @param translation
     * @return
     */
    ITranslatableDisplay setTranslation(Vector3f translation);

    /**
     * Adds the inputted translation vector to the existing one
     * @param translation
     * @return
     */
    ITranslatableDisplay translate(Vector3f translation);
    Vector3f getTranslation();


}
