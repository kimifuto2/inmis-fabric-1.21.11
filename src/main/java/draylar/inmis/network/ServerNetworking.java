package draylar.inmis.network;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.TrinketComponent;
import dev.emi.trinkets.api.TrinketsApi;
import draylar.inmis.Inmis;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.EnderBackpackItem;
import draylar.inmis.mixin.trinkets.TrinketsMixinPlugin;
import draylar.inmis.network.packet.BackpackPacket;
import draylar.inmis.network.packet.BackpackScreenPacket;
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.screen.GenericContainerScreenHandler;
import net.minecraft.screen.SimpleNamedScreenHandlerFactory;
import net.minecraft.stat.Stats;
import net.minecraft.text.Text;
import net.minecraft.util.Pair;

import java.util.List;
import java.util.Optional;

public class ServerNetworking {

    public static void init() {
        PayloadTypeRegistry.playC2S().register(BackpackPacket.PACKET_ID, BackpackPacket.PACKET_CODEC);
        PayloadTypeRegistry.playS2C().register(BackpackScreenPacket.PACKET_ID, BackpackScreenPacket.PACKET_CODEC);

        ServerPlayNetworking.registerGlobalReceiver(BackpackPacket.PACKET_ID, (payload, context) -> {
            context.server().execute(() -> {
                PlayerEntity player = context.player();

                if (TrinketsMixinPlugin.isTrinketsLoaded && Inmis.CONFIG.enableTrinketCompatibility) {
                    Optional<TrinketComponent> component = TrinketsApi.getTrinketComponent(player);

                    if (component.isPresent()) {
                        List<Pair<SlotReference, ItemStack>> allEquipped = component.get().getAllEquipped();
                        for (Pair<SlotReference, ItemStack> entry : allEquipped) {
                            if (entry.getRight().getItem() instanceof BackpackItem) {
                                BackpackItem.openScreen(player, entry.getRight());
                                return;
                            }
                        }
                    }
                }

                // Depending on the "disallow main inventory backpacks" option, search all slots or only armor slots
                ItemStack firstBackpackItemStack;
                if (!Inmis.CONFIG.requireArmorTrinketToOpen) {
                    // Search all inventory slots
                    firstBackpackItemStack = ItemStack.EMPTY;
                    for (int i = 0; i < player.getInventory().size(); i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (stack.getItem() instanceof BackpackItem) {
                            firstBackpackItemStack = stack;
                            break;
                        }
                    }
                } else {
                    // Search only armor slots (MAIN_SIZE to MAIN_SIZE+3)
                    firstBackpackItemStack = ItemStack.EMPTY;
                    for (int i = PlayerInventory.MAIN_SIZE; i < PlayerInventory.MAIN_SIZE + 4; i++) {
                        ItemStack stack = player.getInventory().getStack(i);
                        if (stack.getItem() instanceof BackpackItem) {
                            firstBackpackItemStack = stack;
                            break;
                        }
                    }
                }

                if (firstBackpackItemStack != ItemStack.EMPTY) {
                    BackpackItem.openScreen(player, firstBackpackItemStack);
                } else {
                    for (int u = 0; u < player.getInventory().size(); u++) {
                        if (player.getInventory().getStack(u).isOf(Inmis.ENDER_POUCH)) {
                            EnderChestInventory enderChestInventory = player.getEnderChestInventory();
                            player.openHandledScreen(new SimpleNamedScreenHandlerFactory((i, playerInventory, playerEntity) ->
                                    GenericContainerScreenHandler.createGeneric9x3(i, playerInventory, enderChestInventory), Text.translatable("container.enderchest")));
                            player.incrementStat(Stats.OPEN_ENDERCHEST);
                            break;
                        }
                    }
                }
            });
        });
    }
}
