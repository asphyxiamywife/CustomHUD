package com.minenash.customhud.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
import net.minecraft.advancements.criterion.BlockPredicate;
import net.minecraft.world.item.AdventureModePredicate;

@Mixin(AdventureModePredicate.class)
public interface BlockPredicatesComponentAccessor {

    @Accessor List<BlockPredicate> getPredicates();

}
