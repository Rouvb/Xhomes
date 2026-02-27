package com.xhomes.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import co.aikar.commands.annotation.Optional;
import com.xhomes.Xhomes;
import com.xhomes.gui.HomeGui;
import com.xhomes.manager.HomeManager;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable; // Import this class


@CommandAlias("home|homes")
@CommandPermission("xhomes.home")
public class HomeCommand extends BaseCommand {

    private final Xhomes plugin;
    private final HomeManager homeManager;

    public HomeCommand(Xhomes plugin, HomeManager homeManager) {
        this.plugin = plugin;
        this.homeManager = homeManager;
    }

    @Default
    public void onCommand(Player sender, @Optional String home) {
        // Check if an optional home name is provided
        if (home != null) {
            Location homeLocation = homeManager.getHomes(sender.getUniqueId()).get(home);
            if (homeLocation != null) {
                homeManager.startTeleportCountdown(sender, homeLocation);
            } else {
                sender.sendMessage(ColorParser.of("Home '<home>' does not exist.")
                        .with("home", home).legacy().build());
            }
            return;
        }

        new HomeGui(plugin, homeManager).open(sender);
    }
}