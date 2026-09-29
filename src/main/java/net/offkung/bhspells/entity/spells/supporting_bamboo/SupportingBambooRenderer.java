package net.offkung.bhspells.entity.spells.supporting_bamboo;

import com.gametechbc.traveloptics.api.render.TORenderEmissiveLayer;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import javax.annotation.Nullable;

public class SupportingBambooRenderer extends GeoEntityRenderer<SupportingBamboo> {
    public SupportingBambooRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SupportingBambooModel());
        this.addRenderLayer(new AutoGlowingGeoLayer<>(this));
    }

    @Override
    public RenderType getRenderType(SupportingBamboo animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }
}
