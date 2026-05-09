package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.PlayerFaceExtractor;
import net.minecraft.client.multiplayer.PlayerInfo;
import net.minecraft.client.renderer.entity.player.AvatarRenderer;
import net.minecraft.world.entity.player.Player;

import static com.minenash.customhud.CustomHud.CLIENT;

public class SupPlayerHeadIconElement extends IconElement {

    private final Supplier<PlayerInfo> supplier;
    public SupPlayerHeadIconElement(UUID providerID, Supplier<PlayerInfo> supplier, Flags flags) {
        super(flags, 10);
        this.providerID = providerID;
        this.supplier = supplier;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        int y = piece.y;
        PlayerInfo playerEntry = piece.value != null ? (PlayerInfo) piece.value : supplier.get();
        if (playerEntry == null)
            return;

        context.pose().pushMatrix();
        if (!referenceCorner)
            y -= (10*scale-10)/2;

        Player playerEntity = CLIENT.level.getPlayerByUUID(playerEntry.getProfile().id());
        boolean flip = playerEntity != null && AvatarRenderer.isPlayerUpsideDown(playerEntity);
        boolean hat = playerEntity != null && CLIENT.getConnection().getPlayerInfo(CLIENT.player.getUUID()).showHat();
        context.pose().translate(piece.x+((int)scale) + shiftX, y + shiftY);
        int size = (int)(8*scale);
        rotate(context.pose(), size, size);
        PlayerFaceExtractor.extractRenderState(context, playerEntry.getSkin().body().texturePath(), 0, 0, size, hat, flip, -1);
        context.pose().popMatrix();
    }

}
