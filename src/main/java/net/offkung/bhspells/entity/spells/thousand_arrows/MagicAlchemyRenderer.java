package net.offkung.bhspells.entity.spells.thousand_arrows;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class MagicAlchemyRenderer extends GeoEntityRenderer<MagicAlchemyEntity> {
    public static final ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/magic_circle/magic_circle.png");

    public MagicAlchemyRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new MagicAlchemyModel());
        this.shadowRadius = 0.0F;
    }

    public ResourceLocation getTextureLocation(MagicAlchemyEntity animatable) {
        return textureLocation;
    }

    public RenderType getRenderType(MagicAlchemyEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.energySwirl(texture, 0.0F, 0.0F);
    }
}
