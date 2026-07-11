package me.simoncrafter.CraftersDisplayLibrary.persistence;

import me.simoncrafter.CraftersDisplayLibrary.PluginHolder;
import me.simoncrafter.CraftersDisplayLibrary.core.AbstractEntityBackedDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.Tags;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.display.filledwireframecube.FilledWireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.line.LineColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.BlockDisplayObject;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.ItemDisplayObject;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.TextDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.CubeEdge;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.entity.ShulkerBasedCollisionBox;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.gson.GsonComponentSerializer;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.BlockDisplay;
import org.bukkit.entity.Display;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Shulker;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Transformation;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Serializes ("tags") a live {@link PositionObject} scene-graph tree onto the {@link Tags#CDL_ENTITY}
 * PDC of every entity it owns, and reconstructs ("reads") that tree back out of already-tagged
 * entities sitting in the world - e.g. after a server restart, or after a chunk unload/reload.
 * <p>
 * See {@code core/Tags.java} for the tag keys and {@link DisplayDataCodec} for the blob format used
 * by {@code CDL_DISPLAY_DATA}/{@code CDL_ANCESTOR_CHAIN}.
 *
 * <h2>How the tree is walked</h2>
 * {@link #tag(PositionObject)} recursively walks {@code root} and {@link IDisplayable#getChildren()}.
 * Every node gets its own random UUID for this call. A node that owns a physical entity (or several -
 * {@link ShulkerBasedCollisionBox}'s marker+shulker pair, {@link LineColorDisplay}'s 4 panels) tags
 * that entity/those entities directly with its own uuid/type/blob/parent-uuid. A "pure grouping" node
 * that owns no entity of its own ({@link CubeColorDisplay}, {@link WireframeCubeColorDisplay},
 * {@link FilledWireframeCubeColorDisplay}, or a plain {@link PositionObject} handle e.g. from
 * {@code StructureBuilder}) instead extends a running "ancestor chain" that gets embedded as an extra
 * tag ({@code CDL_ANCESTOR_CHAIN}) on every entity in its subtree that doesn't already have a closer
 * entity-backed ancestor - this is what lets a tree with entity-less nodes be fully reconstructed from
 * nothing but real, physically-present entities.
 * <p>
 * {@link #readChunk(Chunk)}/{@link #readWorld(World)} reverse this: group tagged entities by
 * {@code CDL_DISPLAY_UUID}, reconstruct every entity-backed leaf group via the matching
 * {@code adopt(...)} factory, reconstruct every "pure grouping" node recorded in any ancestor chain
 * via the matching {@code assemble(...)} factory (bottom-up, so a composite's children are always
 * resolved before the composite itself), wire parent/child links back up via the ordinary
 * {@link IDisplayable#addChild} used by every {@code assemble(...)} factory, and return only the
 * roots - nodes with no parent resolvable within the current scan (which, for {@link #readChunk},
 * includes a node whose parent's entities happen to live in a different chunk).
 */
public final class DisplayPersistence {

    private DisplayPersistence() {}

    // --- CDL_DISPLAY_TYPE values -------------------------------------------------------------

    private static final String TYPE_COLOR_DISPLAY = "ColorDisplay";
    private static final String TYPE_TEXT_DISPLAY = "TextDisplay";
    private static final String TYPE_BLOCK_DISPLAY = "BlockDisplayObject";
    private static final String TYPE_ITEM_DISPLAY = "ItemDisplayObject";
    private static final String TYPE_SHULKER_BOX = "ShulkerBasedCollisionBox";
    private static final String TYPE_LINE_COLOR_DISPLAY = "LineColorDisplay";
    private static final String TYPE_CUBE_COLOR_DISPLAY = "CubeColorDisplay";
    private static final String TYPE_WIREFRAME_CUBE = "WireframeCubeColorDisplay";
    private static final String TYPE_FILLED_WIREFRAME_CUBE = "FilledWireframeCubeColorDisplay";
    private static final String TYPE_POSITION_OBJECT = "PositionObject";

    // --- CDL_ENTITY_ROLE values ---------------------------------------------------------------

    private static final String ROLE_SELF = "SELF";
    private static final String ROLE_MARKER = "MARKER";
    private static final String ROLE_SHULKER = "SHULKER";

    /** {@code (uuid, type, blob)} for one entity-less "pure grouping" ancestor. See the class Javadoc. */
    record AncestorRecord(UUID uuid, String type, String blob) {}

    // =============================================================================================
    // tag()
    // =============================================================================================

    /**
     * Recursively tags {@code root} and every descendant with {@code CDL_PLUGIN}/{@code CDL_ITERATION}/
     * {@code CDL_DISPLAY_UUID}/{@code CDL_PARENT_UUID}/{@code CDL_DISPLAY_TYPE}/{@code CDL_ENTITY_ROLE}/
     * {@code CDL_DISPLAY_DATA} (plus {@code CDL_ANCESTOR_CHAIN} where needed) on every physical entity
     * it owns. Call explicitly after spawning a tree you want to survive a restart - not automatic on
     * every {@code spawnDisplay()}.
     */
    public static void tag(PositionObject root) {
        walk(root, null, new ArrayList<>());
    }

    private static void walk(IDisplayable node, UUID parentUuid, List<AncestorRecord> ancestorChain) {
        if (node instanceof LineColorDisplay line) {
            UUID ownUuid = tagLineColorDisplay(line, parentUuid, ancestorChain);
            for (IDisplayable child : line.getChildren()) walk(child, ownUuid, new ArrayList<>());
            return;
        }
        if (node instanceof ColorDisplay || node instanceof TextDisplay || node instanceof BlockDisplayObject || node instanceof ItemDisplayObject) {
            UUID ownUuid = tagSingleEntityLeaf(node, parentUuid, ancestorChain);
            for (IDisplayable child : node.getChildren()) walk(child, ownUuid, new ArrayList<>());
            return;
        }
        if (node instanceof ShulkerBasedCollisionBox box) {
            UUID ownUuid = tagShulkerBox(box, parentUuid, ancestorChain);
            for (IDisplayable child : box.getChildren()) walk(child, ownUuid, new ArrayList<>());
            return;
        }
        if (node instanceof PositionObject po) {
            // Pure grouping node: CubeColorDisplay, WireframeCubeColorDisplay,
            // FilledWireframeCubeColorDisplay, or a plain PositionObject handle - owns no entity of
            // its own, so its own uuid/type/blob rides along as an ancestor record on every
            // entity-backed descendant instead.
            UUID ownUuid = UUID.randomUUID();
            String type = groupingTypeNameFor(po);
            String blob = blobForGroupingNode(po);
            List<AncestorRecord> newChain = new ArrayList<>(ancestorChain);
            newChain.add(new AncestorRecord(ownUuid, type, blob));
            for (IDisplayable child : po.getChildren()) walk(child, ownUuid, newChain);
            return;
        }
        // Every concrete IDisplayable in this library extends PositionObject; an unrecognized
        // implementor that doesn't has nothing we can tag.
    }

    private static UUID tagSingleEntityLeaf(IDisplayable node, UUID parentUuid, List<AncestorRecord> ancestorChain) {
        UUID ownUuid = UUID.randomUUID();
        String type;
        String blob;
        Entity entity;
        if (node instanceof ColorDisplay cd) {
            type = TYPE_COLOR_DISPLAY;
            blob = blobForColorDisplay(cd);
            entity = cd.getEntity();
        } else if (node instanceof TextDisplay td) {
            type = TYPE_TEXT_DISPLAY;
            blob = blobForTextDisplay(td);
            entity = td.getEntity();
        } else if (node instanceof BlockDisplayObject bd) {
            type = TYPE_BLOCK_DISPLAY;
            blob = blobForBlockDisplay(bd);
            entity = bd.getEntity();
        } else if (node instanceof ItemDisplayObject id) {
            type = TYPE_ITEM_DISPLAY;
            blob = blobForItemDisplay(id);
            entity = id.getEntity();
        } else {
            return ownUuid;
        }
        if (entity != null) tagEntity(entity, ownUuid, parentUuid, type, ROLE_SELF, blob, ancestorChain);
        return ownUuid;
    }

    private static UUID tagShulkerBox(ShulkerBasedCollisionBox box, UUID parentUuid, List<AncestorRecord> ancestorChain) {
        UUID ownUuid = UUID.randomUUID();
        String blob = blobForTransformLocation(box.getLocalTransform(), box.getLocation());
        ArmorStand marker = box.getMarkerEntity();
        Shulker shulker = box.getShulker();
        if (marker != null) tagEntity(marker, ownUuid, parentUuid, TYPE_SHULKER_BOX, ROLE_MARKER, blob, ancestorChain);
        if (shulker != null) tagEntity(shulker, ownUuid, parentUuid, TYPE_SHULKER_BOX, ROLE_SHULKER, blob, ancestorChain);
        return ownUuid;
    }

    private static UUID tagLineColorDisplay(LineColorDisplay line, UUID parentUuid, List<AncestorRecord> ancestorChain) {
        UUID ownUuid = UUID.randomUUID();
        String blob = blobForLineColorDisplay(line);
        for (int i = 0; i < 4; i++) {
            ColorDisplay panel = line.getPanel(i);
            if (panel == null || panel.getEntity() == null) continue;
            tagEntity(panel.getEntity(), ownUuid, parentUuid, TYPE_LINE_COLOR_DISPLAY, "PANEL_" + i, blob, ancestorChain);
        }
        return ownUuid;
    }

    private static void tagEntity(Entity entity, UUID ownUuid, UUID parentUuid, String type, String role, String blob, List<AncestorRecord> ancestorChain) {
        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        pdc.set(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN, true);
        pdc.set(Tags.CDL_PLUGIN, PersistentDataType.STRING, PluginHolder.getPlugin().getName());
        pdc.set(Tags.CDL_ITERATION, PersistentDataType.LONG, PluginHolder.getRegisterTimestamp());
        pdc.set(Tags.CDL_DISPLAY_UUID, PersistentDataType.STRING, ownUuid.toString());
        if (parentUuid != null) {
            pdc.set(Tags.CDL_PARENT_UUID, PersistentDataType.STRING, parentUuid.toString());
        } else {
            pdc.remove(Tags.CDL_PARENT_UUID);
        }
        pdc.set(Tags.CDL_DISPLAY_TYPE, PersistentDataType.STRING, type);
        pdc.set(Tags.CDL_ENTITY_ROLE, PersistentDataType.STRING, role);
        pdc.set(Tags.CDL_DISPLAY_DATA, PersistentDataType.STRING, blob);
        if (!ancestorChain.isEmpty()) {
            DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
            w.writeAncestorChain(ancestorChain);
            pdc.set(Tags.CDL_ANCESTOR_CHAIN, PersistentDataType.STRING, w.build());
        } else {
            pdc.remove(Tags.CDL_ANCESTOR_CHAIN);
        }
    }

    private static String groupingTypeNameFor(PositionObject po) {
        if (po instanceof CubeColorDisplay) return TYPE_CUBE_COLOR_DISPLAY;
        if (po instanceof WireframeCubeColorDisplay) return TYPE_WIREFRAME_CUBE;
        if (po instanceof FilledWireframeCubeColorDisplay) return TYPE_FILLED_WIREFRAME_CUBE;
        return TYPE_POSITION_OBJECT;
    }

    // --- Blob writers ----------------------------------------------------------------------------

    private static void writeTransformAndLocation(DisplayDataCodec.Writer w, Transformation transform, Location loc) {
        w.writeTransformation(transform);
        w.writeLocation(loc);
    }

    private static void writeGenericDisplayProps(DisplayDataCodec.Writer w, AbstractEntityBackedDisplay<?> d) {
        w.writeString(d.getBillboard().name());
        w.writeInt(d.getTeleportDuration());
        w.writeFloat(d.getViewRange());
        w.writeFloat(d.getShadowRadius());
        w.writeFloat(d.getShadowStrength());
        w.writeColor(d.getGlowColorOverride());
        w.writeBrightness(d.getBrightness());
    }

    private static String blobForColorDisplay(ColorDisplay cd) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, cd.getLocalTransform(), cd.getLocation());
        w.writeColor(cd.getColor());
        w.writeBoolean(cd.isSeeThrough());
        writeGenericDisplayProps(w, cd);
        return w.build();
    }

    private static String blobForTextDisplay(TextDisplay td) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, td.getLocalTransform(), td.getLocation());
        w.writeColor(td.getColor());
        w.writeBoolean(td.isSeeThrough());
        w.writeString(GsonComponentSerializer.gson().serialize(td.getText()));
        w.writeString(td.getAlignment().name());
        w.writeInt(td.getLineWidth());
        w.writeBoolean(td.hasBackground());
        w.writeColor(td.getBackgroundColor());
        writeGenericDisplayProps(w, td);
        return w.build();
    }

    private static String blobForBlockDisplay(BlockDisplayObject bd) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, bd.getLocalTransform(), bd.getLocation());
        w.writeString(bd.getBlock().getAsString());
        writeGenericDisplayProps(w, bd);
        return w.build();
    }

    private static String blobForItemDisplay(ItemDisplayObject id) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, id.getLocalTransform(), id.getLocation());
        w.writeBytes(id.getItem() != null ? id.getItem().serializeAsBytes() : null);
        w.writeString(id.getItemDisplayTransform().name());
        writeGenericDisplayProps(w, id);
        return w.build();
    }

    private static String blobForLineColorDisplay(LineColorDisplay line) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, line.getLocalTransform(), line.getLocation());
        w.writeVector3f(line.getBaseStartPoint());
        w.writeVector3f(line.getBaseDirection());
        w.writeFloat(line.getThickness());
        w.writeColor(line.getColor());
        w.writeBoolean(line.isSeeThrough());
        return w.build();
    }

    private static String blobForTransformLocation(Transformation transform, Location loc) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, transform, loc);
        return w.build();
    }

    private static String blobForGroupingNode(PositionObject po) {
        DisplayDataCodec.Writer w = new DisplayDataCodec.Writer();
        writeTransformAndLocation(w, po.getLocalTransform(), po.getLocation());
        if (po instanceof CubeColorDisplay cube) {
            w.writeBoolean(cube.isSeeThrough());
        } else if (po instanceof WireframeCubeColorDisplay wf) {
            w.writeBoolean(wf.isSeeThrough());
            w.writeFloat(wf.getThickness());
        }
        // FilledWireframeCubeColorDisplay and a plain PositionObject handle need nothing beyond
        // Transform + Location - their visible state is fully expressed by their children's blobs.
        return w.build();
    }

    // =============================================================================================
    // readWorld() / readChunk()
    // =============================================================================================

    /** Concatenates {@link #readChunk(Chunk)} over every currently-loaded chunk in {@code world}. */
    public static List<PositionObject> readWorld(World world) {
        List<PositionObject> result = new ArrayList<>();
        for (Chunk chunk : world.getLoadedChunks()) {
            result.addAll(readChunk(chunk));
        }
        return result;
    }

    /**
     * Scans {@code chunk}'s entities, groups the ones tagged by {@link #tag(PositionObject)} by
     * {@code CDL_DISPLAY_UUID}, reconstructs each group and every entity-less ancestor recorded in any
     * group's {@code CDL_ANCESTOR_CHAIN} into live objects wrapping the existing entities (no new
     * entities are spawned), wires parent/child links back up, and returns only the roots - nodes
     * with no parent resolvable within this one-chunk scan (which includes a node whose parent's
     * entities happen to live in a different chunk; call {@link #readWorld(World)} to resolve those
     * too).
     */
    public static List<PositionObject> readChunk(Chunk chunk) {
        Map<UUID, List<Entity>> groupEntities = new LinkedHashMap<>();
        for (Entity e : chunk.getEntities()) {
            PersistentDataContainer pdc = e.getPersistentDataContainer();
            if (!Boolean.TRUE.equals(pdc.get(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN))) continue;
            String uuidStr = pdc.get(Tags.CDL_DISPLAY_UUID, PersistentDataType.STRING);
            if (uuidStr == null) continue;
            groupEntities.computeIfAbsent(UUID.fromString(uuidStr), k -> new ArrayList<>()).add(e);
        }
        if (groupEntities.isEmpty()) return List.of();

        Map<UUID, LeafGroupInfo> leafGroups = new LinkedHashMap<>();
        Map<UUID, AncestorRecord> compositeNodes = new LinkedHashMap<>();
        Map<UUID, UUID> compositeParent = new HashMap<>();

        for (Map.Entry<UUID, List<Entity>> entry : groupEntities.entrySet()) {
            UUID uuid = entry.getKey();
            List<Entity> members = entry.getValue();
            PersistentDataContainer first = members.get(0).getPersistentDataContainer();
            String type = first.get(Tags.CDL_DISPLAY_TYPE, PersistentDataType.STRING);
            String blob = first.get(Tags.CDL_DISPLAY_DATA, PersistentDataType.STRING);
            String parentUuidStr = first.get(Tags.CDL_PARENT_UUID, PersistentDataType.STRING);
            UUID parentUuid = parentUuidStr != null ? UUID.fromString(parentUuidStr) : null;
            String ancestorChainStr = first.get(Tags.CDL_ANCESTOR_CHAIN, PersistentDataType.STRING);
            List<AncestorRecord> ancestorChain = ancestorChainStr != null
                    ? new DisplayDataCodec.Reader(ancestorChainStr).readAncestorChain()
                    : List.of();

            Map<String, Entity> roles = new HashMap<>();
            for (Entity member : members) {
                String role = member.getPersistentDataContainer().get(Tags.CDL_ENTITY_ROLE, PersistentDataType.STRING);
                if (role != null) roles.put(role, member);
            }

            leafGroups.put(uuid, new LeafGroupInfo(uuid, type, blob, parentUuid, roles));

            for (int i = 0; i < ancestorChain.size(); i++) {
                AncestorRecord rec = ancestorChain.get(i);
                compositeNodes.putIfAbsent(rec.uuid(), rec);
                compositeParent.putIfAbsent(rec.uuid(), i > 0 ? ancestorChain.get(i - 1).uuid() : null);
            }
        }

        Map<UUID, UUID> parentOf = new HashMap<>();
        for (LeafGroupInfo g : leafGroups.values()) {
            UUID p = g.parentUuid();
            // A parent uuid that isn't resolvable within this scan (e.g. its entities live in a
            // different chunk) makes this node a root for the purposes of this call.
            if (p != null && !leafGroups.containsKey(p) && !compositeNodes.containsKey(p)) p = null;
            parentOf.put(g.uuid(), p);
        }
        for (UUID compUuid : compositeNodes.keySet()) parentOf.put(compUuid, compositeParent.get(compUuid));

        Map<UUID, List<UUID>> childrenOf = new HashMap<>();
        for (Map.Entry<UUID, UUID> e : parentOf.entrySet()) {
            if (e.getValue() == null) continue;
            childrenOf.computeIfAbsent(e.getValue(), k -> new ArrayList<>()).add(e.getKey());
        }

        Set<UUID> allUuids = new LinkedHashSet<>();
        allUuids.addAll(leafGroups.keySet());
        allUuids.addAll(compositeNodes.keySet());

        Map<UUID, IDisplayable> resolved = new HashMap<>();
        List<PositionObject> roots = new ArrayList<>();
        for (UUID uuid : allUuids) {
            if (parentOf.get(uuid) != null) continue;
            IDisplayable node = resolveNode(uuid, leafGroups, compositeNodes, childrenOf, resolved);
            if (node instanceof PositionObject po) roots.add(po);
        }
        return roots;
    }

    private record LeafGroupInfo(UUID uuid, String type, String blob, UUID parentUuid, Map<String, Entity> roles) {}

    private static IDisplayable resolveNode(UUID uuid, Map<UUID, LeafGroupInfo> leafGroups, Map<UUID, AncestorRecord> compositeNodes,
                                             Map<UUID, List<UUID>> childrenOf, Map<UUID, IDisplayable> resolved) {
        IDisplayable cached = resolved.get(uuid);
        if (cached != null) return cached;

        LeafGroupInfo leaf = leafGroups.get(uuid);
        if (leaf != null) {
            IDisplayable obj = reconstructLeaf(leaf);
            if (obj != null) resolved.put(uuid, obj);
            return obj;
        }

        AncestorRecord meta = compositeNodes.get(uuid);
        if (meta == null) return null;

        List<UUID> childUuids = childrenOf.getOrDefault(uuid, List.of());
        List<IDisplayable> children = new ArrayList<>();
        for (UUID childUuid : childUuids) {
            IDisplayable child = resolveNode(childUuid, leafGroups, compositeNodes, childrenOf, resolved);
            if (child != null) children.add(child);
        }
        IDisplayable composite = assembleComposite(meta.type(), meta.blob(), children);
        if (composite != null) resolved.put(uuid, composite);
        return composite;
    }

    // --- Leaf reconstruction ----------------------------------------------------------------------

    private static IDisplayable reconstructLeaf(LeafGroupInfo leaf) {
        DisplayDataCodec.Reader r = new DisplayDataCodec.Reader(leaf.blob());
        return switch (leaf.type()) {
            case TYPE_COLOR_DISPLAY -> {
                if (!(leaf.roles().get(ROLE_SELF) instanceof org.bukkit.entity.TextDisplay entity)) yield null;
                Transformation transform = r.readTransformation();
                Location loc = r.readLocation();
                Color color = r.readColor();
                boolean seeThrough = r.readBoolean();
                GenericDisplayProps props = readGenericDisplayProps(r);
                yield ColorDisplay.adopt(entity, loc, transform, color, seeThrough, props.billboard(), props.teleportDuration(), props.viewRange(),
                        props.shadowRadius(), props.shadowStrength(), props.glowColorOverride(), props.brightness());
            }
            case TYPE_TEXT_DISPLAY -> {
                if (!(leaf.roles().get(ROLE_SELF) instanceof org.bukkit.entity.TextDisplay entity)) yield null;
                Transformation transform = r.readTransformation();
                Location loc = r.readLocation();
                Color color = r.readColor();
                boolean seeThrough = r.readBoolean();
                String textJson = r.readString();
                Component text = textJson == null ? Component.text("") : GsonComponentSerializer.gson().deserialize(textJson);
                org.bukkit.entity.TextDisplay.TextAlignment alignment = org.bukkit.entity.TextDisplay.TextAlignment.valueOf(r.readString());
                int lineWidth = r.readInt();
                boolean hasBackground = r.readBoolean();
                Color backgroundColor = r.readColor();
                GenericDisplayProps props = readGenericDisplayProps(r);
                yield TextDisplay.adopt(entity, loc, transform, text, color, hasBackground, backgroundColor, lineWidth, alignment, seeThrough,
                        props.billboard(), props.teleportDuration(), props.viewRange(), props.shadowRadius(), props.shadowStrength(), props.glowColorOverride(), props.brightness());
            }
            case TYPE_BLOCK_DISPLAY -> {
                if (!(leaf.roles().get(ROLE_SELF) instanceof BlockDisplay entity)) yield null;
                Transformation transform = r.readTransformation();
                Location loc = r.readLocation();
                BlockData blockData = org.bukkit.Bukkit.createBlockData(r.readString());
                GenericDisplayProps props = readGenericDisplayProps(r);
                yield BlockDisplayObject.adopt(entity, loc, transform, blockData, props.billboard(), props.teleportDuration(), props.viewRange(),
                        props.shadowRadius(), props.shadowStrength(), props.glowColorOverride(), props.brightness());
            }
            case TYPE_ITEM_DISPLAY -> {
                if (!(leaf.roles().get(ROLE_SELF) instanceof ItemDisplay entity)) yield null;
                Transformation transform = r.readTransformation();
                Location loc = r.readLocation();
                byte[] itemBytes = r.readBytes();
                ItemStack item = itemBytes == null ? null : ItemStack.deserializeBytes(itemBytes);
                ItemDisplay.ItemDisplayTransform itemTransform = ItemDisplay.ItemDisplayTransform.valueOf(r.readString());
                GenericDisplayProps props = readGenericDisplayProps(r);
                yield ItemDisplayObject.adopt(entity, loc, transform, item, itemTransform, props.billboard(), props.teleportDuration(), props.viewRange(),
                        props.shadowRadius(), props.shadowStrength(), props.glowColorOverride(), props.brightness());
            }
            case TYPE_SHULKER_BOX -> {
                if (!(leaf.roles().get(ROLE_MARKER) instanceof ArmorStand marker) || !(leaf.roles().get(ROLE_SHULKER) instanceof Shulker shulker)) yield null;
                Transformation transform = r.readTransformation();
                Location loc = r.readLocation();
                yield ShulkerBasedCollisionBox.adopt(marker, shulker, loc, transform);
            }
            case TYPE_LINE_COLOR_DISPLAY -> {
                if (!(leaf.roles().get("PANEL_0") instanceof org.bukkit.entity.TextDisplay p0)
                        || !(leaf.roles().get("PANEL_1") instanceof org.bukkit.entity.TextDisplay p1)
                        || !(leaf.roles().get("PANEL_2") instanceof org.bukkit.entity.TextDisplay p2)
                        || !(leaf.roles().get("PANEL_3") instanceof org.bukkit.entity.TextDisplay p3)) yield null;
                Transformation transform = r.readTransformation();
                Location loc = r.readLocation();
                Vector3f baseStart = r.readVector3f();
                Vector3f baseDirection = r.readVector3f();
                float thickness = r.readFloat();
                Color color = r.readColor();
                boolean seeThrough = r.readBoolean();
                yield LineColorDisplay.adopt(p0, p1, p2, p3, loc, transform, baseStart, baseDirection, color, seeThrough, thickness);
            }
            default -> null;
        };
    }

    private record GenericDisplayProps(Display.Billboard billboard, int teleportDuration, float viewRange, float shadowRadius, float shadowStrength,
                                        Color glowColorOverride, Display.Brightness brightness) {}

    private static GenericDisplayProps readGenericDisplayProps(DisplayDataCodec.Reader r) {
        Display.Billboard billboard = Display.Billboard.valueOf(r.readString());
        int teleportDuration = r.readInt();
        float viewRange = r.readFloat();
        float shadowRadius = r.readFloat();
        float shadowStrength = r.readFloat();
        Color glowColorOverride = r.readColor();
        Display.Brightness brightness = r.readBrightness();
        return new GenericDisplayProps(billboard, teleportDuration, viewRange, shadowRadius, shadowStrength, glowColorOverride, brightness);
    }

    // --- Composite (entity-less "pure grouping" node) reconstruction ------------------------------

    private static IDisplayable assembleComposite(String type, String blob, List<IDisplayable> children) {
        DisplayDataCodec.Reader r = new DisplayDataCodec.Reader(blob);
        Transformation transform = r.readTransformation();
        Location loc = r.readLocation();
        return switch (type) {
            case TYPE_CUBE_COLOR_DISPLAY -> {
                boolean seeThrough = r.readBoolean();
                ColorDisplay top = null, bottom = null, left = null, right = null, front = null, back = null;
                for (IDisplayable child : children) {
                    if (!(child instanceof ColorDisplay cd)) continue;
                    switch (matchCubeFace(cd)) {
                        case "TOP" -> top = cd;
                        case "BOTTOM" -> bottom = cd;
                        case "LEFT" -> left = cd;
                        case "RIGHT" -> right = cd;
                        case "FRONT" -> front = cd;
                        case "BACK" -> back = cd;
                        default -> {}
                    }
                }
                if (top == null || bottom == null || left == null || right == null || front == null || back == null) yield null;
                yield CubeColorDisplay.assemble(loc, transform, seeThrough, new CubeColorInformation(), top, bottom, left, right, front, back);
            }
            case TYPE_WIREFRAME_CUBE -> {
                boolean seeThrough = r.readBoolean();
                float thickness = r.readFloat();
                EnumMap<CubeEdge, LineColorDisplay> edges = new EnumMap<>(CubeEdge.class);
                for (IDisplayable child : children) {
                    if (!(child instanceof LineColorDisplay line)) continue;
                    CubeEdge edge = matchCubeEdge(line);
                    if (edge != null) edges.put(edge, line);
                }
                if (edges.size() != CubeEdge.values().length) yield null;
                yield WireframeCubeColorDisplay.assemble(loc, transform, new WireframeCubeColorInformation(), seeThrough, thickness, edges);
            }
            case TYPE_FILLED_WIREFRAME_CUBE -> {
                CubeColorDisplay faceCube = null;
                WireframeCubeColorDisplay wireframeCube = null;
                for (IDisplayable child : children) {
                    if (child instanceof CubeColorDisplay cd) faceCube = cd;
                    else if (child instanceof WireframeCubeColorDisplay wf) wireframeCube = wf;
                }
                if (faceCube == null || wireframeCube == null) yield null;
                yield FilledWireframeCubeColorDisplay.assemble(loc, transform, faceCube, wireframeCube);
            }
            case TYPE_POSITION_OBJECT -> {
                PositionObject obj = new PositionObject(new ArrayList<>(), transform, loc);
                for (IDisplayable child : children) obj.addChild(child);
                yield obj;
            }
            default -> null;
        };
    }

    /** {@code CubeColorDisplay.spawnDisplay()}'s 6 fixed per-face local left-rotation constants - deterministic, so a face's identity survives the round trip without a separate slot tag. */
    private static final Map<String, Quaternionf> CUBE_FACE_ROTATIONS = Map.of(
            "TOP", new Quaternionf(-0.707f, 0, 0, 0.707f),
            "BOTTOM", new Quaternionf(0.707f, 0, 0, 0.707f),
            "LEFT", new Quaternionf(0, 0, 0, 1),
            "RIGHT", new Quaternionf(0, 1, 0, 0),
            "FRONT", new Quaternionf(0, 0.707f, 0, 0.707f),
            "BACK", new Quaternionf(0, -0.707f, 0, 0.707f)
    );

    private static String matchCubeFace(ColorDisplay face) {
        Quaternionf rot = face.getLocalTransform().getLeftRotation();
        String best = null;
        float bestDot = -2f;
        for (Map.Entry<String, Quaternionf> e : CUBE_FACE_ROTATIONS.entrySet()) {
            Quaternionf q = e.getValue();
            // abs() handles the sign ambiguity of quaternion equivalence (q and -q represent the same rotation).
            float dot = Math.abs(rot.x * q.x + rot.y * q.y + rot.z * q.z + rot.w * q.w);
            if (dot > bestDot) {
                bestDot = dot;
                best = e.getKey();
            }
        }
        return best;
    }

    private static CubeEdge matchCubeEdge(LineColorDisplay line) {
        Vector3f start = line.getBaseStartPoint();
        Vector3f direction = line.getBaseDirection();
        CubeEdge best = null;
        float bestScore = Float.MAX_VALUE;
        for (CubeEdge edge : CubeEdge.values()) {
            float score = new Vector3f(start).sub(edge.getStart()).length() + new Vector3f(direction).sub(edge.getDirection()).length();
            if (score < bestScore) {
                bestScore = score;
                best = edge;
            }
        }
        return best;
    }

    // =============================================================================================
    // Iteration bookkeeping
    // =============================================================================================

    /** Distinct {@code CDL_ITERATION} values currently present among tagged entities in every loaded chunk of {@code world}. */
    public static List<Long> listIterationTimestamps(World world) {
        Set<Long> set = new LinkedHashSet<>();
        for (Chunk chunk : world.getLoadedChunks()) set.addAll(listIterationTimestamps(chunk));
        return new ArrayList<>(set);
    }

    /** Distinct {@code CDL_ITERATION} values currently present among tagged entities in {@code chunk}. */
    public static List<Long> listIterationTimestamps(Chunk chunk) {
        Set<Long> set = new LinkedHashSet<>();
        for (Entity e : chunk.getEntities()) {
            PersistentDataContainer pdc = e.getPersistentDataContainer();
            if (!Boolean.TRUE.equals(pdc.get(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN))) continue;
            Long iteration = pdc.get(Tags.CDL_ITERATION, PersistentDataType.LONG);
            if (iteration != null) set.add(iteration);
        }
        return new ArrayList<>(set);
    }

    /** Removes every tagged entity belonging to {@code iterationTimestamp}, across every loaded chunk in {@code world}. */
    public static void removeIteration(World world, long iterationTimestamp) {
        for (Chunk chunk : world.getLoadedChunks()) removeIteration(chunk, iterationTimestamp);
    }

    /** Removes every tagged entity (any iteration) across every loaded chunk in {@code world}. */
    public static void removeAllIterations(World world) {
        for (Chunk chunk : world.getLoadedChunks()) removeAllIterations(chunk);
    }

    /**
     * Removes every tagged entity in {@code chunk} belonging to {@code iterationTimestamp}. Each
     * tagged entity (e.g. a {@link ShulkerBasedCollisionBox}'s marker and shulker, or a
     * {@link LineColorDisplay}'s 4 panels) is removed independently rather than specially paired up -
     * Bukkit dismounts a vehicle's passengers as part of {@code Entity#remove()}, and every physical
     * entity in a tagged group carries the same {@code CDL_ITERATION} value, so an independent sweep
     * removes the whole group regardless of removal order.
     */
    public static void removeIteration(Chunk chunk, long iterationTimestamp) {
        for (Entity e : chunk.getEntities()) {
            PersistentDataContainer pdc = e.getPersistentDataContainer();
            if (!Boolean.TRUE.equals(pdc.get(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN))) continue;
            Long iteration = pdc.get(Tags.CDL_ITERATION, PersistentDataType.LONG);
            if (iteration != null && iteration == iterationTimestamp) e.remove();
        }
    }

    /** Removes every tagged entity in {@code chunk}, regardless of iteration. See {@link #removeIteration(Chunk, long)} for the removal strategy. */
    public static void removeAllIterations(Chunk chunk) {
        for (Entity e : chunk.getEntities()) {
            PersistentDataContainer pdc = e.getPersistentDataContainer();
            if (Boolean.TRUE.equals(pdc.get(Tags.CDL_ENTITY, PersistentDataType.BOOLEAN))) {
                e.remove();
            }
        }
    }
}
