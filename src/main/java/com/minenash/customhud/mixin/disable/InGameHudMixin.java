package com.minenash.customhud.mixin.disable;

import com.llamalad7.mixinextras.injector.v2.WrapWithCondition;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.minenash.customhud.CustomHud;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.minenash.customhud.data.DisableElement.*;

@Mixin(value = Gui.class, priority = 10000)
public abstract class InGameHudMixin {

    @Inject(method = "extractItemHotbar", at = @At("HEAD"), cancellable = true, require = 0)
    public void customhud$disableHotbar(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(HOTBAR))
            ci.cancel();
    }

    @Inject(method = "extractBossOverlay", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableBossBar(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(BOSSBARS))
            ci.cancel();
    }

    @Inject(method = "extractPlayerHealth", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableStatusBars(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (CustomHud.isDisabled(STATUS_BARS))
            ci.cancel();
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractArmor(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIII)V"), require = 0)
    public void customhud$disableArmor(GuiGraphicsExtractor context, Player player, int i, int j, int k, int x, Operation<Void> original) {
        if (CustomHud.isNotDisabled(ARMOR))
            original.call(context, player, i, j, k, x);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractHearts(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;IIIIFIIIZ)V"), require = 0)
    public void customhud$disableHealthBar(Gui instance, GuiGraphicsExtractor context, Player player, int x, int y, int lines, int regeneratingHeartIndex, float maxHealth, int lastHealth, int health, int absorption, boolean blinking, Operation<Void> original) {
        if (CustomHud.isNotDisabled(HEALTH))
            original.call(instance, context, player, x, y, lines, regeneratingHeartIndex, maxHealth, lastHealth, health, absorption, blinking);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractFood(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;II)V"), require = 0)
    public void customhud$disableHunger(Gui instance, GuiGraphicsExtractor context, Player player, int top, int right, Operation<Void> original) {
        if (CustomHud.isNotDisabled(HUNGER))
            original.call(instance, context, player, top, right);
    }

    @WrapOperation(method = "extractPlayerHealth", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;extractAirBubbles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/world/entity/player/Player;III)V"), require = 0)
    public void customhud$disableAir(Gui instance, GuiGraphicsExtractor context, Player player, int heartCount, int top, int left, Operation<Void> original) {
        if (CustomHud.isNotDisabled(AIR))
            original.call(instance, context, player, heartCount, top, left);
    }

    @Inject(method = "extractVehicleHealth", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableHorseHealth(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (CustomHud.isDisabled(HORSE) || CustomHud.isDisabled(HORSE_HEALTH) || CustomHud.isDisabled(STATUS_BARS))
            ci.cancel();
    }

    @WrapWithCondition(method = "extractHotbarAndDecorations", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/contextualbar/ContextualBarRenderer;renderExperienceLevel(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/gui/Font;I)V"), require = 0)
    public boolean customhud$disableXPLvl(GuiGraphicsExtractor context, Font textRenderer, int level) {
        return CustomHud.isNotDisabled(XP);
    }

    @Inject(method = "extractSelectedItemName", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableHotbar(GuiGraphicsExtractor context, CallbackInfo ci) {
        if (CustomHud.isDisabled(ITEM_TOOLTIP))
            ci.cancel();
    }

    @Inject(method = "extractEffects", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableStatusEffects(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(STATUS_EFFECTS))
            ci.cancel();
    }

    @Inject(method = "extractSubtitleOverlay", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableSubtitles(GuiGraphicsExtractor context, boolean defer, CallbackInfo ci) {
        if (CustomHud.isDisabled(SUBTITLES))
            ci.cancel();
    }

    @Inject(method = "extractScoreboardSidebar(Lnet/minecraft/client/gui/GuiGraphicsExtractor;Lnet/minecraft/client/DeltaTracker;)V", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableScoreboard(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(SCOREBOARD))
            ci.cancel();
    }

    @Inject(method = "extractChat", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableChat(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(CHAT))
            ci.cancel();
    }

    @Inject(method = "extractTitle", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableTitles(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(TITLES))
            ci.cancel();
    }

    @Inject(method = "extractOverlayMessage", at = @At(value = "HEAD"), cancellable = true, require = 0)
    public void customhud$disableActionbarMsg(GuiGraphicsExtractor context, DeltaTracker tickCounter, CallbackInfo ci) {
        if (CustomHud.isDisabled(ACTIONBAR))
            ci.cancel();
    }

}
