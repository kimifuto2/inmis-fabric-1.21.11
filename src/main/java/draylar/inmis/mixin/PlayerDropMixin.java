package draylar.inmis.mixin;

import draylar.inmis.Inmis;
import draylar.inmis.api.TrinketCompat;
import draylar.inmis.item.BackpackItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerDropMixin extends LivingEntity {

    private PlayerDropMixin(EntityType<? extends LivingEntity> entityType, World world) {
        super(entityType, world);
    }

    @Inject(method = "onDeath", at = @At("HEAD"))
    private void emptyBackpacks(DamageSource source, CallbackInfo ci) {
        PlayerEntity self = (PlayerEntity) (Object) this;
        World world = self.getEntityWorld();
        if (!(world instanceof ServerWorld serverWorld)) return;
        if (serverWorld.getGameRules().getValue(GameRules.KEEP_INVENTORY)) return;

        List<ItemStack> toDrop = new ArrayList<>();
        List<Integer> toRemove = new ArrayList<>();

        // Main inventory (hotbar + 27 inventory slots) and the offhand slot.
        // Collector slot layout: main = 0..MAIN_SIZE-1, armor = MAIN_SIZE..MAIN_SIZE+3, offhand = MAIN_SIZE+4.
        if (Inmis.CONFIG.spillMainBackpacksOnDeath) {
            for (int i = 0; i < PlayerInventory.MAIN_SIZE; i++) {
                collectBackpack(self, i, toDrop, toRemove);
            }
            collectBackpack(self, PlayerInventory.MAIN_SIZE + 4, toDrop, toRemove); // offhand
        }

        // Armor slot backpacks.
        if (Inmis.CONFIG.spillArmorBackpacksOnDeath) {
            for (int i = PlayerInventory.MAIN_SIZE; i < PlayerInventory.MAIN_SIZE + 4; i++) {
                collectBackpack(self, i, toDrop, toRemove);
            }
        }

        // Trinkets (identical to the armor-scatter flag, as in the original).
        if (Inmis.TRINKETS_LOADED && Inmis.CONFIG.spillArmorBackpacksOnDeath) {
            TrinketCompat.spillTrinketInventory(self);
        }

        // Remove the emptied backpacks only after iterating. Remove highest slots first so
        // that a removing Stack does not shift the indices of lower slots that are still pending.
        toRemove.sort((a, b) -> b - a);
        for (int slot : toRemove) {
            self.getInventory().removeStack(slot);
        }
        for (ItemStack drop : toDrop) {
            self.dropItem(drop, true, false);
        }
    }

    @Unique
    private void collectBackpack(PlayerEntity self, int slot, List<ItemStack> toDrop, List<Integer> toRemove) {
        ItemStack stack = self.getInventory().getStack(slot);
        if (stack.getItem() instanceof BackpackItem) {
            List<ItemStack> contents = Inmis.getBackpackContents(stack);
            if (contents != null) {
                toDrop.addAll(contents);
            }
            // Wipe the backpack's contents, then drop the (now empty) backpack itself.
            Inmis.wipeBackpack(stack);
            toDrop.add(stack.copy());
            toRemove.add(slot);
        }
    }
}
