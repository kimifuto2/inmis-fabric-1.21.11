package draylar.inmis.mixin.client;

import draylar.inmis.item.BackpackItem;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;

@Environment(EnvType.CLIENT)
@Mixin(ItemStack.class)
public class ItemStackMixin {

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof ItemStack) {
            ItemStack thisStack = (ItemStack) (Object) this;
            ItemStack checkStack = (ItemStack) obj;

            Item thisStackItem = thisStack.getItem();
            Item checkStackItem = checkStack.getItem();

            if(thisStackItem instanceof BackpackItem && checkStackItem instanceof BackpackItem) {
                return true;
            }
        }

        return super.equals(obj);
    }
}
