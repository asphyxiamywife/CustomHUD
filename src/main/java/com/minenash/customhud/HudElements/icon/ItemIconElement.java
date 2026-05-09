package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemIconElement extends IconElement {

    private final ItemStack stack;

    public ItemIconElement(ItemStack stack, Flags flags) {
        super(flags, 11);
        this.stack = stack;
    }

    @Override
    public Number getNumber() {
        return Item.getId(stack.getItem());
    }

    @Override
    public boolean getBoolean() {
        return stack.isEmpty();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        renderItemStack(context, piece.x, piece.y, stack, piece.shiftTextUpOrFitItemIcon);
    }

    @Override
    public int getTextWidth() {
        return CustomHudRenderer3.theme.fitItemIconsToLine ? width : (int) (width * 16F/11);
    }
}
