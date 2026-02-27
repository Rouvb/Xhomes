package com.xhomes.gui;

import org.bukkit.Material;
import xyz.xenondevs.invui.gui.PagedGui;
import xyz.xenondevs.invui.item.ItemProvider;
import xyz.xenondevs.invui.item.builder.ItemBuilder;
import xyz.xenondevs.invui.item.impl.controlitem.PageItem;

public class ForwardItem extends PageItem {

    public ForwardItem() {
        super(true);
    }

    @Override
    public ItemProvider getItemProvider(PagedGui<?> pagedGui) {
        return new ItemBuilder(Material.ARROW).setDisplayName("Next");
    }
}
