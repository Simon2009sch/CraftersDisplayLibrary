package me.simoncrafter.CraftersDisplayLibrary.entity;

import io.papermc.paper.event.player.PrePlayerAttackEntityEvent;
import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.animation.GlobalAnimationTickHandler;
import me.simoncrafter.CraftersDisplayLibrary.animation.spi.CustomTypeAnimationInterpolationFunction;
import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedPositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.PropertyLock;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Interaction;
import org.bukkit.entity.Player;
import org.bukkit.entity.Shulker;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityRemoveEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.loot.LootTables;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.util.Transformation;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;


public class InteractionBox extends AbstractEntityBackedPositionObject<Interaction> implements Listener {

    private final Plugin plugin;

    private static final double MOUNT_HEIGHT_OFFSET = 0.5;
    private float width = 1;
    private float height = 1;
    private boolean responsive = true;

    private BukkitTask periodicUnregisterCheck = null;

    private Consumer<PlayerInteractAtEntityEvent> onRightClick = null;
    private Consumer<PrePlayerAttackEntityEvent> onLeftClick = null;

    private InteractionBox(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Plugin plugin) {
        super(List.of(), new Transformation(translation, leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc);
        this.plugin = plugin;
    }

    public static InteractionBox create(Location loc, Vector3f scale, Vector3f translation, Plugin plugin) {
        return create(loc, scale, translation, new Quaternionf(), plugin);
    }

    public static InteractionBox create(Location loc, Vector3f scale, Vector3f translation,
                                        Quaternionf leftRotation, Plugin plugin) {
        return new InteractionBox(loc, scale, translation, leftRotation, plugin);
    }





    public Interaction spawnEntity() {
        if (entity != null) {
            return entity;
        }

        Vector3f translation = resolveEntityTransform().getTranslation();
        Location renderLocation = getLocation().add(translation.x, translation.y, translation.z);
        Location markerLocation = renderLocation.clone().subtract(0, MOUNT_HEIGHT_OFFSET, 0);


        entity = markerLocation.getWorld().spawn(markerLocation, Interaction.class);
        entity.setGravity(false);
        entity.setInvisible(false);
        entity.setVisibleByDefault(!hiddenByDefault);
        entity.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);
        entity.setResponsive(responsive);
        adjustWidthAndHeightToTransform(); // sets the size of the interaction


        updateEntity(0);

        Bukkit.getPluginManager().registerEvents(this, plugin);
        startPeriodicUnregisterCheck();

        return entity;
    }

    private void startPeriodicUnregisterCheck() {
        if (periodicUnregisterCheck != null && !periodicUnregisterCheck.isCancelled()) {
            return;
        }
        InteractionBox box = this;
        periodicUnregisterCheck = new BukkitRunnable(){
            @Override
            public void run() {
                if (entity == null || !entity.isValid() || entity.isDead()) {
                    HandlerList.unregisterAll(box);
                    cancel();
                }
            }
        }.runTaskTimer(plugin, 20, 20);
    }

    @Override
    protected Transformation resolveEntityTransform() {
        return getFinalTransform();
    }


    @Override
    protected void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;

        Transformation trans = resolveEntityTransform();
        Vector3f scale = trans.getScale();
        Vector3f translation = trans.getTranslation();
        Location renderLocation = getLocation().add(translation.x, translation.y, translation.z);
        Location markerLocation = renderLocation.clone().subtract(0, MOUNT_HEIGHT_OFFSET*scale.y, 0);


        if (time > 1) {
            animateEntityPositions(markerLocation, time);
        } else {
            entity.teleport(markerLocation);
            applyEntityRotation();
            afterUpdateEntity();
        }
    }

    private void animateEntityPositions(Location markerTarget, int duration) {
        Location markerStart = entity.getLocation().clone();

        GlobalAnimationTickHandler.registerNewGeneralAnimation(this, new CustomTypeAnimationInterpolationFunction<Location, PositionObject>(duration, markerStart, markerTarget, this){
            @Override
            public void nextTick(int duration, int tick, Location startLocation, Location endLocation, PositionObject obj) {
                float progress = Math.min((float) tick / duration, 1.0f);

                Vector3f startVec = startLocation.toVector().toVector3f();
                Vector3f endVec = endLocation.toVector().toVector3f();
                Vector3f interpolatedVec = new Vector3f(startVec).lerp(endVec, progress);
                Location interpolatedPos = new Location(startLocation.getWorld(), interpolatedVec.x, interpolatedVec.y, interpolatedVec.z);

                entity.teleport(interpolatedPos);
                applyEntityRotation();
                afterUpdateEntity();
            }
        });
    }


    /**
     * Multiplies the hitbox width and height according to the object scale<br>
     * Beware that hitboxes can only be square in the base area.
     * You can disable this with {@link #setPropertyLock(PropertyLock)} that locks scale on this object. Create with {@link PropertyLock#createScaleLock()}
     */
    @Override
    protected void afterUpdateEntity() {
        adjustWidthAndHeightToTransform();
    }

    private void adjustWidthAndHeightToTransform() {
        Vector3f scale = getFinalTransform().getScale();
        entity.setInteractionWidth((Math.abs(Math.max(scale.x, scale.z)) * width)+0.01f);
        entity.setInteractionHeight((height * Math.abs(scale.y))+0.01f);
    }


    public float getWidth() {
        return width;
    }

    public void setWidth(float width, int time) {
        this.width = width;
        updateEntity(time);
    }

    public float getHeight() {
        return height;
    }

    public void setHeight(float height, int time) {
        this.height = height;
        updateEntity(time);
    }

    public boolean isResponsive() {
        return responsive;
    }

    public void setResponsive(boolean responsive) {
        this.responsive = responsive;
        if (entity != null && entity.isValid()) {
            entity.setResponsive(responsive);
        }
    }

    public void setOnRightClick(Consumer<PlayerInteractAtEntityEvent> onRightClick) {
        this.onRightClick = onRightClick;
    }

    public void setOnLeftClick(Consumer<PrePlayerAttackEntityEvent> onLeftClick) {
        this.onLeftClick = onLeftClick;
    }

    /**
     * {@inheritDoc}
     * <p>
     * Produces a new, unspawned {@code ShulkerBasedCollisionBox} with the same transform, location
     * and children (shallow copy) as this one. The copy's backing entities are always {@code null} -
     * call {@link #spawnEntity()} on it separately to bring it to life.
     */
    @Override
    public IDisplayable clone() {
        Transformation local = getLocalTransform();
        InteractionBox copy = new InteractionBox(
                getLocation(),
                new Vector3f(local.getScale()),
                new Vector3f(local.getTranslation()),
                new Quaternionf(local.getLeftRotation()),
                plugin);
        copy.setChildren(getChildren());
        copy.hiddenByDefault = hiddenByDefault;
        copy.width = width;
        copy.height = height;
        copy.responsive = responsive;
        copy.onRightClick = onRightClick;
        copy.onLeftClick = onLeftClick;
        return copy;
    }


    @EventHandler
    public void onPlayerInteractAtEntity(PlayerInteractAtEntityEvent event) {
        if (event.getRightClicked() == entity && onRightClick != null && event.getHand() == EquipmentSlot.HAND) {
            onRightClick.accept(event);
        }
    }

    @EventHandler
    public void onPrePlayerAttackEntity(PrePlayerAttackEntityEvent event) {
        if (event.getAttacked() == entity && onLeftClick != null) {
            onLeftClick.accept(event);
            event.setCancelled(true);
        }
    }

    @Override
    public void remove() {
        super.remove();
        if (periodicUnregisterCheck != null) {
            periodicUnregisterCheck.cancel();
            periodicUnregisterCheck = null;
        }
        HandlerList.unregisterAll(this);
    }

}
