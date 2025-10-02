package me.simoncrafter.mods.displayTestPlugin;

import me.simoncrafter.CraftersDisplayLibrary.def.active.ColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorDisplay;
import me.simoncrafter.CraftersDisplayLibrary.def.active.Cube.CubeColorInformation;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.CommandBlock;
import org.bukkit.command.*;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;
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
                cube.moveRelativeToWorld(loc.toVector().toVector3f(), 20);
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
                cube.moveAbsolute(vector3f, time);
            } else {
                cube.moveRelative(vector3f, time);
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
                cube.scaleAbsolute(vector3f, time);
            } else {
                cube.scaleRelative(vector3f, time);
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
                cube.LRotateAbsolute(quaternionf, time);
            } else {
                cube.LRotateRelative(quaternionf, time);
            }
        } else if (args[0].equals("entity_move")) {
            cube.moveEntityStatic(loc);
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

        }


        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (args.length == 1) {
            return List.of("block", "move", "scale", "rotate", "entity_move");
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

}
