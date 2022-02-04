package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import itsokay.chatmanager.DBGetter;
import org.bukkit.ChatColor;
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

public class IgnoreCommand implements CommandExecutor, TabCompleter {
    public ConfigManager config;
    DBGetter date = new DBGetter();

    public IgnoreCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("ignore")).setExecutor(this);
        Objects.requireNonNull(plugin.getCommand("unignore")).setExecutor(this);
        config = new ConfigManager(plugin);
    }



    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 0) {
            return false;
        }
        try {
            List<String> ignore_players = config.getConfig("players").getStringList("players." + commandSender.getName() + ".ignore_players");
            int ignore_chat = config.getConfig("players").getInt("players." + commandSender.getName() + ".ignore_chat");
            switch (s) {
                case "ignore" -> {
                    switch (strings[0]) {
                        case "@all" -> {
                            if (ignore_chat == 3) {
                                commandSender.sendMessage(ChatColor.RED + "У Вас уже отключены все сообщения в чате");
                                return true;
                            }
                            date.setPrivate(commandSender.getName(), 3);
                            config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 3);
                            commandSender.sendMessage(ChatColor.GREEN + "Вы отключили все сообщения от игроков в чате");
                        }
                        case "@global" -> {
                            if (ignore_chat >= 2) {
                                commandSender.sendMessage(ChatColor.RED + "У Вас уже отключены все сообщения в " + ChatColor.GOLD + "глобальном" + ChatColor.RED + " чате");
                                return true;
                            } else if (ignore_chat == 1) {
                                date.setPrivate(commandSender.getName(), 3);
                                config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 3);
                                commandSender.sendMessage(ChatColor.GREEN + "Вы отключили все сообщения от игроков в чате");
                                break;
                            }
                            date.setPrivate(commandSender.getName(), 2);
                            config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 2);
                            commandSender.sendMessage(ChatColor.GREEN + "Вы отключили все сообщения от игроков в " + ChatColor.GOLD + "глобальном" + ChatColor.GREEN + " чате");
                        }
                        case "@private" -> {
                            if (ignore_chat == 2) {
                                date.setPrivate(commandSender.getName(), 3);
                                config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 3);
                                commandSender.sendMessage(ChatColor.GREEN + "Вы отключили все сообщения от игроков в чате");
                                break;
                            } else if (ignore_chat >= 1) {
                                commandSender.sendMessage(ChatColor.RED + "У Вас уже отключены " + ChatColor.GOLD + "личные сообщения");
                                return true;
                            }
                            date.setPrivate(commandSender.getName(), 1);
                            config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 1);
                            commandSender.sendMessage(ChatColor.GREEN + "Вы отключили " + ChatColor.GOLD + "личные сообщения");
                        }
                        default -> {
                            if (commandSender.getName().equalsIgnoreCase(strings[0])) {
                                commandSender.sendMessage(ChatColor.RED + "Вы на столько себя ненавидите?");
                                return true;
                            }
                            if (ignore_players.contains(strings[0].toLowerCase())) {
                                commandSender.sendMessage(ChatColor.RED + "Этот игрок уже добавлен в список игнорируемых");
                                return true;
                            }
                            if (strings[0].length() < 3) {
                                commandSender.sendMessage(ChatColor.RED + "Не верный ник игрока");
                                return true;
                            }
                            for (int i = 0; i < strings[0].length(); i++) {
                                if (!"abcdefghijklmnopqrstuvwxyz1234567890_".contains(strings[0].toLowerCase().substring(i, i + 1))) {
                                    commandSender.sendMessage(ChatColor.RED + "Не верный ник игрока");
                                    return true;
                                }
                            }
                            date.addIgnore(commandSender.getName(), strings[0]);
                            ignore_players.add(strings[0].toLowerCase());
                            config.getConfig("players").set("players." + commandSender.getName() + ".ignore_players", ignore_players);
                            config.saveConfig("players");
                            commandSender.sendMessage(ChatColor.GREEN + strings[0] + " добавлен в список игнорируемых");
                        }
                    }
                    return true;
                }
                case "unignore" -> {
                    switch (strings[0]) {
                        case "@global":
                            if (ignore_chat == 3) {
                                date.setPrivate(commandSender.getName(), 1);
                                config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 1);
                                config.saveConfig("players");
                                commandSender.sendMessage(ChatColor.GREEN + "Игнорирование" + ChatColor.GOLD + " глобального " + ChatColor.GREEN + "чата отключено");
                                break;
                            } else if (ignore_chat == 1) {
                                commandSender.sendMessage(ChatColor.RED + "Глобальный чат уже включен");
                                break;
                            }
                        case "@private":
                            if (ignore_chat == 3) {
                                date.setPrivate(commandSender.getName(), 2);
                                config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 2);
                                config.saveConfig("players");
                                commandSender.sendMessage(ChatColor.GREEN + "Вы открыли " + ChatColor.GOLD + "личные сообщения");
                                break;
                            } else if (ignore_chat == 2 && strings[0].equals("@private")) {
                                commandSender.sendMessage(ChatColor.RED + "Личные сообщения уже включены");
                                break;
                            }
                        case "@all":
                            if (ignore_chat == 0) {
                                commandSender.sendMessage(ChatColor.RED + "Все сообщения в чате уже включены");
                                return true;
                            }
                            date.setPrivate(commandSender.getName(), 0);
                            config.getConfig("players").set("players." + commandSender.getName() + ".ignore_chat", 0);
                            config.saveConfig("players");
                            commandSender.sendMessage(ChatColor.GREEN + "Игнорирование чата отключено");
                            break;
                        default:
                            if (commandSender.getName().equalsIgnoreCase(strings[0])) {
                                commandSender.sendMessage(ChatColor.RED + "Вы не находитесь у себя в списке игнорируемых");
                                return true;
                            }
                            if (strings[0].length() < 3) {
                                commandSender.sendMessage(ChatColor.RED + "Не верный ник игрока");
                                return true;
                            }
                            for (int i = 0; i < strings[0].length(); i++) {
                                if (!"abcdefghijklmnopqrstuvwxyz1234567890_".contains(strings[0].toLowerCase().substring(i, i + 1))) {
                                    commandSender.sendMessage(ChatColor.RED + "Не верный ник игрока");
                                    return true;
                                }
                            }
                            if (!ignore_players.contains(strings[0].toLowerCase())) {
                                commandSender.sendMessage(ChatColor.RED + "Этого игрока нет в списке игнорируемых");
                                return true;
                            }
                            date.removeIgnore(commandSender.getName(), strings[0]);
                            ignore_players.remove(strings[0].toLowerCase());
                            commandSender.sendMessage(ChatColor.GREEN + strings[0] + " удалён из списка игнорируемых");
                            break;
                    }
                    return true;
                }
            }
        } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        List<String> turns = new ArrayList<>();
        if (strings.length == 1) {
            turns.add("@all");
            turns.add("@global");
            turns.add("@private");
            for (Player p : commandSender.getServer().getOnlinePlayers()) {
                if (p.getName().equals(commandSender.getName())) {
                    continue;
                }
                turns.add(p.getName());
            }
        }
        return turns;
    }
}
