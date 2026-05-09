//package com.minenash.customhud.render;
//
//import com.minenash.customhud.CustomHud;
//import com.minenash.customhud.HudElements.interfaces.HudElement;
//import com.minenash.customhud.HudElements.interfaces.MultiElement;
//import com.minenash.customhud.HudElements.interfaces.ExecuteElement;
//import com.minenash.customhud.HudElements.functional.FunctionalElement;
//import com.minenash.customhud.HudElements.icon.IconElement;
//import com.minenash.customhud.HudElements.text.TextElement;
//import com.minenash.customhud.ProfileManager;
//import com.minenash.customhud.complex.ListManager;
//import com.minenash.customhud.data.CHFormatting;
//import com.minenash.customhud.data.HudTheme;
//import com.minenash.customhud.data.Profile;
//import com.minenash.customhud.data.Section;
//import com.mojang.blaze3d.systems.RenderSystem;
//import net.minecraft.client.MinecraftClient;
//import net.minecraft.client.gui.DrawContext;
//import net.minecraft.client.gui.screen.ChatScreen;
//import net.minecraft.client.render.*;
//import net.minecraft.text.Text;
//import net.minecraft.util.Identifier;
//import org.joml.Matrix4f;
//
//import java.util.ArrayList;
//import java.util.List;
//
//public class CustomHudRenderer {
//
//    private static final MinecraftClient client = MinecraftClient.getInstance();
//
//    public static Identifier font;
//    public static HudTheme theme;
//
//    public static void extractRenderState(DrawContext context, float tickDelta) {
//
//        Profile profile = ProfileManager.getActive();
//        if (profile == null || client.getDebugHud().shouldShowDebugHud())
//            return;
//
//        if (profile.baseTheme.getTargetGuiScale() != client.getWindow().getScaleFactor())
//            client.onResolutionChanged();
//
//        boolean isChatOpen = client.currentScreen instanceof ChatScreen;
//
//        List<RenderPiece> pieces = new ArrayList<>();
//
//        context.getMatrices().push();
//
//        context.getMatrices().scale(profile.baseTheme.getScale(), profile.baseTheme.getScale(), 1);
//        BufferBuilder bgBuilder = Tessellator.getInstance().getBuffer();
//        bgBuilder.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);
//
//        for (Section section : profile.sections) {
//            theme = profile.baseTheme;
//
//            if (section == null || isChatOpen && section.hideOnChat)
//                continue;
//
//            CHFormatting formatting = theme.fgColor.copy();
//            final int right = (int) (client.getWindow().getScaledWidth() * (1 / theme.getScale())) - 3 + section.xOffset;
//            final boolean dynamicWidth = section.width == -1;
//            final boolean maxWidth = section.width == -2;
//            int initialPiecesOffset = pieces.size();
//            int piecesOffset = initialPiecesOffset;
//            int maxLineWidth = 0;
//            List<MaxLineRenderPiece> maxLineRenderPieces = maxWidth ? new ArrayList<>() : null;
//
//
//            int lineCount = 0;
//            List<HudElement> elements = new ArrayList<>();
//            for (HudElement e : section.elements)
//                lineCount += addElement(elements, e, true);
//            outer:
//            for (int j = 0; j < elements.size()-1; j++) {
//                if (elements.get(j) instanceof FunctionalElement.IgnoreNewLineIfSurroundedByNewLine
//                && (j == 0 || elements.get(j-1) instanceof FunctionalElement.NewLine)) {
//                    for (int k = j+1; k < elements.size(); k++) {
//                        if (elements.get(k) instanceof FunctionalElement.IgnoreNewLineIfSurroundedByNewLine)
//                            continue;
//                        if (elements.get(k) instanceof FunctionalElement.NewLine) {
//                            lineCount--;
//                            j = k+1;
//                            continue outer;
//                        }
//                        break outer;
//                    }
//                }
//            }
//            boolean removeExtraNewLines = false;
//            for (int i = elements.size() - 1; i >= 0; i--) {
//                if (!(elements.get(i) instanceof FunctionalElement.NewLine))
//                    break;
//                if (removeExtraNewLines)
//                    elements.remove(i);
//                removeExtraNewLines = true;
//            }
//
//            StringBuilder builder = new StringBuilder();
//            int y = section.getStartY(theme, lineCount);
//            int xOffset = 0;
//
//            int staticWidthY = y;
//
//            for (int ei = 0; ei < elements.size(); ei++) {
//                HudElement e = elements.get(ei);
//                if (e instanceof FunctionalElement) {
//                    String str = builder.toString();
//                    if (!str.isEmpty()) {
//                        str = formatting.getFormatting() + str;
//                        pieces.add(new RenderPiece(str, null, theme.font, xOffset, y, formatting.getColor(), theme.bgColor, theme.textShadow, theme.lineSpacing == 0));
//                        xOffset += client.textRenderer.getWidth(str);
//                        builder.setLength(0);
//                    }
//                    if (e instanceof FunctionalElement.NewLine) {
//                        maxLineWidth = Math.max(maxLineWidth, xOffset);
//                        for (int i = piecesOffset; i < pieces.size(); i++)
//                            pieces.get(i).lineWith = xOffset;
//                        piecesOffset = pieces.size();
//
//                        y += 9 + theme.lineSpacing;
//                        xOffset = 0;
//                        formatting = theme.fgColor.copy();
//                    } else if (e instanceof FunctionalElement.IgnoreNewLineIfSurroundedByNewLine) {
//                        outer:
//                        if ( (ei+1 >= elements.size() || elements.get(ei+1) instanceof FunctionalElement.NewLine) ) {
//                            for (int j = ei-1; j >= 0; j--) {
//                                HudElement element = elements.get(j);
//                                if (element instanceof FunctionalElement.IgnoreNewLineIfSurroundedByNewLine)
//                                    continue;
//                                if (element instanceof FunctionalElement.NewLine)
//                                    ei++;
//                                break outer;
//                            }
//                            ei++;
//                        }
//
//                    } else if (e instanceof FunctionalElement.ChangeFormatting cfe && cfe.getFormatting() != null) {
//                        formatting.apply(cfe.getFormatting(), theme);
//                    } else if (e instanceof FunctionalElement.ChangeTheme cte) {
//                        if ((maxWidth || !dynamicWidth) && theme.bgColor != cte.theme.bgColor) {
//                            if (maxWidth)
//                                maxLineRenderPieces.add(new MaxLineRenderPiece(theme.bgColor, staticWidthY - 2, y-2));
//                            else {
//                                int x1 = section.getSetWidthBgX(right, maxLineWidth);
//                                addLineBg(context, bgBuilder, x1, staticWidthY - 2, x1 + section.width, y - 2, theme.bgColor);
//                            }
//                            staticWidthY = y;
//                        }
//                        theme = cte.theme;
//                        font = cte.theme.font;
//                    } else if (e instanceof IconElement ie) {
//                        pieces.add( new RenderPiece(ie, ListManager.getValue(ie.getProviderID()), null, xOffset, y, formatting.getColor(), theme.bgColor, false, false) );
//                        xOffset += ie.getTextWidth();
//                    } else if (e instanceof TextElement te) {
//                        pieces.add( new RenderPiece(te.getText(), null, font, xOffset, y, te.getColor(formatting.getColor()), theme.bgColor, theme.textShadow, theme.lineSpacing == 0) );
//                        xOffset += te.getTextWidth();
//                    } else if (e instanceof FunctionalElement.ExitList el) {
//                        for (int j = ei+1; j < elements.size(); j++) {
//                            if (elements.get(j) instanceof FunctionalElement.PopList pl && pl.providerID.equals(el.providerID))
//                                break;
//                            ei++;
//                        }
//                    } else if (e instanceof FunctionalElement.ContinueList cl) {
//                        for (int j = ei+1; j < elements.size(); j++) {
//                            if (elements.get(j) instanceof FunctionalElement.AdvanceList pl && pl.providerID.equals(cl.providerID))
//                                break;
//                            if (elements.get(j) instanceof FunctionalElement.PopList pl && pl.providerID.equals(cl.providerID))
//                                break;
//                            ei++;
//                        }
//                    }
//                    if (e instanceof ExecuteElement ee) ee.run();
//
//                } else {
//                    builder.append(e.getString());
//                }
//            }
//
//            int lastY = Integer.MIN_VALUE;
//            for (;initialPiecesOffset < pieces.size(); initialPiecesOffset++) {
//                RenderPiece piece = pieces.get(initialPiecesOffset);
//                int left = section.getStartX(right, piece.lineWith, maxLineWidth);
//
//                if (dynamicWidth && piece.x == 0 && piece.y != lastY) {
//                    addLineBg(context, bgBuilder, left-2, piece.y - 2, left + piece.lineWith + 2, piece.y + 9 + theme.lineSpacing - 2, piece.bgColor);
//                    lastY = piece.y;
//                }
//
//                piece.x += left;
//            }
//
//            if (maxLineWidth != 0) {
//                if (maxLineRenderPieces != null) {
//                    int x1 = section.getSetWidthBgX(right, maxLineWidth);
//                    for (MaxLineRenderPiece piece : maxLineRenderPieces) {
//                        if (piece.y1 != piece.y2)
//                            addLineBg(context, bgBuilder, x1 - 2, piece.y1, x1 + maxLineWidth + 2, piece.y2, piece.color);
//                    }
//                    addLineBg(context, bgBuilder, x1 - 2, staticWidthY - 2, x1 + maxLineWidth + 2, y - 2, theme.bgColor);
//                } else if (!dynamicWidth) {
//                    int x1 = section.getSetWidthBgX(right + 3, maxLineWidth) - 2;
//                    addLineBg(context, bgBuilder, x1, staticWidthY - 2, x1 + section.width, y - 2, theme.bgColor);
//                }
//            }
//
//        }
//
//        RenderSystem.enableBlend();
//        RenderSystem.defaultBlendFunc();
//        RenderSystem.setShader(GameRenderer::getPositionColorProgram);
//        BufferRenderer.drawWithGlobalProgram(bgBuilder.end());
//        RenderSystem.disableBlend();
//
//        for (RenderPiece piece : pieces) {
//            font = piece.font;
//            if (piece.element instanceof IconElement ie )
//                try { ie.extractRenderState(context, piece); }
//                catch (Exception e){
//                    CustomHud.LOGGER.catching(e);
//                }
//            else if (piece.element instanceof String value && !value.isEmpty())
//                context.drawText(client.textRenderer, value, piece.x, piece.shiftTextUpOrFitItemIcon ? piece.y-1 : piece.y, piece.color, piece.shadow);
//            else if (piece.element instanceof Text text)
//                context.drawText(client.textRenderer, text, piece.x, piece.shiftTextUpOrFitItemIcon ? piece.y-1 : piece.y, piece.color, piece.shadow);
//
//        }
//
//        context.getMatrices().pop();
//        font = null;
//        theme = null;
//
//    }
//
//
//    public static int addElement(List<HudElement> allElements, HudElement element, boolean convertLineBreak) {
//        if (element instanceof MultiElement me) {
//            int nl = 0;
//            List<HudElement> elements = me.expand();
//            if (elements.isEmpty()) {
//                if (me.ignoreNewlineIfEmpty() && convertLineBreak)
//                    allElements.add(new FunctionalElement.IgnoreNewLineIfSurroundedByNewLine());
//                return nl;
//            }
//            for (HudElement e : me.expand())
//                nl += addElement(allElements, e, convertLineBreak);
//            return nl;
//        }
//        else {
//            if (element instanceof FunctionalElement.XList xl) xl.run();
//            allElements.add(element);
//            return element instanceof FunctionalElement.NewLine ? 1 : 0;
//        }
//
//
//    }
//
//    private static void addLineBg(DrawContext context, BufferBuilder builder, int x1, int y1, int x2, int y2, int color) {
//        Matrix4f matrix = context.getMatrices().peek().getPositionMatrix();
//        float f = (float)(color >> 24 & 255) / 255.0F;
//        float g = (float)(color >> 16 & 255) / 255.0F;
//        float h = (float)(color >> 8 & 255) / 255.0F;
//        float j = (float)(color & 255) / 255.0F;
//        builder.vertex(matrix, (float)x1, (float)y2, 0.0F).color(g, h, j, f).next();
//        builder.vertex(matrix, (float)x2, (float)y2, 0.0F).color(g, h, j, f).next();
//        builder.vertex(matrix, (float)x2, (float)y1, 0.0F).color(g, h, j, f).next();
//        builder.vertex(matrix, (float)x1, (float)y1, 0.0F).color(g, h, j, f).next();
//    }
//
//
//}
