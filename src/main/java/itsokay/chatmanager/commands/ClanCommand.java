package itsokay.chatmanager.commands;

import itsokay.chatmanager.*;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ClanCommand implements CommandExecutor, TabCompleter {
    public ConfigManager config;
    DBGetter date = new DBGetter();

    public ClanCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("clan")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("c")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("с")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0 || (strings.length == 1 && !(strings[0].equals("help") || strings[0].equals("leave") || strings[0].equals("delete") || strings[0].equals("info")))) {
            Objects.requireNonNull(commandSender.getServer().getPlayer(commandSender.getName())).performCommand("clan help");
            return true;
        }
        switch (strings[0]) {
            case "help" -> {
                commandSender.sendMessage(ChatColor.GOLD + "\n------------Информация по команде /clan------------");
                commandSender.sendMessage(ChatColor.GOLD + "/clan info" + ChatColor.AQUA + " - Узнать информацию о своём клане");
                commandSender.sendMessage(ChatColor.GOLD + "/clan create <clan name>" + ChatColor.AQUA + " - создать клан");
                commandSender.sendMessage(ChatColor.GOLD + "/clan accept <invitor nick>" + ChatColor.AQUA + " - принять приглашение в клан");
                commandSender.sendMessage(ChatColor.GOLD + "/clan leave" + ChatColor.AQUA + " - покинуть клан");
                commandSender.sendMessage(ChatColor.GOLD + "/clan invite <player>" + ChatColor.AQUA + " - пригласить игрока в клан");
                commandSender.sendMessage(ChatColor.GOLD + "/clan kick <player>" + ChatColor.AQUA + " - исключить игрока из клана");
                commandSender.sendMessage(ChatColor.GOLD + "/clan promote <player>" + ChatColor.AQUA + " - передать права лидера клана игроку");
                commandSender.sendMessage(ChatColor.GOLD + "/clan rename <new clan name>" + ChatColor.AQUA + " - переименовать клан");
                commandSender.sendMessage(ChatColor.GOLD + "/clan tag <tag>" + ChatColor.AQUA + " - поставить тег для клана (не более 7-ти символов)");
                commandSender.sendMessage(ChatColor.GOLD + "/clan color <color>" + ChatColor.AQUA + " - выбрать цвет тега для клана");
                commandSender.sendMessage(ChatColor.GOLD + "/clan delete" + ChatColor.AQUA + " - удалить клан");
                commandSender.sendMessage(ChatColor.GOLD + "/c <message>" + ChatColor.AQUA + " - написать сообщение в чат клана");
                return true;
            }
            case "info" -> {
                try {
                    String[] info;
                    if (strings.length == 1) {
                        int clan_id = config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id");
                        if (clan_id == 0) {
                            commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                            return true;
                        }
                        info = date.getClanInfo(clan_id);
                    } else {
                        info = date.getClanInfo(Integer.parseInt(strings[1]));
                    }
                    if (info.length == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Такого клана не существует");
                        return true;
                    }

                    PlayerObject playerObject = new PlayerObject(config);
                    playerObject.set_player(info[1]);
                    commandSender.sendMessage(ChatColor.GOLD + "\nИнформация о клане §" + info[3] + info[0] + ChatColor.GOLD + ":");
                    commandSender.sendMessage(new TextComponent(ChatColor.GOLD + "Лидер: "), playerObject.get_player_without_clan());
                    commandSender.sendMessage(ChatColor.GOLD + "Тег клана: §" + info[3] + info[2]);
                    commandSender.sendMessage(ChatColor.GOLD + "Количество участников клана: " + ChatColor.AQUA + info[4]);
                    if (commandSender.getName().equals(info[1])) {
                        commandSender.sendMessage(ChatColor.GOLD + "Участники: " + ChatColor.GREEN + String.join(", ", date.getClanMembers(commandSender.getName())));
                    }
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                } catch (NumberFormatException throwables) {
                    commandSender.sendMessage(ChatColor.RED + "Такого клана не существует");
                }
                return true;
            }
            case "create" -> {
                String name = String.join(" ", strings).substring(7);
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") != 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не можете создать клан, когда находитесь в другом клане");
                        return true;
                    }
                    if (date.checkClanName(name)) {
                        commandSender.sendMessage(ChatColor.RED + "Клан с таким названием уже существует");
                        return true;
                    }
                    date.createClan(name, commandSender.getName(), config);
                    commandSender.sendMessage(ChatColor.GREEN + "Клан " + ChatColor.YELLOW + name + ChatColor.GREEN + " успешно создан!");
                    TabManager.getTabString((Player) commandSender, config);
                    config.getConfig("players").set("players." + commandSender.getName() + ".clan_invite", "");
                    config.saveConfig("players");
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "delete" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "У Вас должен быть клан, чтобы его удалить");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    date.deleteClan(commandSender.getName());
                    commandSender.sendMessage(ChatColor.GREEN + "Клан успешно удалён!");
                    for (String member : date.getClanMembers(commandSender.getName())) {
                        if (commandSender.getServer().getPlayer(member) == null) {
                            continue;
                        }
                        config.getConfig("players").set("players." + member + ".clan_id", 0);
                        config.getConfig("players").set("players." + member + ".clan", "");
                        config.saveConfig("players");
                        TabManager.getTabString(Objects.requireNonNull(commandSender.getServer().getPlayer(member)), config);
                    }
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "invite" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "У Вас должен быть клан, чтобы приглашать в него участников");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    Player player = commandSender.getServer().getPlayer(strings[1]);
                    assert player != null;
                    if (!commandSender.getServer().getOnlinePlayers().contains(player)) {
                        commandSender.sendMessage(ChatColor.RED + "Не верно введён ник игрока");
                        return true;
                    }
                    if (config.getConfig("players").getInt("players." + player.getName() + ".clan_id") != 0) {
                        commandSender.sendMessage(ChatColor.RED + "Игрок уже находится в клане");
                        return true;
                    }
                    if (Objects.equals(config.getConfig("players").getString("players." + player.getName() + ".clan_invite"), commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы уже отправили приглашение этому игроку");
                        return true;
                    }
                    config.getConfig("players").set("players." + player.getName() + ".clan_invite", commandSender.getName());
                    config.saveConfig("players");
                    commandSender.sendMessage(ChatColor.GREEN + "Игрок приглашён в клан");

                    String[] clan_info = date.getClanInfo(config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id"));

                    TextComponent invite = new TextComponent("\n" + ChatColor.GREEN + "Вам пришло приглашение в клан " + ChatColor.COLOR_CHAR + clan_info[3] + "[" + clan_info[2] + "] " + clan_info[0] +
                            ChatColor.GREEN + " от игрока " + commandSender.getName() + ChatColor.GOLD + " (Нажмите, чтобы принять)");
                    invite.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan accept " + commandSender.getName()));
                    invite.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите, чтобы принять приглашение").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

                    if (config.getConfig("players").getBoolean("players." + player.getName() + ".notifications")) {
                        player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                    player.sendMessage(invite);
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "accept" -> {
                try {
                    if (!Objects.equals(config.getConfig("players").getString("players." + commandSender.getName() + ".clan_invite"), strings[1])) {
                        commandSender.sendMessage(ChatColor.RED + "Вам не приходило приглашений от этого игрока");
                        return true;
                    }
                    date.acceptInvite(commandSender.getName(), strings[1], config);
                    TabManager.getTabString(Objects.requireNonNull(commandSender.getServer().getPlayer(commandSender.getName())), config);
                    String[] clan_info = date.getClanInfo(config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id"));
                    commandSender.sendMessage(ChatColor.GREEN + "Добро пожаловать в клан " + ChatColor.COLOR_CHAR + clan_info[3] + clan_info[0]);
                    PlayerObject playerObject = new PlayerObject(config);
                    playerObject.set_player(commandSender.getName());
                    for (String player : date.getClanMembers(strings[1])) {
                        Player p = commandSender.getServer().getPlayer(player);
                        if (p == null) {
                            continue;
                        }
                        if (p.getName().equals(commandSender.getName())) {
                            continue;
                        }
                        p.sendMessage(new TextComponent(ChatColor.GREEN + "Игрок "), playerObject.get_player_without_clan(), new TextComponent(ChatColor.GREEN + " присоединился к клану"));
                    }
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "leave" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                        return true;
                    }
                    if (date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не можете выйти из своего клана");
                        return true;
                    }
                    PlayerObject playerObject = new PlayerObject(config);
                    playerObject.set_player(commandSender.getName());
                    for (String player : date.getClanMembers(commandSender.getName())) {
                        Player p = commandSender.getServer().getPlayer(player);
                        if (p == null) {
                            continue;
                        }
                        if (player.equals(commandSender.getName())) {
                            continue;
                        }
                        p.sendMessage(new TextComponent(ChatColor.RED + "Игрок "), playerObject.get_player_without_clan(), new TextComponent(ChatColor.RED + " покинул клан"));
                    }
                    date.leaveClan(commandSender.getName(), config);
                    config.getConfig("players").set("players." + commandSender.getName() + ".clan_id", 0);
                    config.getConfig("players").set("players." + commandSender.getName() + ".clan", "");
                    config.saveConfig("players");
                    TabManager.getTabString(Objects.requireNonNull(commandSender.getServer().getPlayer(commandSender.getName())), config);
                    commandSender.sendMessage(ChatColor.GREEN + "Вы покинули клан");
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "kick" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    if (commandSender.getName().equals(strings[1])) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не можете кикнуть себя");
                        return true;
                    }
                    List<String> clan_members = date.getClanMembers(commandSender.getName());
                    if (!clan_members.contains(strings[1])) {
                        commandSender.sendMessage(ChatColor.RED + "Этого игрока нет в клане");
                        return true;
                    }
                    date.leaveClan(strings[1], config);
                    config.getConfig("players").set("players." + strings[1] + ".clan_id", 0);
                    config.getConfig("players").set("players." + strings[1] + ".clan", "");
                    config.saveConfig("players");
                    if (commandSender.getServer().getPlayer(strings[1]) != null) {
                        TabManager.getTabString(Objects.requireNonNull(commandSender.getServer().getPlayer(strings[1])), config);
                    }
                    PlayerObject playerObject = new PlayerObject(config);
                    playerObject.set_player(strings[1]);
                    for (String player : clan_members) {
                        Player p = commandSender.getServer().getPlayer(player);
                        if (p == null) {
                            continue;
                        }
                        if (p.getName().equals(strings[1])) {
                            p.sendMessage(ChatColor.RED + "Вас исключили из клана");
                            continue;
                        }
                        p.sendMessage(new TextComponent(ChatColor.RED + "Игрок "), playerObject.get_player_without_clan(), new TextComponent(ChatColor.RED + " покинул клан"));
                    }
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "promote" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    if (commandSender.getName().equals(strings[1])) {
                        commandSender.sendMessage(ChatColor.RED + "Вы и так лидер клана");
                        return true;
                    }
                    List<String> clan_members = date.getClanMembers(commandSender.getName());
                    if (!clan_members.contains(strings[1])) {
                        commandSender.sendMessage(ChatColor.RED + "Этого игрока нет в клане");
                        return true;
                    }
                    PlayerObject playerObject = new PlayerObject(config);
                    playerObject.set_player(strings[1]);
                    for (String player : clan_members) {
                        Player p = commandSender.getServer().getPlayer(player);
                        if (p == null) {
                            continue;
                        }
                        p.sendMessage(new TextComponent(ChatColor.GREEN + "Игрок "), playerObject.get_player_without_clan(), new TextComponent(" стал новым лидером клана"));
                    }
                    date.promoteClanMember(strings[1]);
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "rename" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    String new_name = String.join(" ", strings).substring(7);
                    if (date.checkClanName(new_name)) {
                        commandSender.sendMessage(ChatColor.RED + "Клан с таким названием уже существует");
                        return true;
                    }
                    date.renameClan(new_name, commandSender.getName());
                    commandSender.sendMessage(ChatColor.GREEN + "Новое имя клана установлено");
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "tag" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    String tag = String.join(" ", strings).substring(4);
                    if (tag.length() > 7) {
                        commandSender.sendMessage(ChatColor.RED + "Тег не может быть больше 7-ми символов");
                        return true;
                    }
                    if (date.checkClanTag(tag)) {
                        commandSender.sendMessage(ChatColor.RED + "Такой тег уже существует");
                        return true;
                    }
                    date.setClanTag(commandSender.getName(), tag);
                    for (String member : date.getClanMembers(commandSender.getName())) {
                        if (commandSender.getServer().getPlayer(member) == null) {
                            continue;
                        }
                        config.getConfig("players").set("players." + member + ".clan", Objects.requireNonNull(config.getConfig("players").getString("players." + member + ".clan")).substring(0, 2) + tag);
                        config.saveConfig("players");
                        TabManager.getTabString(Objects.requireNonNull(commandSender.getServer().getPlayer(member)), config);
                    }
                    commandSender.sendMessage(ChatColor.GREEN + "Тег установлен");
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            case "color" -> {
                try {
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".donate") < 100) {
                        commandSender.sendMessage(ChatColor.RED + "Это платная функция, ознакомьтесь с возможностью её получения по команде /donate");
                        return true;
                    }
                    if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                        return true;
                    }
                    if (!date.isClanOwner(commandSender.getName())) {
                        commandSender.sendMessage(ChatColor.RED + "Вы не являетесь лидером этого клана");
                        return true;
                    }
                    String color = getColor(strings[1]);
                    if (color.equals("")) {
                        commandSender.sendMessage(ChatColor.RED + "Не верный цвет");
                        return true;
                    }
                    date.setClanColor(commandSender.getName(), color);
                    for (String member : date.getClanMembers(commandSender.getName())) {
                        if (commandSender.getServer().getPlayer(member) == null) {
                            continue;
                        }
                        config.getConfig("players").set("players." + member + ".clan",ChatColor.COLOR_CHAR + color + Objects.requireNonNull(config.getConfig("players").getString("players." + member + ".clan")).substring(2));
                        config.saveConfig("players");
                        TabManager.getTabString(Objects.requireNonNull(commandSender.getServer().getPlayer(member)), config);
                    }
                    commandSender.sendMessage(ChatColor.COLOR_CHAR + color + "Цвет клана изменён");
                } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
                    throwables.printStackTrace();
                }
                return true;
            }
            default -> {
                if (config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id") == 0) {
                    commandSender.sendMessage(ChatColor.RED + "Вы не находитесь в клане");
                    return true;
                }
                String message = String.join(" ", strings).replace("&", "§");
                if (message.equals("")) {
                    return true;
                }
                int clan_id = config.getConfig("players").getInt("players." + commandSender.getName() + ".clan_id");
                PlayerObject playerObject = new PlayerObject(config);
                playerObject.set_player(commandSender.getName(), ": ");
                for (Player player : commandSender.getServer().getOnlinePlayers()) {
                    if (config.getConfig("players").getInt("players." + player.getName() + ".clan_id") != clan_id) {
                        continue;
                    }
                    if (message.contains(player.getName()) && !player.getName().equals(commandSender.getName())) {
                        if (config.getConfig("players").getBoolean("players." + player.getName() + ".notifications")) {
                            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                        }
                        player.sendMessage(new TextComponent(ChatColor.GRAY + "CLAN> "), playerObject.get_player_without_clan(), new TextComponent(ChatColor.YELLOW + message));
                    } else {
                        player.sendMessage(new TextComponent(ChatColor.GRAY + "CLAN> "), playerObject.get_player_without_clan(), new TextComponent(ChatColor.WHITE + message));
                    }
                }
                return true;
            }
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> turns = new ArrayList<>();
        if (strings.length == 1) {
            turns.add("create");
            turns.add("accept");
            turns.add("leave");
            turns.add("invite");
            turns.add("kick");
            turns.add("promote");
            turns.add("rename");
            turns.add("tag");
            turns.add("color");
            turns.add("delete");
            turns.add("info");
        }
        if (strings.length == 2) {
            switch (strings[0]) {
                case "create":
                case "leave":
                case "rename":
                case "tag":
                case "delete":
                case "info":
                    break;
                case "color":
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
                    break;
                default:
                    return null;
            }
        }
        return turns;
    }

    private String getColor(String color) {
        return switch (color) {
            case "white" -> "f";
            case "gray" -> "7";
            case "dark_gray" -> "8";
            case "black" -> "0";
            case "red" -> "c";
            case "dark_red" -> "4";
            case "green" -> "a";
            case "dark_green" -> "2";
            case "blue" -> "9";
            case "dark_blue" -> "1";
            case "aqua" -> "b";
            case "dark_aqua" -> "3";
            case "yellow" -> "e";
            case "gold" -> "6";
            case "pink" -> "d";
            case "purple" -> "5";
            default -> "";
        };
    }
}
