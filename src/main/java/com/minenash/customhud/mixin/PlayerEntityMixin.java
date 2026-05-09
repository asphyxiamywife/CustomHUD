package com.minenash.customhud.mixin;


import com.minenash.customhud.complex.ComplexData;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public class PlayerEntityMixin {

    @Inject(method = "attack", at = @At("HEAD"))
    private void logAttack(Entity target, CallbackInfo ci) {
        if (((Object)this) == Minecraft.getInstance().player && ComplexData.targetEntityHitPos != null) {
            ComplexData.lastHitEntity = target;
            ComplexData.lastHitEntityDist = ComplexData.targetEntityHitPos.distanceTo(Minecraft.getInstance().getCameraEntity().position());
            ComplexData.lastHitEntityTime = System.currentTimeMillis();
        }
    }

}
