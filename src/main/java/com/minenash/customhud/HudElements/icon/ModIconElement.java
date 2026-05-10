package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import com.terraformersmc.modmenu.util.mod.Mod;
import com.terraformersmc.modmenu.util.mod.fabric.FabricIconHandler;
import org.joml.Matrix3x2fStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.Identifier;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ModIconElement extends IconElement {

    private static Set<Identifier> cached = new HashSet<>();
    private static FabricIconHandler handler = new FabricIconHandler();

    public ModIconElement(UUID providerID, Flags flags) {
        super(flags, 11);
        this.providerID = providerID;
        cached.clear();
        handler.close();
        handler = new FabricIconHandler();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        Mod mod = (Mod) piece.value;
        int size = (int)(10 * scale * CLIENT.options.guiScale().get());

        Identifier id = Identifier.fromNamespaceAndPath("custom_hud", size + "___" + mod.getId());

        if (!cached.contains(id)) {
            try {
                DynamicTexture icon = mod.getIcon(handler, size);
                CLIENT.getTextureManager().register(id, icon);
                cached.add(id);
            }
            catch (Exception e) {
                id = Identifier.parse("textures/misc/unknown_pack.png");
            }
        }

        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        try {
            matrices.translate(piece.x + shiftX, piece.y + shiftY - 2);
            if (!referenceCorner)
                matrices.translate(0, -(11*scale-11)/2F);
//        matrices.scale(scale, scale, 0);
            int w = (int) (11 * scale);
            rotate(matrices, w, w);

            context.blit(RenderPipelines.GUI_TEXTURED, id, 0, 0, 0, 0, w, w, w, w);
        }
        finally {
            matrices.popMatrix();
        }
    }

}
