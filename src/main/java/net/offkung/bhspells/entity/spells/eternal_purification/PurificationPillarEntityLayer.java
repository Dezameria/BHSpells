package net.offkung.bhspells.entity.spells.eternal_purification;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class PurificationPillarEntityLayer extends GeoRenderLayer<PurificationPillarEntity> {
    public static final ResourceLocation BASE_EMISSIVE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/purification_pillar/purification_pillar_layer_base.png");
    public static final ResourceLocation RESONATE_EMISSIVE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/purification_pillar/purification_pillar_layer_resonate.png");

    public PurificationPillarEntityLayer(GeoRenderer<PurificationPillarEntity> entityRenderer) {
        super(entityRenderer);
    }

    public void render(PoseStack poseStack, PurificationPillarEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        float glowIntensity = animatable.getEmissiveBrightness();
        if (!(glowIntensity <= 0.0F)) {
            ResourceLocation emissiveTexture = this.getEmissiveTexture(animatable);
            RenderType glowRenderType = RenderType.eyes(emissiveTexture);
            VertexConsumer vertexconsumer = bufferSource.getBuffer(glowRenderType);
            poseStack.pushPose();
            this.getRenderer().actuallyRender(poseStack, animatable, bakedModel, glowRenderType, bufferSource, vertexconsumer, true, partialTick, 15728880, OverlayTexture.NO_OVERLAY, glowIntensity, glowIntensity, glowIntensity, 1.0F);
            poseStack.popPose();
        }
    }

    private ResourceLocation getEmissiveTexture(PurificationPillarEntity animatable) {
        PurificationPillarEntity.Stage stage = animatable.getStage();
        switch (stage.value) {
            case 1:
                return BASE_EMISSIVE_TEXTURE;
            case 2:
            case 3:
            case 4:
                return RESONATE_EMISSIVE_TEXTURE;
            default:
                return BASE_EMISSIVE_TEXTURE;
        }
    }
}
