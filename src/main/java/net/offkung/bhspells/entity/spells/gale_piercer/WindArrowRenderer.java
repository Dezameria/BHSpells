package net.offkung.bhspells.entity.spells.gale_piercer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;

public class WindArrowRenderer extends EntityRenderer<WindArrowEntity> {
    public static final ResourceLocation TEXTURE = BHSpells.id("textures/entity/gale_piercer/white_gray_arrow.png");
    public static final RenderType RENDER_TYPE = RenderType.entityTranslucent(TEXTURE);

    public WindArrowRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(WindArrowEntity entity, float yaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();

        Vec3 motion = entity.deltaMovementOld.add(entity.getDeltaMovement().subtract(entity.deltaMovementOld).scale(partialTick));
        GalePiercerArrowModel.orientToDirection(poseStack, motion);
        GalePiercerArrowModel.render(poseStack, bufferSource.getBuffer(RENDER_TYPE), LightTexture.FULL_BRIGHT, 0.13F, 179, 255, 217, 230);

        poseStack.popPose();
        super.render(entity, yaw, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WindArrowEntity entity) {
        return TEXTURE;
    }
}
