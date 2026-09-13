package net.offkung.bhspells.entity.spells.golden_cloud;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class GoldenCloudModel extends GeoModel<GoldenCloudEntity> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/golden_cloud.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/golden_cloud/c.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/golden_cloud.animation.json");

    @Override
    public ResourceLocation getModelResource(GoldenCloudEntity object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(GoldenCloudEntity object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(GoldenCloudEntity animatable) {
        return animationResource;
    }
}
