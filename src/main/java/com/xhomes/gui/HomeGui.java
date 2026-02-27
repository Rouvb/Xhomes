package com.xhomes.gui;

import com.xhomes.Xhomes;
import com.xhomes.manager.HomeManager;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import xyz.xenondevs.invui.gui.Gui;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.gui.structure.Markers;
import xyz.xenondevs.invui.item.Item;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.SimpleItem;
import xyz.xenondevs.invui.window.Window;

import java.util.ArrayList;
import java.util.List;

public class HomeGui {

    private final HomeManager homeManager;

    public HomeGui(Xhomes plugin, HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    public void open(Player viewer) {
        Item border = new SimpleItem(new ItemBuilder(Material.GRAY_STAINED_GLASS_PANE).setDisplayName(""));

        List<Item> homeItems = getHomeItems(viewer);

        Gui gui = PagedGui.items()
                .setStructure(
                        "# # # # # # # # #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# x x x x x x x #",
                        "# # < # # # > # #")
                .addIngredient('x', Markers.CONTENT_LIST_SLOT_HORIZONTAL)
                .addIngredient('#', border)
                .addIngredient('<', new BackItem())
                .addIngredient('>', new ForwardItem())
                .setContent(homeItems)
                .build();

        Window window = Window.single()
                .setViewer(viewer)
                .setTitle("Homes")
                .setGui(gui)
                .build();

        window.open();
    }

    private List<Item> getHomeItems(Player viewer) {
        List<Item> homeItems = new ArrayList<>();
        homeManager.getHomes(viewer.getUniqueId()).forEach((home, homeLocation) -> homeItems.add(new HomeItem(homeManager, home)));
        return homeItems;
    }
}
