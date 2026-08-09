package net.offkung.bhspells.entity.spells.six_petal_waltz;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.client.render.BHRenderType;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class PetalWaltzSwordRenderer extends GeoEntityRenderer<PetalWaltzSword> {
    public PetalWaltzSwordRenderer(EntityRendererProvider.Context context) {
        super(context, new PetalWaltzSwordModel());
        this.shadowRadius = 0f;
    }

    @Override
    public void preRender(PoseStack poseStack, PetalWaltzSword animatable, BakedGeoModel model, @Nullable MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        Vec3 motion = animatable.getDeltaMovement();
        float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) + 90.0F);
        float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

    public RenderType getRenderType(PetalWaltzSword animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return BHRenderType.energySwirlOutline(texture, 0.0F, 0.0F);
    }
}
