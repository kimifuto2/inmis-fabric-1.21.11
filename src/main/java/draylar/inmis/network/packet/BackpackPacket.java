package draylar.inmis.network.packet;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;

public record BackpackPacket() implements CustomPayload {

    public static final CustomPayload.Id<BackpackPacket> PACKET_ID = new CustomPayload.Id<>(Identifier.of("inmis", "backpack_packet"));

    public static final PacketCodec<RegistryByteBuf, BackpackPacket> PACKET_CODEC = PacketCodec.of((value, buf) -> {
    }, buf -> new BackpackPacket());

    @Override
    public Id<? extends CustomPayload> getId() {
        return PACKET_ID;
    }

}
