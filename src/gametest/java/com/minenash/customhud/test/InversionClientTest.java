package com.minenash.customhud.test;

import com.minenash.customhud.ProfileManager;
import com.minenash.customhud.HudElements.icon.SimpleTextureIconElement;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.fabric.api.client.gametest.v1.FabricClientGameTest;
import net.fabricmc.fabric.api.client.gametest.v1.context.ClientGameTestContext;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.TitleScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

import java.nio.file.Files;

import static com.minenash.customhud.test.ProfileFixtures.check;

@SuppressWarnings("UnstableApiUsage")
public final class InversionClientTest implements FabricClientGameTest {
    private static final int[] BACKGROUNDS = {0xff88b0ff, 0xff355a20, 0xff000000, 0xffffffff};
    private static final int MASK_Y = 150;

    @Override
    public void runTest(ClientGameTestContext context) {
        context.getInput().resizeWindow(1280, 720);
        context.runOnClient(client -> {
            ProfileManager.enabled = false;
            client.options.guiScale().set(2);
            client.resizeGui();
        });
        context.setScreen(ComparisonScreen::new);
        context.waitTicks(3);
        var path = context.takeScreenshot("inversion_backgrounds");
        int guiWidth = context.computeOnClient(client -> client.getWindow().getGuiScaledWidth());
        int guiHeight = context.computeOnClient(client -> client.getWindow().getGuiScaledHeight());
        try (var input = Files.newInputStream(path); var image = NativeImage.read(input)) {
            for (int i = 0; i < BACKGROUNDS.length; i++) {
                int left = i * (guiWidth / 4) + 16;
                int background = BACKGROUNDS[i];
                // The vanilla crosshair's center is opaque white; its top-left corner is transparent.
                int center = sample(image, left + 11, MASK_Y + 11, guiWidth, guiHeight);
                int corner = sample(image, left, MASK_Y, guiWidth, guiHeight);
                assertRgb(center, 0xff000000 | (~background & 0xffffff), "White-mask inversion, column " + i);
                assertRgb(corner, background, "Transparent-mask pixel, column " + i);
            }
        } catch (Exception e) {
            throw new RuntimeException("Cannot inspect inversion screenshot " + path, e);
        } finally {
            context.setScreen(TitleScreen::new);
        }
        context.runOnClient(client -> ProfileFixtures.report(
                "PASS: GPU inversion and transparent-mask pixels against sky, foliage, black and white backgrounds"));
    }

    private static int sample(NativeImage image, int x, int y, int guiWidth, int guiHeight) {
        return image.getPixel(x * image.getWidth() / guiWidth, y * image.getHeight() / guiHeight);
    }

    private static void assertRgb(int actual, int expected, String label) {
        for (int shift : new int[]{0, 8, 16})
            check(Math.abs(((actual >>> shift) & 255) - ((expected >>> shift) & 255)) <= 3,
                    label + ": expected " + Integer.toHexString(expected) + ", got " + Integer.toHexString(actual));
    }

    private static final class ComparisonScreen extends Screen {
        private final SimpleTextureIconElement normal;
        private final SimpleTextureIconElement inverted;
        private final SimpleTextureIconElement mask;

        ComparisonScreen() {
            super(Component.literal("CustomHUD inversion comparison"));
            Flags flags = new Flags();
            flags.scale = 2;
            flags.iconReferenceCorner = true;
            Identifier diamond = Identifier.parse("minecraft:textures/item/diamond.png");
            normal = new SimpleTextureIconElement(diamond, false, flags);
            inverted = new SimpleTextureIconElement(diamond, true, flags);
            mask = new SimpleTextureIconElement(Identifier.parse("minecraft:textures/gui/sprites/hud/crosshair.png"), true, flags);
            check(normal.isIconAvailable() && inverted.isIconAvailable() && mask.isIconAvailable(), "Missing test textures");
        }

        @Override
        public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float partialTick) {
            String[] names = {"Sky blue", "Foliage green", "Black", "White"};
            int columnWidth = graphics.guiWidth() / 4;
            for (int i = 0; i < BACKGROUNDS.length; i++) {
                int left = i * columnWidth;
                graphics.fill(left, 0, i == 3 ? graphics.guiWidth() : left + columnWidth,
                        graphics.guiHeight(), BACKGROUNDS[i]);
                graphics.text(font, names[i], left + 4, 8, 0xffff00ff, false);
                graphics.text(font, "Normal", left + 4, 26, 0xffff00ff, false);
                draw(normal, graphics, left + 16, 40);
                graphics.text(font, "Colored inversion", left + 4, 80, 0xffff00ff, false);
                draw(inverted, graphics, left + 16, 94);
                graphics.text(font, "White mask", left + 4, 136, 0xffff00ff, false);
                draw(mask, graphics, left + 16, MASK_Y);
            }
        }

        private static void draw(SimpleTextureIconElement icon, GuiGraphicsExtractor graphics, int x, int y) {
            icon.extractRenderState(graphics, new RenderPiece(icon, null, null, x, y + 2, 0xffffffff, 0, false, false));
        }
    }
}
