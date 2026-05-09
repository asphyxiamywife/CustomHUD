package com.minenash.customhud.HudElements.interfaces;

import com.minenash.customhud.data.Flags;
import net.minecraft.resources.Identifier;

public interface IdElement {

    Identifier getIdentifier();

    static String getString(Identifier id, Flags.IdPart part) {
        if (id == null) return "-";
        return switch (part) {
            case FULL -> id.toString();
            case NAMESPACE -> id.getNamespace();
            case PATH -> id.getPath();
        };
    }

}
