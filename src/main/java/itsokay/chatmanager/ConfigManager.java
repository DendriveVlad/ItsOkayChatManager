package itsokay.chatmanager;

import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

public class ConfigManager {
    private final ChatManager plugin;
    private FileConfiguration playersConfig = null;
    private FileConfiguration messagesConfig = null;
    private File playerConfig = null;
    private File messageConfig = null;

    public ConfigManager(ChatManager plugin) {
        this.plugin = plugin;
        saveDefaultConfig();
    }

    public void reloadConfig() {
        if (this.playerConfig == null) {
            this.playerConfig = new File(this.plugin.getDataFolder(), "players.yml");
        }
        if (this.messageConfig == null) {
            this.messageConfig = new File(this.plugin.getDataFolder(), "messages.yml");
        }
        this.playersConfig = YamlConfiguration.loadConfiguration(this.playerConfig);
        this.messagesConfig = YamlConfiguration.loadConfiguration(this.messageConfig);

        InputStream defaultStreamPlayers = this.plugin.getResource("players.yml");
        InputStream defaultStreamMessages = this.plugin.getResource("messages.yml");
        if (defaultStreamPlayers != null) {
            this.playersConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStreamPlayers)));
        }
        if (defaultStreamMessages != null) {
            this.messagesConfig.setDefaults(YamlConfiguration.loadConfiguration(new InputStreamReader(defaultStreamMessages)));
        }
    }

    public FileConfiguration getConfig(String config) {
        if (config.equals("players")) {
            if (playersConfig == null) {
                reloadConfig();
            }
            return this.playersConfig;
        } else if (config.equals("messages")) {
            if (messagesConfig == null) {
                reloadConfig();
            }
            return this.messagesConfig;
        }
        return null;
    }

    public void saveConfig(String config) throws IOException, InvalidConfigurationException {
        if (config.equals("players")) {
            if (this.playersConfig == null || this.playerConfig == null) {
                return;
            }
            this.getConfig("players").save(this.playerConfig);
            this.getConfig("players").load(this.playerConfig);
        } else if (config.equals("messages")) {
            if (this.messagesConfig == null || this.messageConfig == null) {
                return;
            }
            this.getConfig("messages").save(this.messageConfig);
            this.getConfig("messages").load(this.messageConfig);
        }
    }

    public void saveDefaultConfig() {
        if (this.playerConfig == null) {
            this.playerConfig = new File(this.plugin.getDataFolder(), "players.yml");
        }
        if (this.messageConfig == null) {
            this.messageConfig = new File(this.plugin.getDataFolder(), "messages.yml");
        }

        if (!this.playerConfig.exists()) {
            this.plugin.saveResource("players.yml", false);
        }if (!this.messageConfig.exists()) {
            this.plugin.saveResource("messages.yml", false);
        }
    }
}
