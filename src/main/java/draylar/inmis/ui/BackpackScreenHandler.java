package draylar.inmis.ui;

import draylar.inmis.Inmis;
import draylar.inmis.api.Dimension;
import draylar.inmis.api.Point;
import draylar.inmis.config.BackpackInfo;
import draylar.inmis.item.BackpackItem;
import draylar.inmis.item.component.BackpackComponent;
import draylar.inmis.network.packet.BackpackScreenPacket;
import net.minecraft.block.ShulkerBoxBlock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.InventoryChangedListener;
import net.minecraft.inventory.SimpleInventory;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandler;
import net.minecraft.screen.slot.Slot;
import net.minecraft.util.Identifier;

public class BackpackScreenHandler extends ScreenHandler {

    private final ItemStack backpackStack;
    private final BackpackComponent backpackComponent;
    private final int padding = 8;
    private final int titleSpace = 10;

    public BackpackScreenHandler(int synchronizationID, PlayerInventory playerInventory, BackpackScreenPacket backpackScreenPacket) {
        this(synchronizationID, playerInventory, backpackScreenPacket.stack());
    }

    public BackpackScreenHandler(int synchronizationID, PlayerInventory playerInventory, ItemStack backpackStack) {
        super(Inmis.BACKPACK_SCREEN_HANDLER, synchronizationID);
        this.backpackStack = backpackStack;

        if (backpackStack.getItem() instanceof BackpackItem) {
            Dimension dimension = getDimension();
            BackpackInfo tier = getItem().getTier();
            int rowWidth = tier.getRowWidth();
            int numberOfRows = tier.getNumberOfRows();

            SimpleInventory existingInventory = backpackStack.get(Inmis.BACKPACK_COMPONENT) != null
                    ? backpackStack.get(Inmis.BACKPACK_COMPONENT).getSimpleInventory()
                    : null;

            SimpleInventory backpackInventory;
            if (existingInventory != null) {
                backpackInventory = new SimpleInventory(existingInventory.size());
                for (int i = 0; i < existingInventory.size(); i++) {
                    backpackInventory.setStack(i, existingInventory.getStack(i));
                }
            } else {
                backpackInventory = new SimpleInventory(rowWidth * numberOfRows);
            }

            this.backpackComponent = new BackpackComponent(backpackInventory);

            backpackInventory.addListener(new InventoryChangedListener() {
                @Override
                public void onInventoryChanged(Inventory inventory) {
                    backpackStack.set(Inmis.BACKPACK_COMPONENT, new BackpackComponent(backpackInventory));
                }
            });

            for (int y = 0; y < numberOfRows; y++) {
                for (int x = 0; x < rowWidth; x++) {
                    Point backpackSlotPosition = getBackpackSlotPosition(dimension, x, y);
                    addSlot(new BackpackLockedSlot(backpackInventory, y * rowWidth + x, backpackSlotPosition.x + 1, backpackSlotPosition.y + 1));
                }
            }

            for (int y = 0; y < 3; ++y) {
                for (int x = 0; x < 9; ++x) {
                    Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, x, y);
                    this.addSlot(new BackpackLockedSlot(playerInventory, x + y * 9 + 9, playerInvSlotPosition.x + 1, playerInvSlotPosition.y + 1));
                }
            }

            for (int x = 0; x < 9; ++x) {
                Point playerInvSlotPosition = getPlayerInvSlotPosition(dimension, x, 3);
                this.addSlot(new BackpackLockedSlot(playerInventory, x, playerInvSlotPosition.x + 1, playerInvSlotPosition.y + 1));
            }
        } else {
            this.backpackComponent = null;
            PlayerEntity player = playerInventory.player;
            this.onClosed(player);
        }
    }


    public BackpackItem getItem() {
        return (BackpackItem) backpackStack.getItem();
    }

    public Dimension getDimension() {
        BackpackInfo tier = getItem().getTier();
        return new Dimension(padding * 2 + Math.max(tier.getRowWidth(), 9) * 18, padding * 2 + titleSpace * 2 + 8 + (tier.getNumberOfRows() + 4) * 18);
    }

    public Point getBackpackSlotPosition(Dimension dimension, int x, int y) {
        BackpackInfo tier = getItem().getTier();
        return new Point(dimension.getWidth() / 2 - tier.getRowWidth() * 9 + x * 18, padding + titleSpace + y * 18);
    }

    public Point getPlayerInvSlotPosition(Dimension dimension, int x, int y) {
        // BackpackInfo tier = getItem().getTier();
        return new Point(dimension.getWidth() / 2 - 9 * 9 + x * 18, dimension.getHeight() - padding - 4 * 18 - 3 + y * 18 + (y == 3 ? 4 : 0));
    }

    @Override
    public void onClosed(PlayerEntity player) {
        if (this.backpackComponent != null) {
            this.backpackStack.set(Inmis.BACKPACK_COMPONENT, this.backpackComponent);
        }
    }

    @Override
    public boolean canUse(PlayerEntity player) {
        return backpackStack.getItem() instanceof BackpackItem;
    }

    public ItemStack getBackpackStack() {
        return backpackStack;
    }

    @Override
    public ItemStack quickMove(PlayerEntity player, int index) {
        ItemStack itemStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(index);
        if (slot != null && slot.hasStack()) {
            ItemStack toInsert = slot.getStack();
            itemStack = toInsert.copy();
            BackpackInfo tier = getItem().getTier();
            if (index < tier.getNumberOfRows() * tier.getRowWidth()) {
                if (!this.insertItem(toInsert, tier.getNumberOfRows() * tier.getRowWidth(), this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.insertItem(toInsert, 0, tier.getNumberOfRows() * tier.getRowWidth(), false)) {
                return ItemStack.EMPTY;
            }

            if (toInsert.isEmpty()) {
                slot.setStack(ItemStack.EMPTY);
            } else {
                slot.markDirty();
            }
        }

        return itemStack;
    }

    private class BackpackLockedSlot extends Slot {

        public BackpackLockedSlot(Inventory inventory, int index, int x, int y) {
            super(inventory, index, x, y);
        }

        @Override
        public boolean canTakeItems(PlayerEntity playerEntity) {
            return stackMovementIsAllowed(getStack());
        }

        @Override
        public boolean canInsert(ItemStack stack) {
            // If the "unstackables only" config option is turned on,
            // do not allow players to insert stacks with >1 max count.
            if (Inmis.CONFIG.unstackablesOnly) {
                if (stack.getMaxCount() > 1) {
                    return false;
                }
            }

            Item item = stack.getItem();
            // Do not allow players to insert shulkers into backpacks.
            if (inventory instanceof  SimpleInventory) {
                if (Inmis.CONFIG.disableShulkers) {//&& inventory instanceof BackpackInventory
                    if (item instanceof BlockItem blockItem) {
                        return !(blockItem.getBlock() instanceof ShulkerBoxBlock);
                    }
                }
                if (Inmis.CONFIG.blacklist.stream().map(Identifier::of).toList().contains(Registries.ITEM.getId(item))) {
                    return false;
                }
            }

            return stackMovementIsAllowed(stack);
        }

        private boolean stackMovementIsAllowed(ItemStack stack) {
            return !(stack.getItem() instanceof BackpackItem) && stack != backpackStack;
        }
    }

}
