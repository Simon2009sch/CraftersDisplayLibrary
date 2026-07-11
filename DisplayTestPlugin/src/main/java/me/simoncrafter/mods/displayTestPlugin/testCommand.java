package me.simoncrafter.mods.displayTestPlugin;

import me.simoncrafter.CraftersDisplayLibrary.builder.StructureBuilder;
import me.simoncrafter.CraftersDisplayLibrary.core.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.core.PropertyLock;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.display.filledwireframecube.FilledWireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.line.LineColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.CubeEdge;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.CubeFace;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.wireframecube.WireframeCubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.TextDisplay;
import me.simoncrafter.CraftersDisplayLibrary.display.panel.BlockDisplayObject;
import me.simoncrafter.CraftersDisplayLibrary.entity.ShulkerBasedCollisionBox;
import me.simoncrafter.CraftersDisplayLibrary.animation.AnimationFactory;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.IColorableDisplay;
import me.simoncrafter.CraftersDisplayLibrary.core.interfaces.ICuboidDisplay;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.BlockHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.HighlightDisplayType;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.IHighlighterFunction;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.PulsingColorHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.PingHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.RainbowHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.GlowingHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.highlighter.prefabs.ScalingPulseHighlighter;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.ViewTinter;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.IViewTinterFunction;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs.ColorShiftTinter;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs.FadeInTinter;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs.FadeOutTinter;
import me.simoncrafter.CraftersDisplayLibrary.effect.viewtinter.prefabs.PulsingTinter;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.util.Transformation;import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testCommand implements CommandExecutor, TabExecutor {

    Map<String, PositionObject> objectMap = new HashMap<>();
    Map<String, DisplayTypeHandler> typeHandlers = new HashMap<>();

    public testCommand() {
        registerTypeHandlers();
    }

    private void registerTypeHandlers() {
        typeHandlers.put("color", new ColorDisplayHandler());
        typeHandlers.put("cube", new CubeDisplayHandler());
        typeHandlers.put("line", new LineDisplayHandler());
        typeHandlers.put("wireframe", new WireframeDisplayHandler());
        typeHandlers.put("filledwireframe", new FilledWireframeDisplayHandler());
        typeHandlers.put("text", new TextDisplayHandler());
        typeHandlers.put("blockdisplay", new BlockDisplayHandler());
        typeHandlers.put("collision", new CollisionBoxHandler());
        typeHandlers.put("structure", new StructureHandler());
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 1) {
            sender.sendMessage(Component.text("Usage: /test <create|edit|remove|spawn|despawn> <id> [type] [properties...]", NamedTextColor.RED));
            return false;
        }

        Location loc = getCommandLocation(sender);
        String action = args[0].toLowerCase();

        switch (action) {
            case "create" -> handleCreate(sender, args, loc);
            case "edit" -> handleEdit(sender, args);
            case "remove" -> handleRemove(sender, args);
            case "spawn" -> handleSpawn(sender, args);
            case "despawn" -> handleDespawn(sender, args);
            case "highlight" -> handleHighlight(sender, args);
            case "clearhighlights" -> handleClearHighlights(sender, args);
            case "tint" -> handleTint(sender, args);
            case "cleartints" -> handleClearTints(sender, args);
            case "setanimation" -> handleSetAnimation(sender, args);
            case "addcollision" -> handleAddCollision(sender, args);
            case "addchild" -> handleAddChild(sender, args);
            case "assemble" -> handleAssemble(sender, args);
            case "disassemble" -> handleDisassemble(sender, args);
            case "propertylock" -> handlePropertyLock(sender, args);
            default -> sender.sendMessage(Component.text("Unknown action: " + action, NamedTextColor.RED));
        }

        return true;
    }

    private void handleCreate(CommandSender sender, String[] args, Location loc) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /test create <id> <type>", NamedTextColor.RED));
            return;
        }

        String id = args[1];
        String type = args[2].toLowerCase();

        if (objectMap.containsKey(id)) {
            sender.sendMessage(Component.text("\"" + id + "\" already exists!", NamedTextColor.RED));
            return;
        }

        DisplayTypeHandler handler = typeHandlers.get(type);
        if (handler == null) {
            sender.sendMessage(Component.text("Unknown type: " + type, NamedTextColor.RED));
            return;
        }

        PositionObject obj = handler.create(loc);
        if (obj != null) {
            objectMap.put(id, obj);
            sender.sendMessage(Component.text("Created " + type + " with id \"" + id + "\"", NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Failed to create " + type, NamedTextColor.RED));
        }
    }

    private void handleEdit(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /test edit <id> <property> <value> [absolute|relative] [duration] [interpolation]", NamedTextColor.RED));
            return;
        }

        String id = args[1];
        if (!objectMap.containsKey(id)) {
            sender.sendMessage(Component.text("\"" + id + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        PositionObject obj = objectMap.get(id);
        String property = args[2].toLowerCase();
        String[] values = Arrays.copyOfRange(args, 3, args.length);

        boolean success = false;
        for (DisplayTypeHandler handler : typeHandlers.values()) {
            if (handler.canHandle(obj)) {
                success = handler.edit(obj, property, values, sender);
                break;
            }
        }

        if (!success) {
            sender.sendMessage(Component.text("Failed to edit property: " + property, NamedTextColor.RED));
        }
    }

    private void handleRemove(CommandSender sender, String[] args) {
        String id = args[1];
        if (!objectMap.containsKey(id)) {
            sender.sendMessage(Component.text("\"" + id + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        PositionObject obj = objectMap.remove(id);
        obj.remove();
        sender.sendMessage(Component.text("Removed \"" + id + "\"", NamedTextColor.GREEN));
    }

    private void handleSpawn(CommandSender sender, String[] args) {
        String id = args[1];
        if (!objectMap.containsKey(id)) {
            sender.sendMessage(Component.text("\"" + id + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        PositionObject obj = objectMap.get(id);
        boolean success = false;
        for (DisplayTypeHandler handler : typeHandlers.values()) {
            if (handler.canHandle(obj)) {
                success = handler.spawn(obj, sender);
                break;
            }
        }

        if (!success) {
            sender.sendMessage(Component.text("Failed to spawn \"" + id + "\"", NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.text("Spawned \"" + id + "\"", NamedTextColor.GREEN));
        }
    }

    private void handleDespawn(CommandSender sender, String[] args) {
        String id = args[1];
        if (!objectMap.containsKey(id)) {
            sender.sendMessage(Component.text("\"" + id + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        PositionObject obj = objectMap.get(id);
        boolean success = false;
        for (DisplayTypeHandler handler : typeHandlers.values()) {
            if (handler.canHandle(obj)) {
                success = handler.despawn(obj, sender);
                break;
            }
        }

        if (!success) {
            sender.sendMessage(Component.text("Failed to despawn \"" + id + "\"", NamedTextColor.RED));
        } else {
            sender.sendMessage(Component.text("Despawned \"" + id + "\"", NamedTextColor.GREEN));
        }
    }

    private void handleAddCollision(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /test addcollision <parentId> [scale] [translation]", NamedTextColor.RED));
            return;
        }

        String parentId = args[1];
        PositionObject parent = objectMap.get(parentId);
        if (parent == null) {
            sender.sendMessage(Component.text("\"" + parentId + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        String childId = parentId + "/colision";
        if (objectMap.containsKey(childId)) {
            sender.sendMessage(Component.text("\"" + childId + "\" already exists!", NamedTextColor.RED));
            return;
        }

        Vector3f scale = new Vector3f(1, 1, 1);
        if (args.length > 2) {
            Vector3f parsed = parseVector(args[2]);
            if (parsed == null) {
                sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                return;
            }
            scale = parsed;
        }

        Vector3f translation = new Vector3f(0, 0, 0);
        if (args.length > 3) {
            Vector3f parsed = parseVector(args[3]);
            if (parsed == null) {
                sender.sendMessage(Component.text("Invalid translation format. Use x,y,z", NamedTextColor.RED));
                return;
            }
            translation = parsed;
        }

        ShulkerBasedCollisionBox collisionBox = ShulkerBasedCollisionBox.create(parent.getLocation(), scale, translation);
        objectMap.put(childId, collisionBox);
        parent.addChild(collisionBox);

        sender.sendMessage(Component.text("Added collision box \"" + childId + "\" as a child of \"" + parentId + "\" - use /test spawn " + childId + " to spawn it", NamedTextColor.GREEN));
    }

    private void handleAddChild(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /test addchild <parentId> <childId>", NamedTextColor.RED));
            return;
        }

        String parentId = args[1];
        String childId = args[2];

        if (parentId.equals(childId)) {
            sender.sendMessage(Component.text("An object cannot be its own child!", NamedTextColor.RED));
            return;
        }

        PositionObject parent = objectMap.get(parentId);
        if (parent == null) {
            sender.sendMessage(Component.text("\"" + parentId + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        PositionObject child = objectMap.get(childId);
        if (child == null) {
            sender.sendMessage(Component.text("\"" + childId + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        parent.addChild(child);
        sender.sendMessage(Component.text("Added \"" + childId + "\" as a child of \"" + parentId + "\"", NamedTextColor.GREEN));
    }

    private void handleAssemble(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return;
        }

        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /test assemble [id] <startX,startY,startZ> <endX,endY,endZ> [spawn] [removeblocks] [addcollision]", NamedTextColor.RED));
            return;
        }

        String id;
        int startArgIndex;
        Vector3f startVec = parseVector(args[1]);

        if (startVec != null) {
            id = "structure_" + System.currentTimeMillis();
            startArgIndex = 1;
        } else {
            id = args[1];
            startArgIndex = 2;
        }

        if (objectMap.containsKey(id)) {
            sender.sendMessage(Component.text("\"" + id + "\" already exists!", NamedTextColor.RED));
            return;
        }

        if (startVec == null) {
            startVec = parseVector(args[startArgIndex]);
        }
        if (startVec == null) {
            sender.sendMessage(Component.text("Invalid start vector format. Use x,y,z", NamedTextColor.RED));
            return;
        }

        int endArgIndex = startArgIndex + 1;
        if (endArgIndex >= args.length) {
            sender.sendMessage(Component.text("Missing end vector. Usage: /test assemble [id] <startX,startY,startZ> <endX,endY,endZ> [spawn] [removeblocks] [addcollision]", NamedTextColor.RED));
            return;
        }

        Vector3f endVec = parseVector(args[endArgIndex]);
        if (endVec == null) {
            sender.sendMessage(Component.text("Invalid end vector format. Use x,y,z", NamedTextColor.RED));
            return;
        }

        boolean spawn = false;
        boolean removeBlocks = false;
        boolean addCollision = false;

        for (int i = endArgIndex + 1; i < args.length; i++) {
            switch (args[i].toLowerCase()) {
                case "spawn" -> spawn = true;
                case "removeblocks" -> removeBlocks = true;
                case "addcollision" -> addCollision = true;
            }
        }

        World world = player.getWorld();
        Vector corner1 = new Vector(startVec.x, startVec.y, startVec.z);
        Vector corner2 = new Vector(endVec.x, endVec.y, endVec.z);

        PositionObject structure = StructureBuilder.assembleOutOfBlocks(
                world, corner1.add(new Vector(0.5f, 0.5f, 0.5f)), corner1, corner2,
                List.of(Material.AIR, Material.WATER, Material.LAVA),              // blocksToIgnore — none by default
                addCollision,
                removeBlocks,
                Material.AIR,            // removeBlockMaterial
                spawn
        );
        objectMap.put(id, structure);

        int blockCount = structure.getChildren().size();
        sender.sendMessage(Component.text("Assembled structure \"" + id + "\" from " + blockCount + " blocks"
                + (spawn ? " (spawned)" : " (not spawned — use /test spawn " + id + ")")
                + (removeBlocks ? " (blocks removed)" : "")
                + (addCollision ? " (collision added)" : ""), NamedTextColor.GREEN));
    }

    private void handleDisassemble(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /test disassemble <id>", NamedTextColor.RED));
            return;
        }

        String id = args[1];
        PositionObject obj = objectMap.get(id);
        if (obj == null) {
            sender.sendMessage(Component.text("\"" + id + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        try {
            int blockCount = obj.getChildren().size();
            StructureBuilder.disassembleOutOfObject(obj);
            obj.remove();
            sender.sendMessage(Component.text("Disassembled \"" + id + "\" (" + blockCount + " blocks placed in world)", NamedTextColor.GREEN));
        } catch (Exception e) {
            sender.sendMessage(Component.text("Error disassembling structure: " + e.getMessage(), NamedTextColor.RED));
        }
    }

    private static final List<String> PROPERTY_LOCK_NAMES = List.of(
            "leftxrot", "leftyrot", "leftzrot", "rightxrot", "rightyrot", "rightzrot",
            "xscale", "yscale", "zscale", "xtranslation", "ytranslation", "ztranslation");

    private static final List<String> ROTATION_LOCK_NAMES = List.of(
            "leftxrot", "leftyrot", "leftzrot", "rightxrot", "rightyrot", "rightzrot");

    private void handlePropertyLock(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(Component.text("Usage: /test propertylock <id> <none|all|prop1,prop2,...> [self|recursive|children]", NamedTextColor.RED));
            return;
        }

        String id = args[1];
        PositionObject obj = objectMap.get(id);
        if (obj == null) {
            sender.sendMessage(Component.text("\"" + id + "\" doesn't exist!", NamedTextColor.RED));
            return;
        }

        PropertyLock lock = parsePropertyLock(args[2]);
        if (lock == null) {
            sender.sendMessage(Component.text("Invalid property list. Use \"none\", \"all\", \"rotation\" (locks all rotation axes), or a comma-separated list of: "
                    + String.join(",", PROPERTY_LOCK_NAMES), NamedTextColor.RED));
            return;
        }

        String target = args.length > 3 ? args[3].toLowerCase() : "self";
        String targetInfo;
        switch (target) {
            case "recursive" -> {
                obj.setPropertyLockRecursive(lock);
                targetInfo = " and all its descendants";
            }
            case "children" -> {
                obj.setChildrenPropertyLockRecursive(lock);
                targetInfo = "'s descendants only (not itself)";
            }
            case "self" -> {
                obj.setPropertyLock(lock);
                targetInfo = "";
            }
            default -> {
                sender.sendMessage(Component.text("Invalid target: " + target + ". Use \"self\", \"recursive\", or \"children\"", NamedTextColor.RED));
                return;
            }
        }

        sender.sendMessage(Component.text("Set property lock on \"" + id + "\"" + targetInfo
                + " (" + args[2].toLowerCase() + ")", NamedTextColor.GREEN));
    }

    private PropertyLock parsePropertyLock(String arg) {
        if (arg.equalsIgnoreCase("none")) {
            return new PropertyLock(false, false, false, false, false, false, false, false, false, false, false, false);
        }
        if (arg.equalsIgnoreCase("all")) {
            return new PropertyLock(true, true, true, true, true, true, true, true, true, true, true, true);
        }

        Set<String> locked = new HashSet<>();
        for (String part : arg.split(",")) {
            String name = part.trim().toLowerCase();
            if (name.equals("rotation")) {
                locked.addAll(ROTATION_LOCK_NAMES);
                continue;
            }
            if (!PROPERTY_LOCK_NAMES.contains(name)) {
                return null;
            }
            locked.add(name);
        }

        return new PropertyLock(
                locked.contains("leftxrot"), locked.contains("leftyrot"), locked.contains("leftzrot"),
                locked.contains("rightxrot"), locked.contains("rightyrot"), locked.contains("rightzrot"),
                locked.contains("xscale"), locked.contains("yscale"), locked.contains("zscale"),
                locked.contains("xtranslation"), locked.contains("ytranslation"), locked.contains("ztranslation")
        );
    }

    private void handleHighlight(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return;
        }

        Block targetBlock = player.getTargetBlockExact(5);
        if (targetBlock == null || targetBlock.isEmpty()) {
            sender.sendMessage(Component.text("No block targeted (must be within 5 blocks)", NamedTextColor.RED));
            return;
        }

        Color color = Color.WHITE;
        boolean seeThrough = false;
        IHighlighterFunction<ICuboidDisplay> animation = null;
        int lifeTime = -1;
        HighlightDisplayType shape = HighlightDisplayType.CUBE;

        if (args.length > 1) {
            String colorArg = args[1].toLowerCase();
            color = parseColor(colorArg);
            if (color == null) {
                sender.sendMessage(Component.text("Invalid color: " + colorArg + " (use preset names or r,g,b or r,g,b,a)", NamedTextColor.RED));
                return;
            }
        }

        if (args.length > 2) {
            seeThrough = args[2].equalsIgnoreCase("true") || args[2].equalsIgnoreCase("yes");
        }

        if (args.length > 3) {
            String animationName = args[3].toLowerCase();
            if (!animationName.equals("none")) {
                animation = createAnimation(animationName, color);
                if (animation == null) {
                    sender.sendMessage(Component.text("Unknown animation: " + animationName, NamedTextColor.RED));
                    return;
                }
            }
        }

        if (args.length > 4) {
            try {
                lifeTime = Integer.parseInt(args[4]);
            } catch (NumberFormatException e) {
                sender.sendMessage(Component.text("Invalid lifetime: " + args[4], NamedTextColor.RED));
                return;
            }
        }

        if (args.length > 5) {
            shape = switch (args[5].toLowerCase()) {
                case "wireframe" -> HighlightDisplayType.WIREFRAME;
                case "filledwireframe" -> HighlightDisplayType.FILLED_WIREFRAME;
                default -> HighlightDisplayType.CUBE;
            };
        }

        targetBlock.setType(targetBlock.getType());
        ICuboidDisplay display;
        if (animation != null) {
            // The animation's own onAnimationRestart fires on the very first tick after
            // registration and already carries this same `color` (baked into the prefab's
            // constructor by createAnimation), so no separate setColor is needed here.
            display = lifeTime > 0
                    ? BlockHighlighter.highlightBlock(targetBlock, shape, animation, lifeTime)
                    : BlockHighlighter.highlightBlock(targetBlock, shape, animation);
        } else {
            display = lifeTime > 0
                    ? BlockHighlighter.highlightBlock(targetBlock, shape, color, lifeTime)
                    : BlockHighlighter.highlightBlock(targetBlock, shape, color);
        }

        if (display != null && seeThrough) {
            display.setSeeThrough(true);
        }

        String highlightInfo = "Color: " + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + " | Shape: " + shape.name();
        if (animation != null) {
            highlightInfo += " | Animation: " + args[3];
        }
        if (lifeTime > 0) {
            highlightInfo += " | Lifetime: " + lifeTime + " ticks";
        }
        if (seeThrough) {
            highlightInfo += " | See-through: yes";
        }
        sender.sendMessage(Component.text("Highlighting block at " + targetBlock.getX() + "," + targetBlock.getY() + "," + targetBlock.getZ() + " - " + highlightInfo, NamedTextColor.GREEN));
    }

    private void handleClearHighlights(CommandSender sender, String[] args) {
        BlockHighlighter.unhighlightAllBlocks();
        sender.sendMessage(Component.text("Cleared all block highlights", NamedTextColor.GREEN));
    }

    private void handleTint(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return;
        }

        Color color = Color.WHITE;
        IViewTinterFunction tintAnimation = null;

        if (args.length > 1) {
            String colorArg = args[1].toLowerCase();
            color = parseColor(colorArg);
            if (color == null) {
                sender.sendMessage(Component.text("Invalid color: " + colorArg + " (use preset names or r,g,b or r,g,b,a)", NamedTextColor.RED));
                return;
            }
        }

        if (args.length > 2) {
            String animationName = args[2].toLowerCase();
            if (!animationName.equals("none")) {
                tintAnimation = createTintAnimation(animationName, color);
                if (tintAnimation == null) {
                    sender.sendMessage(Component.text("Unknown tint animation: " + animationName, NamedTextColor.RED));
                    return;
                }
            }
        }

        // No more separate "duration" arg: each tint animation now carries its own inherent
        // cycle duration (see IViewTinterFunction#getInherentCycleDuration()), so the old
        // duration-passed-twice parameter that used to be threaded through here is gone.
        if (tintAnimation != null) {
            ViewTinter.tintPlayer(player, color, tintAnimation);
        } else {
            ViewTinter.tintPlayer(player, color);
        }
        String tintInfo = "Color: " + color.getRed() + "," + color.getGreen() + "," + color.getBlue();
        if (tintAnimation != null) {
            tintInfo += " | Animation: " + args[2];
        }
        sender.sendMessage(Component.text("Applied view tint - " + tintInfo, NamedTextColor.GREEN));
    }

    private void handleClearTints(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return;
        }

        ViewTinter.untintPlayer(player);
        sender.sendMessage(Component.text("Cleared view tint", NamedTextColor.GREEN));
    }

    private void handleSetAnimation(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("This command can only be used by players", NamedTextColor.RED));
            return;
        }

        if (!ViewTinter.isPlayerTinted(player)) {
            sender.sendMessage(Component.text("Player is not tinted", NamedTextColor.RED));
            return;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /cdl setanimation <animation>", NamedTextColor.RED));
            return;
        }

        String animationName = args[1].toLowerCase();

        IViewTinterFunction animation = createTintAnimation(animationName, Color.WHITE);
        if (animation == null && !animationName.equals("none")) {
            sender.sendMessage(Component.text("Unknown animation: " + animationName, NamedTextColor.RED));
            return;
        }

        // No more separate "duration" arg: the animation's own inherent cycle duration
        // (see IViewTinterFunction#getInherentCycleDuration()) now drives its cadence.
        ViewTinter.setPlayerAnimation(player, animation);
        String animInfo = "Animation: " + animationName;
        if (animation != null) {
            animInfo += " | Type: " + (animation.isRepeating() ? "Repeating" : "Transition");
        }
        sender.sendMessage(Component.text("Updated animation - " + animInfo, NamedTextColor.GREEN));
    }

    private IViewTinterFunction createTintAnimation(String animationName, Color color) {
        return switch (animationName) {
            case "fadein" -> new FadeInTinter(color, 20);
            case "fadeout" -> new FadeOutTinter(color, 20);
            case "pulse" -> new PulsingTinter(color, 20);
            case "colorshift" -> new ColorShiftTinter(color, Color.WHITE, 20);
            case "none" -> null;
            default -> null;
        };
    }

    private Color parseColor(String colorStr) {
        if (colorStr.equals("white")) return Color.WHITE;
        if (colorStr.equals("red")) return Color.RED;
        if (colorStr.equals("green")) return Color.GREEN;
        if (colorStr.equals("blue")) return Color.BLUE;
        if (colorStr.equals("yellow")) return Color.YELLOW;
        if (colorStr.equals("cyan")) return Color.AQUA;
        if (colorStr.equals("magenta")) return Color.FUCHSIA;
        if (colorStr.equals("orange")) return Color.fromRGB(255, 165, 0);
        if (colorStr.equals("purple")) return Color.PURPLE;
        if (colorStr.equals("lime")) return Color.LIME;
        if (colorStr.equals("pink")) return Color.fromRGB(255, 192, 203);
        if (colorStr.equals("gray")) return Color.GRAY;
        if (colorStr.equals("black")) return Color.BLACK;

        if (colorStr.contains(",")) {
            int[] rgba = parseRGBA(colorStr);
            if (rgba != null) {
                if (rgba.length == 4) {
                    return Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]);
                } else if (rgba.length == 3) {
                    return Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                }
            }
        }

        return null;
    }

    private IHighlighterFunction<ICuboidDisplay> createAnimation(String animationName, Color color) {
        return switch (animationName) {
            case "pulse" -> new PulsingColorHighlighter(color, 20);
            case "rainbow" -> new RainbowHighlighter();
            case "glow" -> new GlowingHighlighter(color, 20);
            case "scale" -> new ScalingPulseHighlighter(0.8f, 1.2f, 20);
            case "ping" -> new PingHighlighter(0.01f, 2, color, 5);
            case "none" -> null;
            default -> null;
        };
    }

    private Location getCommandLocation(CommandSender sender) {
        Location loc;
        if (sender instanceof Player player) {
            loc = player.getLocation().clone();
        } else if (sender instanceof BlockCommandSender block) {
            loc = block.getBlock().getLocation().clone().add(0, 5, 0);
        } else {
            loc = new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
        }

        loc.setPitch(0);
        loc.setYaw(0);
        return loc;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (args.length == 1) {
            return recommendListThatContainsObject(List.of("create", "edit", "remove", "spawn", "despawn", "highlight", "clearhighlights", "tint", "cleartints", "setanimation", "addcollision", "addchild", "assemble", "disassemble", "propertylock"), args[0]);
        }

        if (args[0].equalsIgnoreCase("create") && args.length == 3) {
            return recommendListThatContainsObject(typeHandlers.keySet(), args[2]);
        }

        if ((args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("despawn") || args[0].equalsIgnoreCase("disassemble")) && args.length == 2) {
            return recommendListThatContainsObject(objectMap.keySet(), args[1]);
        }

        if ((args[0].equalsIgnoreCase("addcollision") || args[0].equalsIgnoreCase("addchild")) && args.length == 2) {
            return recommendListThatContainsObject(objectMap.keySet(), args[1]);
        }

        if (args[0].equalsIgnoreCase("propertylock") && args.length == 2) {
            return recommendListThatContainsObject(objectMap.keySet(), args[1]);
        }

        if (args[0].equalsIgnoreCase("propertylock") && args.length == 3) {
            List<String> options = new ArrayList<>(List.of("none", "all", "rotation"));
            options.addAll(PROPERTY_LOCK_NAMES);
            return recommendListThatContainsObject(options, args[2]);
        }

        if (args[0].equalsIgnoreCase("propertylock") && args.length == 4) {
            return recommendListThatContainsObject(List.of("self", "recursive", "children"), args[3]);
        }

        if (args[0].equalsIgnoreCase("assemble") && args.length >= 4) {
            return recommendListThatContainsObject(List.of("spawn", "removeblocks", "addcollision"), args[args.length - 1]);
        }

        if (args[0].equalsIgnoreCase("addchild") && args.length == 3) {
            return recommendListThatContainsObject(objectMap.keySet(), args[2]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 3) {
            return recommendListThatContainsObject(List.of("position", "rotation", "rrotation", "scale", "color", "randomcolor", "direction", "thickness", "seethrough", "startpoint", "endpoint",
                    "facecolor", "edgecolor", "facescolor", "edgescolor", "faceedgecolor", "facesseethrough", "edgesseethrough",
                    "text", "billboard", "background", "backgroundcolor", "linewidth", "block"), args[2]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 4 && args[2].equalsIgnoreCase("billboard")) {
            return recommendListThatContainsObject(List.of("fixed", "vertical", "horizontal", "center"), args[3]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 4 && (args[2].equalsIgnoreCase("facecolor") || args[2].equalsIgnoreCase("faceedgecolor"))) {
            return recommendListThatContainsObject(List.of("top", "bottom", "front", "back", "left", "right"), args[3]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 4 && args[2].equalsIgnoreCase("edgecolor")) {
            return recommendListThatContainsObject(List.of(
                    "topFront", "topBack", "topLeft", "topRight",
                    "bottomFront", "bottomBack", "bottomLeft", "bottomRight",
                    "frontLeft", "frontRight", "backLeft", "backRight"), args[3]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 5) {
            return recommendListThatContainsObject(List.of("absolute", "relative"), args[4]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 7) {
            return recommendListThatContainsObject(List.of("linear", "smooth"), args[6]);
        }

        if (args[0].equalsIgnoreCase("highlight") && args.length == 2) {
            return recommendListThatContainsObject(List.of("white", "red", "green", "blue", "yellow", "cyan", "magenta", "orange", "purple", "lime", "pink", "gray", "black"), args[1]);
        }

        if (args[0].equalsIgnoreCase("highlight") && args.length == 3) {
            return recommendListThatContainsObject(List.of("true", "false"), args[2]);
        }

        if (args[0].equalsIgnoreCase("highlight") && args.length == 4) {
            return recommendListThatContainsObject(List.of("none", "pulse", "rainbow", "glow", "scale", "ping"), args[3]);
        }

        if (args[0].equalsIgnoreCase("highlight") && args.length == 6) {
            return recommendListThatContainsObject(List.of("cube", "wireframe", "filledwireframe"), args[5]);
        }

        if (args[0].equalsIgnoreCase("tint") && args.length == 2) {
            return recommendListThatContainsObject(List.of("white", "red", "green", "blue", "yellow", "cyan", "magenta", "orange", "purple", "lime", "pink", "gray", "black"), args[1]);
        }

        if (args[0].equalsIgnoreCase("tint") && args.length == 3) {
            return recommendListThatContainsObject(List.of("none", "fadein", "fadeout", "pulse", "colorshift"), args[2]);
        }

        if (args[0].equalsIgnoreCase("setanimation") && args.length == 2) {
            return recommendListThatContainsObject(List.of("none", "fadein", "fadeout", "pulse", "colorshift"), args[1]);
        }

        return List.of();
    }

    private List<String> recommendListThatContainsObject(Collection<String> list, String input) {
        List<String> out = new ArrayList<>();
        for (String s : list) {
            if (s.startsWith(input.toLowerCase())) {
                out.add(s);
            }
        }
        return out;
    }

    private int parseDuration(String[] values) {
        if (values.length < 2) return 0;
        try {
            String lastArg = values[values.length - 1].toLowerCase();
            if (lastArg.equals("linear") || lastArg.equals("smooth")) {
                return Integer.parseInt(values[values.length - 2]);
            }
            return Integer.parseInt(values[values.length - 1]);
        } catch (NumberFormatException e) {
            return 0;
        }
    }

    private boolean isAbsolute(String[] values) {
        if (values.length < 2) return true;
        String mode = values[values.length - 2].toLowerCase();
        if (mode.equals("linear") || mode.equals("smooth")) {
            mode = values.length >= 3 ? values[values.length - 3].toLowerCase() : "absolute";
        }
        return mode.equals("absolute");
    }

    private boolean isRelative(String[] values) {
        if (values.length < 2) return false;
        String mode = values[values.length - 2].toLowerCase();
        if (mode.equals("linear") || mode.equals("smooth")) {
            mode = values.length >= 3 ? values[values.length - 3].toLowerCase() : "";
        }
        return mode.equals("relative");
    }

    private boolean hasLinearInterpolation(String[] values) {
        if (values.length < 1) return false;
        return values[values.length - 1].toLowerCase().equals("linear");
    }

    private boolean hasSmoothInterpolation(String[] values) {
        if (values.length < 1) return false;
        return values[values.length - 1].toLowerCase().equals("smooth");
    }

    private String getInterpolationMode(String[] values) {
        if (values.length < 1) return "linear";
        String last = values[values.length - 1].toLowerCase();
        if (last.equals("smooth") || last.equals("linear")) {
            return last;
        }
        return "linear";
    }

    private Color randomColor() {
        Random rand = new Random();
        return Color.fromRGB(rand.nextInt(256), rand.nextInt(256), rand.nextInt(256));
    }

    private Vector3f parseVector(String args) {
        Pattern pattern = Pattern.compile("^(?<x>(-)?\\d+(\\.\\d+)?),(?<y>(-)?\\d+(\\.\\d+)?),(?<z>(-)?\\d+(\\.\\d+)?)$");
        Matcher matcher = pattern.matcher(args);
        if (matcher.find()) {
            return new Vector3f(Float.parseFloat(matcher.group("x")), Float.parseFloat(matcher.group("y")), Float.parseFloat(matcher.group("z")));
        }
        return null;
    }

    private Quaternionf parseQuaternion(String args) {
        Pattern pattern = Pattern.compile("^(?<x>(-)?\\d+(\\.\\d+)?),(?<y>(-)?\\d+(\\.\\d+)?),(?<z>(-)?\\d+(\\.\\d+)?),(?<w>(-)?\\d+(\\.\\d+)?)$");
        Matcher matcher = pattern.matcher(args);
        if (matcher.find()) {
            return new Quaternionf(Float.parseFloat(matcher.group("x")), Float.parseFloat(matcher.group("y")), Float.parseFloat(matcher.group("z")), Float.parseFloat(matcher.group("w")));
        }
        return null;
    }

    private int[] parseRGB(String args) {
        Pattern pattern = Pattern.compile("^(?<r>\\d+),(?<g>\\d+),(?<b>\\d+)$");
        Matcher matcher = pattern.matcher(args);
        if (matcher.find()) {
            int r = Integer.parseInt(matcher.group("r"));
            int g = Integer.parseInt(matcher.group("g"));
            int b = Integer.parseInt(matcher.group("b"));

            if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                return new int[]{r, g, b};
            }
        }
        return null;
    }

    private int[] parseRGBA(String args) {
        Pattern rgbaPattern = Pattern.compile("^(?<r>\\d+),(?<g>\\d+),(?<b>\\d+),(?<a>\\d+)$");
        Matcher rgbaMatcher = rgbaPattern.matcher(args);
        if (rgbaMatcher.find()) {
            int r = Integer.parseInt(rgbaMatcher.group("r"));
            int g = Integer.parseInt(rgbaMatcher.group("g"));
            int b = Integer.parseInt(rgbaMatcher.group("b"));
            int a = Integer.parseInt(rgbaMatcher.group("a"));

            if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255 && a >= 0 && a <= 255) {
                return new int[]{r, g, b, a};
            }
        }

        Pattern rgbPattern = Pattern.compile("^(?<r>\\d+),(?<g>\\d+),(?<b>\\d+)$");
        Matcher rgbMatcher = rgbPattern.matcher(args);
        if (rgbMatcher.find()) {
            int r = Integer.parseInt(rgbMatcher.group("r"));
            int g = Integer.parseInt(rgbMatcher.group("g"));
            int b = Integer.parseInt(rgbMatcher.group("b"));

            if (r >= 0 && r <= 255 && g >= 0 && g <= 255 && b >= 0 && b <= 255) {
                return new int[]{r, g, b};
            }
        }

        return null;
    }

    interface DisplayTypeHandler {
        PositionObject create(Location loc);

        boolean canHandle(PositionObject obj);

        boolean edit(PositionObject obj, String property, String[] values, CommandSender sender);

        boolean spawn(PositionObject obj, CommandSender sender);

        boolean despawn(PositionObject obj, CommandSender sender);
    }

    class ColorDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return ColorDisplay.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf(), Color.WHITE);
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof ColorDisplay;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            ColorDisplay display = (ColorDisplay) obj;
            try {
                display.spawnDisplay();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            ColorDisplay display = (ColorDisplay) obj;
            try {
                display.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            ColorDisplay display = (ColorDisplay) obj;

            return switch (property) {
                case "color" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Color currentColor = display instanceof IColorableDisplay colorable ?
                            Color.fromARGB(255, 255, 255, 255) : Color.WHITE;
                        AnimationFactory.registerColorAnimationSmooth(display, duration, currentColor, targetColor);
                    } else {
                        display.setColor(targetColor);
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = pos;
                        if (isRelative(values)) {
                            targetPos = new Vector3f(currentPos).add(pos);
                        }
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = display.getLocalTransform().getScale();
                        Vector3f targetScale = scale;
                        if (isRelative(values)) {
                            targetScale = new Vector3f(currentScale).add(scale);
                        }
                        AnimationFactory.registerScalingAnimationSmooth(display, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            display.scaleRelative(scale, duration);
                        } else {
                            display.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = rot;
                        if (isRelative(values)) {
                            targetRot = new Quaternionf(currentRot).mul(rot);
                        }
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = rot;
                        if (isRelative(values)) {
                            targetRot = new Quaternionf(currentRot).mul(rot);
                        }
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "seethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> seethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set see-through to " + seeThrough, NamedTextColor.GREEN));
                    yield true;
                }
                case "randomcolor" -> {
                    Color randomCol = randomColor();
                    display.setColor(randomCol);
                    sender.sendMessage(Component.text("Set color to random RGB(" + randomCol.getRed() + "," + randomCol.getGreen() + "," + randomCol.getBlue() + ")", NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class CubeDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return CubeColorDisplay.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf(), new CubeColorInformation(Color.WHITE), false);
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof CubeColorDisplay;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            CubeColorDisplay display = (CubeColorDisplay) obj;
            try {
                display.spawnDisplay();

                /*// Add a small child cube at one corner for parenting system testing
                CubeColorDisplay childCube = CubeColorDisplay.create(
                        display.getLocation(),
                        new Vector3f(0.25f, 0.25f, 0.25f),
                        new Vector3f(0.5f, 0.5f, 0.5f),
                        new Quaternionf(),
                        new CubeColorInformation(Color.fromRGB(255, 0, 0)),
                        false
                        );
                childCube.spawnDisplay();
                objectMap.put("child", childCube);
                display.addChild(childCube);*/

                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning cube display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            CubeColorDisplay display = (CubeColorDisplay) obj;
            display.remove();
            return true;
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            CubeColorDisplay display = (CubeColorDisplay) obj;

            return switch (property) {
                case "color" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        AnimationFactory.registerColorAnimationSmooth(display, duration, Color.fromRGB(255, 255, 255), targetColor);
                    } else {
                        display.setColor(targetColor);
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = pos;
                        if (isRelative(values)) {
                            targetPos = new Vector3f(currentPos).add(pos);
                        }
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = display.getLocalTransform().getScale();
                        Vector3f targetScale = scale;
                        if (isRelative(values)) {
                            targetScale = new Vector3f(currentScale).add(scale);
                        }
                        AnimationFactory.registerScalingAnimationSmooth(display, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            display.scaleRelative(scale, duration);
                        } else {
                            display.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = rot;
                        if (isRelative(values)) {
                            targetRot = new Quaternionf(currentRot).mul(rot);
                        }
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = rot;
                        if (isRelative(values)) {
                            targetRot = new Quaternionf(currentRot).mul(rot);
                        }
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "seethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> seethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set see-through to " + seeThrough, NamedTextColor.GREEN));
                    yield true;
                }
                case "randomcolor" -> {
                    Color topColor = randomColor();
                    Color bottomColor = randomColor();
                    Color leftColor = randomColor();
                    Color rightColor = randomColor();
                    Color frontColor = randomColor();
                    Color backColor = randomColor();

                    if (display.getTop() != null) display.getTop().setColor(topColor);
                    if (display.getBottom() != null) display.getBottom().setColor(bottomColor);
                    if (display.getLeft() != null) display.getLeft().setColor(leftColor);
                    if (display.getRight() != null) display.getRight().setColor(rightColor);
                    if (display.getFront() != null) display.getFront().setColor(frontColor);
                    if (display.getBack() != null) display.getBack().setColor(backColor);

                    sender.sendMessage(Component.text("Set random colors for all cube faces", NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class LineDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return LineColorDisplay.createFromDirection(new Vector3f(0, 0, 0), new Vector3f(0, 1, 0), Color.WHITE, loc, 0.1f);
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof LineColorDisplay;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            LineColorDisplay display = (LineColorDisplay) obj;
            try {
                display.spawnDisplay();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning line display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            LineColorDisplay display = (LineColorDisplay) obj;
            try {
                display.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning line display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            LineColorDisplay display = (LineColorDisplay) obj;

            return switch (property) {
                case "color" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        AnimationFactory.registerColorAnimationSmooth(display, duration, Color.WHITE, targetColor);
                    } else {
                        display.setColor(targetColor);
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "direction" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> direction <x,y,z> [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f direction = parseVector(values[0]);
                    if (direction == null) {
                        sender.sendMessage(Component.text("Invalid direction format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    display.setDirection(direction, duration);
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated direction (length: " + String.format("%.2f", direction.length()) + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = pos;
                        if (isRelative(values)) {
                            targetPos = new Vector3f(currentPos).add(pos);
                        }
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "thickness" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> thickness <thickness> [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    try {
                        float thickness = Float.parseFloat(values[0]);
                        int duration = parseDuration(values);
                        display.setThickness(thickness, duration);
                        sender.sendMessage(Component.text("Updated thickness to " + String.format("%.2f", thickness) + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                        yield true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid thickness format. Use a number", NamedTextColor.RED));
                        yield false;
                    }
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = rot;
                        if (isRelative(values)) {
                            targetRot = new Quaternionf(currentRot).mul(rot);
                        }
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = rot;
                        if (isRelative(values)) {
                            targetRot = new Quaternionf(currentRot).mul(rot);
                        }
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "seethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> seethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set see-through to " + seeThrough, NamedTextColor.GREEN));
                    yield true;
                }
                case "randomcolor" -> {
                    Color randomCol = randomColor();
                    display.setColor(randomCol);
                    sender.sendMessage(Component.text("Set color to random RGB(" + randomCol.getRed() + "," + randomCol.getGreen() + "," + randomCol.getBlue() + ")", NamedTextColor.GREEN));
                    yield true;
                }
                case "startpoint" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> startpoint <x,y,z> [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f startPoint = parseVector(values[0]);
                    if (startPoint == null) {
                        sender.sendMessage(Component.text("Invalid start point format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    display.setStartPoint(startPoint, duration);
                    sender.sendMessage(Component.text("Updated start point to " + startPoint.x + ", " + startPoint.y + ", " + startPoint.z + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "endpoint" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> endpoint <x,y,z> [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f endPoint = parseVector(values[0]);
                    if (endPoint == null) {
                        sender.sendMessage(Component.text("Invalid end point format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    display.setEndPoint(endPoint, duration);
                    sender.sendMessage(Component.text("Updated end point to " + endPoint.x + ", " + endPoint.y + ", " + endPoint.z + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class WireframeDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return WireframeCubeColorDisplay.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf(), new WireframeCubeColorInformation(Color.WHITE), false, 0.1f);
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof WireframeCubeColorDisplay;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            WireframeCubeColorDisplay display = (WireframeCubeColorDisplay) obj;
            try {
                display.spawnDisplay();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning wireframe display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            WireframeCubeColorDisplay display = (WireframeCubeColorDisplay) obj;
            try {
                display.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning wireframe display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            WireframeCubeColorDisplay display = (WireframeCubeColorDisplay) obj;

            return switch (property) {
                case "color" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setColor(targetColor);
                    sender.sendMessage(Component.text("Updated all edge colors", NamedTextColor.GREEN));
                    yield true;
                }
                case "facecolor" -> {
                    if (values.length < 2) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> facecolor <top|bottom|front|back|left|right> <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    CubeFace face = CubeFace.fromString(values[0]);
                    if (face == null) {
                        sender.sendMessage(Component.text("Unknown face: " + values[0], NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[1]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setFaceColor(face, targetColor);
                    sender.sendMessage(Component.text("Updated color of face " + face.name() + " (4 edges)", NamedTextColor.GREEN));
                    yield true;
                }
                case "edgecolor" -> {
                    if (values.length < 2) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> edgecolor <edge> <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    CubeEdge edge = CubeEdge.fromString(values[0]);
                    if (edge == null) {
                        sender.sendMessage(Component.text("Unknown edge: " + values[0], NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[1]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setEdgeColor(edge, targetColor);
                    sender.sendMessage(Component.text("Updated color of edge " + edge.name(), NamedTextColor.GREEN));
                    yield true;
                }
                case "thickness" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> thickness <thickness> [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    try {
                        float thickness = Float.parseFloat(values[0]);
                        int duration = parseDuration(values);
                        display.setThickness(thickness, duration);
                        sender.sendMessage(Component.text("Updated thickness to " + String.format("%.2f", thickness) + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                        yield true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid thickness format. Use a number", NamedTextColor.RED));
                        yield false;
                    }
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = isRelative(values) ? new Vector3f(currentPos).add(pos) : pos;
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = display.getLocalTransform().getScale();
                        Vector3f targetScale = isRelative(values) ? new Vector3f(currentScale).add(scale) : scale;
                        AnimationFactory.registerScalingAnimationSmooth(display, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            display.scaleRelative(scale, duration);
                        } else {
                            display.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "seethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> seethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set see-through to " + seeThrough, NamedTextColor.GREEN));
                    yield true;
                }
                case "randomcolor" -> {
                    for (CubeEdge edge : CubeEdge.values()) {
                        display.setEdgeColor(edge, randomColor());
                    }
                    sender.sendMessage(Component.text("Set random colors for all edges", NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class FilledWireframeDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return FilledWireframeCubeColorDisplay.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf(), new CubeColorInformation(Color.WHITE), new WireframeCubeColorInformation(Color.BLACK));
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof FilledWireframeCubeColorDisplay;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            FilledWireframeCubeColorDisplay display = (FilledWireframeCubeColorDisplay) obj;
            try {
                display.spawnDisplay();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning filled wireframe display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            FilledWireframeCubeColorDisplay display = (FilledWireframeCubeColorDisplay) obj;
            try {
                display.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning filled wireframe display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            FilledWireframeCubeColorDisplay display = (FilledWireframeCubeColorDisplay) obj;

            return switch (property) {
                case "color" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setColor(targetColor);
                    sender.sendMessage(Component.text("Updated all face and edge colors", NamedTextColor.GREEN));
                    yield true;
                }
                case "facescolor" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> facescolor <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setFacesColor(targetColor);
                    sender.sendMessage(Component.text("Updated all face colors", NamedTextColor.GREEN));
                    yield true;
                }
                case "edgescolor" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> edgescolor <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setEdgesColor(targetColor);
                    sender.sendMessage(Component.text("Updated all edge colors", NamedTextColor.GREEN));
                    yield true;
                }
                case "facecolor" -> {
                    if (values.length < 2) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> facecolor <top|bottom|front|back|left|right> <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    CubeFace face = CubeFace.fromString(values[0]);
                    if (face == null) {
                        sender.sendMessage(Component.text("Unknown face: " + values[0], NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[1]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setFaceColor(face, targetColor);
                    sender.sendMessage(Component.text("Updated color of face " + face.name(), NamedTextColor.GREEN));
                    yield true;
                }
                case "edgecolor" -> {
                    if (values.length < 2) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> edgecolor <edge> <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    CubeEdge edge = CubeEdge.fromString(values[0]);
                    if (edge == null) {
                        sender.sendMessage(Component.text("Unknown edge: " + values[0], NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[1]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setEdgeColor(edge, targetColor);
                    sender.sendMessage(Component.text("Updated color of edge " + edge.name(), NamedTextColor.GREEN));
                    yield true;
                }
                case "faceedgecolor" -> {
                    if (values.length < 2) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> faceedgecolor <top|bottom|front|back|left|right> <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    CubeFace face = CubeFace.fromString(values[0]);
                    if (face == null) {
                        sender.sendMessage(Component.text("Unknown face: " + values[0], NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[1]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setFaceEdgesColor(face, targetColor);
                    sender.sendMessage(Component.text("Updated edge colors bordering face " + face.name() + " (4 edges)", NamedTextColor.GREEN));
                    yield true;
                }
                case "thickness" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> thickness <thickness> [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    try {
                        float thickness = Float.parseFloat(values[0]);
                        int duration = parseDuration(values);
                        display.setThickness(thickness, duration);
                        sender.sendMessage(Component.text("Updated edge thickness to " + String.format("%.2f", thickness) + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                        yield true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid thickness format. Use a number", NamedTextColor.RED));
                        yield false;
                    }
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = isRelative(values) ? new Vector3f(currentPos).add(pos) : pos;
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = display.getLocalTransform().getScale();
                        Vector3f targetScale = isRelative(values) ? new Vector3f(currentScale).add(scale) : scale;
                        AnimationFactory.registerScalingAnimationSmooth(display, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            display.scaleRelative(scale, duration);
                        } else {
                            display.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "seethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> seethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set see-through to " + seeThrough + " for faces and edges", NamedTextColor.GREEN));
                    yield true;
                }
                case "facesseethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> facesseethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setFacesSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set face see-through to " + seeThrough, NamedTextColor.GREEN));
                    yield true;
                }
                case "edgesseethrough" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> edgesseethrough <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean seeThrough = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setEdgesSeeThrough(seeThrough);
                    sender.sendMessage(Component.text("Set edge see-through to " + seeThrough, NamedTextColor.GREEN));
                    yield true;
                }
                case "randomcolor" -> {
                    for (CubeFace face : CubeFace.values()) {
                        display.setFaceColor(face, randomColor());
                    }
                    for (CubeEdge edge : CubeEdge.values()) {
                        display.setEdgeColor(edge, randomColor());
                    }
                    sender.sendMessage(Component.text("Set random colors for all faces and edges", NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class TextDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return TextDisplay.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf());
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof TextDisplay;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            TextDisplay display = (TextDisplay) obj;
            try {
                display.spawnDisplay();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning text display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            TextDisplay display = (TextDisplay) obj;
            try {
                display.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning text display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            TextDisplay display = (TextDisplay) obj;

            return switch (property) {
                case "text" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> text <text...>", NamedTextColor.RED));
                        yield false;
                    }
                    String text = String.join(" ", values);
                    display.setText(text);
                    sender.sendMessage(Component.text("Updated text to \"" + text + "\"", NamedTextColor.GREEN));
                    yield true;
                }
                case "color" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        AnimationFactory.registerColorAnimationSmooth(display, duration, display.getColor(), targetColor);
                    } else {
                        display.setColor(targetColor);
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "background" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> background <true|false>", NamedTextColor.RED));
                        yield false;
                    }
                    boolean hasBackground = values[0].equalsIgnoreCase("true") || values[0].equalsIgnoreCase("on");
                    display.setHasBackground(hasBackground);
                    sender.sendMessage(Component.text("Set background to " + hasBackground, NamedTextColor.GREEN));
                    yield true;
                }
                case "backgroundcolor" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> backgroundcolor <r,g,b[,a]>", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    Color targetColor = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setBackgroundColor(targetColor);
                    sender.sendMessage(Component.text("Updated background color", NamedTextColor.GREEN));
                    yield true;
                }
                case "linewidth" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> linewidth <width>", NamedTextColor.RED));
                        yield false;
                    }
                    try {
                        int width = Integer.parseInt(values[0]);
                        display.setLineWidth(width);
                        sender.sendMessage(Component.text("Updated line width to " + width, NamedTextColor.GREEN));
                        yield true;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(Component.text("Invalid line width format. Use an integer", NamedTextColor.RED));
                        yield false;
                    }
                }
                case "billboard" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> billboard <fixed|vertical|horizontal|center>", NamedTextColor.RED));
                        yield false;
                    }
                    Display.Billboard billboard = switch (values[0].toLowerCase()) {
                        case "vertical" -> Display.Billboard.VERTICAL;
                        case "horizontal" -> Display.Billboard.HORIZONTAL;
                        case "center" -> Display.Billboard.CENTER;
                        default -> Display.Billboard.FIXED;
                    };
                    display.setBillboard(billboard);
                    sender.sendMessage(Component.text("Updated billboard mode to " + billboard.name(), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = isRelative(values) ? new Vector3f(currentPos).add(pos) : pos;
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = display.getLocalTransform().getScale();
                        Vector3f targetScale = isRelative(values) ? new Vector3f(currentScale).add(scale) : scale;
                        AnimationFactory.registerScalingAnimationSmooth(display, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            display.scaleRelative(scale, duration);
                        } else {
                            display.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "randomcolor" -> {
                    Color randomCol = randomColor();
                    display.setColor(randomCol);
                    sender.sendMessage(Component.text("Set color to random RGB(" + randomCol.getRed() + "," + randomCol.getGreen() + "," + randomCol.getBlue() + ")", NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class BlockDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return BlockDisplayObject.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf(), Material.STONE.createBlockData());
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof BlockDisplayObject;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            BlockDisplayObject display = (BlockDisplayObject) obj;
            try {
                display.spawnDisplay();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning block display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            BlockDisplayObject display = (BlockDisplayObject) obj;
            try {
                display.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning block display: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            BlockDisplayObject display = (BlockDisplayObject) obj;

            return switch (property) {
                case "block" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> block <material>", NamedTextColor.RED));
                        yield false;
                    }
                    Material material = Material.matchMaterial(values[0]);
                    if (material == null || !material.isBlock()) {
                        sender.sendMessage(Component.text("Unknown or non-block material: " + values[0], NamedTextColor.RED));
                        yield false;
                    }
                    BlockData blockData = material.createBlockData();
                    display.setBlock(blockData);
                    sender.sendMessage(Component.text("Updated block to " + material.name(), NamedTextColor.GREEN));
                    yield true;
                }
                case "billboard" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> billboard <fixed|vertical|horizontal|center>", NamedTextColor.RED));
                        yield false;
                    }
                    Display.Billboard billboard = switch (values[0].toLowerCase()) {
                        case "vertical" -> Display.Billboard.VERTICAL;
                        case "horizontal" -> Display.Billboard.HORIZONTAL;
                        case "center" -> Display.Billboard.CENTER;
                        default -> Display.Billboard.FIXED;
                    };
                    display.setBillboard(billboard);
                    sender.sendMessage(Component.text("Updated billboard mode to " + billboard.name(), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = display.getLocalTransform().getTranslation();
                        Vector3f targetPos = isRelative(values) ? new Vector3f(currentPos).add(pos) : pos;
                        AnimationFactory.registerTranslationAnimationSmooth(display, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            display.moveRelative(pos, duration);
                        } else {
                            display.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = display.getLocalTransform().getScale();
                        Vector3f targetScale = isRelative(values) ? new Vector3f(currentScale).add(scale) : scale;
                        AnimationFactory.registerScalingAnimationSmooth(display, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            display.scaleRelative(scale, duration);
                        } else {
                            display.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerLRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.LRotateRelative(rot, duration);
                        } else {
                            display.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rrotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rrotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = display.getLocalTransform().getRightRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerRRotationAnimationSmooth(display, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            display.RRotateRelative(rot, duration);
                        } else {
                            display.RRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated right rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class CollisionBoxHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return ShulkerBasedCollisionBox.create(loc, new Vector3f(1, 1, 1), new Vector3f());
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof ShulkerBasedCollisionBox;
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            ShulkerBasedCollisionBox box = (ShulkerBasedCollisionBox) obj;
            try {
                box.spawnEntity();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning collision box: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            ShulkerBasedCollisionBox box = (ShulkerBasedCollisionBox) obj;
            try {
                box.remove();
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning collision box: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            ShulkerBasedCollisionBox box = (ShulkerBasedCollisionBox) obj;

            return switch (property) {
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentPos = box.getLocalTransform().getTranslation();
                        Vector3f targetPos = isRelative(values) ? new Vector3f(currentPos).add(pos) : pos;
                        AnimationFactory.registerTranslationAnimationSmooth(box, duration, currentPos, targetPos);
                    } else {
                        if (isRelative(values)) {
                            box.moveRelative(pos, duration);
                        } else {
                            box.moveAbsolute(pos, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Vector3f currentScale = box.getLocalTransform().getScale();
                        Vector3f targetScale = isRelative(values) ? new Vector3f(currentScale).add(scale) : scale;
                        AnimationFactory.registerScalingAnimationSmooth(box, duration, currentScale, targetScale);
                    } else {
                        if (isRelative(values)) {
                            box.scaleRelative(scale, duration);
                        } else {
                            box.scaleAbsolute(scale, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear|smooth]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (duration > 0 && hasSmoothInterpolation(values)) {
                        Quaternionf currentRot = box.getLocalTransform().getLeftRotation();
                        Quaternionf targetRot = isRelative(values) ? new Quaternionf(currentRot).mul(rot) : rot;
                        AnimationFactory.registerLRotationAnimationSmooth(box, duration, currentRot, targetRot);
                    } else {
                        if (isRelative(values)) {
                            box.LRotateRelative(rot, duration);
                        } else {
                            box.LRotateAbsolute(rot, duration);
                        }
                    }
                    String interpolation = " (" + getInterpolationMode(values) + ")";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class StructureHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return new PositionObject(new ArrayList<>(), new Transformation(new Vector3f(), new Quaternionf(), new Vector3f(1, 1, 1), new Quaternionf()), loc);
        }

        @Override
        public boolean canHandle(PositionObject obj) {
            return obj instanceof PositionObject && !(obj instanceof ColorDisplay) && !(obj instanceof CubeColorDisplay)
                    && !(obj instanceof LineColorDisplay) && !(obj instanceof WireframeCubeColorDisplay)
                    && !(obj instanceof FilledWireframeCubeColorDisplay) && !(obj instanceof TextDisplay)
                    && !(obj instanceof BlockDisplayObject) && !(obj instanceof ShulkerBasedCollisionBox);
        }

        @Override
        public boolean spawn(PositionObject obj, CommandSender sender) {
            try {
                obj.getChildren().forEach(child -> {
                    if (child instanceof ColorDisplay) {
                        ((ColorDisplay) child).spawnDisplay();
                    }
                });
                sender.sendMessage(Component.text("Spawned structure children", NamedTextColor.GREEN));
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error spawning structure: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean despawn(PositionObject obj, CommandSender sender) {
            try {
                obj.remove();
                sender.sendMessage(Component.text("Despawned structure", NamedTextColor.GREEN));
                return true;
            } catch (Exception e) {
                sender.sendMessage(Component.text("Error despawning structure: " + e.getMessage(), NamedTextColor.RED));
                return false;
            }
        }

        @Override
        public boolean edit(PositionObject obj, String property, String[] values, CommandSender sender) {
            return switch (property) {
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f pos = parseVector(values[0]);
                    if (pos == null) {
                        sender.sendMessage(Component.text("Invalid position format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (isRelative(values)) {
                        obj.moveRelative(pos, duration);
                    } else {
                        obj.moveAbsolute(pos, duration);
                    }
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    Quaternionf rot = parseQuaternion(values[0]);
                    if (rot == null) {
                        sender.sendMessage(Component.text("Invalid rotation format. Use x,y,z,w", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (isRelative(values)) {
                        obj.LRotateRelative(rot, duration);
                    } else {
                        obj.LRotateAbsolute(rot, duration);
                    }
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f scale = parseVector(values[0]);
                    if (scale == null) {
                        sender.sendMessage(Component.text("Invalid scale format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    String modeStr = isRelative(values) ? "relative" : "absolute";

                    if (isRelative(values)) {
                        obj.scaleRelative(scale, duration);
                    } else {
                        obj.scaleAbsolute(scale, duration);
                    }
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

}
