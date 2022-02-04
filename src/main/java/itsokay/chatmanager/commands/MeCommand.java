package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import itsokay.chatmanager.PlayerObject;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class MeCommand implements CommandExecutor {
    public ConfigManager config;
    public MeCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("me")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length == 0) {
            return false;
        }
        String message = ChatColor.WHITE + (ChatColor.ITALIC + String.join(" ", args).toLowerCase());

        for (Player p : sender.getServer().getOnlinePlayers()) {
            if (message.contains(p.getName().toLowerCase())) {
                message = message.replace(p.getName().toLowerCase(), ChatColor.YELLOW + (ChatColor.ITALIC + p.getName()) + ChatColor.WHITE + ChatColor.ITALIC);
                if (!p.getName().equals(sender.getName()) && config.getConfig("players").getBoolean("players." + p.getName() + ".notifications")) {
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
            }
        }
        for (Player p : sender.getServer().getOnlinePlayers()) {
            PlayerObject playerObject = new PlayerObject(config);
            int ignore_chat = config.getConfig("players").getInt("players." + p.getName() + ".ignore_chat");
            List<String> ignore_players = config.getConfig("players").getStringList("players." + p.getName() + ".ignore_players");
            playerObject.set_player(sender.getName());
            if (ignore_chat > 1 || ignore_players.contains(sender.getName().toLowerCase())) {
                if (p.getName().equals(sender.getName())) {
                    p.sendMessage(new TextComponent(ChatColor.ITALIC + ""), playerObject.get_player_without_clan(), new TextComponent(" " + message));
                }
                continue;
            }
            p.sendMessage(playerObject.get_player_without_clan(), new TextComponent(" " + message));
        }
        return true;
    }
}
