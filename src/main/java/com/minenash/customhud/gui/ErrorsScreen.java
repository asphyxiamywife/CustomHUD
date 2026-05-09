package com.minenash.customhud.gui;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.data.Profile;
import com.minenash.customhud.errors.ErrorType;
import com.minenash.customhud.errors.Errors;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.AbstractSelectionList;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.util.Util;
import java.util.Iterator;
import java.util.List;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ErrorsScreen extends Screen {

    private ErrorListWidget listWidget = null;
    private final Screen parent;
    private Profile profile;
    public int y_offset = 0;
    private static final int lineColumnX = 15;
    public int sourceSectionWidth = 120;
    public boolean openedFromNullScreen;

    public ErrorsScreen(Screen parent) {
        this(parent, ProfileManager.getActive());
    }

    public ErrorsScreen(Screen parent, Profile profile) {
        super(Component.literal((profile == null || profile.name == null ? "Unknown" : "'" + profile.name + "'") + " Errors"));
        this.parent = parent;
        this.profile = profile;
        openedFromNullScreen = parent == null;
    }

    public void changeProfile(Profile profile) {
        this.profile = profile;
        init();
    }

    protected void init() {
        children().clear();
        this.listWidget = new ErrorListWidget(profile);
        this.addWidget(listWidget);

//        profiles[0] = this.addDrawableChild( ButtonWidget.builder(Text.literal("Profile 1"), button -> changeProfile(1))
//                .position(this.width / 2 - 40 - 90, 24).size(80, 20).build() );

//        profiles.add(this.addDrawableChild( ButtonWidget.builder(Text.literal("Profile 2"), button -> changeProfile(ProfileManager.getActive().name))
//                .position(this.width / 2 - 40, 24).size(80, 20).build() ));

//        profiles[2] = this.addDrawableChild( ButtonWidget.builder(Text.literal("Profile 3"), button -> changeProfile(3))
//                .position(this.width / 2 - 40 + 90, 24).size(80, 20).build() );

        if (openedFromNullScreen) {
            this.addRenderableWidget( Button.builder(Component.literal("Open Profile"), button -> ProfileManager.open(profile))
                    .pos(this.width / 2 - 155, this.height - 26).size(100, 20)
                    .tooltip(ProfileManager.openTooltip).build() );

            this.addRenderableWidget( Button.builder(Component.literal("Profiles"), button -> CLIENT.setScreen( new NewConfigScreen(null) ))
                    .pos(this.width / 2 - 155 + 100 + 5, this.height - 26).size(100, 20).build() );

            this.addRenderableWidget( Button.builder(CommonComponents.GUI_DONE, button -> CLIENT.setScreen(parent))
                    .pos(this.width / 2 - 155 + 160 + 50, this.height - 26).size(100, 20).build() );
        }
        else {
            this.addRenderableWidget( Button.builder(Component.literal("Open Profile"), button -> ProfileManager.open(profile))
                    .pos(this.width / 2 - 155, this.height - 26).size(150, 20)
                    .tooltip(ProfileManager.openTooltip).build() );


            this.addRenderableWidget( Button.builder(CommonComponents.GUI_DONE, button -> CLIENT.setScreen(parent))
                    .pos(this.width / 2 - 155 + 160, this.height - 26).size(150, 20).build() );
        }


        super.init();
    }

    @Override
    public void onClose() {
        CLIENT.setScreen(parent);
    }

    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        super.extractRenderState(context, mouseX, mouseY, delta);

        y_offset = 0;
        this.listWidget.extractRenderState(context, mouseX, mouseY, delta);
        context.centeredText(this.font, this.title, this.width / 2, 11, 0xFFFFFFFF);

        for (var d : renderables)
            d.extractRenderState(context, mouseX, mouseY, delta);
    }

    class ErrorListWidget extends AbstractSelectionList<ErrorListWidget.ErrorEntry> {

        public ErrorListWidget(Profile profile) {
            super(CLIENT, ErrorsScreen.this.width, ErrorsScreen.this.height - 30 - 32, 30, /*ErrorsScreen.this.height - 36 + 4,*/ 18);
            this.addEntry( new ErrorEntryHeader() );

            if (profile == null) {
                this.addEntry( new ErrorEntry( new Errors.Error("0", "Error: Null Profile", ErrorType.NONE, "") ) );
                return;
            }
            if (!Errors.hasErrors(profile.name))
                this.addEntry( new ErrorEntry( new Errors.Error("0", "No Errors", ErrorType.NONE, "") ) );

            for (var e : Errors.getErrors(profile.name))
                this.addEntry(new ErrorEntry(e));

//            if (this.getSelectedOrNull() != null)
//                this.centerScrollOn(ent);

        }

        @Override
        protected int scrollBarX() {
            return width - 8;
        }

        @Override
        protected ErrorEntry getEntryAtPosition(double x, double y) {
            // - this.headerHeight
            int m = Mth.floor(y - (double)this.getY()) + (int)this.scrollAmount() - 4;
            int n = m / this.defaultEntryHeight;

            ErrorEntry entry = getSelected();
            if (entry != null ) {
                int index = children().indexOf( entry );
                if (n >= index && n <= index + entry.expandedMsg.size())
                    n = index;
                else if (n == index + entry.expandedMsg.size() + 1)
                    n = 0;
                else if (n > index)
                    n -= entry.expandedMsg.size() + 1;
            }
            return x < this.scrollBarX() && n >= 0 && m >= 0 && n < this.getItemCount() ? this.children().get(n) : null;
        }

        @Override
        public boolean isFocused() {
            return ErrorsScreen.this.getFocused() == this;
        }

        @Override
        protected void updateWidgetNarration(NarrationElementOutput builder) {}

        public class ErrorEntryHeader extends ErrorEntry {

            public ErrorEntryHeader() {
                super(new Errors.Error("§nLine", "Source", ErrorType.HEADER, ""));
            }

            @Override
            public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int y = getY();
                context.centeredText(font, error.line().formatted(ChatFormatting.UNDERLINE), lineColumnX, y + y_offset, 0xFFFFFFFF);
                context.text(font, Component.literal(collapsedSource).withStyle(ChatFormatting.UNDERLINE), 36, y + y_offset, 0xFFFFFFFF);
                context.text(font, Component.literal(collapsedMsg).withStyle(ChatFormatting.UNDERLINE), msgX, y + y_offset, 0xFFFFFFFF);
                context.text(font, error.type().linkText.withStyle(ChatFormatting.WHITE), refX, y + y_offset, 0xFFFFFFFF);
            }
        }

        @Environment(EnvType.CLIENT)
        public class ErrorEntry extends AbstractSelectionList.Entry<ErrorEntry> {
            final Errors.Error error;
            final String collapsedSource;
            final String collapsedMsg;
            final List<FormattedCharSequence> expandedMsg;
            final String expandedSource;
            final int msgX;
            final int refX, refLength;
            boolean expands = false;

            public ErrorEntry(Errors.Error error) {
                this.error = error;
                msgX = 36 + sourceSectionWidth + 15;

                refLength = error.type().linkText == null ? 0 : font.width(error.type().linkText);
                refX = error.type().linkText == null ? 0 : width - 28 - refLength;

                expandedMsg = font.split(FormattedText.of( error.type().message + error.context() ), width - 36 - 16);
                collapsedMsg = ensureLength(error.type().message + error.context(), width - msgX - 16 - refLength, "…");
                collapsedSource = ensureLength(error.source().replace('§', '&'), sourceSectionWidth,
                        error.source().startsWith("{{") ? "…}}" : error.source().startsWith("{") ? "…}" : "…");
                expandedSource = ensureLength(error.source().replace('§', '&'), width - 36 - 16 - refLength,
                        error.source().startsWith("{{") ? "…}}" : error.source().startsWith("{") ? "…}" : "…");
            }

            private String ensureLength(String str, int width, String suffix) {
                if (font.width(str) <= width)
                    return str;
                else {
                    str = str.substring(0, str.length() - suffix.length() + 1);
                    expands = true;
                    int endWidth = font.width("suffix");
                    while (font.width(str) > width - endWidth)
                        str = str.substring(0, str.length() - 1);
                    return str + suffix;
                }
            }

            public void extractContent(GuiGraphicsExtractor context, int mouseX, int mouseY, boolean hovered, float tickDelta) {
                int y = getY();
                if (hovered) {
                    int extendedHeight = ErrorListWidget.this.getSelected() == this ? (18 * expandedMsg.size()) : 0;
                    context.fill(0, y + y_offset, width, y + y_offset + 18 + extendedHeight, 0x22FFFFFF);
                    if (mouseX >= refX && mouseX <= refX + refLength)
                        context.setTooltipForNextFrame(font, Component.literal("§eClick to open the " + error.type().linkText.getString() + " page"), mouseX, mouseY);
                }

                y += 6;
                int ceX = maxScrollAmount()  > 0 ? width-16 : width-12;

                context.centeredText(font, error.line(), lineColumnX, y + y_offset, 0xFFFFFFFF);
                if (refX > 0)
                    context.text(font, error.type().linkText, refX, y + y_offset, 0xFFFFFFFF);
                if (ErrorListWidget.this.getSelected() != this) {
                    if (expands)
                        context.text(font, "▶", ceX, y + y_offset, 0xFFFFFFFF);
                    context.text(font, collapsedSource, 36, y + y_offset, 0xFFFFFFFF);
                    context.text(font, collapsedMsg, msgX, y + y_offset, 0xFFFFFFFF);
                }
                else {
                    if (expands)
                        context.text(font, "▼", ceX, y + y_offset, 0xFFFFFFFF);
                    context.text(font, expandedSource, 36, y + y_offset, 0xFFFFFFFF);
                    for (FormattedCharSequence msgLine : expandedMsg) {
                        y_offset += 18;
                        context.centeredText(font, "→", lineColumnX, y + y_offset, 0xFFFFFFFF);
                        context.text(font, msgLine, 36, y + y_offset, 0xFFFFFFFF);
                    }
                    y_offset += 18;

                }

            }

            @Override
            public boolean mouseClicked(MouseButtonEvent click, boolean doubled) {
                if (click.x() >= refX && click.x() <= refX + refLength)
                    Util.getPlatform().openUri(error.type().link);
                else if (expands && ErrorListWidget.this.getSelected() != this)
                    ErrorListWidget.this.setSelected(this);
                else
                    ErrorListWidget.this.setSelected(null);
                return super.mouseClicked(click, doubled);
            }

        }
    }
}