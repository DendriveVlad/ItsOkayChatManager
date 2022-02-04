package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigChecker;
import itsokay.chatmanager.ConfigManager;
import itsokay.chatmanager.PlayerObject;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class ACommand implements CommandExecutor, TabCompleter {
    public ConfigManager config;
    public ACommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("a")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length <= 1) {
            return false;
        }
        try {
            String original_message = config.getConfig("messages").getString("messages." + args[0] + ".message");
            String author = config.getConfig("messages").getString("messages." + args[0] + ".author");
            assert original_message != null;
            if (original_message.equals("")) {
                return true;
            }
            args[0] = "";
            String message = String.join(" ", args).substring(1);
            if (message.contains("&") && config.getConfig("players").getInt("players." + sender.getName() + ".donate") >= 300) {
                message = message.replace("&", "§");
            }

            ConfigChecker checker = new ConfigChecker();
            int id = checker.getMessageID(config);

            PlayerObject playerObject = new PlayerObject(config);
            playerObject.set_answer_message(sender.getName(), original_message, author, message, id);

            for (Player p : sender.getServer().getOnlinePlayers()) {
                if ((p.getName().equals(author) || message.toLowerCase().contains(p.getName().toLowerCase())) && !p.getName().equals(sender.getName())) {
                    int ignore_chat = config.getConfig("players").getInt("players." + p.getName() + ".ignore_chat");
                    List<String> ignore_players = config.getConfig("players").getStringList("players." + p.getName() + ".ignore_players");
                    if (ignore_chat > 1 || ignore_players.contains(sender.getName().toLowerCase())) {
                        if (p.getName().equals(sender.getName())) {
                            sender.sendMessage(playerObject.get_default_message());
                        }
                        continue;
                    }
                    if (config.getConfig("players").getBoolean("players." + p.getName() + ".notifications")) {
                        p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                    }
                    p.sendMessage(playerObject.get_answer_notify_message());
                } else {
                    p.sendMessage(playerObject.get_answer_message());
                }
            }
            checker.addMessage(config, id, sender.getName(), message);
            checker.deleteMessages(config);
        } catch (IOException | InvalidConfigurationException throwables) {
            throwables.printStackTrace();
        } catch (NumberFormatException throwables) {
            return false;
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length > 1) {
            return null;
        }
        List<String> warning = new ArrayList<>();
        warning.add("НЕ ИСПОЛЬЗУЙТЕ ЭТУ КОМАНДУ САМИ!!!");
        return warning;
    }
}
