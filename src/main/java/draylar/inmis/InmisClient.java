package draylar.inmis;

import draylar.inmis.api.TrinketCompat;
import draylar.inmis.client.InmisKeybinds;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.ui.BackpackHandledScreen;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.gui.screen.ingame.HandledScreens;

@Environment(EnvType.CLIENT)
public class InmisClient implements ClientModInitializer {

    @Override
    public void onInitializeClient() {
        // HandledScreens.register is now private in 1.21.11
        // Screen registration happens automatically through ScreenHandlerProvider
        HandledScreens.register(Inmis.BACKPACK_SCREEN_HANDLER, BackpackHandledScreen::new);
        InmisKeybinds.initialize();

        for (BackpackItem backpack : Inmis.BACKPACKS) {
            if (Inmis.TRINKETS_LOADED) {
                TrinketCompat.registerTrinketRenderer(backpack);
            }
        }
    }
}
