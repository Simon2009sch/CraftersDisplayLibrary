package me.simoncrafter.CraftersDisplayLibrary.core;

import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IHidable;
import org.bukkit.Location;
import org.bukkit.entity.Display;
import org.bukkit.util.Transformation;

import java.util.List;

/**
 * Shared base for the four "leaf" panel-family displays ({@code ColorDisplay}, {@code TextDisplay},
 * {@code BlockDisplayObject}, {@code ItemDisplayObject}) that are each backed directly by a single
 * live Bukkit {@link Display} entity of type {@code E}.
 * <p>
 * All entity-agnostic boilerplate (the {@code entity}/{@code hiddenByDefault} fields, every
 * transform-mutator override that resyncs the live entity via {@link #updateEntity(int)},
 * {@link #setLocation(Location)}, {@link #rebaseEntity(Location)}, and the {@link IHidable}
 * delegation) lives in {@link AbstractEntityBackedPositionObject} and is inherited as-is; this class
 * only adds what is specific to {@link Display} entities:
 * <ul>
 *     <li>{@link #updateEntity(int)} is overridden to push {@link #resolveEntityTransform()} onto
 *     the entity via {@code Display.setTransformation}/{@code setInterpolationDuration}, instead of
 *     the generic {@link AbstractEntityBackedPositionObject}'s {@code LivingEntity}-scale-attribute
 *     sync (a {@link Display} is not a {@code LivingEntity}, so that generic logic would be a no-op
 *     here regardless).</li>
 *     <li>{@link #setBillboard(Display.Billboard)}/{@link #getBillboard()}, which have no equivalent
 *     on the generic entity base since billboard mode is a {@link Display}-only concept.</li>
 * </ul>
 * Subclasses supply {@link #resolveEntityTransform()} to compute the actual transform to push (plain
 * {@link #getFinalTransform() final transform}, a {@code scaleToBlock(...)}-corrected version, or a
 * {@code centerOrigin(...)}-corrected version, depending on how their entity type interprets
 * scale/translation), and may override {@link #afterUpdateEntity()} to run extra resync logic after
 * every transform push (e.g. {@code ColorDisplay} resyncing its see-through flag).
 * <p>
 * {@link #setLocation(Location)} (inherited) always null-checks the backing entity before teleporting
 * it - previously {@code ColorDisplay} did not, which was a latent NPE risk; that bug was fixed as a
 * side effect of the original deduplication into this class. {@link #setBillboard(Display.Billboard)}
 * applies immediately to the live entity if spawned, for all four subclasses - previously only
 * {@code TextDisplay} did this, while the other three only applied the billboard mode on next spawn;
 * this is an intentional, approved behaviour change made uniform here.
 * <p>
 * <b>Known asymmetry - intentionally not addressed here:</b> {@code moveEntityStatic(Location)} is
 * deliberately NOT part of this base class. {@code ColorDisplay}, {@code BlockDisplayObject} and
 * {@code ItemDisplayObject} each override it identically (teleport the entity directly, bypassing the
 * transform/animation system) and keep that small duplication; {@code TextDisplay} does not override
 * it at all, so moving a {@code TextDisplay} via {@code moveEntityStatic} does not currently teleport
 * its live entity. This is a pre-existing quirk that the restructuring intentionally leaves in place -
 * do not "fix" it by pulling {@code moveEntityStatic} into this base class.
 * <p>
 * {@link #rebaseEntity(Location)}, unlike {@code moveEntityStatic}, teleports the backing entity's
 * raw location <i>without</i> moving the visible display - it compensates the local transform
 * translation to cancel out the jump, and is implemented once in {@link AbstractEntityBackedPositionObject}
 * for all four subclasses.
 *
 * @param <E> the concrete Bukkit {@link Display} subtype backing this display
 */
public abstract class AbstractEntityBackedDisplay<E extends Display> extends AbstractEntityBackedPositionObject<E> {

    /** The billboard mode; applied immediately to the live entity by {@link #setBillboard(Display.Billboard)}. */
    protected Display.Billboard billboard = Display.Billboard.FIXED;

    protected AbstractEntityBackedDisplay(List<IDisplayable> children, Transformation localTransform, Location location) {
        super(children, localTransform, location);
    }

    /**
     * Pushes {@link #resolveEntityTransform()} onto the live entity, with client-side interpolation
     * over {@code time} ticks, then invokes {@link #afterUpdateEntity()}. A no-op if the entity hasn't
     * been spawned yet or is no longer valid.
     */
    @Override
    protected void updateEntity(int time) {
        if (entity == null || !entity.isValid()) return;
        entity.setTransformation(resolveEntityTransform());
        entity.setInterpolationDelay(0);
        entity.setInterpolationDuration(time);
        afterUpdateEntity();
    }

    /** Sets the billboard mode; applies immediately to the live entity if already spawned and valid. */
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
}
