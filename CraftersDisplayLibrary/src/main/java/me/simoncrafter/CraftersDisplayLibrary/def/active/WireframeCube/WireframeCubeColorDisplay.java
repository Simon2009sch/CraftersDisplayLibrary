package me.simoncrafter.CraftersDisplayLibrary.def.active.WireframeCube;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Line.LineColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.def.interfaces.IHidable;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;

/**
 * A wireframe cube whose 12 edges are {@link LineColorDisplay}s and whose faces are not drawn.
 * <p>
 * Built in the same style as
 * {@link me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay}: the edges are
 * children of this object, so all of the inherited transform controls (move / rotate / scale, with
 * or without animation) propagate to them. Non-uniform scaling turns the cube into a rectangular
 * box; edge thickness is kept constant regardless of scale.
 * <p>
 * Edge colours can be set individually ({@link #setEdgeColor}), per face
 * ({@link #setFaceColor}, colours all 4 edges of that face) or all at once ({@link #setColor}).
 */
public class WireframeCubeColorDisplay extends PositionObject implements IHidable, IColorableDisplay, ICuboidDisplay {

    private boolean seeTrough;
    private boolean hiddenByDefault = false;
    private float thickness;
    private WireframeCubeColorInformation colorInformation;

    private final EnumMap<CubeEdge, LineColorDisplay> edges = new EnumMap<>(CubeEdge.class);

    private WireframeCubeColorDisplay(Transformation localTransform, Location location, WireframeCubeColorInformation colorInformation, boolean seeThrough, float thickness) {
        super(List.of(), new Transformation(localTransform.getTranslation(), localTransform.getLeftRotation(), localTransform.getScale(), localTransform.getRightRotation()), location);
        this.colorInformation = colorInformation;
        this.seeTrough = seeThrough;
        this.thickness = thickness;
    }

    public static WireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, WireframeCubeColorInformation colorInformation, boolean seeThrough, float thickness) {
        return new WireframeCubeColorDisplay(new Transformation(translation, leftRotation, scale, new Quaternionf(0, 0, 0, 1)), loc, colorInformation, seeThrough, thickness);
    }

    public static WireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, WireframeCubeColorInformation colorInformation, boolean seeThrough) {
        return create(loc, scale, translation, leftRotation, colorInformation, seeThrough, 0.1f);
    }

    public static WireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, WireframeCubeColorInformation colorInformation) {
        return create(loc, scale, translation, leftRotation, colorInformation, false, 0.1f);
    }

    public static WireframeCubeColorDisplay create(Location loc, Vector3f scale, Vector3f translation, Quaternionf leftRotation, Quaternionf rightRotation, WireframeCubeColorInformation colorInformation, boolean seeThrough, float thickness) {
        return new WireframeCubeColorDisplay(new Transformation(translation, leftRotation, scale, rightRotation), loc, colorInformation, seeThrough, thickness);
    }

    public void spawnDisplay() {
        try {
            for (CubeEdge edge : CubeEdge.values()) {
                Vector3f start = edge.getStart();
                Vector3f direction = edge.getDirection();

                LineColorDisplay line = LineColorDisplay.createFromDirection(start, direction, colorInformation.getEdge(edge), getLocation(), thickness);
                line.setSeeTrough(seeTrough);
                line.spawnDisplay();
                line.hideByDefault(hiddenByDefault);

                edges.put(edge, line);
                addChild(line);
            }
            // Push the cube's current transform onto every freshly-spawned edge.
            updateChildren(0);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public LineColorDisplay getEdge(CubeEdge edge) {
        return edges.get(edge);
    }

    /** The 4 edge displays that border the given face. */
    public List<LineColorDisplay> getFaceEdges(CubeFace face) {
        List<LineColorDisplay> out = new ArrayList<>();
        for (CubeEdge edge : face.getEdges()) {
            LineColorDisplay line = edges.get(edge);
            if (line != null) out.add(line);
        }
        return out;
    }

    public Color getEdgeColor(CubeEdge edge) {
        LineColorDisplay line = edges.get(edge);
        return line != null ? line.getColor() : colorInformation.getEdge(edge);
    }

    public void setEdgeColor(CubeEdge edge, Color color) {
        colorInformation = colorInformation.withEdge(edge, color);
        LineColorDisplay line = edges.get(edge);
        if (line != null) line.setColor(color);
    }

    /** Colours all 4 edges of the given face. */
    public void setFaceColor(CubeFace face, Color color) {
        colorInformation = colorInformation.withFace(face, color);
        for (CubeEdge edge : face.getEdges()) {
            LineColorDisplay line = edges.get(edge);
            if (line != null) line.setColor(color);
        }
    }

    @Override
    public void setColor(Color color) {
        colorInformation = new WireframeCubeColorInformation(color);
        for (LineColorDisplay line : edges.values()) {
            line.setColor(color);
        }
    }

    public float getThickness() {
        return thickness;
    }

    public void setThickness(float thickness) {
        setThickness(thickness, 0);
    }

    public void setThickness(float thickness, int duration) {
        this.thickness = thickness;
        for (LineColorDisplay line : edges.values()) {
            line.setThickness(thickness, duration);
        }
    }

    public boolean isSeeTrough() {
        return seeTrough;
    }

    public void setSeeTrough(boolean seeTrough) {
        this.seeTrough = seeTrough;
        for (LineColorDisplay line : edges.values()) {
            line.setSeeTrough(seeTrough);
        }
    }

    @Override
    public void moveEntityStatic(Location location) {
        Vector oldLoc = getLocation().toVector();
        Vector newLoc = location.toVector();
        Vector diff = newLoc.subtract(oldLoc);

        moveRelative(diff.toVector3f().mul(-1), 0);

        super.moveEntityStatic(location);
        for (LineColorDisplay line : edges.values()) {
            line.moveEntityStatic(location);
        }
    }

    @Override
    public boolean isHiddenByDefault() {
        return hiddenByDefault;
    }

    @Override
    public IDisplayable hideByDefault(boolean hide) {
        hiddenByDefault = hide;
        for (LineColorDisplay line : edges.values()) {
            line.hideByDefault(hide);
        }
        return this;
    }

    @Override
    public IDisplayable showForPlayer(Player player) {
        for (LineColorDisplay line : edges.values()) {
            line.showForPlayer(player);
        }
        return this;
    }

    @Override
    public IDisplayable hideForPlayer(Player player) {
        for (LineColorDisplay line : edges.values()) {
            line.hideForPlayer(player);
        }
        return this;
    }

    @Override
    public void remove() {
        super.remove();
        for (LineColorDisplay line : edges.values()) {
            line.remove();
        }
        edges.clear();
    }
}
