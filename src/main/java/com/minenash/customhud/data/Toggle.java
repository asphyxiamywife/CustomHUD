package com.minenash.customhud.data;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.resources.language.I18n;
import net.minecraft.locale.Language;

public class Toggle {

    public final String name;
    public final boolean direct;
    public final List<Integer> lines;
    public KeyMapping modifier;
    public KeyMapping key;

    public boolean inProfile;
    public boolean value;
    public long lastPressed = 0;

    public Toggle(String name, boolean direct, int line, boolean inProfile, KeyMapping modifier, KeyMapping key) {
        this.name = name;
        this.direct = direct;
        this.lines = new ArrayList<>();
        this.lines.add(line);
        this.modifier = modifier;
        this.key = key;
        this.inProfile = inProfile;
    }

    public boolean getValue() {
        return value;
    }

    public void toggle() {
        value = !value;
        lastPressed = System.currentTimeMillis();
    }

    public String getDisplayName() {
        if (!direct)
            return name;
        if (Language.getInstance().has(name))
            return "Key: " + I18n.get(name);
        if (name.startsWith("key.mouse."))
            return "Key: " + name.substring(10);
        //name.startsWith("key.keyboard.")
        return "Key: " + name.substring(13).toUpperCase();
    }

}
