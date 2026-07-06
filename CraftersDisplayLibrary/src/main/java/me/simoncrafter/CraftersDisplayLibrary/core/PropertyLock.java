package me.simoncrafter.CraftersDisplayLibrary.core;

public class PropertyLock implements Cloneable {
    
    private boolean leftXRot = false;
    private boolean leftYRot = false;
    private boolean leftZRot = false;
    private boolean rightXRot = false;
    private boolean rightYRot = false;
    private boolean rightZRot = false;

    private boolean xScale = false;
    private boolean yScale = false;
    private boolean zScale = false;

    private boolean xTranslation = false;
    private boolean yTranslation = false;
    private boolean zTranslation = false;

    public PropertyLock(boolean leftXRot, boolean leftYRot, boolean leftZRot, boolean rightXRot, boolean rightYRot, boolean rightZRot, boolean xScale, boolean yScale, boolean zScale, boolean xTranslation, boolean yTranslation, boolean zTranslation) {
        this.leftXRot = leftXRot;
        this.leftYRot = leftYRot;
        this.leftZRot = leftZRot;
        this.rightXRot = rightXRot;
        this.rightYRot = rightYRot;
        this.rightZRot = rightZRot;
        this.xScale = xScale;
        this.yScale = yScale;
        this.zScale = zScale;
        this.xTranslation = xTranslation;
        this.yTranslation = yTranslation;
        this.zTranslation = zTranslation;
    }


    @Override
    protected PropertyLock clone() throws CloneNotSupportedException {
        super.clone();
        return new PropertyLock(leftXRot, leftYRot, leftZRot, rightXRot, rightYRot, rightZRot,  xScale, yScale, zScale, xTranslation, yTranslation, zTranslation);
    }
    
    public void leftXRot(boolean leftXRot) {
        this.leftXRot = leftXRot;
    }
    public void leftYRot(boolean leftYRot) {
        this.leftYRot = leftYRot;
    }
    public void leftZRot(boolean leftZRot) {
        this.leftZRot = leftZRot;
    }
        
    public void rightXRot(boolean rightXRot) {
        this.rightXRot = rightXRot;
    }
    public void rightYRot(boolean rightYRot) {
        this.rightYRot = rightYRot;
    }
    public  void rightZRot(boolean rightZRot) {
        this.rightZRot = rightZRot;
    }
    public void xScale(boolean xScale) {
        this.xScale = xScale;
    }
    public void yScale(boolean yScale) {
        this.yScale = yScale;
    }
    public void zScale(boolean zScale) {
        this.zScale = zScale;
    }
    
    public void xTranslation(boolean xTranslation) {
        this.xTranslation = xTranslation;
    }
    public void yTranslation(boolean yTranslation) {
        this.yTranslation = yTranslation;
    }
    public void zTranslation(boolean zTranslation) {
        this.zTranslation = zTranslation;
    }

    public boolean leftXRot() {
        return leftXRot;
    }

    public boolean leftYRot() {
        return leftYRot;
    }

    public boolean leftZRot() {
        return leftZRot;
    }

    public boolean rightXRot() {
        return rightXRot;
    }

    public boolean rightYRot() {
        return rightYRot;
    }

    public boolean rightZRot() {
        return rightZRot;
    }

    public boolean xScale() {
        return xScale;
    }

    public boolean yScale() {
        return yScale;
    }

    public boolean zScale() {
        return zScale;
    }

    public boolean xTranslation() {
        return xTranslation;
    }

    public boolean yTranslation() {
        return yTranslation;
    }

    public boolean zTranslation() {
        return zTranslation;
    }
}
