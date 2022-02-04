package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import itsokay.chatmanager.PlayerObject;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;
import java.util.Objects;

public class MCommand implements CommandExecutor {
    public ConfigManager config;
    public MCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("m")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        if (args.length == 0 || args.length == 1) {
            return false;
        }
        String nick = args[0];
        args[0] = "";
        if (nick.equalsIgnoreCase(sender.getName())) {
            sender.sendMessage("§e[§aYou§e -> §aYou§e]" + String.join(" ", args));
            return true;
        }
        List<String> ignore_players = config.getConfig("players").getStringList("players." + sender.getName() + ".ignore_players");
        int ignore_chat = config.getConfig("players").getInt("players." + sender.getName() + ".ignore_chat");
        List<String> ignore_players_second = config.getConfig("players").getStringList("players." + sender.getName() + ".ignore_players");
        int ignore_chat_second = config.getConfig("players").getInt("players." + sender.getName() + ".ignore_chat");
        PlayerObject playerObject = new PlayerObject(config);
        playerObject.set_player(sender.getName());
        if (ignore_chat == 1 || ignore_chat == 3 || ignore_players.contains(nick.toLowerCase())) {
            sender.sendMessage("§cВы отключили лс или этот игрок находится у вас в чёрном списке");
            return true;
        }
        if (ignore_chat_second == 1 || ignore_chat_second == 3 || ignore_players_second.contains(sender.getName().toLowerCase())) {
            sender.sendMessage("§cИгрок отключил лс или добавил Вас в чёрный список");
            return true;
        }

        Player player = sender.getServer().getPlayer(nick);
        if (player == null) {
            sender.sendMessage("§cНе верный ник игрока");
            return true;
        }
        PlayerObject player_two = new PlayerObject(config);
        player_two.set_player(player.getName());
        player.sendMessage(new TextComponent("§e["), playerObject.get_player_without_clan(), new TextComponent("§e -> §aYou§e]" + String.join(" ", args)));
        sender.sendMessage(new TextComponent("§e[§aYou§e -> §a"), player_two.get_player_without_clan(), new TextComponent("§e]" + String.join(" ", args)));
        config.getConfig("players").set("players." + player.getName() + ".last_pm", sender.getName());
        config.getConfig("players").set("players." + sender.getName() + ".last_pm", player.getName());
        try {
            config.saveConfig("players");
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
        return true;
    }
}
