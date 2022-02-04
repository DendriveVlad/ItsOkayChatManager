package itsokay.chatmanager;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;

public class PlayerObject {
    ConfigManager config;

    TextComponent clan = new TextComponent();
    TextComponent nick = new TextComponent();
    TextComponent message = new TextComponent();
    TextComponent notify_message = new TextComponent();
    TextComponent answer_player = new TextComponent();

    public PlayerObject(ConfigManager config) {
        this.config = config;
    }

    public void set_message(String player_nick, String message, int message_id) {
        set_player(player_nick, ": ");

        this.message.setText(ChatColor.GRAY + message);
        this.message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/a " + message_id + " "));
        this.message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите, чтобы ответить").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

        this.notify_message.setText(ChatColor.YELLOW + message);
        this.notify_message.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/a " + message_id + " "));
        this.notify_message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите, чтобы ответить").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
    }

    public void set_answer_message(String player_nick, String original_message, String author, String message, int message_id) {
        set_message(player_nick, message, message_id);

        if (original_message.length() > 192) {
            original_message = original_message.substring(0, 192) + "...";
        }

        this.answer_player.setText(ChatColor.DARK_GRAY + "To " + author + " -> ");
        this.answer_player.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/a " + message_id + " "));
        this.answer_player.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(original_message).color(net.md_5.bungee.api.ChatColor.GREEN).create()));
    }

    public TextComponent get_default_message() {
        return new TextComponent(this.clan, this.nick, this.message);
    }

    public TextComponent get_answer_message() {
        return new TextComponent(this.clan, this.nick, this.answer_player, this.message);
    }

    public TextComponent get_notify_message() {
        return new TextComponent(this.clan, this.nick, this.notify_message);
    }

    public TextComponent get_answer_notify_message() {
        return new TextComponent(this.clan, this.nick, this.answer_player, this.notify_message);
    }


    public void set_player(String player_nick, String additional) {
        String clan_tag = config.getConfig("players").getString("players." + player_nick + ".clan");
        int clan_id = config.getConfig("players").getInt("players." + player_nick + ".clan_id");
        assert clan_tag != null;
        if (!clan_tag.equals("")) {
            if (clan_tag.length() == 2) {
                this.clan.setText(ChatColor.YELLOW + "[...] ");
            } else {
                this.clan.setText(ChatColor.YELLOW + "[" + clan_tag + ChatColor.YELLOW + "] ");
            }
            this.clan.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan info " + clan_id));
            this.clan.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите, чтобы узнать информацию о клане").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
        }

        this.nick.setText(ChatColor.COLOR_CHAR + config.getConfig("players").getString("players." + player_nick + ".name_color") + player_nick + additional);
        this.nick.setClickEvent(new ClickEvent(ClickEvent.Action.SUGGEST_COMMAND, "/m " + player_nick + " "));
        this.nick.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите, чтобы написать игроку в лс").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
    }

    public void set_player(String player_nick) {
        set_player(player_nick, "");
    }

    public TextComponent get_player() {
        return new TextComponent(this.clan, this.nick);
    }

    public TextComponent get_player_without_clan() {
        return this.nick;
    }

    public TextComponent get_death_message(String reason, int subs) {
        TextComponent message = new TextComponent(ChatColor.RED + "Игрока ");
        message.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(reason.substring(subs)).color(net.md_5.bungee.api.ChatColor.RED).create()));
        TextComponent message2 = new TextComponent(ChatColor.RED + " настигла смерть");
        message2.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(reason.substring(subs)).color(net.md_5.bungee.api.ChatColor.RED).create()));
        return new TextComponent(message, this.get_player(), message2);
    }
}
