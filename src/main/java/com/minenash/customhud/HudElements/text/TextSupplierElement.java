package com.minenash.customhud.HudElements.text;

import com.minenash.customhud.complex.MusicAndRecordTracker;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.mixin.accessors.HudAccessor;
import java.util.function.Supplier;
import net.minecraft.network.chat.Component;

import static com.minenash.customhud.CustomHud.CLIENT;

public class TextSupplierElement extends TextElement {

    public static final Supplier<Component> DISPLAY_NAME = () -> CLIENT.player.getDisplayName();
    public static final Supplier<Component> ACTIONBAR_MSG = () -> ((HudAccessor) CLIENT.gui.hud).getOverlayMessageTime() == 0 ? null : ((HudAccessor) CLIENT.gui.hud).getOverlayMessageString();
    public static final Supplier<Component> TITLE_MSG = () -> ((HudAccessor) CLIENT.gui.hud).getTitle();
    public static final Supplier<Component> SUBTITLE_MSG = () -> ((HudAccessor) CLIENT.gui.hud).getTitleTime() == 0 ? null : ((HudAccessor) CLIENT.gui.hud).getSubtitle();
    public static final Supplier<Component> RECORD_NAME = () -> MusicAndRecordTracker.isRecordPlaying ? MusicAndRecordTracker.getClosestRecord().name : null;
    public static final Supplier<Component> PLAYER_TEAM_NAME = () -> CLIENT.player.getTeam() == null ? null : CLIENT.player.getTeam().getDisplayName();


    private final Supplier<Component> supplier;

    public TextSupplierElement(Supplier<Component> supplier, Flags flags) {
        this.supplier = supplier;
    }

    public int getTextWidth() {
        return CLIENT.font.width(getText());
    }

    public Component getText() {
        return sanitize(supplier, Component.literal("-"));
    }

    @Override
    public String getString() {
        return getText().getString();
    }

    @Override
    public Number getNumber() {
        try {
            Component text = supplier.get();
            return text == null ? Double.NaN : text.getString().length();
        }
        catch (Exception e) {
            return Double.NaN;
        }
    }

    @Override
    public boolean getBoolean() {
        return getNumber().intValue() > 0;
    }
}
