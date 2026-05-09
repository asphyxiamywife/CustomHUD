package com.minenash.customhud.mixin;

import com.minenash.customhud.complex.ComplexData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.UUID;
import net.minecraft.network.protocol.game.ClientboundBossEventPacket;
import net.minecraft.server.bossevents.CustomBossEvent;
import net.minecraft.world.BossEvent;

@Mixin(ClientboundBossEventPacket.class)
public class BossBarS2CPacketMixin {

    @Inject(method = "createAddPacket", at = @At("HEAD"))
    private static void addBossBar(BossEvent bar, CallbackInfoReturnable<ClientboundBossEventPacket> cir) {
        if (!(bar instanceof CustomBossEvent))
            ComplexData.bossbars.put(bar.getId(), bar);
    }

    @Inject(method = "createRemovePacket", at = @At("HEAD"))
    private static void removeBossBar(UUID uuid, CallbackInfoReturnable<ClientboundBossEventPacket> cir) {
        ComplexData.bossbars.remove(uuid);
    }

}
