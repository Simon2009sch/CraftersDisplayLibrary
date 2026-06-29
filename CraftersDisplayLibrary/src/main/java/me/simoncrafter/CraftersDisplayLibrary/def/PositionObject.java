package me.simoncrafter.CraftersDisplayLibrary.def;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

public class PositionObject implements IDisplayable {
    private static final Vector3f BLOCK_TEXT_DIFFERENCE = new Vector3f(0f, 0f, 0);


    private List<IDisplayable> children = new ArrayList<>();
    private Transformation localTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));
    private Location location;

    private BiFunction<Transformation, Transformation, Transformation> parentAplerFunction = (parent, local) -> {
        // Apply transformation in correct order: scale → rotate → translate
        Vector3f scaledTranslation = new Vector3f(local.getTranslation()).mul(parent.getScale());
        Vector3f rotatedTranslation = new Vector3f(scaledTranslation)
                .rotate(parent.getLeftRotation())
                .rotate(parent.getRightRotation());
        Vector3f finalTranslation = rotatedTranslation.add(parent.getTranslation());

        return new Transformation(
                finalTranslation,
                parent.getLeftRotation().mul(local.getLeftRotation(), new Quaternionf()).normalize(),
                local.getScale().mul(parent.getScale(), new Vector3f()),
                parent.getRightRotation().mul(local.getRightRotation(), new Quaternionf()).normalize()
        );
    };

    private Transformation parentTransform = new Transformation(new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new Vector3f(1, 1, 1), new Quaternionf(0, 0, 0, 1));

    // Animation tracking
    private BukkitTask animationTask = null;
    private Transformation animationStartTransform = null;
    private Transformation animationEndTransform = null;
    private int animationDurationTicks = 0;
    private int animationCurrentTick = 0;

    public PositionObject(List<IDisplayable> children, Transformation localTransform, Location location) {
        this.children = new ArrayList<>(children);
        this.localTransform = localTransform;
        this.location = location;
    }

    @Override
    public Location getLocation() {
        return location.clone();
    }

    @Override
    public void setLocation(Location loc) {
        Vector oldLoc = location.toVector();
        Vector newLoc = loc.toVector();

        Vector diff = newLoc.subtract(oldLoc);

        runForEveryChild(child -> child.setLocation(child.getLocation().add(diff)));

        this.location = loc;
    }

    @Override
    public void setLocationNoUpdate(Location loc) {
        this.location = loc;
    }

    @Override
    public Transformation getLocalTransform() {
        return localTransform;
    }

    @Override
    public void setLocalTransform(Transformation transformation) {
        setLocalTransform(transformation, 0);
    }
    @Override
    public void setLocalTransform(Transformation transformation, int time) {
        localTransform = transformation;
        updateChildren(time);
    }


    public void moveEntityStatic(Location location) {
        Vector oldLoc = this.location.toVector();
        Vector newLoc = location.toVector();

        Vector diff = newLoc.subtract(oldLoc);

        runForEveryChild((child) -> child.moveEntityStatic(diff.toLocation(location.getWorld())));
        this.location = location;
    }

    @Override
    public void setParentTransform(Transformation transformation, int time) {
        parentTransform = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        updateChildren(time);
    }

    @Override
    public Transformation getParentTransform() {
        return new Transformation(parentTransform.getTranslation(), parentTransform.getLeftRotation(), parentTransform.getScale(), parentTransform.getRightRotation());
    }

    @Override
    public void addChild(IDisplayable child) {
        children.add(child);
        updateChildren(0);
    }

    @Override
    public void removeChild(IDisplayable child) {
        children.remove(child);
    }

    @Override
    public void setChildren(List<IDisplayable> children) {
        this.children = new ArrayList<>(children);
        updateChildren(0);
    }

    protected void forEveryChild(Consumer<IDisplayable> consumer) {
        for (IDisplayable display : children) {
            consumer.accept(display);
        }
    }

    @Override
    public List<IDisplayable> getChildren() {
        return children;
    }

    @Override
    public void moveRelative(Vector3f movement, int time) {
        try {
            localTransform = new Transformation(localTransform.getTranslation()
                    .add(movement), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        } catch (NullPointerException ignored) {}
        updateChildren(time);
    }

    @Override
    public void moveAbsolute(Vector3f position, int time) {
        localTransform = new Transformation(position, localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        updateChildren(time);
    }

    @Override
    public void moveRelativeToWorld(Vector3f position, int time) {
        Vector3f currentPos = location.toVector().toVector3f().add(getFinalTransform().getTranslation());
        Vector3f diff = position.sub(currentPos);
        Bukkit.broadcast(Component.text(diff.toString()));
        moveRelative(diff, time);
    }

    @Override
    public void LRotateAbsolute(Quaternionf rotation, int time) {
        LRotateAbsolute(rotation, time, true);
    }

    public void LRotateAbsolute(Quaternionf rotation, int time, boolean useAnimation) {
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), rotation, localTransform.getScale(), localTransform.getRightRotation());
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), rotation, localTransform.getScale(), localTransform.getRightRotation());
            updateChildren(time);
        }
    }

    @Override
    public void LRotateRelative(Quaternionf rotation, int time) {
        LRotateRelative(rotation, time, true);
    }

    public void LRotateRelative(Quaternionf rotation, int time, boolean useAnimation) {
        Quaternionf newRotation = new Quaternionf(localTransform.getLeftRotation()).mul(rotation);
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), newRotation, localTransform.getScale(), localTransform.getRightRotation());
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), newRotation, localTransform.getScale(), localTransform.getRightRotation());
            updateChildren(time);
        }
    }

    @Override
    public void RRotateAbsolute(Quaternionf rotation, int time) {
        RRotateAbsolute(rotation, time, true);
    }

    public void RRotateAbsolute(Quaternionf rotation, int time, boolean useAnimation) {
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), rotation);
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), rotation);
            updateChildren(time);
        }
    }

    @Override
    public void RRotateRelative(Quaternionf rotation, int time) {
        RRotateRelative(rotation, time, true);
    }

    public void RRotateRelative(Quaternionf rotation, int time, boolean useAnimation) {
        Quaternionf newRotation = new Quaternionf(localTransform.getRightRotation()).mul(rotation);
        if (useAnimation) {
            Transformation endTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), newRotation);
            startAnimation(endTransform, time);
        } else {
            localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), newRotation);
            updateChildren(time);
        }
    }

    @Override
    public void scaleAbsolute(Vector3f scale, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), scale, localTransform.getRightRotation());
        updateChildren(time);
    }

    @Override
    public void scaleRelative(Vector3f scale, int time) {
        localTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale().add(scale), localTransform.getRightRotation());
        updateChildren(time);
    }


    @Override
    public Transformation getTransformation() {
        return new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
    }

    @Override
    public IDisplayable setTransformation(Transformation transformation) {
        localTransform = new Transformation(transformation.getTranslation(), transformation.getLeftRotation(), transformation.getScale(), transformation.getRightRotation());
        return this;
    }

    protected void runForEveryChild(Consumer<IDisplayable> action) {
        for (IDisplayable child : children) {
            action.accept(child);
        }
    }

    protected void updateChildren(int time) {
        runForEveryChild(c -> {
            c.setParentTransform(getFinalTransform(), time);
        });
    }

    @Override
    public IDisplayable clone() {
        return new PositionObject(
                new ArrayList<>(children),
                new Transformation(
                        localTransform.getTranslation(),
                        localTransform.getLeftRotation(),
                        localTransform.getScale(),
                        localTransform.getRightRotation()),
                location
        );
    }

    @Override
    public void setParentApplierFunction(BiFunction<Transformation, Transformation, Transformation> func) {
        parentAplerFunction = func;
    }

    @Override
    public BiFunction<Transformation, Transformation, Transformation> getParentApplierFunction() {
        return parentAplerFunction;
    }

    @Override
    public void remove() {
        for (IDisplayable obj : getChildren()) {
            obj.remove();
        }
    }

    protected Transformation getFinalTransform() {
        return parentAplerFunction.apply(parentTransform, localTransform);
    }

    private void startAnimation(Transformation endTransform, int durationTicks) {
        cancelAnimation();
        animationStartTransform = new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation());
        animationEndTransform = endTransform;
        animationDurationTicks = durationTicks;
        animationCurrentTick = 0;

        if (durationTicks <= 0) {
            setLocalTransform(endTransform, 0);
            return;
        }

        animationTask = Bukkit.getScheduler().runTaskTimer(PluginHolder.plugin, this::updateAnimation, 0L, 1L);
    }

    private void updateAnimation() {
        if (animationStartTransform == null || animationEndTransform == null) {
            cancelAnimation();
            return;
        }

        float progress = (float) animationCurrentTick / animationDurationTicks;
        progress = Math.min(progress, 1.0f);

        Vector3f interpolatedPos = new Vector3f(animationStartTransform.getTranslation())
                .lerp(animationEndTransform.getTranslation(), progress);

        Quaternionf interpolatedLeftRot = new Quaternionf(animationStartTransform.getLeftRotation())
                .slerp(animationEndTransform.getLeftRotation(), progress);

        Quaternionf interpolatedRightRot = new Quaternionf(animationStartTransform.getRightRotation())
                .slerp(animationEndTransform.getRightRotation(), progress);

        Vector3f interpolatedScale = new Vector3f(animationStartTransform.getScale())
                .lerp(animationEndTransform.getScale(), progress);

        localTransform = new Transformation(interpolatedPos, interpolatedLeftRot, interpolatedScale, interpolatedRightRot);
        updateChildren(1);

        animationCurrentTick++;
        if (animationCurrentTick > animationDurationTicks) {
            cancelAnimation();
        }
    }

    private void cancelAnimation() {
        if (animationTask != null) {
            animationTask.cancel();
            animationTask = null;
        }
        animationStartTransform = null;
        animationEndTransform = null;
    }

    protected Transformation scaleToBlock(Transformation transformation) {
        return new Transformation(transformation.getTranslation().add(BLOCK_TEXT_DIFFERENCE), transformation.getLeftRotation(), transformation.getScale().mul(PluginHolder.BLOCK_SCALE), transformation.getRightRotation());
    }

}
