package com.xhomes.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.xhomes.manager.HomeManager;
import com.xhomes.Xhomes;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

@CommandAlias("sethome")
@CommandPermission("xhomes.sethome")
public class SetHomeCommand extends BaseCommand {

    private final HomeManager homeManager;
    private final Xhomes plugin;

    public SetHomeCommand(HomeManager homeManager, Xhomes plugin) {
        this.homeManager = homeManager;
        this.plugin = plugin;
    }

    @Default
    public boolean onCommand(Player sender, @Optional String home) {
        Location location = sender.getLocation();

        // Get the player's home names
        List<String> playerHomes = new ArrayList<>(homeManager.getHomes(sender.getUniqueId()).keySet());

        if (home == null) {
            int homeNumber = playerHomes.size() + 1;
            home = "Home#" + homeNumber;
        }

        // Get the maximum homes limit from the config
        int maxHomes = getMaxHomesForPlayer(sender);
        if (maxHomes == -1) {
            sender.sendMessage("You do not have permission to set homes.");
            return true;
        }

        // Check if the home name already exists
        if (playerHomes.contains(home)) {
            sender.sendMessage(ColorParser.of("A home with the name '<home>' already exists. Please choose a different name.")
                    .with("home", home).legacy().build());
            return true;
        }

        // Check if the player has reached their max home limit
        if (playerHomes.size() >= maxHomes) {
            sender.sendMessage(ColorParser.of("You have reached your home limit of <max_homes> homes.")
                    .with("max_homes", String.valueOf(maxHomes)).legacy().build());
            return true;
        }

        // Add the home
        homeManager.addHome(sender.getUniqueId(), home, location);
        sender.sendMessage(ColorParser.of("Home '<home>' set at your current location.").with("home", home).legacy().build());
        return true;
    }

    private int getMaxHomesForPlayer(Player player) {
        if (player.hasPermission("xhomes.hometier4")) {
            return plugin.getConfig().getInt("home_tiers.xhomes.hometier4.maxhomes");
        }
        if (player.hasPermission("xhomes.hometier3")) {
            return plugin.getConfig().getInt("home_tiers.xhomes.hometier3.maxhomes");
        }
        if (player.hasPermission("xhomes.hometier2")) {
            return plugin.getConfig().getInt("home_tiers.xhomes.hometier2.maxhomes");
        }
        if (player.hasPermission("xhomes.hometier1")) {
            return plugin.getConfig().getInt("home_tiers.xhomes.hometier1.maxhomes");
        }
        return -1; // No permission found
    }
}