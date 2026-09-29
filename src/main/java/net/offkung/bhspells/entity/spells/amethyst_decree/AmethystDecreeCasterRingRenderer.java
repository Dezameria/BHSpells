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

public class AmethystDecreeCasterRingRenderer extends EntityRenderer<AmethystDecreeCasterRingEntity> {
    private static final int SCATTER_RISE_TICKS = 6;
    private static final int BURST_TICK = 20;
    private static final int BURST_RISE_TICKS = 2;
    private static final int SINK_START = 80;
    private static final int SINK_TICKS = 30;

    private final ModelPart smallUnit;
    private final ModelPart largeUnit;

    public AmethystDecreeCasterRingRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.smallUnit = context.bakeLayer(CrystalUnitModel.SMALL_LAYER);
        this.largeUnit = context.bakeLayer(CrystalUnitModel.LARGE_LAYER);
    }

    @Override
    public void render(AmethystDecreeCasterRingEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int light) {
        float age = entity.tickCount + partialTicks;
        VertexConsumer consumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));

        for (CrystalTransform t : entity.getOrComputeScatter()) {
            float offset = CasterRingLayout.positionOffset(age, t.riseDelayTicks(), SCATTER_RISE_TICKS, SINK_START, SINK_TICKS);
            if (offset <= -1.0f) {
                continue;
            }
            CrystalUnitModel.renderInstance(smallUnit, largeUnit, poseStack, consumer, light, OverlayTexture.NO_OVERLAY, t, offset);
        }
        for (CrystalTransform t : entity.getOrComputeBurstRing()) {
            float offset = CasterRingLayout.positionOffset(age, BURST_TICK, BURST_RISE_TICKS, SINK_START, SINK_TICKS);
            if (offset <= -1.0f) {
                continue;
            }
            CrystalUnitModel.renderInstance(smallUnit, largeUnit, poseStack, consumer, light, OverlayTexture.NO_OVERLAY, t, offset);
        }
        for (CrystalTransform t : entity.getOrComputeBurstScatter()) {
            float offset = CasterRingLayout.positionOffset(age, BURST_TICK, BURST_RISE_TICKS, SINK_START, SINK_TICKS);
            if (offset <= -1.0f) {
                continue;
            }
            CrystalUnitModel.renderInstance(smallUnit, largeUnit, poseStack, consumer, light, OverlayTexture.NO_OVERLAY, t, offset);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(AmethystDecreeCasterRingEntity entity) {
        return CrystalUnitModel.TEXTURE;
    }
}
