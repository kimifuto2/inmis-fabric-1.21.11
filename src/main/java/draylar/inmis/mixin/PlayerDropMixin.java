package draylar.inmis.mixin;

import draylar.inmis.Inmis;
import draylar.inmis.api.TrinketCompat;
import draylar.inmis.item.BackpackItem;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerEntity.class)
public abstract class PlayerDropMixin extends LivingEntity {

    @Shadow
    @Final
    private PlayerInventory inventory;

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

        if (Inmis.CONFIG.spillArmorBackpacksOnDeath || Inmis.CONFIG.spillMainBackpacksOnDeath) {
            for (int i = 0; i < self.getInventory().size(); i++) {
                ItemStack stack = self.getInventory().getStack(i);
                if (stack.getItem() instanceof BackpackItem) {
                    List<ItemStack> contents = Inmis.getBackpackContents(stack);
                    if (contents != null) {
                        toDrop.addAll(contents);
                    }
                    Inmis.wipeBackpack(stack);
                    toDrop.add(stack.copy());
                    self.getInventory().removeStack(i);
                }
            }
        }

        if (Inmis.TRINKETS_LOADED && Inmis.CONFIG.spillArmorBackpacksOnDeath) {
            TrinketCompat.spillTrinketInventory(self);
        }

        for (ItemStack drop : toDrop) {
            self.dropItem(drop, true, false);
        }
    }
}
