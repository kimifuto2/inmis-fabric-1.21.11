package draylar.inmis.network.packet;

import net.minecraft.item.ItemStack;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BackpackScreenPacket(ItemStack stack) implements CustomPayload {

    public static final CustomPayload.Id<BackpackScreenPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("inmis", "backpack_screen_packet"));

    public static final PacketCodec<RegistryByteBuf, BackpackScreenPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
        ItemStack.PACKET_CODEC.encode(buf, value.stack());
    }, buf -> new BackpackScreenPacket(ItemStack.PACKET_CODEC.decode(buf)));

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
