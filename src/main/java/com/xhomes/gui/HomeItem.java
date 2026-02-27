package com.xhomes.gui;

import com.xhomes.manager.HomeManager;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.AbstractItem;

public class HomeItem extends AbstractItem {

    private final HomeManager homeManager;
    private final String homeName;

    public HomeItem(HomeManager homeManager, String homeName) {
        this.homeManager = homeManager;
        this.homeName = homeName;
    }

    @Override
    public void handleClick(@NotNull ClickType clickType, @NotNull Player player, @NotNull InventoryClickEvent inventoryClickEvent) {
        player.closeInventory();
        if (clickType == ClickType.LEFT) {
            Location homeLocation = homeManager.getHomes(player.getUniqueId()).get(homeName);
            homeManager.startTeleportCountdown(player, homeLocation);
        } else if (clickType == ClickType.RIGHT) {
            homeManager.removeHome(player.getUniqueId(), homeName);
        }
    }

    @Override
    public ItemProvider getItemProvider() {
        return new ItemBuilder(Material.BLUE_BED)
                .setDisplayName(homeName)
                .addLoreLines(
                        "Left click to teleport to home",
                        "Right click to delete a home"
                );
    }
}
