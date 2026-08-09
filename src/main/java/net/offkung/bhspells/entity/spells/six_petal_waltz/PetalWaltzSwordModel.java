package net.offkung.bhspells.entity.spells.six_petal_waltz;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class PetalWaltzSwordModel extends GeoModel<PetalWaltzSword> {
    public static final ResourceLocation modelResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "geo/petal_waltz_sword.geo.json");
    public static final ResourceLocation textureResource = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/petal_waltz_sword/petal_waltz_sword.png");

    public PetalWaltzSwordModel() {
    }

    @Override
    public ResourceLocation getModelResource(PetalWaltzSword object) {
        return modelResource;
    }

    @Override
    public ResourceLocation getTextureResource(PetalWaltzSword object) {
        return textureResource;
    }

    @Override
    public ResourceLocation getAnimationResource(PetalWaltzSword animatable) {
        return null;
    }
}
