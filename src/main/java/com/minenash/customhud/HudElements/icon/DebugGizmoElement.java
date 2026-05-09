package com.minenash.customhud.HudElements.icon;

import com.minenash.customhud.data.Flags;
import com.minenash.customhud.mixin.accessors.DebugHudAccessor;
import com.minenash.customhud.render.RenderPiece;
import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.GpuBufferSlice;
import com.mojang.blaze3d.pipeline.RenderPipeline;
import com.mojang.blaze3d.systems.RenderPass;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.textures.GpuTextureView;
import org.joml.Matrix4f;
import org.joml.Matrix4fStack;
import org.joml.Vector3f;
import org.joml.Vector4f;

import java.util.OptionalDouble;
import java.util.OptionalInt;
import net.minecraft.client.Camera;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.util.Mth;

import static com.minenash.customhud.CustomHud.CLIENT;

// No longer works
public class DebugGizmoElement extends IconElement {

    private final float size;

    public DebugGizmoElement(Flags flags) {
        super(flags, 10);
        this.size = (int)(10*scale) / 2F;
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, RenderPiece piece) {
        float scale = -1 * this.scale * 10/18f;
        Camera camera = CLIENT.gameRenderer.getMainCamera();
        Matrix4fStack matrix4fStack = RenderSystem.getModelViewStack();
        matrix4fStack.pushMatrix();
//        matrix4fStack.scale(profileScale,profileScale,1);

        float yaw = Mth.wrapDegrees(camera.yRot());
        float pitch = Mth.wrapDegrees(camera.xRot());

        float x_offset = size / 2;
        float y_offset = (pitch + 90) / 180 * size * 2 - 2;
        if (y_offset > size) y_offset = size;

        if (yaw > 90) {
            x_offset += size - (((yaw - 90) / 90) * size);
            y_offset += size * (-pitch / 90);
        }
        else if (yaw > 0) {
            x_offset += size;
            y_offset += (yaw / 90) * size * (-pitch / 90);
        }
        else if (yaw < -90) {
            x_offset += 0;
            y_offset += ((yaw + 90)/90) * -size * (-pitch / 90);
        }
        else {
            x_offset += size + ((yaw) / 90) * size;
            y_offset += 0;
        }

        matrix4fStack.translate(piece.x + shiftX + x_offset, piece.y + shiftY + y_offset + (size/2), 100);
//        matrix4fStack.mul(context.getMatrices().peek().getPositionMatrix());
        matrix4fStack.rotateX(-camera.xRot() * (float) (Math.PI / 180.0));
        matrix4fStack.rotateY(camera.yRot() * (float) (Math.PI / 180.0));
        matrix4fStack.scale(scale, scale, scale);
        // Minecraft 26.1 moved the debug gizmo renderer to the extracted render state path.
        matrix4fStack.popMatrix();
    }



}
