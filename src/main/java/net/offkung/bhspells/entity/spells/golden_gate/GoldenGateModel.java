package net.offkung.bhspells.entity.spells.golden_gate;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class GoldenGateModel extends GeoModel<GoldenGateEntity> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/golden_gate_entity.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/golden_gate/golden_gate.png");
    public static final ResourceLocation animationResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animations/golden_gate.animation.json");

    @Override
    public ResourceLocation getModelResource(GoldenGateEntity object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(GoldenGateEntity object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(GoldenGateEntity animatable) {
        return animationResource;
    }
}
