package com.minenash.customhud.mixin;

import net.minecraft.client.multiplayer.PingDebugMonitor;
import net.minecraft.network.protocol.ping.ClientboundPongResponsePacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(PingDebugMonitor.class)
public class PingMeasurerMixin {

    @Inject(method = "onPongReceived", at = @At("HEAD"))
    public void getPing(ClientboundPongResponsePacket packet, CallbackInfo ci) {
//        ComplexData.ping = (int) (Util.getMeasuringTimeMs() - packet.getStartTime());
    }

}
