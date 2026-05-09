package com.minenash.customhud.mixin.accessors;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;

@Mixin(AttributeSupplier.class)
public interface DefaultAttributeContainerAccessor {

    @Accessor Map<Attribute, AttributeInstance> getInstances();

}
