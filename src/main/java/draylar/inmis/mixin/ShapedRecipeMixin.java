package draylar.inmis.mixin;

import draylar.inmis.Inmis;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.recipe.ShapedRecipe;
import net.minecraft.recipe.input.CraftingRecipeInput;
import net.minecraft.registry.RegistryWrapper;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ShapedRecipe.class)
public abstract class ShapedRecipeMixin {

    @Shadow
    @Final
    private ItemStack result;

    @Inject(method = "craft(Lnet/minecraft/recipe/input/CraftingRecipeInput;Lnet/minecraft/registry/RegistryWrapper$WrapperLookup;)Lnet/minecraft/item/ItemStack;", at = @At("HEAD"), cancellable = true)
    private void craftMixin(CraftingRecipeInput craftingRecipeInput, RegistryWrapper.WrapperLookup wrapperLookup, CallbackInfoReturnable<ItemStack> info) {
        // get both backpacks
        if (craftingRecipeInput.getStackCount() > 4) {
            ItemStack centerSlotItemStack = craftingRecipeInput.getStackInSlot(4);

            // only attempt to apply nbt if the center stack of the original recipe was a backpack
            if (centerSlotItemStack.getItem() instanceof BackpackItem && !Inmis.isBackpackEmpty(centerSlotItemStack)) {
                ItemStack newBackpackItemStack = this.result.copy();
                if (newBackpackItemStack.getItem() instanceof BackpackItem backpackItem) {
                    SimpleInventory simpleInventory = new SimpleInventory(backpackItem.getTier().getRowWidth() * backpackItem.getTier().getNumberOfRows());
                    SimpleInventory oldInventory = centerSlotItemStack.get(Inmis.BACKPACK_COMPONENT).getSimpleInventory();
                    for (int i = 0; i < oldInventory.size(); i++) {
                        simpleInventory.setStack(i, oldInventory.getStack(i));
                    }

                    newBackpackItemStack.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(simpleInventory));
                    info.setReturnValue(newBackpackItemStack);
                }
            }

        }
    }
}
