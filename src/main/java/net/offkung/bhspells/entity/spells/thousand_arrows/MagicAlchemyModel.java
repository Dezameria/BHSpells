package net.offkung.bhspells.entity.spells.thousand_arrows;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class MagicAlchemyModel extends GeoModel<MagicAlchemyEntity> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/magic_circle.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/magic_circle/magic_circle.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/magic_circle.animation.json");

    public ResourceLocation getModelResource(MagicAlchemyEntity object) {
        return modelResource;
    }

    public ResourceLocation getTextureResource(MagicAlchemyEntity object) {
        return textureResource;
    }

    public ResourceLocation getAnimationResource(MagicAlchemyEntity animatable) {
        return animationResource;
    }
}
