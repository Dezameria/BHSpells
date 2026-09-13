package net.offkung.bhspells.entity.spells.aqua_flower;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

import javax.annotation.Nullable;

public class AquaFlowerRenderer extends GeoEntityRenderer<AquaFlower> {
    public static final ResourceLocation textureLocation = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/lotus_flower/purple_lotus.png");

    public AquaFlowerRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new AquaFlowerModel());
        this.shadowRadius = 0.0F;
        this.scaleWidth = 16.0F;
        this.scaleHeight = 16.0F;
    }

    public @NotNull ResourceLocation getTextureLocation(@NotNull AquaFlower animatable) {
        return textureLocation;
    }

    public RenderType getRenderType(AquaFlower animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.energySwirl(texture, 0.0F, 0.0F);
    }
}
