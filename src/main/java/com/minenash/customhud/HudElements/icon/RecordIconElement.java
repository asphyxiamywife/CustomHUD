package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

public class RecordIconElement extends IconElement {

    private static final ItemStack NO_RECORD = new ItemStack(Items.BARRIER);

    public RecordIconElement(Flags flags) {
        super(flags, 11);
    }

    @Override
    public Number getNumber() {
        return MusicAndRecordTracker.isRecordPlaying ? Item.getId(MusicAndRecordTracker.getClosestRecord().icon.getItem()) : 0;
    }

    @Override
    public boolean getBoolean() {
        return MusicAndRecordTracker.isRecordPlaying && MusicAndRecordTracker.getClosestRecord().icon.isEmpty();
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        renderItemStack(context, piece.x, piece.y, MusicAndRecordTracker.isRecordPlaying ? MusicAndRecordTracker.getClosestRecord().icon : NO_RECORD, piece.shiftTextUpOrFitItemIcon);
    }

    @Override
    public int getTextWidth() {
        return CustomHudRenderer3.theme.fitItemIconsToLine ? width : (int) (width * 16F/11);
    }

}
