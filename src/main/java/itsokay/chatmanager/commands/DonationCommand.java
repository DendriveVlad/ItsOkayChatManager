package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
import itsokay.chatmanager.ConfigManager;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class DonationCommand implements CommandExecutor, TabCompleter {
    public ConfigManager config;

    public DonationCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("donate")).setExecutor(this);
        config = new ConfigManager(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        int donate_value = config.getConfig("players").getInt("players." + sender.getName() + ".donate");
        StringBuilder donation_progress_line = new StringBuilder(ChatColor.GREEN + "[");
        StringBuilder active_permissions = new StringBuilder(ChatColor.GREEN + "");
        for (int i = 0; i <= 50; i++) {
            if (i * 10 - donate_value <= 10 && i * 10 - donate_value >= 0) {
                donation_progress_line.append(ChatColor.GRAY);
                active_permissions.append(ChatColor.GRAY);
            }
            donation_progress_line.append("|");
            switch (i) {
                case 49:
                    active_permissions.append("- Фейк выход с сервера (/fakeleave) - 500р\n- Фейк смерть (/fakedeath) - 500р.\n\n");
                    break;
                case 29:
                    active_permissions.append("- Цветной чат (&[номер цвета] в любой части сообщения,\n/colornumbers для получения цветовых номеров) - 300р.\n");
                    break;
                case 19:
                    active_permissions.append("- Цветной ник (/nickcolor [color]) - 200р.\n");
                    break;
                case 9:
                    active_permissions.append("- Цветной тег клана (/clan color [color]) - 100р.\n");
                    break;
                case 4:
                    active_permissions.append("- Объём выделенной области (/pos1, /pos2) - 50р.\n");
            }
        }
        donation_progress_line.append(ChatColor.GREEN);
        donation_progress_line.append("]\n\n");

        TextComponent kindness_line = new TextComponent(ChatColor.GREEN + "Полоса доброты:\n" + donation_progress_line);
        kindness_line.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Вы поддержали нас в сумме на " + donate_value + "р.").color(net.md_5.bungee.api.ChatColor.GREEN).create()));

        TextComponent final_message = new TextComponent(kindness_line, new TextComponent(ChatColor.GREEN + "За поддержку Вы получите:\n" + active_permissions));

        TextComponent donate_link = new TextComponent(ChatColor.GOLD + (ChatColor.UNDERLINE + "Чтобы поддержать проект нажмите на эту надпись и введите свой ник в игре в поле с ником"));
        donate_link.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Переход на сайт").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
        donate_link.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "http://itsokay.ru:29793/donate"));

        sender.sendMessage(ChatColor.GOLD + "\n------------Donation------------");
        sender.sendMessage(final_message, donate_link);
        sender.sendMessage(ChatColor.GOLD + "--------------------------------\n ");
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        return new ArrayList<>();
    }
}
