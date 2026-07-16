package com.minenash.customhud.gui;

import com.minenash.customhud.ConfigManager;
import com.minenash.customhud.CustomHud;
import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.gui.profiles_widget.LineEntry;
import com.minenash.customhud.gui.profiles_widget.ProfileLineEntry;
import com.minenash.customhud.gui.profiles_widget.ProfileLinesWidget;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.toasts.SystemToast;
import net.minecraft.client.gui.navigation.FocusNavigationEvent;
import net.minecraft.client.gui.navigation.ScreenDirection;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FontDescription;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

import static com.minenash.customhud.CustomHud.CLIENT;
import static com.minenash.customhud.CustomHud.ignoreFirstToast;
import static net.minecraft.client.gui.navigation.ScreenDirection.*;

public class NewConfigScreen extends Screen {

    private final Screen parent;
    private final Font font;

    private ProfileLinesWidget profiles;
    public KeyMapping selectedKeybind;
    public ProfileLineEntry editing;

    public enum Mode {NORMAL, REORDER, DELETE}
    public Mode mode = Mode.NORMAL;

    public NewConfigScreen(Screen parent) {
        super(Component.translatable("sml.config.screen.title"));
        this.parent = parent;
        this.font = Minecraft.getInstance().font;
    }

    @Override
    public void init() {
        clearWidgets();
        profiles = new ProfileLinesWidget(this,30, height-32);
        addRenderableWidget(profiles);

        this.addRenderableWidget( Button.builder(Component.literal("Open Folder"),
                button -> new Thread(() -> Util.getPlatform().openFile(CustomHud.PROFILE_FOLDER.toFile())).start())
                .pos(this.width / 2 - 155, this.height - 26).size(150, 20).build() );

        this.addRenderableWidget( Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .pos(this.width / 2 - 155 + 160, this.height - 26).size(150, 20).build() );

        this.addRenderableWidget( Button.builder(Component.literal("Debug Log: " + (CustomHud.DEBUG_MODE ? "On" : "Off")),
               (Button button) -> {
                    CustomHud.DEBUG_MODE = !CustomHud.DEBUG_MODE;
                    button.setMessage( Component.literal("Debug Log: " + (CustomHud.DEBUG_MODE ? "On" : "Off")) );
                })
                .pos(6, 6).size(86, 16).build() );

        this.addRenderableWidget( Button.builder(linkText("D", " Support"),
                button -> Util.getPlatform().openUri("https://jakobt.dev/discord"))
                .pos(width - 68 - 4, 6).size(68, 16).build() );

        this.addRenderableWidget( Button.builder( Component.literal("Wiki / Docs"),
                button -> Util.getPlatform().openUri("https://customhud.dev/v3/getting_started"))
                .pos(width - 68 - 4 - 68 - 4, 6).size(68, 16).build() );


        this.addRenderableWidget( Button.builder(CommonComponents.GUI_DONE, button -> onClose())
                .pos(this.width / 2 - 155 + 160, this.height - 26).size(150, 20).build() );
    }

    private static final Style ICONS = Style.EMPTY.withFont(new FontDescription.Resource(Identifier.fromNamespaceAndPath("custom_hud", "icons")));
    private static final Style DEFAULT = Style.EMPTY.withFont(FontDescription.DEFAULT);
    private Component linkText(String icon, String msg) {
        return Component.literal(icon).setStyle(ICONS).append(Component.literal(msg).setStyle(DEFAULT));
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(font, Component.translatable("config.custom_hud.title"), this.width / 2, 11, 0xFFFFFFFF);
        context.centeredText(font, "§oDrag and drop profile files here to add it", this.width / 2, this.height-46, 0xFF888888);

    }

    @Override
    public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
        if (selectedKeybind != null) {
            selectedKeybind.setKey(InputConstants.Type.MOUSE.getOrCreate(click.button()));
            selectedKeybind = null;
            profiles.update();
            return true;
        }

        if (super.mouseClicked(click, doubled))
            return true;

        for (var c : profiles.children())
            if (c instanceof ProfileLineEntry e)
                e.editName.setFocused(false);

        return false;
    }

    @Override
    public boolean keyPressed(KeyEvent input) {
        if (selectedKeybind != null) {
            selectedKeybind.setKey(input.key() == InputConstants.KEY_ESCAPE ? InputConstants.UNKNOWN : InputConstants.getKey(input));
            selectedKeybind = null;
            profiles.update();
            return true;
        }
        if (input.key() == CustomHud.kb_showErrors.key.getValue() && ProfileManager.getActive() != null) {
            minecraft.gui.setScreen( new ErrorsScreen(this) );
            return true;
        }

        switch (input.key()) {
            case InputConstants.KEY_ESCAPE, InputConstants.KEY_RETURN, InputConstants.KEY_NUMPADENTER -> {
                boolean wasFocused = false;
                for (var c : profiles.children()) {
                    if (c instanceof ProfileLineEntry e) {
                        if (e.editName.isFocused())
                            wasFocused = true;
                        e.editName.setFocused(false);
                    }
                }
                if (input.key() != InputConstants.KEY_ESCAPE || wasFocused)
                    return true;
            }
            case InputConstants.KEY_LEFT -> {
                for (var c : profiles.children())
                    if (c instanceof ProfileLineEntry e)
                        if (e.editName.isFocused())
                            if (e.editName.getCursorPosition() == 0)
                                return move(LEFT);
                            else break;
                        else if (e.cycled.isFocused()) {
                            e.editName.moveCursorToEnd(false);
                            return move(LEFT);
                        }
            }
            case InputConstants.KEY_RIGHT -> {
                for (var c : profiles.children())
                    if (c instanceof ProfileLineEntry e)
                        if (e.editName.isFocused())
                            if (e.editName.getCursorPosition() == e.editName.getValue().length())
                                return move(RIGHT);
                            else break;
                        else if (e.selected.isFocused()) {
                            e.editName.moveCursorToStart(false);
                            return move(RIGHT);
                        }
            }
            case InputConstants.KEY_UP -> {
                int max = profiles.children().size()-1;
                for (int i = 0; i < max; i++)
                    if (profiles.children().get(i) instanceof ProfileLineEntry e && e.toggles.isFocused())
                        if (i > 0)
                            return move(LEFT, UP);
                        else break;
                if (((LineEntry.NewProfile)profiles.children().get(max)).deleteProfiles.isFocused())
                    return move(LEFT, UP);
            }
            case InputConstants.KEY_DOWN -> {
                for (int i = 0; i < profiles.children().size()-1; i++)
                    if (profiles.children().get(i) instanceof ProfileLineEntry e && e.toggles.isFocused())
                        return move(LEFT, DOWN);
            }
        }
        return super.keyPressed(input);
    }

    private boolean move(ScreenDirection... ds) {
        for (var d : ds) this.changeFocus( super.nextFocusPath(new FocusNavigationEvent.ArrowNavigation(d)) );
        return true;
    }

    @Override
    public void onFilesDrop(List<Path> paths) {
        CustomHud.logInDebugMode("Path's: " + paths);

        for (Path path : paths) {
            if (!path.getFileName().toString().endsWith(".txt"))
                continue;
            try {
                ignoreFirstToast = true;
                Files.copy(path, CustomHud.PROFILE_FOLDER.resolve(path.getFileName()));
            } catch (IOException e) {
                CustomHud.LOGGER.warn("[CustomHud] Failed to copy profile from {} to {}", path, CustomHud.PROFILE_FOLDER.resolve(path.getFileName()));
                SystemToast.onPackCopyFailure(minecraft, path.toString());
            }
        }
    }

    @Override
    public void onClose() {
        CLIENT.gui.setScreen(parent);
        profiles.update();
        ConfigManager.save();
    }
}
