package com.minenash.customhud.mixin.fonts;

import com.minenash.customhud.render.CustomHudRenderer3;
import net.minecraft.client.StringSplitter;
import net.minecraft.network.chat.Style;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;

@Mixin(StringSplitter.class)
public class TextHandlerMixin {

    @ModifyArg(method = "stringWidth(Ljava/lang/String;)F", at = @At(value = "INVOKE", target = "Lnet/minecraft/util/StringDecomposer;iterateFormatted(Ljava/lang/String;Lnet/minecraft/network/chat/Style;Lnet/minecraft/util/FormattedCharSink;)Z"), index = 1)
    public Style changeStyle(Style old) {
        return CustomHudRenderer3.font == null ? old : old.withFont(CustomHudRenderer3.font);
    }

}
