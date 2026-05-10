package com.minenash.customhud.HudElements.text;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.mixin.accessors.HudAccessor;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;
import net.minecraft.util.Mth;

import static com.minenash.customhud.CustomHud.CLIENT;

public class TitleMsgElement extends TextSupplierElement {
    public TitleMsgElement(Supplier<Component> supplier, Flags flags) { super(supplier, flags); }

    @Override
    public int getColor(int current) {
        int l = 0;
        HudAccessor hud = (HudAccessor) CLIENT.gui.hud;
        if (hud.getTitleTime() > hud.getTitleFadeOutTime() + hud.getTitleStayTime()) {
            float o = (float)(hud.getTitleFadeInTime() + hud.getTitleStayTime() + hud.getTitleFadeOutTime()) - hud.getTitleTime();
            l = (int)(o * 255.0F / hud.getTitleFadeInTime());
        }

        if (hud.getTitleTime() <= hud.getTitleFadeOutTime())
            l = (int)(hud.getTitleTime() * 255.0F / hud.getTitleFadeOutTime());

        return (current & 0xFFFFFF) | Mth.clamp(l, 0, 255) << 24 & 0xFF000000;
    }
}
