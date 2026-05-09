package com.minenash.customhud.mixin;

import com.minenash.customhud.ducks.ResourcePackProfileMetadataDuck;
import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.util.InclusiveRange;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(Pack.Metadata.class)
public class ResourcePackProfileMetadataMixin implements ResourcePackProfileMetadataDuck {

    @Unique private InclusiveRange<PackFormat> version = null;

    @Override
    public InclusiveRange<PackFormat> customhud$getPackVersionRange() {
        return version;
    }

    @Override
    public void customhud$setPackVersionRange(InclusiveRange<PackFormat> version) {
        this.version = version;
    }
}
