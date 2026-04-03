package io.Yomicer.magicExpansion.items.misc;

import lombok.Getter;
import org.bukkit.inventory.ItemStack;

@Getter
public class WeightedItem {

    private final ItemStack item;
    private final int weight;

    public WeightedItem(ItemStack item, int weight) {
        this.item = item;
        this.weight = weight;
    }

}
