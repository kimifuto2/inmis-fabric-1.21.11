package draylar.inmis.client;

import dev.emi.trinkets.api.SlotReference;
import dev.emi.trinkets.api.client.TrinketRenderer;
import draylar.inmis.Inmis;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.LivingEntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

public class TrinketBackpackRenderer implements TrinketRenderer {

    @Override
    public void render(ItemStack stack, SlotReference slotReference, EntityModel<? extends LivingEntityRenderState> contextModel, MatrixStack matrices, OrderedRenderCommandQueue queue, int light,
            LivingEntityRenderState state, float limbAngle, float limbDistance) {
        if (!Inmis.CONFIG.trinketRendering) {
            return;
        }

        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        matrices.push();

        ModelPart root = contextModel.getRootPart();
        root.applyTransform(matrices);

        if (contextModel instanceof BipedEntityModel<?> biped) {
            biped.body.applyTransform(matrices);
        }

        matrices.translate(0, 0.25, 0.15);
        matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

        if (client.player.isSneaking()) {
            matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(25));
            matrices.translate(0, -0.2, 0);
        }

        ItemRenderState renderState = new ItemRenderState();
        client.getItemModelManager().updateForLivingEntity(renderState, stack, ItemDisplayContext.FIXED, client.player);
        renderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);

        matrices.pop();
    }
}
