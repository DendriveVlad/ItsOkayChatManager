package itsokay.chatmanager;

import itsokay.chatmanager.commands.*;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.*;
import org.bukkit.permissions.PermissionAttachment;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.sql.SQLException;
import java.util.*;

public final class ChatManager extends JavaPlugin implements Listener {
    public DBGetter date;
    public ConfigManager config;

    @Override
    public void onEnable() {
        this.date = new DBGetter();
        this.config = new ConfigManager(this);

        new HelpCommand(this);
        new MeCommand(this);
        new MCommand(this);
        new RCommand(this);
        new ACommand(this);
        new NotifyCommand(this);
        new ClanCommand(this);
        new IgnoreCommand(this);
        new WatchCommand(this);
        new BanCommand(this);
        new DonationCommand(this);
        new DonateCommands(this);

        Bukkit.getServer().getPluginManager().registerEvents(this, this);
        this.getLogger().info("§aHi");

    }

    @Override
    public void onDisable() {
        this.getLogger().info("§aBye");
    }

    @EventHandler
    public void JoinEvent(PlayerJoinEvent e) throws SQLException, ClassNotFoundException, IOException, InvalidConfigurationException {
        Player player = e.getPlayer();

        if (!config.getConfig("players").contains("players." + player.getName())) {
            if (date.isModer(player.getName())) {
                config.getConfig("players").set("players." + player.getName() + ".mod", true);

                HashMap<UUID, PermissionAttachment> perms = new HashMap<UUID, PermissionAttachment>();
                PermissionAttachment attachment = player.addAttachment(this);
                perms.put(player.getUniqueId(), attachment);
                PermissionAttachment pperms = perms.get(player.getUniqueId());
                pperms.setPermission("coreprotect.co", true);
                pperms.setPermission("coreprotect.inspect", true);
                pperms.setPermission("coreprotect.rollback", true);
                pperms.setPermission("coreprotect.restore", true);
                pperms.setPermission("coreprotect.lookup ", true);
            }
        }

        if (config.getConfig("players").getBoolean("players." + player.getName() + ".mod")) {
            e.setJoinMessage("");
            if (player.getGameMode() != GameMode.SPECTATOR) {
                player.setGameMode(GameMode.SPECTATOR);
            }
            for (Player p : this.getServer().getOnlinePlayers()) {
                p.hidePlayer(this, player);
            }
            player.performCommand("watch @f");
            config.saveConfig("players");
            return;
        }

        config.getConfig("players").set("players." + player.getName() + ".language", date.getLanguage(player.getName()));
        config.getConfig("players").set("players." + player.getName() + ".name_color", date.getNickColor(player.getName()));
        config.getConfig("players").set("players." + player.getName() + ".clan", date.getClanTag(player.getName()));
        config.getConfig("players").set("players." + player.getName() + ".clan_id", date.getClanID(player.getName()));
        config.getConfig("players").set("players." + player.getName() + ".donate", date.getDonationState(player.getName()));
        config.getConfig("players").set("players." + player.getName() + ".notifications", date.getNotifyAccept(player.getName()));
        config.getConfig("players").set("players." + player.getName() + ".clan_invite", "");
        config.getConfig("players").set("players." + player.getName() + ".last_pm", "");

        String[] playerIgnores = date.getPlayerIgnores(player.getName());

        config.getConfig("players").set("players." + player.getName() + ".ignore_chat", Integer.parseInt(playerIgnores[0]));

        List<String> ignore_players = Arrays.asList(playerIgnores[1].substring(1).split(" "));

        config.getConfig("players").set("players." + player.getName() + ".ignore_players", ignore_players);

        getLogger().info(String.valueOf(Objects.requireNonNull(config.getConfig("players").getConfigurationSection("players")).getKeys(false)));

        PlayerObject playerObject = new PlayerObject(config);
        playerObject.set_player(player.getName());
        e.setJoinMessage("");
        for (Player p : this.getServer().getOnlinePlayers()) {
            p.sendMessage(playerObject.get_player(), new TextComponent("§e в игре"));
        }
        TabManager.getTabString(player, config);
        config.saveConfig("players");
    }

    @EventHandler
    public void QuitEvent(PlayerQuitEvent e) throws SQLException, ClassNotFoundException, IOException, InvalidConfigurationException {
        Player player = e.getPlayer();
        if (config.getConfig("players").getBoolean("players." + player.getName() + ".mod")) {
            e.setQuitMessage("");
            date.moderInfoClear(player.getName());
            return;
        }

        PlayerObject playerObject = new PlayerObject(config);
        playerObject.set_player(player.getName());
        if (getServer().getOnlinePlayers().size() == 1) {
            config.getConfig("messages").set("messages", "");
        }
        config.getConfig("players").set("players." + player.getName() + ".clan_invite", "");
        config.getConfig("players").set("players." + player.getName() + ".last_pm", "");
        config.saveConfig("messages");
        config.saveConfig("players");
        e.setQuitMessage("");
        for (Player p : this.getServer().getOnlinePlayers()) {
            p.sendMessage(playerObject.get_player(), new TextComponent("§e покидает игру (§c" + e.getReason() + "§e)"));
        }
    }

    @EventHandler
    public void onMessage(PlayerChatEvent e) throws IOException, InvalidConfigurationException {
        e.setCancelled(true);
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            return;
        }

        ConfigChecker checker = new ConfigChecker();

        String message = e.getMessage();
        if (message.contains("Bad Response")) {
            message = message.replace("Bad Response", "Bad §§Response");
        }
        if (message.contains("[Matrix]")) {
            message = message.replace("[Matrix]", "[§§Matrix]");
        }
        if (message.contains("&") && config.getConfig("players").getInt("players." + player.getName() + ".donate") >= 300) {
            message = message.replace("&", "§");
        }
        int id = checker.getMessageID(config);
        PlayerObject playerObject = new PlayerObject(config);
        this.getLogger().info("§a" + player.getName() + " §7>> " + message);

        playerObject.set_message(player.getName(), message, id);

        for (Player p : this.getServer().getOnlinePlayers()) {
            if (p.getGameMode() == GameMode.SPECTATOR) {
                p.sendMessage(playerObject.get_default_message());
                continue;
            }
            int ignore_chat = config.getConfig("players").getInt("players." + p.getName() + ".ignore_chat");
            List<String> ignore_players = config.getConfig("players").getStringList("players." + p.getName() + ".ignore_players");
            if (ignore_chat > 1 || ignore_players.contains(player.getName().toLowerCase())) {
                if (p == player) {
                    player.sendMessage(playerObject.get_default_message());
                }
                continue;
            }
            if (message.toLowerCase().contains(p.getName().toLowerCase()) && !p.getName().equals(player.getName())) {
                if (config.getConfig("players").getBoolean("players." + p.getName() + ".notifications")) {
                    p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1, 1);
                }
                p.sendMessage(playerObject.get_notify_message());
            } else {
                p.sendMessage(playerObject.get_default_message());
            }
        }
        checker.addMessage(config, id, player.getName(), message);
        checker.deleteMessages(config);
    }
    @EventHandler
    public void onPlayerJourneyWorld(PlayerChangedWorldEvent e) {
        e.getFrom().setGameRule(GameRule.REDUCED_DEBUG_INFO, true);
        getLogger().info(ChatColor.GREEN + e.getPlayer().getName() + ChatColor.GOLD + " travel from " + ChatColor.RED + e.getFrom().getName() + ChatColor.GOLD + " to -> " + ChatColor.RED + e.getPlayer().getWorld().getName());
    }

    @EventHandler
    public void OnPlayerCommand(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage();
        if (message.contains("Bad Response")) {
            message = message.replace("Bad Response", "Bad §§Response");
        }
        if (message.contains("[Matrix]")) {
            message = message.replace("[Matrix]", "[§§Matrix]");
        }
        Player player = e.getPlayer();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            if (!Arrays.asList(AllowedCommands.getModersCommands()).contains(message.split(" ")[0].replace("/", ""))) {
                e.setCancelled(true);
                e.getPlayer().performCommand("help");
            }
            this.getLogger().info("§9" + e.getPlayer().getName() + "§e: " + e.getMessage());
            return;
        }
        if (!Arrays.asList(AllowedCommands.getAllowedCommands()).contains(message.split(" ")[0].replace("/", ""))) {
            e.setCancelled(true);
            e.getPlayer().performCommand("help");
        }
        this.getLogger().info("§a" + e.getPlayer().getName() + "§e: " + e.getMessage());
    }

    @EventHandler
    public void onTabComplete(PlayerCommandSendEvent e) {
        List<String> blockedCommands = new ArrayList<>();
        Player player = e.getPlayer();
        if (config.getConfig("players").getBoolean("players." + player.getName() + ".moder")) {
            for (String cmd : e.getCommands()) {
                if (!Arrays.asList(AllowedCommands.getModersCommands()).contains(cmd)) {
                    blockedCommands.add(cmd);
                }
            }
            e.getCommands().removeAll(blockedCommands);
            return;
        }

        for (String cmd : e.getCommands()) {
            if (!Arrays.asList(AllowedCommands.getAllowedCommands()).contains(cmd) || Arrays.asList(AllowedCommands.getHiddenCommands()).contains(cmd)) {
                blockedCommands.add(cmd);
            }
        }
        e.getCommands().removeAll(blockedCommands);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent e) {
        Player player = e.getEntity();
        if (player.getGameMode() == GameMode.SPECTATOR) {
            e.setDeathMessage("");
            return;
        }
        PlayerObject playerObject = new PlayerObject(config);
        playerObject.set_player(player.getName());
        for (Player p : this.getServer().getOnlinePlayers()) {
            p.sendMessage(playerObject.get_death_message(Objects.requireNonNull(e.getDeathMessage()), player.getName().length() + 1));
        }
        e.setDeathMessage("");
    }
}
