package dev.livingyuji.heaveneyes.util;

import java.time.Duration;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Utility for parsing and formatting human-readable time durations.
 * Supports patterns such as "30m", "1h", "24h", "7d", "60s".
 */
public final class DurationParser {

    private static final Pattern DURATION_PATTERN = Pattern.compile("(?i)(\\d+)\\s*([a-z]+)");

    private DurationParser() {
    }

    /**
     * Parses a string representation of duration into milliseconds.
     *
     * @param input the input string, e.g. "30m", "2h", "7d"
     * @return duration in milliseconds
     * @throws IllegalArgumentException if the format is invalid or duration is non-positive
     */
    public static long parseMillis(String input) {
        if (input == null || input.trim().isEmpty()) {
            throw new IllegalArgumentException("Duration string cannot be null or empty");
        }

        String trimmed = input.trim();
        if (trimmed.contains("-")) {
            throw new IllegalArgumentException("Duration cannot contain negative values");
        }

        String remaining = trimmed.replaceAll("(?i)(\\d+)\\s*([a-z]+)", "").trim();
        if (!remaining.isEmpty()) {
            throw new IllegalArgumentException("Invalid duration string: " + input);
        }

        Matcher matcher = DURATION_PATTERN.matcher(trimmed);
        long totalMillis = 0;
        int foundCount = 0;

        while (matcher.find()) {
            foundCount++;
            long value;
            try {
                value = Long.parseLong(matcher.group(1));
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid number in duration: " + matcher.group(1));
            }

            if (value <= 0) {
                throw new IllegalArgumentException("Duration unit values must be positive");
            }

            String unit = matcher.group(2).toLowerCase(Locale.ROOT);
            long unitMultiplier;

            switch (unit) {
                case "ms", "millis", "millisecond", "milliseconds" -> unitMultiplier = 1L;
                case "s", "sec", "secs", "second", "seconds" -> unitMultiplier = 1_000L;
                case "m", "min", "mins", "minute", "minutes" -> unitMultiplier = 60_000L;
                case "h", "hr", "hrs", "hour", "hours" -> unitMultiplier = 3_600_000L;
                case "d", "day", "days" -> unitMultiplier = 86_400_000L;
                case "w", "week", "weeks" -> unitMultiplier = 604_800_000L;
                case "mo", "month", "months" -> unitMultiplier = 2_592_000_000L;
                case "y", "year", "years" -> unitMultiplier = 31_536_000_000L;
                default -> throw new IllegalArgumentException("Unknown time unit: '" + unit + "'");
            }

            totalMillis += Math.multiplyExact(value, unitMultiplier);
        }

        if (foundCount == 0 || totalMillis <= 0) {
            throw new IllegalArgumentException("Could not parse duration from input: " + input);
        }

        return totalMillis;
    }

    /**
     * Parses a string representation into a java.time.Duration.
     */
    public static Duration parse(String input) {
        return Duration.ofMillis(parseMillis(input));
    }

    /**
     * Formats a duration in milliseconds into a concise human-readable string.
     */
    public static String format(long millis) {
        if (millis <= 0) {
            return "0s";
        }

        long seconds = millis / 1000L;
        long days = seconds / 86400L;
        long hours = (seconds % 86400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;

        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (minutes > 0) {
            sb.append(minutes).append("m ");
        }
        if (secs > 0 || sb.isEmpty()) {
            sb.append(secs).append("s");
        }

        return sb.toString().trim();
    }
}
