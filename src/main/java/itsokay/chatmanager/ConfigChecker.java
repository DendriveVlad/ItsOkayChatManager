package itsokay.chatmanager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.Objects;
import java.util.Random;

public class ConfigChecker {
    public int getMessageID(ConfigManager config) {
        int id = new Random().nextInt(999);
        while (config.getConfig("messages").contains(String.valueOf(id))) {
            id = new Random().nextInt(999);
        }
        return id;
    }

    public void addMessage(ConfigManager config, int id, @NotNull String player, String content) throws IOException, InvalidConfigurationException {
        config.getConfig("messages").set("messages." + id + ".date", System.currentTimeMillis() / 1000);
        config.getConfig("messages").set("messages." + id + ".author", player);
        config.getConfig("messages").set("messages." + id + ".message", content);
        config.saveConfig("messages");
    }

    public void deleteMessages(ConfigManager config) throws IOException, InvalidConfigurationException {
        for (String key : Objects.requireNonNull(config.getConfig("messages").getConfigurationSection("messages")).getKeys(false)) {
            if (System.currentTimeMillis() / 1000 - config.getConfig("messages").getInt("messages." + key + ".date") >= 600) {
                config.getConfig("messages").set("messages." + key, null);
            }
        }
        config.saveConfig("messages");
    }
}
