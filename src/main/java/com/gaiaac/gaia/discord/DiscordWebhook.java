package com.gaiaac.gaia.discord;

import java.awt.Color;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Discord Webhook integration for Gaia Anticheat.
 * Sends rich embed notifications when players are flagged.
 * All HTTP calls are dispatched to a dedicated single-thread executor to avoid blocking.
 */
public class DiscordWebhook {

    private final String webhookUrl;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "Gaia-Discord");
        t.setDaemon(true);
        return t;
    });

    public DiscordWebhook(String webhookUrl) {
        this.webhookUrl = webhookUrl;
    }

    /**
     * Send a detection alert to Discord.
     *
     * @param playerName   The player who was flagged
     * @param checkName    The check name (e.g., "KillAura")
     * @param checkType    The check variant (e.g., "A")
     * @param violationLevel Current VL
     * @param threshold    Threshold for punishment
     * @param debugInfo    Debug details
     * @param ping         Player ping in ms
     * @param tps          Server TPS
     * @param clientBrand  Client brand string
     * @param protocolVersion Protocol version
     * @param location     Player location string
     */
    public void sendAlert(String playerName, String checkName, String checkType,
                          double violationLevel, double threshold, String debugInfo,
                          int ping, double tps, String clientBrand, int protocolVersion,
                          String location) {

        List<EmbedField> fields = new ArrayList<>();
        fields.add(new EmbedField("Player", playerName, true));
        fields.add(new EmbedField("Check", checkName + " (" + checkType + ")", true));
        fields.add(new EmbedField("VL", String.format("%.1f / %.1f", violationLevel, threshold), true));
        fields.add(new EmbedField("Debug", debugInfo.isEmpty() ? "N/A" : debugInfo, false));
        fields.add(new EmbedField("Ping", ping + "ms", true));
        fields.add(new EmbedField("TPS", String.format("%.1f", tps), true));
        fields.add(new EmbedField("Client", clientBrand + " (v" + protocolVersion + ")", true));
        fields.add(new EmbedField("Location", location, false));

        // Color based on severity: green < yellow < orange < red
        int color;
        double ratio = violationLevel / threshold;
        if (ratio < 0.25) color = 0x2ECC71;      // Green
        else if (ratio < 0.5) color = 0xF1C40F;   // Yellow
        else if (ratio < 0.75) color = 0xE67E22;  // Orange
        else color = 0xE74C3C;                      // Red

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);

        sendEmbed("Gaia Anticheat", null, color, fields, timestamp,
                "Gaia AC • " + playerName);
    }

    /**
     * Send a punishment alert to Discord.
     */
    public void sendPunishment(String playerName, String checkName, double violationLevel, String command) {
        List<EmbedField> fields = new ArrayList<>();
        fields.add(new EmbedField("Player", playerName, true));
        fields.add(new EmbedField("Check", checkName, true));
        fields.add(new EmbedField("VL", String.format("%.1f", violationLevel), true));
        fields.add(new EmbedField("Action", "`" + command + "`", false));

        String timestamp = OffsetDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        sendEmbed("Gaia Anticheat - Punishment", null, 0xE74C3C, fields, timestamp,
                "Gaia AC • Punishment");
    }

    private void sendEmbed(String title, String description, int color,
                           List<EmbedField> fields, String timestamp, String footer) {
        try {
            StringBuilder json = new StringBuilder();
            json.append("{\"embeds\":[{");
            json.append("\"title\":\"").append(escapeJson(title)).append("\",");
            if (description != null) {
                json.append("\"description\":\"").append(escapeJson(description)).append("\",");
            }
            json.append("\"color\":").append(color).append(",");
            json.append("\"timestamp\":\"").append(timestamp).append("\",");
            json.append("\"footer\":{\"text\":\"").append(escapeJson(footer)).append("\"},");
            json.append("\"fields\":[");

            for (int i = 0; i < fields.size(); i++) {
                EmbedField f = fields.get(i);
                if (i > 0) json.append(",");
                json.append("{\"name\":\"").append(escapeJson(f.name)).append("\",");
                json.append("\"value\":\"").append(escapeJson(f.value)).append("\",");
                json.append("\"inline\":").append(f.inline).append("}");
            }

            json.append("]}]}");

            sendJson(json.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void sendJson(String json) {
        executor.submit(() -> sendJsonBlocking(json));
    }

    private void sendJsonBlocking(String json) {
        try {
            URL url = new URL(webhookUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setRequestProperty("Content-Type", "application/json");
            conn.setRequestProperty("User-Agent", "GaiaAC");
            conn.setDoOutput(true);
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);

            try (OutputStream os = conn.getOutputStream()) {
                os.write(json.getBytes(StandardCharsets.UTF_8));
                os.flush();
            }

            int responseCode = conn.getResponseCode();
            if (responseCode == 429) {
                // Rate limited - back off
                try { Thread.sleep(1000); } catch (InterruptedException ignored) {}
            } else if (responseCode < 200 || responseCode >= 300) {
                System.err.println("[Gaia] Discord webhook returned HTTP " + responseCode);
            }

            conn.disconnect();
        } catch (Exception e) {
            // Silently fail - don't let Discord issues affect gameplay
        }
    }

    /**
     * Shutdown the async executor. Call from plugin onDisable().
     */
    public void shutdown() {
        executor.shutdownNow();
    }

    private String escapeJson(String text) {
        if (text == null) return "";
        return text.replace("\\", "\\\\")
                   .replace("\"", "\\\"")
                   .replace("\n", "\\n")
                   .replace("\r", "\\r")
                   .replace("\t", "\\t");
    }

    /**
     * Check if the webhook URL is configured and valid.
     */
    public boolean isEnabled() {
        return webhookUrl != null && !webhookUrl.isEmpty()
                && webhookUrl.startsWith("https://discord.com/api/webhooks/");
    }

    private static class EmbedField {
        final String name;
        final String value;
        final boolean inline;

        EmbedField(String name, String value, boolean inline) {
            this.name = name;
            this.value = value;
            this.inline = inline;
        }
    }
}
