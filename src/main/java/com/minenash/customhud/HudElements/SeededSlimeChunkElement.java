package com.minenash.customhud.HudElements;

import com.minenash.customhud.HudElements.supplier.BooleanSupplierElement;
import java.util.OptionalLong;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.WorldgenRandom;

public class SeededSlimeChunkElement extends BooleanSupplierElement {

    public SeededSlimeChunkElement(String seedStr) {
        super( () -> {
            long seed = WorldOptions.parseSeed(seedStr).getAsLong();
            return WorldgenRandom.seedSlimeChunk(blockPos().getX() >> 4, blockPos().getZ() >> 4, seed, 987234911L).nextInt(10) == 0;
        } );
    }

}
