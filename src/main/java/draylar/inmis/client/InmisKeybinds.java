package draylar.inmis.client;

import draylar.inmis.network.packet.BackpackPacket;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

public class InmisKeybinds {

    private static final KeyBinding OPEN_BACKPACK = KeyBindingHelper
            .registerKeyBinding(new KeyBinding("key.inmis.open_backpack", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_B, KeyBinding.Category.create(Identifier.of("category.inmis.keybindings"))));

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (OPEN_BACKPACK.wasPressed()) {
                ClientPlayNetworking.send(new BackpackPacket());
            }
        });
    }
}
