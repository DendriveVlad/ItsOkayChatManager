package itsokay.chatmanager;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public class TabManager {
    public static void getTabString(Player player, ConfigManager config) {
        String clan = config.getConfig("players").getString("players." + player.getName() + ".clan");
        assert clan != null;
        if (clan.equals("")) {
            player.setPlayerListName(ChatColor.COLOR_CHAR + config.getConfig("players").getString("players." + player.getName() + ".name_color") + player.getName());
        } else {
            if (clan.length() == 2) {
                clan = clan + "...";
            }
            player.setPlayerListName(ChatColor.YELLOW + "[" + clan + ChatColor.YELLOW + "] " + ChatColor.COLOR_CHAR + config.getConfig("players").getString("players." + player.getName() + ".name_color") + player.getName());
        }
    }
}
