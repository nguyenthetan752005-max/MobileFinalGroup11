package com.english.learning.util;

import java.time.Duration;
import java.time.LocalDateTime;

public class TimeFormatUtil {

    private TimeFormatUtil() {
        // Prevent instantiation
    }

    public static String formatActiveTime(int totalSeconds) {
        int minutes = totalSeconds / 60;
        if (minutes < 1) {
            return "0 phút";
        }
        int hours = minutes / 60;
        int remainingMinutes = minutes % 60;
        if (hours > 0) {
            return hours + "h" + " " + remainingMinutes + "m";
        } else {
            return remainingMinutes + " phút";
        }
    }

    public static String formatTimeAgo(LocalDateTime timestamp) {
        if (timestamp == null) {
            return "just now";
        }

        Duration duration = Duration.between(timestamp, LocalDateTime.now());
        long seconds = Math.max(duration.getSeconds(), 0L);

        if (seconds < 60) {
            return "just now";
        }
        if (seconds < 3600) {
            long minutes = seconds / 60;
            return minutes + (minutes == 1 ? " minute ago" : " minutes ago");
        }
        if (seconds < 86400) {
            long hours = seconds / 3600;
            return hours + (hours == 1 ? " hour ago" : " hours ago");
        }
        if (seconds < 2592000) {
            long days = seconds / 86400;
            return days + (days == 1 ? " day ago" : " days ago");
        }
        if (seconds < 31536000) {
            long months = seconds / 2592000;
            return months + (months == 1 ? " month ago" : " months ago");
        }
        long years = seconds / 31536000;
        return years + (years == 1 ? " year ago" : " years ago");
    }
}
