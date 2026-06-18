package draylar.inmis.item;

import draylar.inmis.Inmis;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.network.packet.BackpackScreenPacket;
import draylar.inmis.ui.BackpackScreenHandler;
import net.fabricmc.fabric.api.item.v1.FabricItem;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerFactory;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.world.World;
import org.jetbrains.annotations.Nullable;

public class BackpackItem extends Item implements FabricItem {

    private final BackpackInfo backpack;

    public BackpackItem(BackpackInfo backpack, Item.Settings settings) {
        super(settings);
        this.backpack = backpack;
    }

    @Override
    public ActionResult use(World world, PlayerEntity user, Hand hand) {
        if (!Inmis.CONFIG.requireArmorTrinketToOpen) {
            user.setCurrentHand(hand);

            if (Inmis.CONFIG.playSound) {
                if (world.isClient()) {
                    world.playSound(user, user.getBlockPos(), Registries.SOUND_EVENT.get(Identifier.of(backpack.getOpenSound())), SoundCategory.PLAYERS, 1, 1);
                }
            }

            openScreen(user, user.getStackInHand(hand));
            return ActionResult.SUCCESS;
        }

        return ActionResult.PASS;
    }

    public static void openScreen(PlayerEntity player, ItemStack backpackItemStack) {
        if (player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.openHandledScreen(new ExtendedScreenHandlerFactory<BackpackScreenPacket>() {
                @Override
                public BackpackScreenPacket getScreenOpeningData(ServerPlayerEntity player) {
                    return new BackpackScreenPacket(backpackItemStack);
                }

                @Override
                public Text getDisplayName() {
                    return Text.translatable(backpackItemStack.getItem().getTranslationKey());
                }

                @Nullable
                @Override
                public ScreenHandler createMenu(int syncId, PlayerInventory inv, PlayerEntity player) {
                    return new BackpackScreenHandler(syncId, inv, backpackItemStack);
                }
            });
        }
    }

    public BackpackInfo getTier() {
        return backpack;
    }

    @Override
    public boolean allowComponentsUpdateAnimation(PlayerEntity player, Hand hand, ItemStack oldStack, ItemStack newStack) {
        return false;
    }

}
