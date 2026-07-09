package me.simoncrafter.CraftersDisplayLibrary.display.panel;

import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import net.kyori.adventure.text.Component;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A renderable text display using Bukkit's {@link org.bukkit.entity.TextDisplay}.
 * <p>
 * Instances are created with {@link #create}; the constructor is private. Call {@link #spawnDisplay()}
 * once the object's transform/location is set up to actually spawn the backing entity - it is
 * idempotent and simply returns the existing entity on subsequent calls. Transform-mutating methods
 * (move/rotate/scale, animated or not) are inherited from {@link AbstractEntityBackedDisplay}, which
 * pushes every change onto the live entity via {@link #resolveEntityTransform()}.
 * <p>
 * Unlike {@link ColorDisplay} (which fakes a colour panel out of an empty {@code TextDisplay} and
 * needs the {@code scaleToBlock} correction to render it at one full block's size), this class
 * represents a normal text display - a scale of {@code (1,1,1)} is Minecraft's default text size,
 * not a block, matching vanilla {@code TextDisplay} behaviour.
 */
public class TextDisplay extends AbstractEntityBackedDisplay<org.bukkit.entity.TextDisplay> implements IColorableDisplay {

	private Component textContent = Component.text("");
	private Color textColor = Color.WHITE;
	private boolean hasBackground = false;
	private Color backgroundColor = Color.fromARGB(0, 0, 0, 0);
	private int lineWidth = 200;
	private org.bukkit.entity.TextDisplay.TextAlignment alignment = org.bukkit.entity.TextDisplay.TextAlignment.LEFT;

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
		entity.setAlignment(alignment);

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

	/**
	 * {@inheritDoc} No block-scale correction is applied - a normal text display's own scale of
	 * {@code (1,1,1)} already matches Minecraft's default text size.
	 */
	@Override
	protected Transformation resolveEntityTransform() {
		return getFinalTransform();
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

	/**
	 * Sets this display's tint, applied as the entity's background color (Minecraft text displays
	 * have no separate "text color" API distinct from the {@link Component} styling of the text
	 * itself) - applies immediately to the live entity if spawned.
	 */
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

	public org.bukkit.entity.TextDisplay.TextAlignment getAlignment() {
		return alignment;
	}

	public void setAlignment(org.bukkit.entity.TextDisplay.TextAlignment alignment) {
		this.alignment = alignment;
		if (entity != null) {
			entity.setAlignment(alignment);
		}
	}

	/**
	 * {@inheritDoc}
	 * <p>
	 * Produces a new, unspawned {@code TextDisplay} with the same transform, location, children
	 * (shallow copy) and text/colour state as this one. The copy's backing entity is always
	 * {@code null} - call {@link #spawnDisplay()} on it separately to bring it to life.
	 */
	@Override
	public IDisplayable clone() {
		Transformation local = getLocalTransform();
		TextDisplay copy = new TextDisplay(
				getLocation(),
				new Vector3f(local.getScale()),
				new Vector3f(local.getTranslation()),
				new Quaternionf(local.getLeftRotation()),
				new Quaternionf(local.getRightRotation()));
		copy.setChildren(getChildren());
		copy.textContent = textContent;
		copy.textColor = textColor;
		copy.hasBackground = hasBackground;
		copy.backgroundColor = backgroundColor;
		copy.lineWidth = lineWidth;
		copy.billboard = billboard;
		copy.hiddenByDefault = hiddenByDefault;
		copy.alignment = alignment;
		return copy;
	}
}
