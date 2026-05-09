package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.CustomHudRenderer3;
import com.minenash.customhud.render.RenderPiece;
import org.joml.Matrix3x2fStack;

import java.util.UUID;
import java.util.function.Function;
import java.util.function.Supplier;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.util.Mth;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;

public class RichItemSupplierIconElement extends IconElement {
    private static final Minecraft client = Minecraft.getInstance();

    private final Supplier<?> supplier;
    private final boolean showCount, showDur, showCooldown;
    private final int numSize;
    private final boolean invCount;

    public RichItemSupplierIconElement(UUID providerID, Supplier<?> supplier, Flags flags, boolean invCount) {
        super(flags, 11);
        this.supplier = supplier;
        this.showCount = flags.iconShowCount | invCount;
        this.showDur = flags.iconShowDur;
        this.showCooldown = flags.iconShowCooldown;
        this.numSize = flags.numSize;
        this.providerID = providerID;
        this.invCount = invCount;
    }

    @Override
    public Number getNumber() {
        return Item.getId(getStack().getItem());
    }

    @Override
    public boolean getBoolean() {
        return getStack().isEmpty();
    }

    @Override
    public int getTextWidth() {
        return CustomHudRenderer3.theme.fitItemIconsToLine ? width : (int)(16/11F * width);
    }

    private ItemStack getStack() {
        Object result = supplier.get();
        if (result instanceof ItemStack stack)
            return stack;
        if (result instanceof Function<?,?> func)
            return ((Function<RenderPiece, ItemStack>)func).apply(null);
        return ItemStack.EMPTY;
    }
    private ItemStack getStack(RenderPiece piece) {
        if (piece.value instanceof ItemLike ic)
            return ic.asItem().getDefaultInstance();
        if (piece.value instanceof ItemStack stack)
            return stack;
        Object result = supplier.get();
        if (result instanceof ItemStack stack)
            return stack;
        if (result instanceof Function<?,?> func)
            return ((Function<RenderPiece, ItemStack>)func).apply(piece);
        return ItemStack.EMPTY;
    }

    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        ItemStack stack = getStack(piece);
        if (stack == null || stack.isEmpty())
            return;
        Matrix3x2fStack matrices = context.pose();

        matrices.pushMatrix();
        matrices.translate(piece.x + shiftX, piece.y + shiftY - 2);
        int size = piece.shiftTextUpOrFitItemIcon ? 11 : 16;
        if (!referenceCorner)
            matrices.translate(0, -(size*scale-11)/2);
        matrices.scale(size/16F * scale, size/16F * scale);
        rotate(matrices, 16, 16);

        context.item(stack, 0, 0);

        int count = !invCount ? stack.getCount() : client.player.getInventory().countItem(stack.getItem());

        if (showCount && count != 1) {
            String string = String.valueOf(count);
            string = numSize == 0 ? string : numSize == 1 ? Flags.subNums(string) : Flags.supNums(string);
            context.text(client.font, string, 19 - 2 - client.font.width(string), numSize == 2 ? 0 : 9, 16777215, true);
        }

        if (showDur && stack.isBarVisible()) {
            int i = stack.getBarWidth();
            int j = stack.getBarColor();
            context.fill(RenderPipelines.GUI, 2, 13, 2 + 13, 13 + 2, -16777216);
            context.fill(RenderPipelines.GUI, 2, 13, 2 + i, 13 + 1, j | -16777216);
        }

        if (showCooldown) {
            float f = client.player.getCooldowns().getCooldownPercent(stack, client.getDeltaTracker().getGameTimeDeltaPartialTick(true));
            if (f > 0.0F) {
                int k = Mth.floor(16.0F * (1.0F - f));
                int l = k + Mth.ceil(16.0F * f);
                context.fill(RenderPipelines.GUI, 0, k, 16, l, Integer.MAX_VALUE);
            }
        }

        matrices.popMatrix();
    }

}
