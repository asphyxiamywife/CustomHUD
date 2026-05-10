package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.ARGB;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;

public class StatusEffectIconElement extends IconElement {
    private static final Identifier EFFECT_BACKGROUND_AMBIENT_TEXTURE = Identifier.parse("hud/effect_background_ambient");
    private static final Identifier EFFECT_BACKGROUND_TEXTURE = Identifier.parse("hud/effect_background");

    private final Supplier<MobEffectInstance> supplier;
    private final boolean background;
    private final int effectOffset;
    private final int renderWidth;

    public StatusEffectIconElement(UUID providerID, Supplier<MobEffectInstance> supplier, Flags flags, boolean background) {
        super(flags, flags.scale == 1 ? 11 : 12);
        this.supplier = supplier;
        this.background = background;
        this.effectOffset = scale == 1 ? 1 : Math.round(3F/2*scale);
        this.providerID = providerID;
        this.renderWidth = flags.scale == 1 ? 11 : (int) (flags.scale * 12);
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        context.pose().pushMatrix();
        try {
            MobEffectInstance effect = piece.value != null ? (MobEffectInstance) piece.value : supplier.get();
            if (effect == null)
                return;

            int y= piece.y - 2;
            if (!referenceCorner && scale != 1)
               y-= (renderWidth-12)/2;

            Identifier texture = Gui.getMobEffectSprite(effect.getEffect());
            int m = effect.getDuration();
            float f = !effect.endsWithin(200) ? 1.0f :
                Mth.clamp((float)m / 10.0f / 5.0f * 0.5f, 0.0f, 0.5f) + Mth.cos((float)m * (float)Math.PI / 5.0f) * Mth.clamp((float)(10 - m / 20) / 10.0f * 0.25f, 0.0f, 0.25f);

            context.pose().translate(piece.x + shiftX, y + shiftY);
            rotate(context.pose(), renderWidth, renderWidth);

            if (background)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, effect.isAmbient() ? EFFECT_BACKGROUND_AMBIENT_TEXTURE : EFFECT_BACKGROUND_TEXTURE, 0, 0, renderWidth, renderWidth);
            context.blitSprite(RenderPipelines.GUI_TEXTURED, texture, effectOffset, effectOffset, (int)(9*scale), (int)(9*scale), ARGB.white(f));
        }
        finally {
            context.pose().popMatrix();
        }

    }

}
