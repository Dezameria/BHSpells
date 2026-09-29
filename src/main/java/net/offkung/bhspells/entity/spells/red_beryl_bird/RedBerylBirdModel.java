package net.offkung.bhspells.entity.spells.red_beryl_bird;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class RedBerylBirdModel extends GeoModel<RedBerylBird> {
    public static final ResourceLocation modelResource = BHSpells.id("geo/red_beryl_bird.geo.json");
    public static final ResourceLocation textureResource = BHSpells.id("textures/entity/jade_bird/red_beryl_bird.png");
    public static final ResourceLocation animationResource = BHSpells.id("animations/red_beryl_bird_fly.animation.json");

    public RedBerylBirdModel() {}

    @Override
    public ResourceLocation getModelResource(RedBerylBird object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(RedBerylBird object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(RedBerylBird animatable) {
        return animationResource;
    }
}
