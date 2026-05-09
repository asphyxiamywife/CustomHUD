package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.conditionals.Operation;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.client.gui.GuiGraphicsExtractor;

public class SpaceElement extends IconElement {

    private final Operation width;

    public SpaceElement(Operation width) {
        super(new Flags(), 0);
        this.width = width;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {}

    @Override
    public int getTextWidth() {
        return (int)width.getValue();
    }
}