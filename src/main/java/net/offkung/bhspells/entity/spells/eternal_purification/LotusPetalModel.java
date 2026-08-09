package net.offkung.bhspells.entity.spells.eternal_purification;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class LotusPetalModel extends GeoModel<LotusPetal> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/lotus_flower1.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/lotus_flower/lotus_flower1.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/lotus_flower1.animation.json");

    public ResourceLocation getModelResource(LotusPetal object) {
        return modelResource;
    }

    public ResourceLocation getTextureResource(LotusPetal object) {
        return textureResource;
    }

    public ResourceLocation getAnimationResource(LotusPetal animatable) {
        return animationResource;
    }
}
