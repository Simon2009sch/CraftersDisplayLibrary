package me.simoncrafter.CraftersDisplayLibrary.def.Animation;

import org.joml.Quaternionf;
import org.joml.Vector3f;

public class AnimationFrameOffset {

    private Vector3f translation;
    private Quaternionf rRotation;
    private Quaternionf lRotation;
    private Vector3f scale;

    public AnimationFrameOffset(Vector3f translation, Quaternionf rRotation, Quaternionf lRotation, Vector3f scale) {
        this.translation = translation;
        this.rRotation = rRotation;
        this.lRotation = lRotation;
        this.scale = scale;
    }

    public Vector3f getTranslation() {
        return translation;
    }

    public Quaternionf getrRotation() {
        return rRotation;
    }

    public Quaternionf getlRotation() {
        return lRotation;
    }

    public Vector3f getScale() {
        return scale;
    }
}
