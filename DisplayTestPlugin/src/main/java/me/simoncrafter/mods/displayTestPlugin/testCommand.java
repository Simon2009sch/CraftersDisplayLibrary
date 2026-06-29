package me.simoncrafter.mods.displayTestPlugin;

import me.simoncrafter.CraftersDisplayLibrary.def.PositionObject;
import me.simoncrafter.CraftersDisplayLibrary.def.active.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Line.LineColorDisplay;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.*;
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
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length < 2) {
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
            return recommendListThatContainsObject(List.of("create", "edit", "remove", "spawn", "despawn"), args[0]);
        }

        if (args[0].equalsIgnoreCase("create") && args.length == 3) {
            return recommendListThatContainsObject(typeHandlers.keySet(), args[2]);
        }

        if ((args[0].equalsIgnoreCase("edit") || args[0].equalsIgnoreCase("remove") || args[0].equalsIgnoreCase("spawn") || args[0].equalsIgnoreCase("despawn")) && args.length == 2) {
            return recommendListThatContainsObject(objectMap.keySet(), args[1]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 3) {
            return recommendListThatContainsObject(List.of("position", "rotation", "scale", "color", "direction", "thickness"), args[2]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 5) {
            return recommendListThatContainsObject(List.of("absolute", "relative"), args[4]);
        }

        if (args[0].equalsIgnoreCase("edit") && args.length == 7) {
            return recommendListThatContainsObject(List.of("linear"), args[6]);
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
            if (lastArg.equals("linear")) {
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
        if (mode.equals("linear")) {
            mode = values.length >= 3 ? values[values.length - 3].toLowerCase() : "absolute";
        }
        return mode.equals("absolute");
    }

    private boolean isRelative(String[] values) {
        if (values.length < 2) return false;
        String mode = values[values.length - 2].toLowerCase();
        if (mode.equals("linear")) {
            mode = values.length >= 3 ? values[values.length - 3].toLowerCase() : "";
        }
        return mode.equals("relative");
    }

    private boolean hasLinearInterpolation(String[] values) {
        if (values.length < 1) return false;
        return values[values.length - 1].toLowerCase().equals("linear");
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
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    if (rgba.length == 4) {
                        display.setColor(Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]), duration);
                    } else {
                        display.setColor(Color.fromRGB(rgba[0], rgba[1], rgba[2]), duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.moveRelative(pos, duration);
                    } else {
                        display.moveAbsolute(pos, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.scaleRelative(scale, duration);
                    } else {
                        display.scaleAbsolute(scale, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.LRotateRelative(rot, duration);
                    } else {
                        display.LRotateAbsolute(rot, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
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
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    Color color = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    display.setColor(color, duration);
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.moveRelative(pos, duration);
                    } else {
                        display.moveAbsolute(pos, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated position (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "scale" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> scale <x,y,z> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.scaleRelative(scale, duration);
                    } else {
                        display.scaleAbsolute(scale, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated scale (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "rotation" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> rotation <x,y,z,w> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.LRotateRelative(rot, duration);
                    } else {
                        display.LRotateAbsolute(rot, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated rotation (" + modeStr + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                default -> false;
            };
        }
    }

    class LineDisplayHandler implements DisplayTypeHandler {
        @Override
        public PositionObject create(Location loc) {
            return LineColorDisplay.create(new Vector3f(), Color.WHITE, loc, new Vector3f(0, 1, 0), 0.1f);
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
                        sender.sendMessage(Component.text("Usage: /test edit <id> color <r,g,b[,a]> [duration] [linear]", NamedTextColor.RED));
                        yield false;
                    }
                    int[] rgba = parseRGBA(values[0]);
                    if (rgba == null) {
                        sender.sendMessage(Component.text("Invalid color format. Use r,g,b or r,g,b,a", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    Color color = rgba.length == 4 ? Color.fromARGB(rgba[3], rgba[0], rgba[1], rgba[2]) : Color.fromRGB(rgba[0], rgba[1], rgba[2]);
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated color" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "direction" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> direction <x,y,z> [duration] [linear]", NamedTextColor.RED));
                        yield false;
                    }
                    Vector3f direction = parseVector(values[0]);
                    if (direction == null) {
                        sender.sendMessage(Component.text("Invalid direction format. Use x,y,z", NamedTextColor.RED));
                        yield false;
                    }
                    int duration = parseDuration(values);
                    display.setDirection(direction, duration);
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
                    sender.sendMessage(Component.text("Updated direction (length: " + String.format("%.2f", direction.length()) + ")" + interpolation + (duration > 0 ? " over " + duration + " ticks" : ""), NamedTextColor.GREEN));
                    yield true;
                }
                case "position" -> {
                    if (values.length < 1) {
                        sender.sendMessage(Component.text("Usage: /test edit <id> position <x,y,z> [absolute|relative] [duration] [linear]", NamedTextColor.RED));
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
                        display.moveRelative(pos, duration);
                    } else {
                        display.moveAbsolute(pos, duration);
                    }
                    String interpolation = hasLinearInterpolation(values) ? " (linear)" : "";
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
                default -> false;
            };
        }
    }

}
