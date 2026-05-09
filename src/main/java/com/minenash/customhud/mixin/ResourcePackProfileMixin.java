package com.minenash.customhud.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.minenash.customhud.ducks.ResourcePackProfileMetadataDuck;
import net.minecraft.server.packs.PackLocationInfo;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.metadata.pack.PackMetadataSection;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.InclusiveRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Pack.class)
public class ResourcePackProfileMixin {

    @Unique private static InclusiveRange<PackFormat> temp = null;

    @WrapOperation(method = "readPackMetadata", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/packs/metadata/pack/PackMetadataSection;supportedFormats()Lnet/minecraft/util/InclusiveRange;"))
    private static InclusiveRange<PackFormat> setPackVersionPart1(PackMetadataSection instance, Operation<InclusiveRange<PackFormat>> original) {
        return temp = original.call(instance);
    }

    @Inject(method = "readPackMetadata", at = @At("RETURN"))
    private static void setPackVersionPart2(PackLocationInfo info, Pack.ResourcesSupplier packFactory, PackFormat version, PackType type, CallbackInfoReturnable<Pack.Metadata> cir) {
        ResourcePackProfileMetadataDuck value = ((ResourcePackProfileMetadataDuck)(Object)cir.getReturnValue());
        if (value != null)
            value.customhud$setPackVersionRange( temp );
    }
}
