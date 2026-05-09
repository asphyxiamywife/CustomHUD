package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.HudElements.interfaces.IdElement;
import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.minenash.customhud.data.Flags;
import java.util.function.Supplier;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.Identifier;

import static com.minenash.customhud.CustomHud.CLIENT;

public class IdentifierSupplier implements HudElement, IdElement {
    private static BlockPos blockPos() { return CLIENT.getCameraEntity().blockPosition(); }

    public static final Supplier<Identifier> DIMENSION_ID = () -> CLIENT.level.dimension().identifier();
    public static final Supplier<Identifier> BIOME_ID = () -> CLIENT.level.getBiome(blockPos()).unwrapKey().get().identifier();
    public static final Supplier<Identifier> MUSIC_ID = () -> MusicAndRecordTracker.isMusicPlaying ? MusicAndRecordTracker.musicId : null;
    public static final Supplier<Identifier> RECORD_ID = () -> MusicAndRecordTracker.isRecordPlaying ? MusicAndRecordTracker.getClosestRecord().id : null;



    private final Supplier<Identifier> supplier;
    private final Flags.IdPart idPart;

    public IdentifierSupplier(Supplier<Identifier> supplier, Flags flags) {
        this.supplier = supplier;
        this.idPart = flags.idPart;
    }

    public Identifier getIdentifier() {
        return sanitize(supplier, null);
    }


    @Override
    public String getString() {
        return IdElement.getString(getIdentifier(), idPart);
    }

    @Override
    public Number getNumber() {
        Identifier id = getIdentifier();
        return id == null ? 0 : id.toString().length();
    }

    @Override
    public boolean getBoolean() {
        Identifier id = getIdentifier();
        return id != null && !id.toString().isEmpty();
    }


}
