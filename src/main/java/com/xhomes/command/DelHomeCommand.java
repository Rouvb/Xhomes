package com.xhomes.command;

import co.aikar.commands.BaseCommand;
import co.aikar.commands.annotation.CommandAlias;
import co.aikar.commands.annotation.CommandPermission;
import co.aikar.commands.annotation.Default;
import com.xhomes.manager.HomeManager;
import io.github.milkdrinkers.colorparser.paper.ColorParser;
import org.bukkit.entity.Player;

@CommandAlias("delhome")
@CommandPermission("xhomes.delhome")
public class DelHomeCommand extends BaseCommand {

    private final HomeManager homeManager;

    public DelHomeCommand(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Default
    public boolean onCommand(Player sender, String home) {
        // Check if the home exists
        if (!homeManager.getHomes(sender.getName()).containsKey(home)) {
            sender.sendMessage(ColorParser.of("&cHome '<home>' not found in your data.").with("home", home).legacy().build());
            return true;
        }

        // Remove the home
        homeManager.removeHome(sender.getName(), home);
        sender.sendMessage(ColorParser.of("&cHome '<home>' deleted.").legacy().with("home", home).build());
        return true;
    }
}