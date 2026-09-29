package net.offkung.bhspells.entity.spells.jade_cluster;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class JadeClusterEntityLayer extends GeoRenderLayer<JadeClusterEntity> {
    public static final ResourceLocation EMISSIVE_TEXTURE = BHSpells.id("textures/entity/jade_cluster/jade_cluster_layer.png");

    public JadeClusterEntityLayer(GeoRenderer<JadeClusterEntity> entityRenderer) {
        super(entityRenderer);
    }

    @Override
    public void render(PoseStack poseStack, JadeClusterEntity animatable, BakedGeoModel bakedModel, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        Player localPlayer = Minecraft.getInstance().player;
        if (localPlayer == null || !animatable.isOwnedBy(localPlayer)) {
            return;
        }

        float glowIntensity = animatable.getEmissiveBrightness();
        if (!(glowIntensity <= 0.0F)) {
            RenderType glowRenderType = RenderType.eyes(EMISSIVE_TEXTURE);
            VertexConsumer vertexConsumer = bufferSource.getBuffer(glowRenderType);
            poseStack.pushPose();
            this.getRenderer().actuallyRender(poseStack, animatable, bakedModel, glowRenderType, bufferSource, vertexConsumer, true, partialTick, 15728880, OverlayTexture.NO_OVERLAY, glowIntensity, glowIntensity, glowIntensity, 1.0F);
            poseStack.popPose();
        }
    }
}
