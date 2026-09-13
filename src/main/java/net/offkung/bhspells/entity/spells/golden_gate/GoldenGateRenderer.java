package net.offkung.bhspells.entity.spells.golden_gate;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class GoldenGateRenderer extends GeoEntityRenderer<GoldenGateEntity> {
    public static final ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/golden_gate/golden_gate.png");

    public GoldenGateRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GoldenGateModel());
        this.shadowRadius = 0f;
        this.scaleWidth = 2f;
        this.scaleHeight = 2f;
    }

    @Override
    public ResourceLocation getTextureLocation(GoldenGateEntity animatable) {
        return textureLocation;
    }

    @Override
    public void preRender(PoseStack poseStack, GoldenGateEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Vec3 motion = animatable.getForward();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));

        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(GoldenGateEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.energySwirl(texture, 0, 0);
    }
}
