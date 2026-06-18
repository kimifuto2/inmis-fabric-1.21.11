package draylar.inmis.ui;

import draylar.inmis.Inmis;
import draylar.inmis.api.Dimension;
import draylar.inmis.api.Rectangle;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.screen.slot.Slot;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public class BackpackHandledScreen extends HandledScreen<BackpackScreenHandler> {

    private static final Identifier GUI_TEXTURE = Identifier.of("inmis", "textures/gui/backpack_container.png");
    private static final Identifier SLOT_TEXTURE = Identifier.of("inmis", "textures/gui/backpack_slot.png");

    private final int guiTitleColor = (int) Long.parseLong(Inmis.CONFIG.guiTitleColor.replaceFirst("^#?0[xX]?", ""), 16);

    public BackpackHandledScreen(BackpackScreenHandler handler, PlayerInventory player, Text title) {
        super(handler, player, handler.getBackpackStack().getName());

        Dimension dimension = handler.getDimension();
        this.backgroundWidth = dimension.getWidth();
        this.backgroundHeight = dimension.getHeight();
        this.titleY = 7;
        this.playerInventoryTitleX = handler.getPlayerInvSlotPosition(dimension, 0, 0).x;
        this.playerInventoryTitleY = this.backgroundHeight - 94;
    }

    @Override
    protected void drawBackground(DrawContext context, float delta, int mouseX, int mouseY) {
        int x = (this.width - this.backgroundWidth) / 2;
        int y = (this.height - this.backgroundHeight) / 2;
        renderBackgroundTexture(context, new Rectangle(x, y, backgroundWidth, backgroundHeight), delta, 0xFFFFFFFF);
        for (Slot slot : getScreenHandler().slots) {
            context.drawTexture(RenderPipelines.GUI_TEXTURED, SLOT_TEXTURE, x + slot.x - 1, y + slot.y - 1, 0f, 0f, 18, 18, 18, 18);
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        super.render(context, mouseX, mouseY, delta);
        this.drawMouseoverTooltip(context, mouseX, mouseY);
    }

    @Override
    public void drawForeground(DrawContext context, int mouseX, int mouseY) {
        context.drawText(this.textRenderer, title, titleX, titleY, guiTitleColor, false);
        context.drawText(this.textRenderer, playerInventoryTitle, playerInventoryTitleX, playerInventoryTitleY, guiTitleColor, false);
    }

    public void renderBackgroundTexture(DrawContext context, Rectangle bounds, float delta, int color) {
        int x = bounds.x, y = bounds.y, width = bounds.width, height = bounds.height;
        int xTextureOffset = 0;
        int yTextureOffset = 66;

        // Four Corners
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y, 106 + xTextureOffset, 124 + yTextureOffset, 8, 8, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + width - 8, y, 248 + xTextureOffset, 124 + yTextureOffset, 8, 8, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x, y + height - 8, 106 + xTextureOffset, 182 + yTextureOffset, 8, 8, 256, 256);
        context.drawTexture(RenderPipelines.GUI_TEXTURED, GUI_TEXTURE, x + width - 8, y + height - 8, 248 + xTextureOffset, 182 + yTextureOffset, 8, 8, 256, 256);

        // Sides
        context.drawTexturedQuad(GUI_TEXTURE, x + 8, y, x + width - 8, y + 8, (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (124 + yTextureOffset) / 256f,
                (132 + yTextureOffset) / 256f);
        context.drawTexturedQuad(GUI_TEXTURE, x + 8, y + height - 8, x + width - 8, y + height, (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f,
                (182 + yTextureOffset) / 256f, (190 + yTextureOffset) / 256f);
        context.drawTexturedQuad(GUI_TEXTURE, x, y + 8, x + 8, y + height - 8, (106 + xTextureOffset) / 256f, (114 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f,
                (182 + yTextureOffset) / 256f);
        context.drawTexturedQuad(GUI_TEXTURE, x + width - 8, y + 8, x + width, y + height - 8, (248 + xTextureOffset) / 256f, (256 + xTextureOffset) / 256f,
                (132 + yTextureOffset) / 256f, (182 + yTextureOffset) / 256f);

        // Center
        context.drawTexturedQuad(GUI_TEXTURE, x + 8, y + 8, x + width - 8, y + height - 8, (114 + xTextureOffset) / 256f, (248 + xTextureOffset) / 256f, (132 + yTextureOffset) / 256f,
                (182 + yTextureOffset) / 256f);
    }

}
