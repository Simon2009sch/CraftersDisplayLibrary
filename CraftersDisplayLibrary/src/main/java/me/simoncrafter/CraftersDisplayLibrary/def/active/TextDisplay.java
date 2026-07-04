package me.simoncrafter.CraftersDisplayLibrary.def.active;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.Tags;
import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A renderable text display using Bukkit's {@link org.bukkit.entity.TextDisplay}.
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call {@link #spawnDisplay()}
 * once the object's transform/location is set up to actually spawn the backing entity - it is
 * idempotent and simply returns the existing entity on subsequent calls. Every inherited
 * transform-mutating method from {@link PositionObject} (move/rotate/scale, animated or not) is
 * overridden here to also push the new {@link Transformation} onto the live entity via
 * {@link #updateEntity(int)}.
 * <p>
 * Unlike {@link ColorDisplay} (which fakes a colour panel out of an empty {@code TextDisplay} and
 * needs the {@code scaleToBlock} correction to render it at one full block's size), this class
 * represents a normal text display - a scale of {@code (1,1,1)} is Minecraft's default text size,
 * not a block, matching vanilla {@code TextDisplay} behaviour.
 */
public class TextDisplay extends PositionObject implements IHidable, IColorableDisplay {

	private org.bukkit.entity.TextDisplay entity = null;
	private Component textContent = Component.text("");
	private Color textColor = Color.WHITE;
	private boolean hiddenByDefault = false;
	private Display.Billboard billboard = Display.Billboard.FIXED;
	private boolean hasBackground = false;
	private Color backgroundColor = Color.fromARGB(0, 0, 0, 0);
	private int lineWidth = 200;

	private TextDisplay(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation) {
		super(List.of(), new Transformation(translation, leftRotation, scale, rightRotation), loc);
	}

	/**
	 * Spawns the backing {@link org.bukkit.entity.TextDisplay} entity at this object's current location and transform.
	 * Safe to call multiple times - if the entity already exists, it is returned unchanged and no new
	 * entity is spawned. Tags the entity with {@link Tags#CDL_ENTITY} so it can be identified later
	 * (e.g. during chunk/world cleanup) as belonging to this library.
	 *
	 * @return the (possibly newly-spawned) backing entity
	 */
	public org.bukkit.entity.TextDisplay spawnDisplay() {
		if (entity != null) {
			return entity;
		}

		entity = getLocation().getWorld().spawn(getLocation(), org.bukkit.entity.TextDisplay.class);
		entity.setBillboard(billboard);
		entity.text(textContent);
		entity.setTextOpacity((byte) 255);
		entity.setLineWidth(lineWidth);
		if (hasBackground) {
			entity.setBackgroundColor(backgroundColor);
		}
		entity.setVisibleByDefault(!hiddenByDefault);
		entity.setTransformation(getFinalTransform());
		entity.getPersistentDataContainer().set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);

		return entity;
	}

	/**
	 * Removes the current backing entity (if any) and spawns a fresh one in its place, re-applying
	 * the current text, transform and visibility state.
	 *
	 * @return the newly-spawned entity
	 */
	public org.bukkit.entity.TextDisplay respawnEntity() {
		if (entity == null) {
			return spawnDisplay();
		}
		entity.remove();
		entity = null;
		return spawnDisplay();
	}

	/** Creates a text display with default settings (fixed billboard, no background). */
	public static TextDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation) {
		return new TextDisplay(loc, scale, translation, leftRotation, rightRotation);
	}

	/** Creates a text display with default settings (fixed billboard, no background), no right rotation. */
	public static TextDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation) {
		return new TextDisplay(loc, scale, translation, leftRotation, new Quaternionf(0, 0, 0, 1));
	}

	/** The backing entity, or {@code null} if {@link #spawnDisplay()} has not been called yet. */
	public org.bukkit.entity.TextDisplay getEntity() {
		return entity;
	}

	/** {@inheritDoc} Also teleports the backing entity by the same delta. */
	@Override
	public void setLocation(Location loc) {
		super.setLocation(loc);

		if (entity != null) {
			Vector oldLocation = entity.getLocation().toVector();
			Vector newLocation = loc.toVector();
			Vector difference = newLocation.subtract(oldLocation);

			entity.teleport(entity.getLocation().clone().add(difference));
		}
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void setParentTransform(Transformation transformation, int time) {
		super.setParentTransform(transformation, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void setLocalTransform(Transformation transformation, int time) {
		super.setLocalTransform(transformation, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void moveRelative(Vector3f movement, int time) {
		super.moveRelative(movement, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void moveAbsolute(Vector3f position, int time) {
		super.moveAbsolute(position, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void LRotateAbsolute(Quaternionf rotation, int time) {
		super.LRotateAbsolute(rotation, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void LRotateRelative(Quaternionf rotation, int time) {
		super.LRotateRelative(rotation, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void RRotateAbsolute(Quaternionf rotation, int time) {
		super.RRotateAbsolute(rotation, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void RRotateRelative(Quaternionf rotation, int time) {
		super.RRotateRelative(rotation, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void scaleAbsolute(Vector3f scale, int time) {
		super.scaleAbsolute(scale, time);
		updateEntity(time);
	}

	/** {@inheritDoc} Also resyncs the backing entity's transform. */
	@Override
	public void scaleRelative(Vector3f scale, int time) {
		super.scaleRelative(scale, time);
		updateEntity(time);
	}

	/**
	 * Pushes this object's current {@link #getFinalTransform() final transform} onto the live
	 * entity, with client-side interpolation over {@code time} ticks. A no-op if the entity
	 * hasn't been spawned yet or is no longer valid.
	 */
	private void updateEntity(int time) {
		if (entity == null || !entity.isValid()) return;
		entity.setTransformation(getFinalTransform());
		entity.setInterpolationDelay(0);
		entity.setInterpolationDuration(time);
	}

	/** Sets the text content; applies immediately to the live entity if spawned. */
	public void setText(Component text) {
		this.textContent = text;
		if (entity != null && entity.isValid()) {
			entity.text(textContent);
		}
	}

	/** Sets the text content from a string; applies immediately to the live entity if spawned. */
	public void setText(String text) {
		setText(Component.text(text));
	}

	/** Gets the current text content. */
	public Component getText() {
		return textContent;
	}

	/** Sets the billboard mode; takes effect on the entity next time it is spawned. */
	public void setBillboard(Display.Billboard billboard) {
		this.billboard = billboard;
		if (entity != null && entity.isValid()) {
			entity.setBillboard(billboard);
		}
	}

	/** Gets the current billboard mode. */
	public Display.Billboard getBillboard() {
		return billboard;
	}

	/** Sets the text color; applies immediately to the live entity if spawned. */
	@Override
	public void setColor(Color color) {
		this.textColor = color;
		if (entity != null && entity.isValid()) {
			entity.setBackgroundColor(textColor);
		}
	}

	/** Gets the current text color. */
	public Color getColor() {
		return textColor;
	}

	/** Enables or disables the background; applies immediately to the live entity if spawned. */
	public void setHasBackground(boolean hasBackground) {
		this.hasBackground = hasBackground;
		if (entity != null && entity.isValid()) {
			if (hasBackground) {
				entity.setBackgroundColor(backgroundColor);
			}
		}
	}

	/** Sets the background color; applies immediately to the live entity if spawned. */
	public void setBackgroundColor(Color color) {
		this.backgroundColor = color;
		if (entity != null && entity.isValid() && hasBackground) {
			entity.setBackgroundColor(backgroundColor);
		}
	}

	/** Gets the current background color. */
	public Color getBackgroundColor() {
		return backgroundColor;
	}

	/** Sets the line width (max pixels per line); applies immediately to the live entity if spawned. */
	public void setLineWidth(int width) {
		this.lineWidth = width;
		if (entity != null && entity.isValid()) {
			entity.setLineWidth(lineWidth);
		}
	}

	/** Gets the current line width. */
	public int getLineWidth() {
		return lineWidth;
	}

	@Override
	public boolean isHiddenByDefault() {
		return hiddenByDefault;
	}

	/** {@inheritDoc} Applies immediately to the live entity if already spawned. */
	@Override
	public IDisplayable hideByDefault(boolean hide) {
		hiddenByDefault = hide;
		if (entity != null && entity.isValid()) {
			entity.setVisibleByDefault(!hide);
		}
		return this;
	}

	@Override
	public IDisplayable showForPlayer(Player player) {
		if (entity != null && entity.isValid()) {
			player.showEntity(PluginHolder.getPlugin(), entity);
		}
		return this;
	}

	@Override
	public IDisplayable hideForPlayer(Player player) {
		if (entity != null && entity.isValid()) {
			player.hideEntity(PluginHolder.getPlugin(), entity);
		}
		return this;
	}

	/** {@inheritDoc} Also removes the backing entity from the world, if spawned. */
	@Override
	public void remove() {
		super.remove();
		if (entity != null) entity.remove();
		entity = null;
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * <b>Not implemented</b> - always returns {@code null} regardless of this object's state.
	 */
	@Override
	public IDisplayable clone() {
		return null;
	}
}