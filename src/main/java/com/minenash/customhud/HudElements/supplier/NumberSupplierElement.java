package com.minenash.customhud.HudElements.supplier;

import com.minenash.customhud.HudElements.interfaces.HudElement;
import com.minenash.customhud.HudElements.interfaces.NumElement;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.data.NumberFlags;
import java.util.function.Supplier;
import net.minecraft.stats.StatFormatter;

public class NumberSupplierElement implements HudElement, NumElement {

    public record Entry(Supplier<Number> supplier, int precision, StatFormatter formatter) {}
    public static Entry of(Supplier<Number> supplier, int precision) {
        return new Entry(supplier, precision, null);
    }
    public static Entry of(Supplier<Number> supplier, int precision, StatFormatter formatter) {
        return new Entry(supplier, precision, formatter);
    }

    private final Supplier<Number> supplier;
    private final NumberFlags flags;

    public NumberSupplierElement(Entry entry, Flags flags) {
        this.supplier = entry.supplier;
        this.flags = NumberFlags.of(flags, entry.precision, entry.formatter);
    }

    public NumberSupplierElement(Supplier<Number> supplier, Flags flags) {
        this.supplier = supplier;
        this.flags = NumberFlags.of(flags);
    }

    @Override
    public int getPrecision() {
        return flags.precision();
    }

    @Override
    public String getString() {
        try { return flags.formatString( supplier.get().doubleValue() ); }
        catch (Exception _e) { return "-"; }
    }

    @Override
    public Number getNumber() {
        return sanitize(supplier, Double.NaN);
    }

    @Override
    public boolean getBoolean() {
        return sanitize(supplier, Double.NaN).doubleValue() > 0;
    }

}
