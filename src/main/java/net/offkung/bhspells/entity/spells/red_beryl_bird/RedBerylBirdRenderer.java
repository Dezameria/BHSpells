package net.offkung.bhspells.entity.spells.red_beryl_bird;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class RedBerylBirdRenderer extends GeoEntityRenderer<RedBerylBird> {
    public RedBerylBirdRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new RedBerylBirdModel());
        this.shadowRadius = 0f;
        this.scaleWidth = 3.2f;
        this.scaleHeight = 3.2f;
    }

    public RenderType getRenderType(RedBerylBird animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.energySwirl(texture, 0.0F, 0.0F);
    }
}
