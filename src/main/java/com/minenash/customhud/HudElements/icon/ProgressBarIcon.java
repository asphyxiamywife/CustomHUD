package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.conditionals.Operation;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.BossHealthOverlay;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.BossEvent;
import net.minecraft.world.BossEvent.BossBarColor;
import net.minecraft.world.BossEvent.BossBarOverlay;
import org.joml.Matrix3x2fStack;

import static com.minenash.customhud.CustomHud.CLIENT;

public class ProgressBarIcon extends IconElement {

    private final boolean background;
    private final Operation numerator;
    private final Operation denominator;
    private final BarStyle style;

    public ProgressBarIcon(boolean background, Operation numerator, Operation denominator, BarStyle style, Flags flags) {
        super(flags, style instanceof VillagerTextureStyle ? 102 : 182);
        this.background = background;
        this.numerator = numerator;
        this.denominator = denominator;
        this.style = style == null ? DEFAULT : style;

    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        matrices.translate(piece.x + shiftX, piece.y + shiftY + 1);
        if (!referenceCorner)
            matrices.translate(0, -(5*scale-5)/2);
        matrices.scale(scale, scale);
        rotate(matrices, 182, 5);

        style.extractRenderState(context, (float) Mth.clamp(numerator.getValue() / denominator.getValue(), 0, 1), background);

        matrices.popMatrix();
    }

    public static BarStyle getStyle(String settings) {
        switch (settings) {
            case "experience", "xp": return XP;
            case "jump", "horse": return JUMP;
            case "villager_green", "villager": return VILLAGER_GREEN;
            case "villager_white": return VILLAGER_WHITE;
        }

        BossBarColor color = null;
        BossBarOverlay style = null;

        switch (settings) {
            case "pink" -> color = BossBarColor.PINK;
            case "blue" -> color = BossBarColor.BLUE;
            case "red" -> color = BossBarColor.RED;
            case "green" -> color = BossBarColor.GREEN;
            case "yellow" -> color = BossBarColor.YELLOW;
            case "purple" -> color = BossBarColor.PURPLE;
            case "white" -> color = BossBarColor.WHITE;
            case "6" -> style = BossBarOverlay.NOTCHED_6;
            case "10" -> style = BossBarOverlay.NOTCHED_10;
            case "12" -> style = BossBarOverlay.NOTCHED_12;
            case "20" -> style = BossBarOverlay.NOTCHED_20;

            case "pink6" -> {color = BossBarColor.PINK; style = BossBarOverlay.NOTCHED_6;}
            case "blue6" -> {color = BossBarColor.BLUE; style = BossBarOverlay.NOTCHED_6;}
            case "red6" -> {color = BossBarColor.RED; style = BossBarOverlay.NOTCHED_6;}
            case "green6" -> {color = BossBarColor.GREEN; style = BossBarOverlay.NOTCHED_6;}
            case "yellow6" -> {color = BossBarColor.YELLOW; style = BossBarOverlay.NOTCHED_6;}
            case "purple6" -> {color = BossBarColor.PURPLE; style = BossBarOverlay.NOTCHED_6;}
            case "white6" -> {color = BossBarColor.WHITE; style = BossBarOverlay.NOTCHED_6;}

            case "pink10" -> {color = BossBarColor.PINK; style = BossBarOverlay.NOTCHED_10;}
            case "blue10" -> {color = BossBarColor.BLUE; style = BossBarOverlay.NOTCHED_10;}
            case "red10" -> {color = BossBarColor.RED; style = BossBarOverlay.NOTCHED_10;}
            case "green10" -> {color = BossBarColor.GREEN; style = BossBarOverlay.NOTCHED_10;}
            case "yellow10" -> {color = BossBarColor.YELLOW; style = BossBarOverlay.NOTCHED_10;}
            case "purple10" -> {color = BossBarColor.PURPLE; style = BossBarOverlay.NOTCHED_10;}
            case "white10" -> {color = BossBarColor.WHITE; style = BossBarOverlay.NOTCHED_10;}

            case "pink12" -> {color = BossBarColor.PINK; style = BossBarOverlay.NOTCHED_12;}
            case "blue12" -> {color = BossBarColor.BLUE; style = BossBarOverlay.NOTCHED_12;}
            case "red12" -> {color = BossBarColor.RED; style = BossBarOverlay.NOTCHED_12;}
            case "green12" -> {color = BossBarColor.GREEN; style = BossBarOverlay.NOTCHED_12;}
            case "yellow12" -> {color = BossBarColor.YELLOW; style = BossBarOverlay.NOTCHED_12;}
            case "purple12" -> {color = BossBarColor.PURPLE; style = BossBarOverlay.NOTCHED_12;}
            case "white12" -> {color = BossBarColor.WHITE; style = BossBarOverlay.NOTCHED_12;}

            case "pink20" -> {color = BossBarColor.PINK; style = BossBarOverlay.NOTCHED_20;}
            case "blue20" -> {color = BossBarColor.BLUE; style = BossBarOverlay.NOTCHED_20;}
            case "red20" -> {color = BossBarColor.RED; style = BossBarOverlay.NOTCHED_20;}
            case "green20" -> {color = BossBarColor.GREEN; style = BossBarOverlay.NOTCHED_20;}
            case "yellow20" -> {color = BossBarColor.YELLOW; style = BossBarOverlay.NOTCHED_20;}
            case "purple20" -> {color = BossBarColor.PURPLE; style = BossBarOverlay.NOTCHED_20;}
            case "white20" -> {color = BossBarColor.WHITE; style = BossBarOverlay.NOTCHED_20;}
        }
        if (color == null && style == null)
            return null;
        return new BossBarStyle(color == null ? BossBarColor.WHITE : color, style == null ? BossBarOverlay.PROGRESS : style);


    }

    public static BarStyle DEFAULT = new BossBarStyle(BossBarColor.WHITE, BossBarOverlay.PROGRESS);
    public static BarStyle XP = new TextureStyle(Identifier.parse("hud/experience_bar_progress"), Identifier.parse("hud/experience_bar_background"));
    public static BarStyle JUMP = new TextureStyle(Identifier.parse("hud/jump_bar_progress"), Identifier.parse("hud/jump_bar_background"));
    public static BarStyle VILLAGER_GREEN = new VillagerTextureStyle(Identifier.parse("container/villager/experience_bar_current"));
    public static BarStyle VILLAGER_WHITE = new VillagerTextureStyle(Identifier.parse("container/villager/experience_bar_result"));
    public interface BarStyle {
        void extractRenderState(GuiGraphicsExtractor context, float progress, boolean background);
    }


    public static class BossBarStyle implements BarStyle {
        private final BossEvent bossBar;
        public BossBarStyle(BossBarColor color, BossBarOverlay style) {this.bossBar = new BossbarIcon.BasicBar(color, style);}
        public void extractRenderState(GuiGraphicsExtractor context, float progress, boolean background) {
            bossBar.setProgress(progress);
            if (background)
                BossbarIcon.extractBar(context, bossBar, 182, BossHealthOverlay.BAR_BACKGROUND_SPRITES, BossHealthOverlay.OVERLAY_BACKGROUND_SPRITES);
            int i = Mth.lerpDiscrete(bossBar.getProgress(), 0, 182);
            if (i > 0)
                BossbarIcon.extractBar(context, bossBar, i, BossHealthOverlay.BAR_PROGRESS_SPRITES, BossHealthOverlay.OVERLAY_PROGRESS_SPRITES);
        }
    }

    public static class TextureStyle implements BarStyle {
        private final Identifier fg;
        private final Identifier bg;
        public TextureStyle(Identifier fg, Identifier bg) {this.fg = fg; this.bg = bg;}
        public void extractRenderState(GuiGraphicsExtractor context, float progress, boolean background) {
            if (background)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, bg, 0, 0, 182, 5);
            if (progress > 0)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, fg, 182, 5, 0, 0, 0, 0, (int)(progress*182), 5);

        }
    }

    private static final Identifier EXPERIENCE_BAR_BACKGROUND_TEXTURE = Identifier.parse("container/villager/experience_bar_background");
    public static class VillagerTextureStyle implements BarStyle {
        private final Identifier fg;
        public VillagerTextureStyle(Identifier fg) {this.fg = fg;}
        public void extractRenderState(GuiGraphicsExtractor context, float progress, boolean background) {
            if (background)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, EXPERIENCE_BAR_BACKGROUND_TEXTURE, 0, 0, 102, 5);
            if (progress > 0)
                context.blitSprite(RenderPipelines.GUI_TEXTURED, fg, 102, 5, 0, 0, 0, 0, (int)(progress*102), 5);
        }

    }

}
