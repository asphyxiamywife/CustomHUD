package com.minenash.customhud.HudElements;

import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.data.NumberFlags;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class ItemTagCountElement implements HudElement {

    private final Identifier tag;
    private final NumberFlags flags;

    public ItemTagCountElement(Identifier tag, Flags flags) {
        this.tag = tag;
        this.flags = NumberFlags.of(flags);
    }

    @Override
    public String getString() {
        return flags.formatString( getNumber().doubleValue() );
    }

    @Override
    public Number getNumber() {
        TagKey<Item> tagKey = TagKey.create(Registries.ITEM, tag);

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null)
            return 0;

        int count = 0;
        for (ItemStack stack : player.getInventory())
            if (stack.is(tagKey))
                count += stack.getCount();

        return count;
    }

    @Override
    public boolean getBoolean() {
        return getNumber().intValue() > 0;
    }
}
