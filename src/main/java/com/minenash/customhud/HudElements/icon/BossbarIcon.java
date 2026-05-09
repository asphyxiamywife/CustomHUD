package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import org.joml.Matrix3x2fStack;

import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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
        matrices.translate(piece.x + shiftX, piece.y + shiftY + 1);
        if (!referenceCorner)
            matrices.translate(0, -(5*scale-5)/2);
        matrices.scale(scale, scale);
        rotate(matrices, 182, 5);

        context.fill(0, 0, 182, 5, 0xFF555555);
        context.fill(1, 1, 1 + (int)(180 * bossBar.getProgress()), 4, 0xFFFFFFFF);

        matrices.popMatrix();
    }

}
