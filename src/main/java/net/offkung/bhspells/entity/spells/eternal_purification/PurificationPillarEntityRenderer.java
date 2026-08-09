package net.offkung.bhspells.entity.spells.eternal_purification;

import com.gametechbc.traveloptics.api.render.TORenderEmissiveLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class PurificationPillarEntityRenderer extends GeoEntityRenderer<PurificationPillarEntity> {
    private static final ResourceLocation BASE_LAYER_TEXTURE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/purification_pillar/purification_pillar_layer_passive.png");

    public PurificationPillarEntityRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new PurificationPillarEntityModel());
        this.addRenderLayer(new TORenderEmissiveLayer<>(this, RenderType.eyes(BASE_LAYER_TEXTURE), 0.8F));
        this.addRenderLayer(new PurificationPillarEntityLayer(this));
        this.scaleWidth = 4.0F;
        this.scaleHeight = 4.0F;
    }

    public void render(PurificationPillarEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-entityYaw));
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
