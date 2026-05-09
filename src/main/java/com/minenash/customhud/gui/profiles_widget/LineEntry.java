package com.minenash.customhud.gui.profiles_widget;

import com.minenash.customhud.gui.NewConfigScreen.Mode;
import java.util.List;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.ContainerObjectSelectionList;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.components.events.GuiEventListener;
import net.minecraft.client.gui.narration.NarratableEntry;
import net.minecraft.network.chat.Component;

public abstract class LineEntry extends ContainerObjectSelectionList.Entry<LineEntry> {
    public void update() {}

    protected void posAndRender(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int x, int y, int width, Button widget, int xOffset) {
        widget.setX(x + (xOffset < 0 ? width + xOffset : xOffset + 16));
        widget.setY(y);
        widget.extractRenderState(context, mouseX, mouseY, delta);
    }

    protected static Button button(String text, int width, Button.OnPress action) {
        return Button.builder(Component.literal(text), action).bounds(0, 0, width, 16).build();
    }
    protected static Button button(String text, String tooltip, int width, Button.OnPress action) {
        return Button.builder(Component.literal(text), action).bounds(0, 0, width, 16)
                .tooltip(Tooltip.create(Component.literal(tooltip))).build();
    }


    public static class NewProfile extends LineEntry {

        private final ProfileLinesWidget parent;
        private final Button newProfile;
        private final Button reorderProfiles;
        public final Button deleteProfiles;
        private final Button deleteDone;

        public NewProfile(ProfileLinesWidget parent) {
            this.parent = parent;
            this.newProfile = button("§a+§f New", 48, b -> parent.newProfile());
            this.reorderProfiles = button("§6⇵§f Reorder", 72, b -> parent.screen.mode = Mode.REORDER);
            this.deleteProfiles = button("§c-§f Delete", 64, b -> parent.screen.mode = Mode.DELETE);
            this.deleteDone = button("§a✔§f Done", 56, b -> {
                if (parent.screen.mode == Mode.REORDER)
                    parent.doneMoving();
                parent.screen.mode = Mode.NORMAL;
            });
        }

        @Override
        public void extractContent(GuiGraphicsExtractor context, int mX, int mY, boolean hovered, float delta) {
            int x = getContentX();
            int y = getContentY();
            int width = getContentWidth();
            if (parent.screen.mode != Mode.NORMAL)
                posAndRender(context, mX, mY, delta, x, y, width, deleteDone, 2);
            else {
                posAndRender(context, mX, mY, delta, x, y, width, newProfile, 2);
                posAndRender(context, mX, mY, delta, x, y, width, reorderProfiles, 2 + 50);
                posAndRender(context, mX, mY, delta, x, y, width, deleteProfiles, 2 + 50 + 74);
            }
        }

        @Override
        public List<? extends NarratableEntry> narratables() {
            if (parent.screen.mode != Mode.NORMAL)
                return List.of(deleteDone);
            return List.of(newProfile, reorderProfiles, deleteProfiles);
        }
        @Override
        public List<? extends GuiEventListener> children() {
            if (parent.screen.mode != Mode.NORMAL)
                return List.of(deleteDone);
            return List.of(newProfile, reorderProfiles, deleteProfiles);
        }
    }


}
