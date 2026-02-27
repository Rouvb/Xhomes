package com.xhomes.manager;

import com.xhomes.Xhomes;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class HomeManager {

    private final Xhomes plugin;

    // UUID → (homeName → Location)
    private final Map<UUID, Map<String, Location>> playerHomes = new ConcurrentHashMap<>();

    private final Map<UUID, Long> teleportCooldowns = new ConcurrentHashMap<>();

    private File homesFile;
    private FileConfiguration homesConfig;

    private static final long COOLDOWN_SECONDS = 30;

    public HomeManager(Xhomes plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "playerhomes.yml");
        createBackup();
        loadHomes();
    }

    // ----------------------------------------------------
    // Backup
    // ----------------------------------------------------

    private void createBackup() {
        if (!homesFile.exists()) return;

        try {
            File backupFile = new File(plugin.getDataFolder(), "playerhomes.yml.backup");
            Files.copy(homesFile.toPath(), backupFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            plugin.getLogger().warning("Failed to create backup of homes file!");
        }
    }

    // ----------------------------------------------------
    // Load
    // ----------------------------------------------------

    public void loadHomes() {
        playerHomes.clear();

        if (!homesFile.exists()) {
            try {
                homesFile.getParentFile().mkdirs();
                homesFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create homes file!");
                return;
            }
        }

        homesConfig = YamlConfiguration.loadConfiguration(homesFile);

        for (String uuidStr : homesConfig.getKeys(false)) {

            UUID uuid;
            try {
                uuid = UUID.fromString(uuidStr);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid UUID in homes file: " + uuidStr);
                continue;
            }

            Map<String, Location> homes = new HashMap<>();

            for (String homeName :
                    homesConfig.getConfigurationSection(uuidStr).getKeys(false)) {

                String locString = homesConfig.getString(uuidStr + "." + homeName);
                if (locString == null) continue;

                Location loc = deserializeLocation(locString);
                if (loc != null) {
                    homes.put(homeName.toLowerCase(), loc);
                }
            }

            if (!homes.isEmpty()) {
                playerHomes.put(uuid, homes);
            }
        }
    }

    // ----------------------------------------------------
    // Save
    // ----------------------------------------------------

    public void saveHomes() {
        createBackup();

        homesConfig.getKeys(false).forEach(key -> homesConfig.set(key, null));

        for (Map.Entry<UUID, Map<String, Location>> entry : playerHomes.entrySet()) {

            String uuidStr = entry.getKey().toString();

            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {

                Location loc = homeEntry.getValue();
                if (loc.getWorld() == null) continue;

                homesConfig.set(
                        uuidStr + "." + homeEntry.getKey(),
                        serializeLocation(loc)
                );
            }
        }

        try {
            homesConfig.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save homes file!");
        }
    }

    // ----------------------------------------------------
    // Teleport Cooldown
    // ----------------------------------------------------

    public boolean canTeleport(Player player) {

        UUID uuid = player.getUniqueId();

        Long last = teleportCooldowns.get(uuid);
        if (last == null) return true;

        long secondsLeft =
                COOLDOWN_SECONDS -
                        TimeUnit.MILLISECONDS.toSeconds(System.currentTimeMillis() - last);

        if (secondsLeft <= 0) {
            teleportCooldowns.remove(uuid);
            return true;
        }

        player.sendMessage("You must wait " + secondsLeft + " seconds.");
        return false;
    }

    public void setTeleportCooldown(Player player) {
        teleportCooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }

    // ----------------------------------------------------
    // Home Operations
    // ----------------------------------------------------

    public void addHome(UUID uuid, String homeName, Location loc) {

        playerHomes
                .computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                .put(homeName.toLowerCase(), loc);

        saveHomes();
    }

    public void removeHome(UUID uuid, String homeName) {

        Map<String, Location> homes = playerHomes.get(uuid);
        if (homes == null) return;

        homes.remove(homeName.toLowerCase());

        if (homes.isEmpty()) {
            playerHomes.remove(uuid);
        }

        saveHomes();
    }

    public Map<String, Location> getHomes(UUID uuid) {
        return playerHomes.getOrDefault(uuid, Collections.emptyMap());
    }

    // ----------------------------------------------------
    // Location Serialization
    // ----------------------------------------------------

    private String serializeLocation(Location loc) {
        return String.format(
                "%s,%.3f,%.3f,%.3f,%.2f,%.2f",
                loc.getWorld().getName(),
                loc.getX(),
                loc.getY(),
                loc.getZ(),
                loc.getYaw(),
                loc.getPitch()
        );
    }

    private Location deserializeLocation(String str) {

        String[] p = str.split(",");
        if (p.length != 6) return null;

        var world = plugin.getServer().getWorld(p[0]);
        if (world == null) return null;

        try {
            return new Location(
                    world,
                    Double.parseDouble(p[1]),
                    Double.parseDouble(p[2]),
                    Double.parseDouble(p[3]),
                    Float.parseFloat(p[4]),
                    Float.parseFloat(p[5])
            );
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public void startTeleportCountdown(Player player, Location homeLocation) {
        player.sendMessage("Teleporting to home in 5 seconds, don't move!");

        new BukkitRunnable() {
            int countdown = 5;
            Location initialLocation = player.getLocation().clone(); // Store initial location

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    return;
                }

                // Check if the player has moved
                if (hasPlayerMoved(player, initialLocation)) {
                    player.sendMessage("Teleportation canceled because you moved!");
                    cancel();
                    return;
                }

                if (countdown <= 0) {
                    player.teleport(homeLocation);
                    player.sendMessage("Teleported to home: " + homeLocation.getWorld().getName());
                    cancel();
                    return;
                }

                // Display countdown message in the center of the screen
                player.sendTitle("", "Teleporting in " + countdown + " seconds", 0, 20, 0);
                countdown--;
            }
        }.runTaskTimer(plugin, 0, 20); // Use homeManager.getPlugin()
    }

    // Helper method to check if a player has moved significantly (walking, not just rotating)
    private boolean hasPlayerMoved(Player player, Location initialLocation) {
        Location currentLocation = player.getLocation();
        return initialLocation.getX() != currentLocation.getX() ||
                initialLocation.getY() != currentLocation.getY() ||
                initialLocation.getZ() != currentLocation.getZ();
    }
}