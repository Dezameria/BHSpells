package net.offkung.bhspells.entity.spells.gale_drive;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class GaleDriveVortexEmissiveLayer extends GeoRenderLayer<GaleDriveVortexEntity> {
    public static final ResourceLocation LAYER = BHSpells.id("textures/entity/gale_vortex/gale_vortex_layer.png");

    public GaleDriveVortexEmissiveLayer(GeoRenderer<GaleDriveVortexEntity> entityRendererIn) {
        super(entityRendererIn);
    }

    @Override
    public void render(PoseStack poseStack, GaleDriveVortexEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        RenderType emissiveType = RenderType.eyes(LAYER);
        VertexConsumer vertexConsumer = bufferSource.getBuffer(emissiveType);
        poseStack.pushPose();

        float pulse = (float) (animatable.tickCount + partialTick + (animatable.getX() + animatable.getZ()) * 500.0) * 0.15f;
        float alpha = Mth.sin(pulse) * 0.5f + 0.5f;

        getRenderer().actuallyRender(poseStack, animatable, bakedModel, emissiveType, bufferSource, vertexConsumer, true, partialTick, 15728880, OverlayTexture.NO_OVERLAY, alpha, alpha, alpha, 1.0f);
        poseStack.popPose();
    }
}
