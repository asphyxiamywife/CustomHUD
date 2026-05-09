package com.minenash.customhud;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.Style;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.Arrays;

public class UpdateChecker {

    private static final String mcVersion = Minecraft.getInstance().getLaunchedVersion();
    private static final String currentVersion;
    static {
        String modVersionRaw = FabricLoader.getInstance().getModContainer("custom_hud").get().getMetadata().getVersion().getFriendlyString();
        currentVersion = modVersionRaw.substring(0, modVersionRaw.indexOf("+"));
    }

    public static String[] latestKnownVersion = null;

    public static Component updateMessage = null;

    public static void check() {
        if (currentVersion.contains("beta") || currentVersion.contains("alpha"))
            return;

        String[] modVersion = currentVersion.split("\\.");

        if (latestKnownVersion == null)
            latestKnownVersion = modVersion;


        JsonObject updateInfo = getUpdateData();
        if (updateInfo == null)
            return;

        CustomHud.logInDebugMode("VERSION: " + mcVersion);

        JsonObject info = updateInfo.getAsJsonObject(mcVersion);
        if (info == null)
            return;


        String versionRaw = info.get("version").getAsString();
        String[] version = versionRaw.split("\\.");
        if (version.length != latestKnownVersion.length || Arrays.equals(latestKnownVersion, version))
            return;
        if (!compareVersions(latestKnownVersion, version))
            return;

        latestKnownVersion = version;
        updateMessage = Component.literal("§eCustomHUD v" + versionRaw + " is available! ")
                .append(Component.literal("[Modrinth]").setStyle(Style.EMPTY
                        .applyFormats(ChatFormatting.GREEN, ChatFormatting.UNDERLINE)
                        .withClickEvent(new ClickEvent.OpenUrl(URI.create(info.get("link").getAsString())))
                        .withHoverEvent(new HoverEvent.ShowText(Component.literal("Download on Modrinth")))
                )).append("\nWhat's New:\n §7" + info.get("msg").getAsString());
        ConfigManager.save();

    }

    public static boolean compareVersions(String[] known, String[] latest) {
        for (int i = 0; i < 3; i++) {
            if (Integer.parseInt(latest[i]) > Integer.parseInt(known[i]))
                return true;
        }
        return false;
    }

    public static JsonObject getUpdateData() {
        try {
            URL url = new URL("https://customhud.dev/updateInfo.json");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.connect();

            int responseCode = conn.getResponseCode();

            if (responseCode != 200)
                throw new RuntimeException("HttpResponseCode: " + responseCode);

            return JsonParser.parseString(new String(conn.getInputStream().readAllBytes())).getAsJsonObject();
        }
        catch (Exception e) {
            CustomHud.LOGGER.error("[CustomHUD] Could not get update info");
            CustomHud.LOGGER.catching(e);
            return null;
        }
    }

    public static String getLatestKnownVersionAsString() {
        if (latestKnownVersion == null)
            return currentVersion;
        StringBuilder version = new StringBuilder();
        for (String part : latestKnownVersion)
            version.append(part).append(".");
        return version.substring(0, version.length()-1);
    }

}
