package me.simoncrafter.mods.displayTestPlugin;

import me.simoncrafter.CraftersDisplayLibrary.def.active.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class testCommand implements CommandExecutor, TabExecutor {

    CubeColorDisplay cube;
    ColorDisplay colorDisplay;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        Location loc;
        if (sender instanceof Player player) {
            loc = player.getLocation().clone();
        } else if (sender instanceof BlockCommandSender block) {
            loc = block.getBlock().getLocation().clone().add(0, 5, 0);
        }else {
            Bukkit.broadcast(Component.text(sender.getClass().getName()));
            loc = new Location(Bukkit.getWorlds().getFirst(), 0, 0, 0);
        }

        loc.setPitch(0);
        loc.setYaw(0);


        if (args[0].equals("block")) {
            cube = CubeColorDisplay.create(loc.toBlockLocation().add(0.5f, 0.5f, 0.5f), new Vector3f(1, 1, 1), new Vector3f(0, 0, 0), new Quaternionf(0, 0, 0, 1), new CubeColorInformation(), false);
            cube.spawnDisplay();
        } else if (args[0].equals("move")) {
            Vector3f vector3f = parseVector(args[1]);

            if (args[1].equals("here")) {
                if (cube != null)cube.moveRelativeToWorld(loc.toVector().toVector3f(), 20);
                if (colorDisplay != null)colorDisplay.moveRelativeToWorld(loc.toVector().toVector3f(), 20);
            } else if (vector3f == null) {
                sender.sendMessage("Invalid vector");
                return true;
            }
            int time = 20;
            if (args.length > 2) {
                try {
                    time = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid time");
                }
            }

            if (args[args.length - 1].equals("abs")) {
                if (cube != null)cube.moveAbsolute(vector3f, time);
                if (colorDisplay != null)colorDisplay.moveAbsolute(vector3f, time);
            } else {
                if (cube != null)cube.moveRelative(vector3f, time);
                if (colorDisplay != null)colorDisplay.moveRelative(vector3f, time);
            }
        } else if (args[0].equals("scale")) {
            Vector3f vector3f = parseVector(args[1]);
            if (vector3f == null) {
                sender.sendMessage("Invalid vector");
            }
            int time = 20;
            if (args.length > 2) {
                try {
                    time = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid time");
                }
            }

            if (args[args.length - 1].equals("abs")) {
                if (cube != null)cube.scaleAbsolute(vector3f, time);
                if (colorDisplay != null)colorDisplay.scaleAbsolute(vector3f, time);
            } else {
                if (cube != null)cube.scaleRelative(vector3f, time);
                if (colorDisplay != null)colorDisplay.scaleRelative(vector3f, time);
            }
        } else if (args[0].equals("rotate")) {
            Quaternionf quaternionf = parseQuaternion(args[1]);
            if (quaternionf == null) {
                sender.sendMessage("Invalid vector");
            }
            int time = 20;
            if (args.length > 2) {
                try {
                    time = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid time");
                }
            }

            if (args[args.length - 1].equals("abs")) {
                if (cube != null)cube.LRotateAbsolute(quaternionf, time);
                if (colorDisplay != null)colorDisplay.LRotateAbsolute(quaternionf, time);
            } else {
                if (cube != null)cube.LRotateRelative(quaternionf, time);
                if (colorDisplay != null)colorDisplay.LRotateRelative(quaternionf, time);
            }
        } else if (args[0].equals("entity_move")) {
            if (cube != null) cube.moveEntityStatic(loc);
            if (colorDisplay != null) colorDisplay.moveEntityStatic(loc);
        } else if (args[0].equals("entity_ride")) {
            ColorDisplay top = cube.getTop();
            ColorDisplay bottom = cube.getBottom();
            ColorDisplay front = cube.getFront();
            ColorDisplay back = cube.getBack();
            ColorDisplay left = cube.getLeft();
            ColorDisplay right = cube.getRight();


            Player player = (Player) sender;

            player.addPassenger(top.getEntity());
            player.addPassenger(bottom.getEntity());
            player.addPassenger(front.getEntity());
            player.addPassenger(back.getEntity());
            player.addPassenger(left.getEntity());
            player.addPassenger(right.getEntity());

        } else if (args[0].equals("line")) {
            cube.setLocalTransform(CubeColorDisplay.makeTransformBetween(new Vector3f(37, 72, -300), loc.toVector().toVector3f()), 50);

        } else if (args[0].equals("color")) {
            colorDisplay = ColorDisplay.create(loc, new Vector3f(1, 1, 1), new Vector3f(), new Quaternionf(), Color.AQUA);
            colorDisplay.respawnEntity();
        } else if (args[0].equals("setColor")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /cdl setColor <r,g,b> [duration] [face]");
                return true;
            }

            int[] rgb = parseRGB(args[1]);
            if (rgb == null) {
                sender.sendMessage("Invalid RGB color. Format: r,g,b (0-255)");
                return true;
            }

            int duration = 0;
            if (args.length > 2) {
                try {
                    duration = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid duration");
                    return true;
                }
            }

            Color newColor = Color.fromARGB(255, rgb[0], rgb[1], rgb[2]);
            String face = args.length > 3 ? args[3].toLowerCase() : "all";

            if (face.equals("all")) {
                if (cube != null) {
                    cube.setColor(newColor, duration);
                }
                if (colorDisplay != null) {
                    colorDisplay.setColor(newColor, duration);
                }
            } else {
                if (cube != null) {
                    switch (face) {
                        case "top" -> cube.getTop().setColor(newColor, duration);
                        case "bottom" -> cube.getBottom().setColor(newColor, duration);
                        case "left" -> cube.getLeft().setColor(newColor, duration);
                        case "right" -> cube.getRight().setColor(newColor, duration);
                        case "front" -> cube.getFront().setColor(newColor, duration);
                        case "back" -> cube.getBack().setColor(newColor, duration);
                        default -> sender.sendMessage("Unknown face: " + face);
                    }
                }
            }
        } else if (args[0].equals("setAlpha")) {
            if (args.length < 2) {
                sender.sendMessage("Usage: /cdl setAlpha <0-255> [duration] [face]");
                return true;
            }

            int alpha;
            try {
                alpha = Integer.parseInt(args[1]);
                if (alpha < 0 || alpha > 255) {
                    sender.sendMessage("Alpha must be 0-255");
                    return true;
                }
            } catch (NumberFormatException e) {
                sender.sendMessage("Invalid alpha value");
                return true;
            }

            int duration = 0;
            if (args.length > 2) {
                try {
                    duration = Integer.parseInt(args[2]);
                } catch (NumberFormatException e) {
                    sender.sendMessage("Invalid duration");
                    return true;
                }
            }

            String face = args.length > 3 ? args[3].toLowerCase() : "all";

            if (face.equals("all")) {
                if (cube != null) {
                    cube.setAlpha(alpha, duration);
                }
                if (colorDisplay != null) {
                    colorDisplay.setAlpha(alpha, duration);
                }
            } else {
                if (cube != null) {
                    switch (face) {
                        case "top" -> cube.getTop().setAlpha(alpha, duration);
                        case "bottom" -> cube.getBottom().setAlpha(alpha, duration);
                        case "left" -> cube.getLeft().setAlpha(alpha, duration);
                        case "right" -> cube.getRight().setAlpha(alpha, duration);
                        case "front" -> cube.getFront().setAlpha(alpha, duration);
                        case "back" -> cube.getBack().setAlpha(alpha, duration);
                        default -> sender.sendMessage("Unknown face: " + face);
                    }
                }
            }
        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("block", "move", "scale", "rotate", "entity_move", "color", "setColor", "setAlpha");
        }

        if ((args.length == 4 && args[0].equals("setColor")) || (args.length == 4 && args[0].equals("setAlpha"))) {
            return List.of("top", "bottom", "left", "right", "front", "back", "all");
        }

        return List.of();
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

}
