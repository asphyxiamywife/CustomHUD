package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.CustomHud;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.IOException;
import java.util.Optional;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.resources.Resource;

public class SimpleTextureIconElement extends IconElement {
    private static final Minecraft client = Minecraft.getInstance();
    private static final Identifier TEXTURE_NOT_FOUND = Identifier.parse("textures/item/barrier.png");

    private final Identifier texture;
    private final int textureWidth;
    private final int textureHeight;
    private final int width;
    private final int height;
    private final int yOffset;
    private final int textWidth;
    private final RenderPipeline pipeline;

    private final boolean iconAvailable;


    public SimpleTextureIconElement(Identifier texture, boolean crosshair, Flags flags) {
        super(flags, 0);
        this.pipeline = crosshair ? RenderPipelines.CROSSHAIR : RenderPipelines.GUI_TEXTURED;

        NativeImage img = null;
        try {
            Optional<Resource> resource = client.getResourceManager().getResource(texture);
            if (resource.isPresent())
                img = NativeImage.read(resource.get().open());
        }
        catch (IOException e) { CustomHud.LOGGER.catching(e); }


        iconAvailable = img != null;
        this.texture = iconAvailable ? texture : TEXTURE_NOT_FOUND;

        textureWidth = iconAvailable ? img.getWidth() : 16;
        textureHeight = iconAvailable ? img.getHeight() : 16;

        height = (int) (11 * flags.scale);
        width = (int) (height * ((float)textureWidth/textureHeight));
        yOffset = referenceCorner ? 0 : (int) ((height*scale-height)/(scale*2));
        textWidth = flags.iconWidth == -1 ? width : flags.iconWidth;

    }

    @Override
    public Number getNumber() {
        return 0;
    }

    @Override
    public boolean getBoolean() {
        return true;
    }

    @Override
    public int getTextWidth() {
        return textWidth;
    }

    public boolean isIconAvailable() {
        return iconAvailable;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        if (width == 0)
            return;
        context.pose().pushMatrix();
        context.pose().translate(piece.x+shiftX, piece.y+shiftY-yOffset-2);
        rotate(context.pose(), width, height);
        context.blit(pipeline, texture, 0, 0, 0, 0, width, height, textureWidth, textureHeight, textureWidth, textureHeight);
        context.pose().popMatrix();
    }


}
