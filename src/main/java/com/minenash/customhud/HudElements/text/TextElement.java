package com.minenash.customhud.HudElements.text;

import com.minenash.customhud.HudElements.functional.FunctionalElement;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.network.chat.Component;

import static com.minenash.customhud.CustomHud.CLIENT;

public abstract class TextElement extends FunctionalElement {

    public abstract int getTextWidth();
    public abstract Component getText();
    public int getColor(int current) {
        return  current;
    }

//    public void extractRenderState(DrawContext context, RenderPiece piece) {
//        context.drawText(CLIENT.textRenderer, getText(), piece.x, piece.y, piece.color, piece.shadow);
//    }
}
