package draylar.inmis.compat;

import com.misterpemodder.shulkerboxtooltip.api.PreviewContext;
import com.misterpemodder.shulkerboxtooltip.api.provider.PreviewProvider;
import draylar.inmis.Inmis;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.item.BackpackItem;
import net.minecraft.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class InmisPreviewProvider implements PreviewProvider {

    @Override
    public boolean shouldDisplay(PreviewContext context) {
        return !getInventory(context).stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public List<ItemStack> getInventory(PreviewContext context) {
        BackpackInfo info = ((BackpackItem) context.stack().getItem()).getTier();
        List<ItemStack> stacks = new ArrayList<>();
        if (context.stack().get(Inmis.BACKPACK_COMPONENT) != null) {
            stacks = context.stack().get(Inmis.BACKPACK_COMPONENT).getSimpleInventory().getHeldStacks();
        }
        return stacks;
    }

    @Override
    public int getInventoryMaxSize(PreviewContext context) {
        return getInventory(context).size();
    }

    @Override
    public int getMaxRowSize(PreviewContext context) {
        return ((BackpackItem) context.stack().getItem()).getTier().getRowWidth();
    }
}
