package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class RCommand implements CommandExecutor {
    public ConfigManager config;
    public RCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("r")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length == 0) {
            return false;
        }
        String nick_to = config.getConfig("players").getString("players." + sender.getName() + ".last_pm");
        assert nick_to != null;
        if (nick_to.equals("")) {
            sender.sendMessage(ChatColor.RED + "Вам некому отвечать");
            return true;
        }
        String request = "m " + nick_to + " " + String.join(" ", args);
        sender.getServer().getLogger().info(request);
        Objects.requireNonNull(sender.getServer().getPlayer(sender.getName())).performCommand(request);
        return true;
    }
}
