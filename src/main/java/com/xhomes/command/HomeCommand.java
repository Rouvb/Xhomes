package com.xhomes.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.xhomes.HomeManager;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Bukkit;
import org.bukkit.Location; 
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.CommandExecutor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable; // Import this class

import java.util.List;
import java.util.Map;

@CommandAlias("home|homes")
@CommandPermission("xhomes.home")
public class HomeCommand extends BaseCommand {

    private final HomeManager homeManager;

    public HomeCommand(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Default
    public boolean onCommand(Player sender, @Optional String home) {
        // Check if an optional home name is provided
        if (home != null) {
            Location homeLocation = homeManager.getHomes(sender.getName()).get(home);
            if (homeLocation != null) {
                startTeleportCountdown(sender, homeLocation);
            } else {
                sender.sendMessage(ColorParser.of("Home '<home>' does not exist.")
                        .with("home", home).legacy().build());
            }
            return true;
        }

        // If no home name is provided, show the home selection inventory
        Inventory inv = Bukkit.createInventory(null, 27, "Select Home");

        Map<String, Location> homes = homeManager.getHomes(sender.getName());

        int slot = 9;
        for (String homeName : homes.keySet()) {
            if (slot >= 18) break;
            ItemStack bedItem = new ItemStack(Material.BLUE_BED);
            ItemMeta meta = bedItem.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(homeName);
                meta.setLore(List.of("Click to teleport to " + homeName));
                bedItem.setItemMeta(meta);
            }
            inv.setItem(slot++, bedItem);
        }

        inv.setItem(26, new ItemStack(Material.BARRIER));

        sender.openInventory(inv);
        return true;
    }

    private void startTeleportCountdown(Player player, Location homeLocation) {
        player.sendMessage("Teleporting to home in 5 seconds, don't move!");

        new BukkitRunnable() {
            int countdown = 5;
            Location initialLocation = player.getLocation(); // Store initial location

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
        }.runTaskTimer(homeManager.getPlugin(), 0, 20); // Use homeManager.getPlugin()
    }

    // Helper method to check if a player has moved significantly (walking, not just rotating)
    private boolean hasPlayerMoved(Player player, Location initialLocation) {
        Location currentLocation = player.getLocation();
        return initialLocation.getX() != currentLocation.getX() || 
               initialLocation.getY() != currentLocation.getY() || 
               initialLocation.getZ() != currentLocation.getZ();
    }
}