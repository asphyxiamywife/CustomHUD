package com.minenash.customhud.HudElements.text;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.mixin.accessors.HudAccessor;
import net.minecraft.util.Mth;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ActionbarMsgElement extends TextSupplierElement {
    public ActionbarMsgElement(Flags flags) { super(ACTIONBAR_MSG, flags); }

    @Override
    public int getColor(int current) {
        HudAccessor hud = (HudAccessor) CLIENT.gui.hud;
        float h = (float) hud.getOverlayMessageTime();
        int k = !hud.getAnimateOverlayMessageColor() ? (current & 0xFFFFFF) : Mth.hsvToRgb(h / 50.0F, 0.7F, 0.6F) & 0xFFFFFF;
        int m = Math.min((int)(h * 255.0F / 20.0F), 255) << 24 & 0xFF000000;

        return k | m;
    }
}
