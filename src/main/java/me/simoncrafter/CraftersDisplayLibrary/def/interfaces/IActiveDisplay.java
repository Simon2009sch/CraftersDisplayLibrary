package me.simoncrafter.CraftersDisplayLibrary.def.interfaces;

import org.joml.Vector3f;

public interface IActiveDisplay {

    void moveRelative(Vector3f movement, int time);
    void moveAbsolute(Vector3f position, int time);
    void rotateAbsolute(float angle, int time);
    void rotateRelative(float angle, int time);
    void scaleAbsolute(float scale, int time);
    void scaleRelative(float scale, int time);
}
