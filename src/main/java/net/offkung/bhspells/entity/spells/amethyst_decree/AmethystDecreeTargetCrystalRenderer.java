package net.offkung.bhspells.entity.spells.amethyst_decree;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class AmethystDecreeTargetCrystalRenderer extends EntityRenderer<AmethystDecreeTargetCrystalEntity> {
    private static final int RISE_TICKS = 6;
    private static final float NO_SINK = Float.MAX_VALUE;
    private static final float LEG_SHRINK_MIN_SCALE = 0.05f;

    private static final int STRIKE_RISE_TICKS = 2;
    private static final int STRIKE_SINK_START = 60;
    private static final int STRIKE_SINK_TICKS = 30;

    private final ModelPart smallUnit;
    private final ModelPart largeUnit;

    public AmethystDecreeTargetCrystalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.smallUnit = context.bakeLayer(CrystalUnitModel.SMALL_LAYER);
        this.largeUnit = context.bakeLayer(CrystalUnitModel.LARGE_LAYER);
    }

    @Override
    public void render(AmethystDecreeTargetCrystalEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTicks;
        int rootTicks = entity.getRootDurationTicks();
        int totalTicks = entity.getTotalLifetimeTicks();
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));

        if (age < rootTicks) {
            for (CrystalTransform t : entity.getOrComputeEncasement()) {
                float offset = CasterRingLayout.positionOffset(age, 0, RISE_TICKS, NO_SINK, 0);
                if (offset <= -1.0f) {
                    continue;
                }
                CrystalUnitModel.renderInstance(smallUnit, largeUnit, poseStack, consumer, light, OverlayTexture.NO_OVERLAY, t, offset);
            }
        } else {
            float legAge = age - rootTicks;
            float legTotal = Math.max(1.0f, totalTicks - rootTicks);
            float shrinkProgress = Mth.clamp(legAge / legTotal, 0.0f, 1.0f);
            float extraScale = Mth.lerp(shrinkProgress, 1.0f, LEG_SHRINK_MIN_SCALE);
            for (CrystalTransform t : entity.getOrComputeLegCrystals()) {
                float offset = CasterRingLayout.positionOffset(age, rootTicks, RISE_TICKS, NO_SINK, 0);
                if (offset <= -1.0f) {
                    continue;
                }
                CrystalUnitModel.renderInstance(smallUnit, largeUnit, poseStack, consumer, light, OverlayTexture.NO_OVERLAY, t, offset, extraScale);
            }
        }

        for (CrystalTransform t : entity.getOrComputeStrikeSpikes()) {
            float offset = CasterRingLayout.positionOffset(age, 0, STRIKE_RISE_TICKS, STRIKE_SINK_START, STRIKE_SINK_TICKS);
            if (offset <= -1.0f) {
                continue;
            }
            CrystalUnitModel.renderInstance(smallUnit, largeUnit, poseStack, consumer, light, OverlayTexture.NO_OVERLAY, t, offset);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystDecreeTargetCrystalEntity entity) {
        return CrystalUnitModel.TEXTURE;
    }
}
