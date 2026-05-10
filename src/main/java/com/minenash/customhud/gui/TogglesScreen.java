package com.minenash.customhud.gui;

import com.minenash.customhud.ConfigManager;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.data.Toggle;
import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import org.apache.commons.lang3.StringUtils;

import java.util.Collections;
import java.util.List;

import static com.minenash.customhud.CustomHud.CLIENT;

public class TogglesScreen extends Screen {

    private ToggleListWidget listWidget = null;
    private final Screen parent;
    private Profile profile;
    public KeyMapping selectedKeybind;

    public TogglesScreen(Screen parent, Profile profile) {
        super(Component.literal("'" + profile.name + "' Profile Toggles"));
        this.parent = parent;
        this.profile = profile;
    }


    public void changeProfile(Profile profile) {
        this.profile = profile;
        init();
    }

    public void init() {
        children().clear();
        this.listWidget = new ToggleListWidget(profile);
        this.addWidget(listWidget);

        this.addRenderableWidget( Button.builder(Component.literal("Open Profile"), button -> ProfileManager.open(profile))
                .pos(this.width / 2 - 155, this.height - 26).size(150, 20)
                .tooltip(ProfileManager.openTooltip).build() );

        this.addRenderableWidget( Button.builder(CommonComponents.GUI_DONE, button -> CLIENT.gui.setScreen(parent))
                .pos(this.width / 2 - 155 + 160, this.height - 26).size(150, 20).build() );

        super.init();
    }

    @Override
    public void onClose() {
        CLIENT.gui.setScreen(parent);
        ConfigManager.save();
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        this.listWidget.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 11, 0xFFFFFFFF);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (selectedKeybind != null) {
            selectedKeybind.setKey(InputConstants.Type.MOUSE.getOrCreate(click.button()));
            selectedKeybind = null;
            for (ToggleListWidget.TEntry e : listWidget.children())
                e.update();
            ConfigManager.save();
            return true;
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (selectedKeybind != null) {
            selectedKeybind.setKey(input.key() == 256 ? InputConstants.UNKNOWN : InputConstants.getKey(input));
            selectedKeybind = null;
            for (ToggleListWidget.TEntry e : listWidget.children())
                e.update();
            ConfigManager.save();
            return true;
        }
        return super.keyPressed(input);
    }

    class ToggleListWidget extends ContainerObjectSelectionList<ToggleListWidget.TEntry> {

        public ToggleListWidget(Profile profile) {
            super(CLIENT, TogglesScreen.this.width, TogglesScreen.this.height - 36 + 4 - 30, 30, /*TogglesScreen.this.height - 36 + 4,*/ 18);

            boolean noEntries = profile == null || profile.toggles.values().isEmpty();

            this.addEntry( new ToggleEntryHeader(noEntries) );

            if (noEntries)
                return;

            int inProfiles = 0;
            for (var e : profile.toggles.entrySet())
                if (e.getValue().inProfile) {
                    this.addEntry(new ToggleEntry(e.getValue(), e.getKey()));
                    inProfiles++;
                }

            if (profile.toggles.size() > inProfiles) {
                this.addEntry(new BlankSeparator());
                this.addEntry(new ToggleEntrySeparator());
                this.addEntry(new BlankSeparator());
            }

            for (var e : profile.toggles.entrySet())
                if (!e.getValue().inProfile)
                    this.addEntry(new ToggleEntry(e.getValue(), e.getKey()));
        }

        @Override
        public int getRowWidth() {
            return super.getRowWidth() + 64;
        }

        @Override
        protected int scrollBarX() {
            return super.scrollBarX() + 32;
        }

        public abstract class TEntry extends ContainerObjectSelectionList.Entry<TEntry> {
            public void update() {}
            @Override public List<? extends NarratableEntry> narratables() { return Collections.emptyList(); }
            @Override public List<? extends GuiEventListener> children() { return Collections.emptyList(); }
        }

        public class ToggleEntryHeader extends TEntry {
            private static final Component LINE = Component.literal("Line").withStyle(ChatFormatting.UNDERLINE);
            private static final Component NAME = Component.literal("Name").withStyle(ChatFormatting.UNDERLINE);
            private static final Component MODIFIER = Component.literal("Modifier").withStyle(ChatFormatting.UNDERLINE);
            private static final Component KEYBIND = Component.literal("Key").withStyle(ChatFormatting.UNDERLINE);
            private static final Component NO_TOGGLES = Component.literal("This profiles has no toggles").withStyle(ChatFormatting.UNDERLINE);
            private final boolean noEntries;

            public ToggleEntryHeader(boolean noEntries) { this.noEntries = noEntries; }

            @Override
            public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int x = getContentX();
                int y = getContentY();
                int entryWidth = getContentWidth();

                context.centeredText(font, LINE, x+0, y+2, 0xFFFFFFFF);
                context.text(font, NAME, x+0+24, y+2, 0xFFFFFFFF);
                context.centeredText(font, MODIFIER, x+entryWidth-40-80-4+15, y+2, 0xFFFFFFFF);
                context.centeredText(font, KEYBIND, x+entryWidth-40+15, y+2, 0xFFFFFFFF);
                if (noEntries)
                    context.centeredText(font, NO_TOGGLES, x + (entryWidth/2), y+2+12, 0xFFFFFFFF);
            }
        }

        public class ToggleEntrySeparator extends TEntry {
            @Override
            public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                context.centeredText(font, "§nPrior Bound Toggles from this Profile", getContentX() + getContentWidth()/2, getContentY()+4, 0xFFFFFFFF);
            }
        }
        public class BlankSeparator extends TEntry {
            @Override
            public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {}
        }

        @Environment(EnvType.CLIENT)
        public class ToggleEntry extends TEntry {
            final Toggle toggle;
            final Button modifier;
            final Button key;
            final Button remove;
            final String keyName;

            public ToggleEntry(Toggle toggle, String keyName) {
                this.toggle = toggle;
                this.keyName = keyName;
                this.modifier = Button.builder(toggle.modifier.saveString().equals("key.keyboard.unknown") ?
                        Component.literal("None") : toggle.modifier.getTranslatedKeyMessage(), b -> {
                    selectedKeybind = toggle.modifier;
                    update();
                }).size(80, 16).build();
                this.key = Button.builder(toggle.key.getTranslatedKeyMessage(), b -> {
                    selectedKeybind = toggle.key;
                    update();
                }).size(80, 16).build();
                this.remove = Button.builder(Component.literal("§c-"), b -> {
                    profile.toggles.remove(keyName);
                    init();
                    ConfigManager.save();
                }).size(16, 16).build();
                this.remove.setTooltip(Tooltip.create(Component.literal("§cRemove")));
                this.key.active = !toggle.direct;
                this.modifier.active = !toggle.direct;
            }

            public void extractContent(GuiGraphicsExtractor context, int mX, int mY, boolean hovered, float delta) {
                int x = getContentX();
                int y = getContentY();
                int eWidth = getContentWidth();
                context.text(font, toggle.getDisplayName(), x+0+24, y+4, 0xFFFFFFFF);

                if (!toggle.inProfile) {
                    remove.setY(y);
                    remove.setX(x + 2 - 10);
                    remove.extractRenderState(context, mX, mY, delta);
                }
                else
                    context.centeredText(font, getLines(), x+0, y+4, 0xFFFFFFFF);

                if (toggle.lines.size() > 2 && hovered && mX > x && mX < x+30)
                    setTooltip(Tooltip.create(Component.literal(StringUtils.join(toggle.lines, ", "))));

                modifier.setY(y);
                modifier.setX(x+eWidth-80-80-4+15);
                modifier.extractRenderState(context, mX, mY, delta);

                key.setY(y);
                key.setX(x+eWidth-80+15);
                key.extractRenderState(context, mX, mY, delta);
            }

            private Component getLines() {
                if (toggle.lines.size() == 1)
                    return Component.literal(String.valueOf(toggle.lines.get(0)));
                else if (toggle.lines.size() == 2)
                    return Component.literal(toggle.lines.get(0) + "," + toggle.lines.get(1));
                return Component.literal(toggle.lines.get(0) + "…");
            }

            @Override
            public void update() {
                modifier.setMessage(toggle.modifier.saveString().equals("key.keyboard.unknown") ? Component.literal("None") : toggle.modifier.getTranslatedKeyMessage());
                if (selectedKeybind == toggle.modifier)
                    modifier.setMessage(Component.literal("> ")
                            .append(modifier.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                            .append(" <").withStyle(ChatFormatting.YELLOW));
                key.setMessage(toggle.key.getTranslatedKeyMessage());
                if (selectedKeybind == toggle.key)
                    key.setMessage(Component.literal("> ")
                            .append(key.getMessage().copy().withStyle(ChatFormatting.WHITE, ChatFormatting.UNDERLINE))
                            .append(" <").withStyle(ChatFormatting.YELLOW));
            }

            @Override public List<? extends NarratableEntry> narratables() { return toggle.inProfile ? List.of(modifier, key) : List.of(remove, modifier, key); }
            @Override public List<? extends GuiEventListener> children() { return toggle.inProfile ? List.of(modifier, key) : List.of(remove, modifier, key); }
        }
    }
}