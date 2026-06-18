package draylar.inmis.item.component;

import com.mojang.serialization.Codec;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;

import java.util.List;

public final class BackpackComponent {

    public static final PacketCodec<RegistryByteBuf, BackpackComponent> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        ItemStack.OPTIONAL_LIST_PACKET_CODEC.encode(buf, value.getSimpleInventory().getHeldStacks());
    }, buf -> {
        List<ItemStack> itemStacks = ItemStack.OPTIONAL_LIST_PACKET_CODEC.decode(buf);
        SimpleInventory inventory = new SimpleInventory(itemStacks.size());
        for (int i = 0; i < itemStacks.size(); i++) {
            inventory.setStack(i, itemStacks.get(i));
        }
        return new BackpackComponent(inventory);
    });


    public static final Codec<BackpackComponent> CODEC = ItemStack.OPTIONAL_CODEC.listOf().xmap(BackpackComponent::new, component -> component.simpleInventory.getHeldStacks());

    private final SimpleInventory simpleInventory;


    public BackpackComponent(SimpleInventory simpleInventory) {
        this.simpleInventory = simpleInventory;
    }

    public BackpackComponent(List<ItemStack> stacks) {
        this.simpleInventory = new SimpleInventory(stacks.size());
        for (int i = 0; i < stacks.size(); i++) {
            this.simpleInventory.setStack(i, stacks.get(i));
        }
    }

    public SimpleInventory getSimpleInventory() {
        return this.simpleInventory;
    }

}

