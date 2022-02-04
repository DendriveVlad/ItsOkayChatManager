package itsokay.chatmanager;

public class AllowedCommands {
    public static String[] getAllowedCommands() {
        return new String[]{"skin", "m", "me", "help", "clan", "c", "r", "a", "notify", "ignore", "unignore", "donate", "pos1", "pos2", "nickcolor", "colornumbers", "fakeleave", "fakedeath"};
    }

    public static String[] getModersCommands() {
        return new String[]{"co", "ban", "unban", "watch"};
    }

    public static String[] getHiddenCommands() {
        return new String[]{};
    }
}
