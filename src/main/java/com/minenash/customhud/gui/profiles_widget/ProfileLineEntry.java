package com.minenash.customhud.gui.profiles_widget;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.errors.Errors;
import com.minenash.customhud.gui.ErrorsScreen;
import com.minenash.customhud.gui.NewConfigScreen.Mode;
import com.minenash.customhud.gui.TogglesScreen;
import java.util.ArrayList;
import java.util.List;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractWidget;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ProfileLineEntry extends LineEntry {

    public final Button selected, cycled, toggles;
    private final Button keybind, edit, error;
    private final Button delete, up, down;
    public final EditBox editName;
    private final ProfileLinesWidget widget;

    public final Profile profile;
    private String displayName;

    public ProfileLineEntry(Profile profile, ProfileLinesWidget widget) {
        this.profile = profile;
        this.widget = widget;
        this.displayName = profile.name;

        this.selected = button(ProfileManager.getActive() == profile ? "☑" : "☐", "Swap to this profile", 16, (b) -> {
            ProfileManager.setActive(profile);
            b.setTooltip(Tooltip.create(Component.literal(ProfileManager.getActive() == profile ? "Turn off this profile" : "Swap to this profile")));
        });

//        String editText = "Will open in your text editor\n\n Not opening? Shift-click to edit in game";

        this.edit = button("Edit", ProfileManager.openTooltipStr, 40, (b) -> ProfileManager.open(profile));
        this.cycled = button(profile.cycle ? "☑" : "☐", "Include this profile in the profile cycle", 16, (b) -> {
            profile.cycle = !profile.cycle;
            b.setMessage(Component.literal(profile.cycle ? "☑" : "☐"));
        });

        this.keybind = button(profile.keyBinding.getTranslatedKeyMessage().getString(), "Keybind to switch to this profile", 80, (b) -> {
            widget.screen.selectedKeybind = profile.keyBinding;
            widget.update();
        });
        int errors = Errors.getErrors(profile.name).size();
        int toggles = profile.toggles.size();
        this.error = button("§c!", "§c" + (errors == 1 ? "1 Error Found" : errors + " Errors Found") , 16, (b) -> CLIENT.setScreen(new ErrorsScreen(widget.screen, profile)));
        this.toggles = button("Toggles", toggles == 1 ? "1 Toggle in the profile" : toggles + " Toggles in the profile",48, (b) -> CLIENT.setScreen(new TogglesScreen(widget.screen, profile)));
        this.delete = button("§cDelete", "§cThis Can't Be §nUndone!!!!", 48, (b) -> widget.deleteProfile(this));
        this.up = button("§a↑", 16, b -> widget.move(this, -1));
        this.down = button("§c↓", 16, b -> widget.move(this, 1));

        this.editName = new EditBox(CLIENT.font, 0, 0, 200, 16, Component.literal("Edit Name"));
        this.editName.setValue(profile.name);
        this.editName.setTooltip(Tooltip.create(Component.literal("Click to edit name")));
        this.editName.setCanLoseFocus(true);
        this.editName.setResponder((n) -> widget.screen.editing = this);
    }

    private static final List<Character> invalidCharacters = List.of('\\', '/', ':', '*', '?', '"', '<', '>', '|');

    public void update() {
        keybind.setMessage(this.profile.keyBinding.getTranslatedKeyMessage());
        if (widget.screen.selectedKeybind == profile.keyBinding)
//            keyButton.setMessage(keyButton.getMessage().copy().formatted(Formatting.YELLOW, Formatting.UNDERLINE));
            keybind.setMessage(Component.literal("> ")
                    .append(keybind.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                    .append(" <").withStyle(ChatFormatting.YELLOW));

        editName.setFocused(false);
        if (!editName.getValue().equals(profile.name)) {
            ProfileManager.rename(profile, editName.getValue());
        }
    }

    @Override
    public void extractContent(GuiGraphicsExtractor context, int mX, int mY, boolean hovered, float delta) {
        int x = getContentX();
        int y = getContentY();
        int eWidth = getContentWidth();

        editName.setX(x + 16 + 20);
        editName.setY(y);
        editName.setWidth(eWidth - 16 - 20 - 16 - 42 - 82 - 18 - (profile.toggles.isEmpty() ? 0 : 50) - 3);

        if (editName.isHoveredOrFocused() || editName.isMouseOver(mX, mY))
            editName.extractRenderState(context, mX, mY, delta);
        else {
            if (!editName.getValue().equals(profile.name)) {
                ProfileManager.rename(profile, editName.getValue());
                displayName = profile.name;
            }
            context.text(CLIENT.font, truncateName(x, eWidth), x + 16 + 20 + 4, y + 4, 0xFFFFFFFF);
        }

        selected.setMessage(Component.literal(ProfileManager.getActive() == profile ? "☑" : "☐"));
        posAndRender(context, mX, mY, delta, x, y, eWidth, selected, 2);

        if (widget.screen.mode == Mode.DELETE) {
            posAndRender(context, mX, mY, delta, x, y, eWidth, delete, -16-42);
            return;
        }
        if (widget.screen.mode == Mode.REORDER) {
            down.active = widget.children().get(widget.children().size()-2) != this;
            down.setMessage(Component.literal(down.active ? "§c↓" : "§4↓"));
            posAndRender(context, mX, mY, delta, x, y, eWidth, down, -16-18);

            up.active = widget.children().get(0) != this;
            up.setMessage(Component.literal(up.active ? "§a↑" : "§2↑"));
            posAndRender(context, mX, mY, delta, x, y, eWidth, up, -16-18-18);
            return;
        }

        if (Errors.hasErrors(profile.name))
            posAndRender(context, mX, mY, delta, x, y, eWidth, error, -16);
        posAndRender(context, mX, mY, delta, x, y, eWidth, edit, -16-42);
        posAndRender(context, mX, mY, delta, x, y, eWidth, keybind, -16-42-82);
        posAndRender(context, mX, mY, delta, x, y, eWidth, cycled, -16-42-82-18);
        toggles.active = !profile.toggles.isEmpty();
        if (toggles.active)
            posAndRender(context, mX, mY, delta, x, y, eWidth, toggles, -16-42-82-18-50);

    }

    private String truncateName(int x, int eWidth) {
        String name = displayName;
        int width = CLIENT.font.width(name);
        int maxWidth = x + eWidth + switch (widget.screen.mode) {
            case NORMAL ->  -16-42-82-18-(profile.toggles.isEmpty() ? 0 : 50);
            case REORDER -> -16-18-18;
            case DELETE -> -16-42;
        } - 2 - (x + 16 + 20 + 4);
        if (maxWidth > width)
            return name;

        maxWidth -= CLIENT.font.width("…") + 2;

        while(width > maxWidth) {
            name = name.substring(0, name.length() - 1);
            width = CLIENT.font.width(name);
        }
        return name + "…";
    }

    @Override public List<? extends NarratableEntry> narratables() { return widgets(); }
    @Override public List<? extends GuiEventListener> children() { return widgets(); }
    public List<AbstractWidget> widgets() {
        List<AbstractWidget> widgets = new ArrayList<>(6);
        widgets.add(selected);
        widgets.add(editName);
        if (widget.screen.mode == Mode.DELETE)
            widgets.add(delete);
        else if (widget.screen.mode == Mode.REORDER) {
            widgets.add(up);
            widgets.add(down);
        }
        else {
//            if (!profile.toggles.isEmpty())
                widgets.add(toggles);
            widgets.add(cycled);
            widgets.add(keybind);
            widgets.add(edit);
            if (Errors.hasErrors(profile.name))
                widgets.add(error);
        }
        return widgets;
    }

}
