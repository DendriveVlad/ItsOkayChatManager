package itsokay.chatmanager.commands;

import itsokay.chatmanager.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DonateCommands implements CommandExecutor, TabCompleter {
    ChatManager me;

    DBGetter date = new DBGetter();
    public ConfigManager config;



    public DonateCommands(ChatManager plugin) {
            Objects.requireNonNull(plugin.getCommand("pos1")).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand("pos2")).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand("nickcolor")).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand("colornumbers")).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand("fakeleave")).setExecutor(this);
            Objects.requireNonNull(plugin.getCommand("fakedeath")).setExecutor(this);
            this.me = plugin;
            config = new ConfigManager(plugin);
        }

        @Override
        public boolean onCommand (@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[]args){
            try {
                if (notEnoughMoney(label, config.getConfig("players").getInt("players." + sender.getName() + ".donate"))) {
                    sender.sendMessage(ChatColor.RED + "Это платная функция, ознакомьтесь с возможностью её получения по команде /donate");
                    return true;
                }

                Player player = (Player) sender;
                PersistentDataContainer dataContainer = player.getPersistentDataContainer();
                long now_time = System.currentTimeMillis() / 1000;
                PlayerObject playerObject = new PlayerObject(config);
                playerObject.set_player(player.getName());

                switch (label) {
                    case "pos1":
                        Location block = Objects.requireNonNull(player.getTargetBlock(31)).getLocation();
                        if (block.distance(player.getLocation()) >= 30) {
                            player.sendMessage(ChatColor.RED + "Блок, на который Вы смотрите, расположен слишком далеко");
                            return true;
                        }
                        dataContainer.set(new NamespacedKey(this.me, "savePosition1x"), PersistentDataType.DOUBLE, block.getX());
                        dataContainer.set(new NamespacedKey(this.me, "savePosition1y"), PersistentDataType.DOUBLE, block.getY());
                        dataContainer.set(new NamespacedKey(this.me, "savePosition1z"), PersistentDataType.DOUBLE, block.getZ());
                        dataContainer.set(new NamespacedKey(this.me, "savePositionWorld"), PersistentDataType.STRING, player.getWorld().getName());
                        player.sendMessage(ChatColor.GREEN + "Положение блока сохранено, посмотрите на другой блок и напишите /pos2, чтобы получить объём выделенной области");
                        return true;
                    case "pos2":
                        Location block2 = Objects.requireNonNull(player.getTargetBlock(31)).getLocation();
                        if (block2.distance(player.getLocation()) >= 30) {
                            player.sendMessage(ChatColor.RED + "Блок, на который Вы смотрите, расположен слишком далеко");
                            return true;
                        }
                        if (!dataContainer.has(new NamespacedKey(this.me, "savePosition1x"), PersistentDataType.DOUBLE)) {
                            player.sendMessage(ChatColor.RED + "Сперва используйте /pos1, а только потом /pos2");
                            return true;
                        }
                        if (!player.getWorld().getName().equals(dataContainer.get(new NamespacedKey(this.me, "savePositionWorld"), PersistentDataType.STRING))) {
                            player.sendMessage(ChatColor.RED + "Координаты /pos1 были сохранены в другом мире");
                            return true;
                        }

                        double blockLenX = Math.abs(dataContainer.get(new NamespacedKey(this.me, "savePosition1x"), PersistentDataType.DOUBLE) - block2.getX()) + 1;
                        double blockLenY = Math.abs(dataContainer.get(new NamespacedKey(this.me, "savePosition1y"), PersistentDataType.DOUBLE) - block2.getY()) + 1;
                        double blockLenZ = Math.abs(dataContainer.get(new NamespacedKey(this.me, "savePosition1z"), PersistentDataType.DOUBLE) - block2.getZ()) + 1;

                        if (blockLenZ > 800 || blockLenX > 800) {
                            player.sendMessage(ChatColor.RED + "Превышено допустимое расстояние между блоками");
                            return true;
                        }
                        player.sendMessage(ChatColor.GREEN + "Объём выделенной области составляет: " + ChatColor.GOLD + (int) (blockLenX * blockLenY * blockLenZ));
                        return true;
                    case "nickcolor":
                        String color = getColor(args[0]);
                        if (color.equals("")) {
                            player.sendMessage(ChatColor.RED + "Не верно выбран цвет");
                            return true;
                        }
                        this.date.setNickColor(player.getName(), color);
                        config.getConfig("players").set("players." + player.getName() + ".name_color", color);
                        config.saveConfig("players");
                        TabManager.getTabString(player, config);
                        player.sendMessage(ChatColor.COLOR_CHAR + color + "Цвет ника изменён");
                        return true;
                    case "colornumbers":
                        player.sendMessage(ChatColor.GOLD + "\n------------Colors------------");
                        player.sendMessage(ChatColor.BLACK + "&0 (Black)");
                        player.sendMessage(ChatColor.DARK_BLUE + "&1 (Dark blue)");
                        player.sendMessage(ChatColor.DARK_GREEN + "&2 (Dark green)");
                        player.sendMessage(ChatColor.DARK_AQUA + "&3 (Dark aqua)");
                        player.sendMessage(ChatColor.DARK_RED + "&4 (Dark red)");
                        player.sendMessage(ChatColor.DARK_PURPLE + "&5 (Purple)");
                        player.sendMessage(ChatColor.GOLD + "&6 (Gold, AKA Orange)");
                        player.sendMessage(ChatColor.GRAY + "&7 (Gray, AKA default chat color)");
                        player.sendMessage(ChatColor.DARK_GRAY + "&8 (Dark gray)");
                        player.sendMessage(ChatColor.BLUE + "&9 (Blue)");
                        player.sendMessage(ChatColor.GREEN + "&a (Green)");
                        player.sendMessage(ChatColor.AQUA + "&b (Aqua)");
                        player.sendMessage(ChatColor.RED + "&c (Red)");
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "&d (Pink)");
                        player.sendMessage(ChatColor.YELLOW + "&e (Yellow)");
                        player.sendMessage(ChatColor.WHITE + "&f (White)");
                        player.sendMessage(ChatColor.GOLD + (ChatColor.ITALIC + "&o (Italic)"));
                        player.sendMessage(ChatColor.GOLD + (ChatColor.UNDERLINE + "&n (Underline)"));
                        player.sendMessage(ChatColor.GOLD + (ChatColor.BOLD + "&l (Bold)"));
                        player.sendMessage(ChatColor.GOLD + (ChatColor.STRIKETHROUGH + "&m (Strikethrough)"));
                        player.sendMessage(ChatColor.GOLD + ("&k " + ChatColor.MAGIC + "(Magic)"));
                        player.sendMessage(ChatColor.GOLD + "--------------------------------\n ");
                        return true;
                    case "fakedeath":
                        if (dataContainer.has(new NamespacedKey(this.me, "fake"), PersistentDataType.LONG)) {
                            if (now_time - dataContainer.get(new NamespacedKey(this.me, "fake"), PersistentDataType.LONG) <= 3600) {
                                player.sendMessage(ChatColor.RED + "Вы можете фейковать только один раз в час");
                                return true;
                            }
                        }
                        dataContainer.set(new NamespacedKey(this.me, "fake"), PersistentDataType.LONG, now_time);
                        String[] messages = {"Fell from a high place", "Fell out of the world", "Drowned", "Was slain by zombie", "Experienced kinetic energy", "Blew up", "Hit the ground too hard", "Burned to death"};
                        java.util.Random random = new java.util.Random();
                        int random_computer_card = random.nextInt(messages.length);
                        for (Player p : player.getServer().getOnlinePlayers()) {
                            p.sendMessage(playerObject.get_death_message(messages[random_computer_card], 0));
                        }
                        return true;
                    case "fakeleave":
                        if (dataContainer.has(new NamespacedKey(this.me, "fake"), PersistentDataType.LONG)) {
                            if (now_time - dataContainer.get(new NamespacedKey(this.me, "fake"), PersistentDataType.LONG) <= 3600) {
                                player.sendMessage(ChatColor.RED + "Вы можете фейковать только один раз в час");
                                return true;
                            }
                        }
                        dataContainer.set(new NamespacedKey(this.me, "fake"), PersistentDataType.LONG, now_time);
                        for (Player p : player.getServer().getOnlinePlayers()) {
                            p.sendMessage(playerObject.get_player(), new TextComponent("§e покидает игру (§cDISCONNECTED§e)"));
                        }
                        return true;
                }
            } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                throwables.printStackTrace();
            }
            return true;
        }

        @Override
        public @Nullable List<String> onTabComplete (@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[]args){
            List<String> turns = new ArrayList<>();
            if (alias.equals("nickcolor") && args.length == 1) {
                turns.add("white");
                turns.add("gray");
                turns.add("dark_gray");
                turns.add("black");
                turns.add("red");
                turns.add("dark_red");
                turns.add("green");
                turns.add("dark_green");
                turns.add("blue");
                turns.add("dark_blue");
                turns.add("aqua");
                turns.add("dark_aqua");
                turns.add("yellow");
                turns.add("gold");
                turns.add("pink");
                turns.add("purple");
            }
            return turns;
        }

        private boolean notEnoughMoney (String command,int money){
            switch (command) {
                case "pos1":
                case "pos2":
                    return money < 50;
                case "nickcolor":
                    return money < 200;
                case "colornumbers":
                    return money < 300;
                case "fakeleave":
                case "fakedeath":
                    return money < 500;
            }
            return false;
        }

        private String getColor (String color){
            switch (color) {
                case "white":
                    return "f";
                case "gray":
                    return "7";
                case "dark_gray":
                    return "8";
                case "black":
                    return "0";
                case "red":
                    return "c";
                case "dark_red":
                    return "4";
                case "green":
                    return "a";
                case "dark_green":
                    return "2";
                case "blue":
                    return "9";
                case "dark_blue":
                    return "1";
                case "aqua":
                    return "b";
                case "dark_aqua":
                    return "3";
                case "yellow":
                    return "e";
                case "gold":
                    return "6";
                case "pink":
                    return "d";
                case "purple":
                    return "5";
                default:
                    return "";
            }
        }
    }
