package net.offkung.bhspells.entity.spells.eternal_purification;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class PurificationPillarEntityModel extends GeoModel<PurificationPillarEntity> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/purification_pillar.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/purification_pillar/purification_pillar.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/purification_pillar.animation.json");

    public ResourceLocation getModelResource(PurificationPillarEntity object) {
        return modelResource;
    }

    public ResourceLocation getTextureResource(PurificationPillarEntity object) {
        return textureResource;
    }

    public ResourceLocation getAnimationResource(PurificationPillarEntity animatable) {
        return animationResource;
    }
}
