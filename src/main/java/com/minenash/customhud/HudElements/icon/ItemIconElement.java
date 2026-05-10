package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemIconElement extends IconElement {

    private final Item item;

    public ItemIconElement(Item item, Flags flags) {
        super(flags, 11);
        this.item = item;
    }

    @Override
    public Number getNumber() {
        return Item.getId(item);
    }

    @Override
    public boolean getBoolean() {
        return item == null;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        try {
            renderItemStack(context, piece.x, piece.y, item.getDefaultInstance(), piece.shiftTextUpOrFitItemIcon);
        }
        catch (NullPointerException ignored) {
            // Some modpacks trigger profile parsing/render setup before item components are bound.
        }
    }

    @Override
    public int getTextWidth() {
        return CustomHudRenderer3.theme.fitItemIconsToLine ? width : (int) (width * 16F/11);
    }
}
