package com.minenash.customhud.mixin;

import com.minenash.customhud.CustomHud;
import net.minecraft.client.MouseHandler;
import net.minecraft.client.input.MouseButtonInfo;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(MouseHandler.class)
public class MouseMixin {

    @Inject(method = "onButton", at = @At("HEAD"))
    public void setIsMouseKeyDown(long window, MouseButtonInfo input, int action, CallbackInfo ci) {
        if (action == GLFW.GLFW_PRESS)
            CustomHud.IS_MOUSE_DOWN.put(input.button(), true);
        else if (action == GLFW.GLFW_RELEASE)
            CustomHud.IS_MOUSE_DOWN.put(input.button(), false);
    }

}
