package net.offkung.bhspells.entity.spells.golden_cloud;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class GoldenCloudRenderer extends GeoEntityRenderer<GoldenCloudEntity> {
    public GoldenCloudRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new GoldenCloudModel());
        this.shadowRadius = 0f;
        this.scaleWidth = 3f;
        this.scaleHeight = 3f;
    }

    @Override
    public RenderType getRenderType(GoldenCloudEntity animatable, ResourceLocation texture, @Nullable net.minecraft.client.renderer.MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
