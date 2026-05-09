package com.minenash.customhud.ducks;

import net.minecraft.server.packs.metadata.pack.PackFormat;
import net.minecraft.util.InclusiveRange;

public interface ResourcePackProfileMetadataDuck {

    InclusiveRange<PackFormat> customhud$getPackVersionRange();
    void customhud$setPackVersionRange(InclusiveRange<PackFormat> version);

}
