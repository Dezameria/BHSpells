package net.offkung.bhspells.entity.spells.jade_cluster;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class JadeClusterEntityModel extends GeoModel<JadeClusterEntity> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/jade_cluster.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/jade_cluster/jade_cluster.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/jade_cluster.animation.json");

    public ResourceLocation getModelResource(JadeClusterEntity object) {
        return modelResource;
    }

    public ResourceLocation getTextureResource(JadeClusterEntity object) {
        return textureResource;
    }

    public ResourceLocation getAnimationResource(JadeClusterEntity animatable) {
        return animationResource;
    }
}
