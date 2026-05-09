package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.minenash.customhud.render.RenderPiece;
import java.util.UUID;
import java.util.function.Supplier;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class ItemConvertableIconElement extends IconElement {

    private final Supplier<ItemLike> supplier;

    public ItemConvertableIconElement(UUID providerID, Supplier<ItemLike> supplier, Flags flags) {
        super(flags, 11);
        this.providerID = providerID;
        this.supplier = supplier;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        ItemStack stack = new ItemStack(piece.value == null ? supplier.get() : (ItemLike)piece.value);
        if (piece.value != null)
            renderItemStack(context, piece.x, piece.y, stack, piece.shiftTextUpOrFitItemIcon);
    }

    @Override
    public int getTextWidth() {
        return CustomHudRenderer3.theme.fitItemIconsToLine ? width : (int) (width * 16F/11);
    }
}
