package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.complex.ComplexData;
import com.minenash.customhud.data.Flags;
import java.util.function.Supplier;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.material.Fluid;

import static com.minenash.customhud.CustomHud.CLIENT;

public class SpecialIdSupplier extends IdentifierSupplier {

    public static final Entry TARGET_BLOCK_ID = of( () -> BuiltInRegistries.BLOCK.getKey(ComplexData.targetBlock.getBlock()),
            () -> Block.getId(ComplexData.targetBlock),
            () -> !ComplexData.targetBlock.isAir());

    public static final Entry TARGET_FLUID_ID = of( () -> BuiltInRegistries.FLUID.getKey(ComplexData.targetFluid.getType()),
            () -> Fluid.FLUID_STATE_REGISTRY.getId(ComplexData.targetFluid),
            () -> !ComplexData.targetFluid.isEmpty());

    @Deprecated
    public static final Entry ITEM_ID = of( () -> BuiltInRegistries.ITEM.getKey(CLIENT.player.getMainHandItem().getItem()),
            () -> Item.getId(CLIENT.player.getMainHandItem().getItem()),
            () -> !CLIENT.player.getMainHandItem().isEmpty());

    @Deprecated
    public static final Entry OFFHAND_ITEM_ID = of( () -> BuiltInRegistries.ITEM.getKey(CLIENT.player.getOffhandItem().getItem()),
            () -> Item.getId(CLIENT.player.getOffhandItem().getItem()),
            () -> !CLIENT.player.getOffhandItem().isEmpty());


    public record Entry(Supplier<Identifier> identifierSupplier, Supplier<Number> numberSupplier, Supplier<Boolean> booleanSupplier) {}
    public static Entry of(Supplier<Identifier> identifierSupplier, Supplier<Number> numberSupplier, Supplier<Boolean> booleanSupplier) {
        return new Entry(identifierSupplier, numberSupplier, booleanSupplier);
    }

    private final Entry entry;

    public SpecialIdSupplier(Entry entry, Flags flags) {
        super(entry.identifierSupplier, flags);
        this.entry = entry;
    }


    @Override
    public Number getNumber() {
        return sanitize(entry.numberSupplier, Double.NaN);
    }

    @Override
    public boolean getBoolean() {
        return sanitize(entry.booleanSupplier, false);
    }

}
