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
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class WatchCommand implements CommandExecutor, TabCompleter {
    ChatManager me;
    DBGetter date = new DBGetter();
    public ConfigManager config;

    public WatchCommand(ChatManager plugin) {
        me = plugin;
        Objects.requireNonNull(plugin.getCommand("watch")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        try {
            if (!date.isModer(sender.getName())) {
                return true;
            }
            Player followPlayer = null;
            Player player = (Player) sender;
            if (args[0].equals("@f")) {
                sender.sendMessage(date.getModerNick(sender.getName()));
                for (Player p: sender.getServer().getOnlinePlayers()) {
                    if (!date.isModer(p.getName()) && !p.getName().equalsIgnoreCase(date.getModerNick(sender.getName()))) {
                        followPlayer = p;
                        break;
                    }
                }
                if (followPlayer == null) {
                    ((Player) sender).kickPlayer("На сервере нет игроков");
                    return true;
                }
            } else {
                if (date.isModer(args[0])) {
                    player.sendMessage(ChatColor.RED + "Вы не можете следить за модератором");
                    return true;
                }
                followPlayer = me.getServer().getPlayer(args[0]);
                if (followPlayer == null) {
                    player.sendMessage(ChatColor.RED + "Не верно введён ник игрока");
                    return true;
                }
                if (followPlayer.getName().equals(date.getModerNick(sender.getName()))) {
                    player.sendMessage(ChatColor.RED + "Вы не можете следить за собой");
                    return true;
                }
            }
            PlayerObject playerObject = new PlayerObject(config);
            playerObject.set_player(followPlayer.getName());
            date.setTargetPlayer(player.getName(), followPlayer.getName());
            player.sendMessage(new TextComponent(ChatColor.BLUE + "Вы наблюдаете за "), playerObject.get_player());
            player.teleport(followPlayer.getLocation());
            seeking(player, followPlayer);
        } catch (SQLException | ClassNotFoundException throwables) {
            throwables.printStackTrace();
        }
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        if (strings.length > 1) {
            return new ArrayList<>();
        }
        return null;
    }

    public void seeking(Player player, Player followPlayer) {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline())
                    this.cancel();
                if (!followPlayer.isOnline()) {
                    player.performCommand("watch @f");
                    this.cancel();
                }
                try {
                    if (!date.getTargetPlayer(player.getName()).equals(followPlayer.getName())) {
                        this.cancel();
                    }
                    if (player.getLocation().distance(Objects.requireNonNull(followPlayer.getLocation())) > 50d && date.getTargetPlayer(player.getName()).equals(followPlayer.getName())) {
                        player.teleport(followPlayer.getLocation());
                        player.sendMessage(ChatColor.BLUE + "Вы улетели слишком далеко от наблюдаемого игрока.");
                    }
                } catch (SQLException | ClassNotFoundException throwables) {
                    throwables.printStackTrace();
                }
            }
        }.runTaskTimer(me,0, 60);
    }
}

