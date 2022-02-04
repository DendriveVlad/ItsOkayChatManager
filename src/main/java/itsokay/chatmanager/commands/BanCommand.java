package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import itsokay.chatmanager.DBGetter;
import itsokay.chatmanager.PlayerObject;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class BanCommand implements CommandExecutor, TabCompleter {
    public ConfigManager config;
    public BanCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("ban")).setExecutor((CommandExecutor) this);
        Objects.requireNonNull(plugin.getCommand("unban")).setExecutor((CommandExecutor) this);
        config = new ConfigManager(plugin);
    }
    DBGetter date = new DBGetter();

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        try {
            if (!config.getConfig("players").getBoolean("players." + sender.getName() + ".mod")) {
                return true;
            }
            String player = args[0];
            args[0] = "";
            if (config.getConfig("players").getBoolean("players." + player + ".mod")) {
                sender.sendMessage(ChatColor.RED + "Нельзя производить действия над аккаунтами модераторов");
                return true;
            }
            PlayerObject playerObject = new PlayerObject(config);
            playerObject.set_player(player);
            switch (command.getName()) {
                case "ban" -> {
                    if (!date.ban(sender.getName(), player, String.join(" ", args))) {
                        ((Player) sender).kickPlayer(ChatColor.RED + "Замечены подозрительные действия на аккаунте, ваш аккаунт заблокирован до выяснения обстоятельств.");
                    } else {
                        sender.sendMessage(new TextComponent(ChatColor.GREEN + "Игрок "), playerObject.get_player(), new TextComponent(ChatColor.GREEN + " заблокирован"));
                    }
                    return true;
                }
                case "unban" -> {
                    date.unban(player);
                    sender.sendMessage(new TextComponent(ChatColor.GREEN + "Игрок "), playerObject.get_player(), new TextComponent(ChatColor.GREEN + " разблокирован"));
                    return true;
                }
            }
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length > 1) {
            return new ArrayList<>();
        }
        return null;
    }
}
