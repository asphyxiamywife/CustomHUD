package com.minenash.customhud.mixin;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.complex.ComplexData;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.protocol.game.ClientboundMerchantOffersPacket;
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket;
import net.minecraft.network.protocol.game.ServerboundContainerClosePacket;
import net.minecraft.world.inventory.MenuType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.customhud.CustomHud.CLIENT;

@Mixin(ClientPacketListener.class)
public class ClientPlayNetworkHandlerMixin {

    @Redirect(method = "tick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/DebugScreenOverlay;showNetworkCharts()Z"))
    private boolean pingForMetricVariables(DebugScreenOverlay hud) {
        return hud.showNetworkCharts() || (ProfileManager.getActive() != null && ProfileManager.getActive().enabled.pingMetrics);
    }

    @Inject(method = "handleMerchantOffers", at = @At("HEAD"))
    public void getTradeOffer(ClientboundMerchantOffersPacket packet, CallbackInfo ci) {
        if (ComplexData.fakeVillagerInteract > 0) {
            ComplexData.fakeVillagerInteract--;
            ComplexData.villagerOffers = packet.getOffers();
            ComplexData.villagerXP = packet.getVillagerXp();
        }
    }

    @Inject(at = @At("HEAD"), method = "handleOpenScreen", cancellable = true)
    public void onOpenScreen(ClientboundOpenScreenPacket packet, CallbackInfo ci) {
        if (packet.getType() == MenuType.MERCHANT && ComplexData.fakeVillagerInteract > 0) {
            CLIENT.getConnection().send(new ServerboundContainerClosePacket(packet.getContainerId()));
            ComplexData.fakeVillagerInteract--;
            ci.cancel();
        }
    }

}
