package draylar.inmis;

import draylar.inmis.api.TrinketCompat;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.config.InmisClothConfig;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.EnderBackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import draylar.inmis.mixin.trinkets.TrinketsMixinPlugin;
import draylar.inmis.network.ServerNetworking;
import draylar.inmis.network.packet.BackpackScreenPacket;
import draylar.inmis.ui.BackpackScreenHandler;
import me.shedaniel.autoconfig.AutoConfig;
import me.shedaniel.autoconfig.serializer.JanksonConfigSerializer;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.itemgroup.v1.FabricItemGroup;
import net.fabricmc.fabric.api.itemgroup.v1.ItemGroupEvents;
import net.fabricmc.fabric.api.screenhandler.v1.ExtendedScreenHandlerType;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.component.ComponentType;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.UnaryOperator;

public class Inmis implements ModInitializer {

    public static final boolean TRINKETS_LOADED = FabricLoader.getInstance().isModLoaded("trinkets");
    public static final Logger LOGGER = LogManager.getLogger();
    public static final Identifier CONTAINER_ID = id("backpack");
    public static final RegistryKey<ItemGroup> GROUP = RegistryKey.of(RegistryKeys.ITEM_GROUP, CONTAINER_ID);

    // public static final InmisConfig CONFIG = OmegaConfig.register(InmisConfig.class);
    public static InmisClothConfig CONFIG = new InmisClothConfig();

    public static final ScreenHandlerType<BackpackScreenHandler> BACKPACK_SCREEN_HANDLER = new ExtendedScreenHandlerType<BackpackScreenHandler, BackpackScreenPacket>(
            BackpackScreenHandler::new, BackpackScreenPacket.PACKET_CODEC);

    public static final List<BackpackItem> BACKPACKS = new ArrayList<>();
    public static Item ENDER_POUCH;

    public static ComponentType<BackpackComponent> BACKPACK_COMPONENT;


    @Override
    public void onInitialize() {
        BACKPACK_COMPONENT = registerComponent("backpack", builder -> builder.codec(BackpackComponent.CODEC).packetCodec(BackpackComponent.PACKET_CODEC));
        ENDER_POUCH = Registry.register(Registries.ITEM, id("ender_pouch"), new EnderBackpackItem());
        registerBackpacks();
        ServerNetworking.init();
        setupTrinkets();
    }

    private void registerBackpacks() {
        Registry.register(Registries.ITEM_GROUP, GROUP,
                FabricItemGroup.builder().icon(() -> new ItemStack(Registries.ITEM.get(id("frayed_backpack")))).displayName(Text.translatable("itemGroup.inmis.backpack")).build());


        AutoConfig.register(InmisClothConfig.class, JanksonConfigSerializer::new);
        CONFIG = AutoConfig.getConfigHolder(InmisClothConfig.class).getConfig();

        // InmisConfig defaultConfig = new InmisConfig();

        for (BackpackInfo backpack : Inmis.CONFIG.backpacks) {
            RegistryKey<Item> itemKey = RegistryKey.of(RegistryKeys.ITEM, Identifier.of("inmis", backpack.getName().toLowerCase() + "_backpack"));
            Item.Settings settings = new Item.Settings().maxCount(1).registryKey(itemKey);

            // If this config option is true, allow players to place backpacks inside the
            // chest slot in their armor inventory.
            if (Inmis.CONFIG.allowBackpacksInChestplate) {
                settings.equipmentSlot((entity, stack) -> EquipmentSlot.CHEST);
            }

            // setup fireproof item settings
            if (backpack.isFireImmune()) {
                settings.fireproof();
            }

            // old config instances do not have the sound stuff
            if (backpack.getOpenSound() == null) {
                Optional<BackpackInfo> any = CONFIG.backpacks.stream().filter(info -> info.getName().equals(backpack.getName())).findAny();
                any.ifPresent(backpackInfo -> backpack.setOpenSound(backpackInfo.getOpenSound()));

                // if it is STILL null, log an error and set a default
                if (backpack.getOpenSound() == null) {
                    LOGGER.info(String.format("Could not find a sound event for %s in inmis.json config.", backpack.getName()));
                    LOGGER.info("Consider regenerating your config, or assigning the openSound value. Rolling with defaults for now.");
                    backpack.setOpenSound("minecraft:item.armor.equip_leather");
                }
            }

            BackpackItem item;
            if (TRINKETS_LOADED && CONFIG.enableTrinketCompatibility) {
                item = TrinketCompat.createTrinketBackpack(backpack, settings);
            } else {
                item = new BackpackItem(backpack, settings);
            }

            BackpackItem registered = Registry.register(Registries.ITEM, Identifier.of("inmis", backpack.getName().toLowerCase() + "_backpack"), item);
            BACKPACKS.add(registered);
            ItemGroupEvents.modifyEntriesEvent(GROUP).register(entries -> entries.add(registered));
            // Register to the TrinketsApi if both conditions are true.
            // This allows TrinketBackpackItem to handle API events (namely canUnequip).
            if (TRINKETS_LOADED && CONFIG.enableTrinketCompatibility) {
                TrinketCompat.registerTrinketBackpack(item);
            }
        }
        ItemGroupEvents.modifyEntriesEvent(GROUP).register(entries -> entries.add(ENDER_POUCH));
        Registry.register(Registries.SCREEN_HANDLER, "inmis:backpack", BACKPACK_SCREEN_HANDLER);
    }

    private void setupTrinkets() {
        if (TrinketsMixinPlugin.isTrinketsLoaded && Inmis.CONFIG.enableTrinketCompatibility) {
            TrinketCompat.registerTrinketPredicate();
        }
    }

    private static <T> ComponentType<T> registerComponent(String id, UnaryOperator<ComponentType.Builder<T>> builderOperator) {
        return Registry.register(Registries.DATA_COMPONENT_TYPE, id, builderOperator.apply(ComponentType.builder()).build());
    }


    public static boolean isBackpackEmpty(ItemStack stack) {
        return stack.get(BACKPACK_COMPONENT) == null || stack.get(BACKPACK_COMPONENT).getSimpleInventory().isEmpty();
    }

    @Nullable
    public static List<ItemStack> getBackpackContents(ItemStack stack) {
        if (stack.get(BACKPACK_COMPONENT) != null) {
            return stack.get(BACKPACK_COMPONENT).getSimpleInventory().getHeldStacks();
        }
        return null;
    }

    public static void wipeBackpack(ItemStack stack) {
        if (stack.get(BACKPACK_COMPONENT) != null) {
            stack.get(BACKPACK_COMPONENT).getSimpleInventory().clear();
        }
    }

    public static Identifier id(String name) {
        return Identifier.of("inmis", name);
    }
}
