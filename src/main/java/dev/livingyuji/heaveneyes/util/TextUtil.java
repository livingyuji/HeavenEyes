package dev.livingyuji.heaveneyes.util;

import java.util.Map;
import java.util.regex.Pattern;

/**
 * Text formatting and color translation utility.
 */
public final class TextUtil {

    private static final Pattern COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    private static final Pattern STRIP_COLOR_PATTERN = Pattern.compile("(?i)[§&][0-9a-fk-or]");

    private TextUtil() {
    }

    /**
     * Translates '&' color codes into standard Minecraft section '§' color codes.
     */
    public static String colorize(String text) {
        if (text == null) {
            return "";
        }
        return COLOR_PATTERN.matcher(text).replaceAll("§$1");
    }

    /**
     * Strips all Minecraft color codes from a string.
     */
    public static String stripColor(String text) {
        if (text == null) {
            return "";
        }
        return STRIP_COLOR_PATTERN.matcher(text).replaceAll("");
    }

    /**
     * Replaces variable placeholders in text.
     */
    public static String format(String template, Map<String, String> placeholders) {
        if (template == null) {
            return "";
        }
        if (placeholders == null || placeholders.isEmpty()) {
            return colorize(template);
        }

        String result = template;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", entry.getValue() != null ? entry.getValue() : "");
        }
        return colorize(result);
    }
}
