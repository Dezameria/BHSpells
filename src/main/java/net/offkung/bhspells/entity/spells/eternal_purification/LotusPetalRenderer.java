package net.offkung.bhspells.entity.spells.eternal_purification;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class LotusPetalRenderer extends GeoEntityRenderer<LotusPetal> {
    public static final ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/lotus_flower/lotus_flower1.png");

    public LotusPetalRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LotusPetalModel());
        this.shadowRadius = 0.0F;
        this.scaleWidth = 7.0F;
        this.scaleHeight = 7.0F;
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull LotusPetal animatable) {
        return textureLocation;
    }

    public RenderType getRenderType(LotusPetal animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.energySwirl(texture, 0.0F, 0.0F);
    }
}
