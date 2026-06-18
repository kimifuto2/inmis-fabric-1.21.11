package draylar.inmis.item;

import dev.emi.trinkets.api.Trinket;
import draylar.inmis.config.BackpackInfo;
import net.minecraft.item.Item;

public class TrinketBackpackItem extends BackpackItem implements Trinket {

    public TrinketBackpackItem(BackpackInfo backpack, Item.Settings settings) {
        super(backpack, settings);
    }
}
