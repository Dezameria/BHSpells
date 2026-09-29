package net.offkung.bhspells.entity.spells.flames_eagle;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class FlamesEagleModel extends GeoModel<FlamesEagleEntity> {
    public static final ResourceLocation modelResource = BHSpells.id("geo/flames_eagle.geo.json");
    public static final ResourceLocation textureResource = BHSpells.id("textures/entity/flames_eagle/flames_eagle.png");
    public static final ResourceLocation animationResource = BHSpells.id("animations/flames_eagle_fly.animation.json");

    public FlamesEagleModel() {}

    @Override
    public ResourceLocation getModelResource(FlamesEagleEntity object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(FlamesEagleEntity object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(FlamesEagleEntity animatable) {
        return animationResource;
    }
}
