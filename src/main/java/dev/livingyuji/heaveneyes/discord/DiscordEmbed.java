package dev.livingyuji.heaveneyes.discord;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

/**
 * Represents a Discord Webhook rich embed adhering to Discord API constraints.
 */
public class DiscordEmbed {

    private String title;
    private String description;
    private Integer color;
    private String timestamp;
    private String footerText;
    private String footerIconUrl;
    private String thumbnailUrl;
    private String authorName;
    private String authorIconUrl;
    private final List<Field> fields = new ArrayList<>();

    public DiscordEmbed() {
        this.timestamp = Instant.now().toString();
    }

    public static String sanitize(String input, int maxLength) {
        if (input == null || input.trim().isEmpty()) {
            return "N/A";
        }
        // Neutralize dangerous Discord mentions
        String safe = input
                .replace("@everyone", "@\u200Beveryone")
                .replace("@here", "@\u200Bhere")
                .replace("<@", "<@\u200B");

        if (safe.length() > maxLength) {
            return safe.substring(0, Math.max(0, maxLength - 3)) + "...";
        }
        return safe;
    }

    public DiscordEmbed setTitle(String title) {
        this.title = sanitize(title, 256);
        return this;
    }

    public DiscordEmbed setDescription(String description) {
        this.description = sanitize(description, 4096);
        return this;
    }

    public DiscordEmbed setColor(int color) {
        this.color = color;
        return this;
    }

    public DiscordEmbed setTimestamp(Instant instant) {
        this.timestamp = instant != null ? instant.toString() : Instant.now().toString();
        return this;
    }

    public DiscordEmbed setFooter(String text, String iconUrl) {
        this.footerText = sanitize(text, 2048);
        this.footerIconUrl = iconUrl;
        return this;
    }

    public DiscordEmbed setThumbnail(String url) {
        this.thumbnailUrl = url;
        return this;
    }

    public DiscordEmbed setAuthor(String name, String iconUrl) {
        this.authorName = sanitize(name, 256);
        this.authorIconUrl = iconUrl;
        return this;
    }

    public DiscordEmbed addField(String name, String value, boolean inline) {
        if (fields.size() < 25) {
            fields.add(new Field(sanitize(name, 256), sanitize(value, 1024), inline));
        }
        return this;
    }

    public JsonObject toJson() {
        JsonObject json = new JsonObject();
        if (title != null) json.addProperty("title", title);
        if (description != null) json.addProperty("description", description);
        if (color != null) json.addProperty("color", color);
        if (timestamp != null) json.addProperty("timestamp", timestamp);

        if (footerText != null) {
            JsonObject footer = new JsonObject();
            footer.addProperty("text", footerText);
            if (footerIconUrl != null) footer.addProperty("icon_url", footerIconUrl);
            json.add("footer", footer);
        }

        if (thumbnailUrl != null) {
            JsonObject thumbnail = new JsonObject();
            thumbnail.addProperty("url", thumbnailUrl);
            json.add("thumbnail", thumbnail);
        }

        if (authorName != null) {
            JsonObject author = new JsonObject();
            author.addProperty("name", authorName);
            if (authorIconUrl != null) author.addProperty("icon_url", authorIconUrl);
            json.add("author", author);
        }

        if (!fields.isEmpty()) {
            JsonArray fieldsArray = new JsonArray();
            for (Field field : fields) {
                JsonObject fieldObj = new JsonObject();
                fieldObj.addProperty("name", field.name);
                fieldObj.addProperty("value", field.value);
                fieldObj.addProperty("inline", field.inline);
                fieldsArray.add(fieldObj);
            }
            json.add("fields", fieldsArray);
        }

        return json;
    }

    public String getTitle() { return title; }
    public String getDescription() { return description; }
    public Integer getColor() { return color; }
    public List<Field> getFields() { return fields; }

    public static class Field {
        public final String name;
        public final String value;
        public final boolean inline;

        public Field(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }
}
