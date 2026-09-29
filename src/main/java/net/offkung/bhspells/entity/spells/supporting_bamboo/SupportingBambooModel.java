package net.offkung.bhspells.entity.spells.supporting_bamboo;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class SupportingBambooModel extends GeoModel<SupportingBamboo> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/dafeng_bamboo.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/dafeng_bamboo/cbc.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/dafeng_bamboo.animation.json");

    @Override
    public ResourceLocation getModelResource(SupportingBamboo object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(SupportingBamboo object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(SupportingBamboo animatable) {
        return animationResource;
    }
}
