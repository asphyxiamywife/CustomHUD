package com.minenash.customhud.HudElements.stats;

import com.minenash.customhud.data.Flags;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import net.minecraft.stats.Stat;
import com.minenash.customhud.HudElements.interfaces.HudElement;

public class CustomStatElement implements HudElement {

    private final Stat<Identifier> stat;
    private final Flags flags;

    public CustomStatElement(Stat<Identifier> stat, Flags flags) {
        this.stat = stat;
        this.flags = flags;
        if (flags.precision == -1)
            flags.precision = 0;
    }

    private int get() {
        return Minecraft.getInstance().player.getStats().getValue(stat);
    }

    @Override
    public String getString() {
        return flags.formatted ? stat.format(get()) : String.format("%."+ flags.precision +"f", get() * flags.scale);
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
