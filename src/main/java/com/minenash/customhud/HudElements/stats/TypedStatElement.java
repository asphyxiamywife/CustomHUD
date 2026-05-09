package com.minenash.customhud.HudElements.stats;

import com.minenash.customhud.data.Flags;
import net.minecraft.client.Minecraft;
import net.minecraft.stats.StatType;
import com.minenash.customhud.HudElements.interfaces.HudElement;

public class TypedStatElement<T> implements HudElement {

    private final StatType<T> type;
    private final T entry;
    private final Flags flags;

    public TypedStatElement(StatType<T> type, T entry, Flags flags) {
        this.type = type;
        this.entry = entry;
        this.flags = flags;
        if (flags.precision == -1)
            flags.precision = 0;
    }

    private int get() {
        return type.contains(entry) ? Minecraft.getInstance().player.getStats().getValue(type.get(entry)) : 0;
    }

    @Override
    public String getString() {
        if (!type.contains(entry))
            return "0";

        int value =  Minecraft.getInstance().player.getStats().getValue(type, entry);
        return flags.formatted ? type.get(entry).format(value) : String.format("%."+ flags.precision +"f", value * flags.scale);
    }

    @Override
    public Number getNumber() {
        return get();
    }

    @Override
    public boolean getBoolean() {
        return get() > 0;
    }
}
