package com.minenash.customhud.mixin.accessors;

import net.minecraft.client.gui.Hud;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Hud.class)
public interface HudAccessor {

    @Accessor Component getOverlayMessageString();
    @Accessor int getOverlayMessageTime();
    @Accessor boolean getAnimateOverlayMessageColor();
    @Accessor int getTitleTime();
    @Accessor Component getTitle();
    @Accessor Component getSubtitle();
    @Accessor int getTitleFadeInTime();
    @Accessor int getTitleStayTime();
    @Accessor int getTitleFadeOutTime();

}
