package com.idreesinc.celeste.utilities;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Arrays;

public final class UpdateChecker {
    private static final String RELEASES_PATH = "/Greaper88/Celeste/releases/";
    private static final String TAG_PREFIX = RELEASES_PATH + "tag/v";

    private final JavaPlugin plugin;

    public UpdateChecker(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void check() {
        plugin.getServer().getScheduler().runTaskAsynchronously(plugin, this::checkLatestRelease);
    }

    private void checkLatestRelease() {
        try {
            URL latest = new URL("https://github.com" + RELEASES_PATH + "latest");
            HttpURLConnection connection = (HttpURLConnection) latest.openConnection();
            connection.setInstanceFollowRedirects(false);
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(5000);
            try {
                if (connection.getResponseCode() != HttpURLConnection.HTTP_MOVED_TEMP) {
                    return;
                }
                String location = connection.getHeaderField("Location");
                if (location == null) {
                    return;
                }
                URL release = new URL(latest, location);
                String path = release.getPath();
                if (path.startsWith(TAG_PREFIX)) {
                    String available = path.substring(TAG_PREFIX.length());
                    String installed = plugin.getDescription().getVersion();
                    if (isNewerVersion(available, installed)) {
                        plugin.getLogger().info("Celeste " + available + " is available at " + release);
                    }
                }
            } finally {
                connection.disconnect();
            }
        } catch (IOException | NumberFormatException e) {
            if (plugin.getConfig().getBoolean("debug")) {
                plugin.getLogger().info("Could not check for Celeste releases: " + e.getMessage());
            }
        }
    }

    static boolean isNewerVersion(String available, String installed) {
        int[] availableParts = Arrays.stream(available.split("\\.", -1)).mapToInt(Integer::parseInt).toArray();
        int[] installedParts = Arrays.stream(installed.split("\\.", -1)).mapToInt(Integer::parseInt).toArray();
        for (int i = 0; i < Math.max(availableParts.length, installedParts.length); i++) {
            int availablePart = i < availableParts.length ? availableParts[i] : 0;
            int installedPart = i < installedParts.length ? installedParts[i] : 0;
            if (availablePart != installedPart) {
                return availablePart > installedPart;
            }
        }
        return false;
    }
}
