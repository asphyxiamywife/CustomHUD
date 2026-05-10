package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import org.joml.Matrix3x2fStack;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.world.BossEvent;

import static com.minenash.customhud.CustomHud.CLIENT;

public class BossbarIcon extends IconElement {

    public static class BasicBar extends BossEvent {
        public BasicBar(BossBarColor color, BossBarOverlay style) {
            super(UUID.randomUUID(), null, color, style);
        }
    }

    private final Supplier<BossEvent> supplier;

    public BossbarIcon(UUID providerID, Supplier<BossEvent> supplier, Flags flags) {
        super(flags, 182);
        this.providerID = providerID;
        this.supplier = supplier;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        BossEvent bossBar = piece.value != null ? (BossEvent) piece.value : supplier.get();
        if (bossBar == null)
            return;

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        try {
            matrices.translate(piece.x + shiftX, piece.y + shiftY + 1);
            if (!referenceCorner)
                matrices.translate(0, -(5*scale-5)/2);
            matrices.scale(scale, scale);
            rotate(matrices, 182, 5);

            extractBar(context, bossBar, 182, BossHealthOverlay.BAR_BACKGROUND_SPRITES, BossHealthOverlay.OVERLAY_BACKGROUND_SPRITES);
            int width = (int)(bossBar.getProgress() * 182);
            if (width > 0)
                extractBar(context, bossBar, width, BossHealthOverlay.BAR_PROGRESS_SPRITES, BossHealthOverlay.OVERLAY_PROGRESS_SPRITES);
        }
        finally {
            matrices.popMatrix();
        }
    }

    public static void extractBar(GuiGraphicsExtractor context, BossEvent bossBar, int width, Identifier[] barSprites, Identifier[] overlaySprites) {
        context.blitSprite(RenderPipelines.GUI_TEXTURED, barSprites[bossBar.getColor().ordinal()], 182, 5, 0, 0, 0, 0, width, 5);
        if (bossBar.getOverlay() != BossEvent.BossBarOverlay.PROGRESS)
            context.blitSprite(RenderPipelines.GUI_TEXTURED, overlaySprites[bossBar.getOverlay().ordinal() - 1], 182, 5, 0, 0, 0, 0, width, 5);
    }

}
