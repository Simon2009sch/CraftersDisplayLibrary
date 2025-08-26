package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public interface IDisplay {

    /**
     * Sets the objects scale to the inputted vector
     * @return Returns the modified object
     */
    IScalableDisplay setScale(Vector3f scale);

    /**
     * Scales the object by the inputted vector.<br>
     * (objectScale + scaleVector = newObjectScale
     * @param scaleVector The vector that is added to the existing one
     * @return Returns the modified object
     */
    IScalableDisplay scale(Vector3f scaleVector);

    /**
     * Returns the objects scale
     * @return The scale vector
     */
    Vector3f getScale();

    IRotatableDisplay setRotation(Quaternionf rotation);

    /**
     * Rotates the object in its local reference frame<br>
     * (currentRotation * inputtedRotation = newRotation)
     * @param rotation
     * @return
     */
    IRotatableDisplay rotateLocal(Quaternionf rotation);

    /**
     * Rotates the object in its local reference frame<br>
     * (inputtedRotationc * urrentRotation = newRotation)
     * @param rotation
     * @return
     */
    IRotatableDisplay rotateGlobal(Quaternionf rotation);

    Quaternionf getRotation();

}
