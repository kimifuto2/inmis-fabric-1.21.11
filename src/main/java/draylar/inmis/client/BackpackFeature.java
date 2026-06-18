package draylar.inmis.client;

import draylar.inmis.item.BackpackItem;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.OverlayTexture;
import net.minecraft.client.render.command.OrderedRenderCommandQueue;
import net.minecraft.client.render.entity.feature.FeatureRenderer;
import net.minecraft.client.render.entity.feature.FeatureRendererContext;
import net.minecraft.client.render.entity.model.BipedEntityModel;
import net.minecraft.client.render.entity.model.EntityModel;
import net.minecraft.client.render.entity.state.EntityRenderState;
import net.minecraft.client.render.item.ItemRenderState;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemDisplayContext;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.RotationAxis;

@SuppressWarnings("rawtypes")
public class BackpackFeature extends FeatureRenderer {

    public BackpackFeature(FeatureRendererContext context) {
        super(context);
    }

    @Override
    public void render(MatrixStack matrices, OrderedRenderCommandQueue queue, int light, EntityRenderState state, float limbAngle, float limbDistance) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player == null) return;

        PlayerEntity player = client.player;
        ItemStack chestSlot = player.getEquippedStack(EquipmentSlot.CHEST);
        if (chestSlot.getItem() instanceof BackpackItem) {
            EntityModel<?> model = getContextModel();

            matrices.push();

            ModelPart root = model.getRootPart();
            root.applyTransform(matrices);

            if (model instanceof BipedEntityModel<?> biped) {
                biped.body.applyTransform(matrices);
            }

            matrices.translate(0, 0.25, 0.15);
            matrices.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(180));

            if (player.isSneaking()) {
                matrices.multiply(RotationAxis.POSITIVE_X.rotationDegrees(25));
                matrices.translate(0, -0.2, 0);
            }

            ItemRenderState renderState = new ItemRenderState();
            client.getItemModelManager().updateForLivingEntity(renderState, chestSlot, ItemDisplayContext.FIXED, player);
            renderState.render(matrices, queue, light, OverlayTexture.DEFAULT_UV, 0);

            matrices.pop();
        }
    }
}
