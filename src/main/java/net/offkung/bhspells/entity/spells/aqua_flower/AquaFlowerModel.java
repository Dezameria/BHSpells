package net.offkung.bhspells.entity.spells.aqua_flower;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class AquaFlowerModel extends GeoModel<AquaFlower> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/purple_lotus.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/lotus_flower/purple_lotus.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/purple_flower.animation.json");

    public ResourceLocation getModelResource(AquaFlower object) {
        return modelResource;
    }

    public ResourceLocation getTextureResource(AquaFlower object) {
        return textureResource;
    }

    public ResourceLocation getAnimationResource(AquaFlower animatable) {
        return animationResource;
    }
}
