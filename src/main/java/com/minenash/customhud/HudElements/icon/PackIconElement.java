package com.minenash.customhud.HudElements.icon;

import com.google.common.collect.Maps;
import com.google.common.hash.Hashing;
import com.minenash.customhud.CustomHud;
import com.minenash.customhud.data.Flags;
import com.minenash.customhud.render.RenderPiece;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.Identifier;
import net.minecraft.server.packs.PackResources;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.resources.IoSupplier;
import net.minecraft.util.Util;
import org.joml.Matrix3x2fStack;

import java.io.InputStream;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;

import static com.minenash.customhud.CustomHud.CLIENT;

public class PackIconElement extends IconElement {

    private final Map<String, Identifier> iconTextures = Maps.newHashMap();
    public PackIconElement(UUID providerID, Flags flags) {
        super(flags, 11);
        this.providerID = providerID;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        Pack pack = (Pack) piece.value;
        Matrix3x2fStack matrices = context.pose();
        matrices.pushMatrix();
        try {
            matrices.translate(piece.x + shiftX, piece.y + shiftY - 2);
            if (!referenceCorner)
                matrices.translate(0, -(11*scale-11)/2F);
//        matrices.scale(scale, scale, 0);
            int width = (int) (11*scale);
            rotate(matrices, width, width);

            context.blit(RenderPipelines.GUI_TEXTURED, getPackIconTexture(pack), 0, 0, 0, 0, width, width, width, width);
        }
        finally {
            matrices.popMatrix();
        }
    }

    private Identifier getPackIconTexture(Pack resourcePackProfile) {
        return this.iconTextures.computeIfAbsent(resourcePackProfile.getId(), (profileName) -> loadPackIcon(CLIENT.getTextureManager(), resourcePackProfile));
    }

    private static final Identifier UNKNOWN_PACK = Identifier.parse("textures/misc/unknown_pack.png");
    public static Identifier loadPackIcon(TextureManager textureManager, Pack resourcePackProfile) {
        try (Stream<PackResources> resourcePacks = resourcePackProfile.open()) {
            for (PackResources resourcePack : resourcePacks.toList()) {
                IoSupplier<InputStream> inputSupplier = resourcePack.getRootResource("pack.png");
                if (inputSupplier == null)
                    continue;

                String name = resourcePackProfile.getId();
                String safeName = Util.sanitizeName(name, Identifier::validPathChar);
                Identifier identifier = Identifier.fromNamespaceAndPath("minecraft", "pack/" + safeName + "/" + Hashing.sha1().hashUnencodedChars(name) + "/icon");

                try (InputStream inputStream = inputSupplier.get()) {
                    NativeImage nativeImage = NativeImage.read(inputStream);
                    textureManager.register(identifier, new DynamicTexture(identifier::toString, nativeImage));
                    return identifier;
                }
            }
            return UNKNOWN_PACK;
        } catch (Exception var14) {
            CustomHud.LOGGER.warn("[CustomHud] Failed to load icon from pack {}", resourcePackProfile.getId(), var14);
            return UNKNOWN_PACK;
        }
    }

}
