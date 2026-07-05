package me.simoncrafter.CraftersDisplayLibrary.builder;

import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IDisplayable;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.BlockDisplayObject;
import me.simoncrafter.CraftersDisplayLibrary.entity.ShulkerBasedCollisionBox;
import net.kyori.adventure.text.BlockNBTComponent;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.util.Transformation;
import org.bukkit.util.Vector;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

public class StructureBuilder {

    public static PositionObject assembleOutOfBlocks(World world, Vector origenLocation, List<Vector> blocks, List<Material> blocksToIgnore, boolean addCollision, boolean removeBlocks, Material removeBlockMaterial, boolean spawn) {
        Location handleLocation = origenLocation.toLocation(world);
        PositionObject handle = new PositionObject(new ArrayList<>(), new Transformation(new Vector3f(0, 0, 0), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf()), handleLocation);

        for (Vector v : blocks) {
            Block block = v.toLocation(world).getBlock();

            if (blocksToIgnore.contains(block.getType())) {
                continue;
            }

            // Calculate relative position from parent origin
            Vector3f relativePos = block.getLocation().toVector().subtract(handleLocation.toVector()).toVector3f();
            Location relativeLocation = handleLocation.clone().add(relativePos.x, relativePos.y, relativePos.z);

            BlockDisplayObject display = BlockDisplayObject.create(relativeLocation, new Vector3f(1, 1, 1), new Vector3f(), block.getBlockData());
            if (removeBlocks) block.setType(removeBlockMaterial);
            ShulkerBasedCollisionBox coll = null;
            if (addCollision) {
                coll = ShulkerBasedCollisionBox.create(relativeLocation, new Vector3f(1, 1, 1), new Vector3f());
                display.addChild(coll);
            }
            if (spawn) {
                display.spawnDisplay();
                if (coll != null) coll.spawnEntity();
            }
            handle.addChild(display);
        }

        return handle;

    }

    /** Full control — no default for {@code blocksToIgnore} or {@code removeBlockMaterial}. BlocksToIgnore defaults to an empty list, and empty blocksToIgnore defaults to not removing blocks. */
    public static PositionObject assembleOutOfBlocks(World world, Vector origenLocation, List<Vector> blocks, boolean addCollision, boolean spawn) {
        return assembleOutOfBlocks(world, origenLocation, blocks, List.of(), addCollision, false, Material.AIR, spawn);
    }

    /** Bare essentials — no collision, no block removal, blocksToIgnore defaults to empty. */
    public static PositionObject assembleOutOfBlocks(World world, Vector origenLocation, List<Vector> blocks, boolean spawn) {
        return assembleOutOfBlocks(world, origenLocation, blocks, false, spawn);
    }

    /** Area-based: generates a block list from two corner vectors, then delegates to the full overload. */
    public static PositionObject assembleOutOfBlocks(World world, Vector corner1, Vector corner2, List<Material> blocksToIgnore, boolean addCollision, boolean removeBlocks, Material removeBlockMaterial, boolean spawn) {
        int minX = Math.min(corner1.getBlockX(), corner2.getBlockX());
        int minY = Math.min(corner1.getBlockY(), corner2.getBlockY());
        int minZ = Math.min(corner1.getBlockZ(), corner2.getBlockZ());
        int maxX = Math.max(corner1.getBlockX(), corner2.getBlockX());
        int maxY = Math.max(corner1.getBlockY(), corner2.getBlockY());
        int maxZ = Math.max(corner1.getBlockZ(), corner2.getBlockZ());

        List<Vector> blocks = new ArrayList<>();
        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    blocks.add(new BlockVector(x, y, z));
                }
            }
        }

        return assembleOutOfBlocks(world, corner1, blocks, blocksToIgnore, addCollision, removeBlocks, removeBlockMaterial, spawn);
    }

    /** Area-based minimal — no collision, no block removal, no ignore list, just build and (optionally) spawn. */
    public static PositionObject assembleOutOfBlocks(World world, Vector corner1, Vector corner2, boolean spawn) {
        return assembleOutOfBlocks(world, corner1, corner2, List.of(), false, false, Material.AIR, spawn);
    }

    public static void disassembleOutOfObject(PositionObject obj) {
        List<IDisplayable> list = obj.getChildren();
        for (IDisplayable i : list) {
            if (!(i instanceof BlockDisplayObject pos)) continue;
            Location positionToPlace = pos.getLocation().add(Vector.fromJOML(pos.getFinalTransform().getTranslation()));
            positionToPlace.getBlock().setBlockData(pos.getBlock());
        }
    }


    private static class BlockVector extends Vector {
        BlockVector(int x, int y, int z) { super(x, y, z); }
    }

    private static BlockDisplayObject turnIntoDisplay(Block block) {
        return BlockDisplayObject.create(block.getLocation(), new Vector3f(1, 1, 1), new Vector3f(), block.getBlockData());
    }

}
