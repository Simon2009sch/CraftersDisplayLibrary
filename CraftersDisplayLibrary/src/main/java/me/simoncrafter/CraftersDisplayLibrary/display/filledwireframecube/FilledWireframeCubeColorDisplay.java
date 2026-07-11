package me.simoncrafter.CraftersDisplayLibrary.display.filledwireframecube;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.display.line.LineColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.CubeEdge;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.CubeFace;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.ApiStatus;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;

/**
 * A cube with both solid, colourable faces ({@link CubeColorDisplay}) and a wireframe edge overlay
 * ({@link WireframeCubeColorDisplay}), kept permanently in sync: both are added as children of this
 * object, so every move / rotate / scale (animated or not) applied to this display propagates
 * identically to both the faces and the edges.
 * <p>
 * Faces and edges are independently colourable and independently see-through-able. Faces expose
 * the same controls as {@link CubeColorDisplay}; edges expose the same controls as
 * {@link WireframeCubeColorDisplay} (including edge thickness).
 */
public class FilledWireframeCubeColorDisplay extends PositionObject implements IColorableDisplay, ICuboidDisplay {

    private final CubeColorDisplay faceCube;
    private final WireframeCubeColorDisplay wireframeCube;

    private FilledWireframeCubeColorDisplay(Transformation localTransform, Location location, CubeColorInformation faceColors, WireframeCubeColorInformation edgeColors, boolean facesSeeThrough, boolean edgesSeeThrough, float edgeThickness) {
        super(List.of(), new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation()), location);

        // Both sub-cubes are spawned with an identity local transform: their placement comes
        // entirely from the parent-transform this object propagates to them as children.
        this.faceCube = CubeColorDisplay.create(location, new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), new Quaternionf(), new Quaternionf(), faceColors, facesSeeThrough);
        this.wireframeCube = WireframeCubeColorDisplay.create(location, new Vector3f(1.01f, 1.01f, 1.01f), new Vector3f(0, 0, 0), new Quaternionf(), new Quaternionf(), edgeColors, edgesSeeThrough, edgeThickness);
    }

    /** Used only by {@link #assemble} - takes already-built sub-cubes instead of constructing fresh ones. */
    private FilledWireframeCubeColorDisplay(Transformation localTransform, Location location, CubeColorDisplay faceCube, WireframeCubeColorDisplay wireframeCube) {
        super(List.of(), new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation()), location);
        this.faceCube = faceCube;
        this.wireframeCube = wireframeCube;
    }

    public static FilledWireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, CubeColorInformation faceColors, WireframeCubeColorInformation edgeColors, boolean facesSeeThrough, boolean edgesSeeThrough, float edgeThickness) {
        return new FilledWireframeCubeColorDisplay(new Transformation(translation, leftRotation, scale, rightRotation), loc, faceColors, edgeColors, facesSeeThrough, edgesSeeThrough, edgeThickness);
    }

    public static FilledWireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, CubeColorInformation faceColors, WireframeCubeColorInformation edgeColors, boolean facesSeeThrough, boolean edgesSeeThrough, float edgeThickness) {
        return create(loc, scale, translation, leftRotation, new Quaternionf(0, 0, 0, 1), faceColors, edgeColors, facesSeeThrough, edgesSeeThrough, edgeThickness);
    }

    public static FilledWireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, CubeColorInformation faceColors, WireframeCubeColorInformation edgeColors) {
        return create(loc, scale, translation, leftRotation, faceColors, edgeColors, false, false, 0.1f);
    }

    /**
     * Assembles a {@code FilledWireframeCubeColorDisplay} out of an already-reconstructed
     * {@link CubeColorDisplay}/{@link WireframeCubeColorDisplay} pair (e.g. themselves assembled from
     * adopted entities), instead of building fresh ones via {@link #spawnDisplay()} - used by
     * {@code me.simoncrafter.CraftersDisplayLibrary.persistence.DisplayPersistence} to reconstruct a
     * live tree from entities found already sitting in the world. Wires both sub-cubes up via the
     * normal public {@link #addChild} rather than reaching into private state.
     */
    @ApiStatus.Internal
    public static FilledWireframeCubeColorDisplay assemble(Location loc, Transformation localTransform, CubeColorDisplay faceCube, WireframeCubeColorDisplay wireframeCube) {
        FilledWireframeCubeColorDisplay obj = new FilledWireframeCubeColorDisplay(localTransform, loc, faceCube, wireframeCube);
        obj.addChild(faceCube);
        obj.addChild(wireframeCube);
        return obj;
    }

    public void spawnDisplay() {
        try {
            faceCube.spawnDisplay();
            wireframeCube.spawnDisplay();

            addChild(faceCube);
            addChild(wireframeCube);

            updateChildren(0);

            // Apply visibility state in case hideByDefault() was called before spawning
            faceCube.hideByDefault(hiddenByDefault);
            wireframeCube.hideByDefault(hiddenByDefault);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public CubeColorDisplay getFaceCube() {
        return faceCube;
    }

    public WireframeCubeColorDisplay getWireframeCube() {
        return wireframeCube;
    }

    // --- Face controls (mirrors CubeColorDisplay) ---

    public ColorDisplay getTop() {
        return faceCube.getTop();
    }

    public ColorDisplay getBottom() {
        return faceCube.getBottom();
    }

    public ColorDisplay getFront() {
        return faceCube.getFront();
    }

    public ColorDisplay getBack() {
        return faceCube.getBack();
    }

    public ColorDisplay getLeft() {
        return faceCube.getLeft();
    }

    public ColorDisplay getRight() {
        return faceCube.getRight();
    }

    private ColorDisplay getFaceDisplay(CubeFace face) {
        return switch (face) {
            case TOP -> faceCube.getTop();
            case BOTTOM -> faceCube.getBottom();
            case FRONT -> faceCube.getFront();
            case BACK -> faceCube.getBack();
            case LEFT -> faceCube.getLeft();
            case RIGHT -> faceCube.getRight();
        };
    }

    /** The current colour of a single face panel. */
    public Color getFaceColor(CubeFace face) {
        ColorDisplay display = getFaceDisplay(face);
        return display != null ? display.getColor() : null;
    }

    /** Colours a single face panel (not its bordering edges - see {@link #setFaceEdgesColor}). */
    public void setFaceColor(CubeFace face, Color color) {
        ColorDisplay display = getFaceDisplay(face);
        if (display != null) display.setColor(color);
    }

    /** Colours all 6 face panels, leaving the wireframe edges untouched. */
    public void setFacesColor(Color color) {
        faceCube.setColor(color);
    }

    public boolean isFacesSeeThrough() {
        return faceCube.isSeeThrough();
    }

    public void setFacesSeeThrough(boolean seeThrough) {
        faceCube.setSeeThrough(seeThrough);
    }

    // --- Edge controls (mirrors WireframeCubeColorDisplay) ---

    public LineColorDisplay getEdge(CubeEdge edge) {
        return wireframeCube.getEdge(edge);
    }

    public List<LineColorDisplay> getFaceEdges(CubeFace face) {
        return wireframeCube.getFaceEdges(face);
    }

    public Color getEdgeColor(CubeEdge edge) {
        return wireframeCube.getEdgeColor(edge);
    }

    public void setEdgeColor(CubeEdge edge, Color color) {
        wireframeCube.setEdgeColor(edge, color);
    }

    /** Colours the 4 edges bordering a face (not the face panel itself - see {@link #setFaceColor}). */
    public void setFaceEdgesColor(CubeFace face, Color color) {
        wireframeCube.setFaceColor(face, color);
    }

    /** Colours all 12 wireframe edges, leaving the face panels untouched. */
    public void setEdgesColor(Color color) {
        wireframeCube.setColor(color);
    }

    public float getThickness() {
        return wireframeCube.getThickness();
    }

    public void setThickness(float thickness) {
        wireframeCube.setThickness(thickness, 0);
    }

    public void setThickness(float thickness, int duration) {
        wireframeCube.setThickness(thickness, duration);
    }

    public boolean isEdgesSeeThrough() {
        return wireframeCube.isSeeThrough();
    }

    public void setEdgesSeeThrough(boolean seeThrough) {
        wireframeCube.setSeeThrough(seeThrough);
    }

    // --- Combined controls ---

    /** Colours all 6 faces AND all 12 edges the same colour. */
    @Override
    public void setColor(Color color) {
        faceCube.setColor(color);
        wireframeCube.setColor(color);
    }

    /** Sets see-through for both faces and edges at once. */
    @Override
    public void setSeeThrough(boolean seeThrough) {
        setFacesSeeThrough(seeThrough);
        setEdgesSeeThrough(seeThrough);
    }

    /** True only if both faces and edges are currently see-through. */
    @Override
    public boolean isSeeThrough() {
        return isFacesSeeThrough() && isEdgesSeeThrough();
    }

    @Override
    public void moveEntityStatic(Location location) {
        Vector oldLoc = getLocation().toVector();
        Vector newLoc = location.toVector();
        Vector diff = newLoc.subtract(oldLoc);

        moveRelative(diff.toVector3f().mul(-1), 0);

        super.moveEntityStatic(location);
        faceCube.moveEntityStatic(location);
        wireframeCube.moveEntityStatic(location);
    }

}
