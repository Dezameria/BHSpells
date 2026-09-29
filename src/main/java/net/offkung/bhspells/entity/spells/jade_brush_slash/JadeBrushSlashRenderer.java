package net.offkung.bhspells.entity.spells.jade_brush_slash;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.Objects;
import java.util.Random;

public class JadeBrushSlashRenderer extends EntityRenderer<JadeBrushSlash> {
    private static final ResourceLocation[] TEXTURES = new ResourceLocation[]{BHSpells.id("textures/entity/jade_brush_slash/red_beryl_slash_1.png"), BHSpells.id("textures/entity/jade_brush_slash/red_beryl_slash_2.png"), BHSpells.id("textures/entity/jade_brush_slash/red_beryl_slash_3.png"), BHSpells.id("textures/entity/jade_brush_slash/red_beryl_slash_4.png")};

    public JadeBrushSlashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    public void render(JadeBrushSlash entity, float yaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int light) {
        poseStack.pushPose();
        PoseStack.Pose pose = poseStack.last();
        poseStack.mulPose(Axis.YP.rotationDegrees(90.0F - entity.getYRot()));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getXRot()));
        float randomZ = (float)(new Random(31L * (long)entity.getId())).nextInt(-8, 8);
        poseStack.mulPose(Axis.XP.rotationDegrees(randomZ));
        this.drawSlash(pose, entity, bufferSource, entity.getBbWidth() * 1.5F, entity.isMirrored());
        poseStack.popPose();
        super.render(entity, yaw, partialTicks, poseStack, bufferSource, light);
    }

    private void drawSlash(PoseStack.Pose pose, JadeBrushSlash entity, MultiBufferSource bufferSource, float width, boolean mirrored) {
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = bufferSource.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(entity)));
        float halfWidth = width * 0.5F;
        float height = entity.getBbHeight() * 0.5F;
        consumer.vertex(poseMatrix, -halfWidth, height, -halfWidth).color(255, 255, 255, 255).uv(0.0F, mirrored ? 0.0F : 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, halfWidth, height, -halfWidth).color(255, 255, 255, 255).uv(1.0F, mirrored ? 0.0F : 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, halfWidth, height, halfWidth).color(255, 255, 255, 255).uv(1.0F, mirrored ? 1.0F : 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
        consumer.vertex(poseMatrix, -halfWidth, height, halfWidth).color(255, 255, 255, 255).uv(0.0F, mirrored ? 1.0F : 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(normalMatrix, 0.0F, 1.0F, 0.0F).endVertex();
    }

    public ResourceLocation getTextureLocation(JadeBrushSlash entity) {
        int tickCount = entity.tickCount;
        Objects.requireNonNull(entity);
        int frame = tickCount / 2 % TEXTURES.length;
        return TEXTURES[frame];
    }
}
