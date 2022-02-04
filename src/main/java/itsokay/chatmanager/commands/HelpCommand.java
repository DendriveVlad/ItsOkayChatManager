package itsokay.chatmanager.commands;

import itsokay.chatmanager.ChatManager;
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

public class HelpCommand implements CommandExecutor, TabCompleter {
    public HelpCommand(ChatManager plugin) {
        Objects.requireNonNull(plugin.getCommand("help")).setExecutor(this);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, String[] args) {
        sender.sendMessage(ChatColor.GOLD + "\n------------Информация по командам сервера------------");
        sender.sendMessage(ChatColor.GOLD + "/donate" + ChatColor.AQUA + " - поддержать сервер и посмотреть дополнительные донатные команды");
        sender.sendMessage(ChatColor.GOLD + "/notify on/off" + ChatColor.AQUA + " - включить/отключить звуковые уведомления");
        sender.sendMessage(ChatColor.GOLD + "/ignore <@all/@global/@private/player nick>" + ChatColor.AQUA + " - отключить сообщения в чате или добавить игрока в чёрный список");
        sender.sendMessage(ChatColor.GOLD + "/unignore <@all/@global/@private/player nick>" + ChatColor.AQUA + " - включить сообщения в чате или удалить игрока из чёрного списка");
        sender.sendMessage(ChatColor.GOLD + "/m <nick> <message>" + ChatColor.AQUA + " - отправить игроку личное сообщение");
        sender.sendMessage(ChatColor.GOLD + "/r <message>" + ChatColor.AQUA + " - ответить игроку в личных сообщениях");
        sender.sendMessage(ChatColor.GOLD + "/me <message>" + ChatColor.AQUA + " - описать какое-либо действие от своего лица [RolePlay]");
        sender.sendMessage(ChatColor.GOLD + "/c <message>" + ChatColor.AQUA + " - написать сообщение в чат клана");

        TextComponent clan = new TextComponent(ChatColor.GOLD + "/clan help/create/rename/tag/color/invite/kick/promote /delete/leave/accept <whatever>" + ChatColor.AQUA + " - Управление кланами");
        clan.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/clan help"));
        clan.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder("Нажмите, чтобы узнать больше о команде /clan").color(net.md_5.bungee.api.ChatColor.GREEN).create()));
        sender.sendMessage(clan);
        return true;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] strings) {
        return new ArrayList<>();
    }
}