package net.offkung.bhspells.entity.spells.gale_piercer;

import com.mojang.blaze3d.vertex.PoseStack;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class GaleArrowRenderer extends EntityRenderer<GaleArrowEntity> {
    public static final ResourceLocation TEXTURE = IronsSpellbooks.id("textures/entity/fire_arrow.png");
    public static final RenderType RENDER_TYPE = RenderType.entityTranslucentEmissive(TEXTURE);

    public GaleArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GaleArrowEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        Vec3 motion = entity.deltaMovementOld.add(entity.getDeltaMovement().subtract(entity.deltaMovementOld).scale(partialTick));
        GalePiercerArrowModel.orientToDirection(poseStack, motion);
        GalePiercerArrowModel.render(poseStack, bufferSource.getBuffer(RENDER_TYPE), LightTexture.FULL_BRIGHT, 0.1625F, 255, 166, 51, 255);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(GaleArrowEntity entity) {
        return TEXTURE;
    }
}
