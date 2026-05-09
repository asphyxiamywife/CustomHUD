package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.complex.ListManager;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import com.terraformersmc.modmenu.util.DrawingUtil;
import com.terraformersmc.modmenu.util.mod.Mod;
import org.joml.Matrix3x2fStack;

import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ModBadgeIconElement extends IconElement{

    public ModBadgeIconElement(UUID providerID, Flags flags) {
        super(flags, -1);
        this.providerID = providerID;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();

        Mod.Badge badge = (Mod.Badge) piece.value;
        int width = CLIENT.font.width(badge.getText()) + 6;

        matrices.translate(piece.x + shiftX, piece.y + shiftY - 1);
        if (!referenceCorner)
            matrices.translate(0, -(9*scale-9)/2);
        matrices.scale(scale, scale);
        rotate(matrices, width+1, 9);


        DrawingUtil.drawBadge(context, 0, 0, width, badge.getText().getVisualOrderText(),badge.getOutlineColor(), badge.getFillColor(), piece.color);

        matrices.popMatrix();
    }

    @Override
    public int getTextWidth() {
        return width >= 0 ? width : Mth.ceil(scale*(CLIENT.font.width( ((Mod.Badge)ListManager.getValue(providerID)).getText() ) + 6 + 1));
    }
}
