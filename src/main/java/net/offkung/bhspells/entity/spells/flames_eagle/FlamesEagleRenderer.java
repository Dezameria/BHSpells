package net.offkung.bhspells.entity.spells.flames_eagle;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class FlamesEagleRenderer extends GeoEntityRenderer<FlamesEagleEntity> {
    public FlamesEagleRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new FlamesEagleModel());
        this.shadowRadius = 0f;
        this.scaleWidth = 1f;
        this.scaleHeight = 1f;
    }

    @Override
    protected void applyRotations(FlamesEagleEntity animatable, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTick) {
        float yRot = Mth.rotLerp(partialTick, animatable.yRotO, animatable.getYRot());
        float xRot = Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot());
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
    }

    @Override
    public void preRender(PoseStack poseStack, FlamesEagleEntity animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        float scale = animatable.isEmpowered() ? 2.0f : 1.0f;
        this.scaleWidth = scale;
        this.scaleHeight = scale;
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public RenderType getRenderType(FlamesEagleEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.energySwirl(texture, 0.0F, 0.0F);
    }
}
