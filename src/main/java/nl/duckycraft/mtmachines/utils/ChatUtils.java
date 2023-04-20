package nl.duckycraft.mtmachines.utils;

import org.bukkit.ChatColor;

public class ChatUtils {

    public static String color(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public static String formatTime(int seconds) {
        if (seconds == -1) {
            return ChatUtils.color("&b00:00");
        }
        int hours = seconds / 3600;
        int minutes = (seconds % 3600) / 60;
        int remSeconds = seconds % 60;

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, remSeconds);
        } else {
            return String.format("%02d:%02d", minutes, remSeconds);
        }
    }

}
