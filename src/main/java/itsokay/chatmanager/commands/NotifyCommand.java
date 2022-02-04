package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import itsokay.chatmanager.DBGetter;
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

public class NotifyCommand implements CommandExecutor, TabCompleter {
    public ConfigManager config;
    public DBGetter date = new DBGetter();

    public NotifyCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("notify")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length != 1) {
            return false;
        }
        try {
            if (strings[0].equals("on")) {
                Player player = commandSender.getServer().getPlayer(commandSender.getName());
                assert player != null;
                player.sendMessage(ChatColor.GREEN + "Звуковые уведомления " + ChatColor.BOLD + "ВКЛЮЧЕНЫ");
                player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                date.updateNotify(commandSender.getName(), 1);
                config.getConfig("players").set("players." + player.getName() + ".notifications", true);
            } else if (strings[0].equals("off")) {
                commandSender.sendMessage(ChatColor.GREEN + "Звуковые уведомления " + ChatColor.BOLD + "" + ChatColor.RED + "ОТКЛЮЧЕНЫ");
                date.updateNotify(commandSender.getName(), 0);
                config.getConfig("players").set("players." + commandSender.getName() + ".notifications", false);
            } else {
                return false;
            }
            config.saveConfig("players");
        } catch (SQLException | ClassNotFoundException | IOException | InvalidConfigurationException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length == 1) {
            List<String> turns = new ArrayList<>();
            turns.add("on");
            turns.add("off");
            return turns;
        }
        return null;
    }
}
