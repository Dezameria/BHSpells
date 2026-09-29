package net.offkung.bhspells.entity.spells.crimson_thornbind;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class CrimsonRootModel extends GeoModel<CrimsonRootEntity> {
    private static final ResourceLocation TEXTURE = BHSpells.id("textures/entity/crimson_root/crimson_root.png");
    private static final ResourceLocation MODEL = BHSpells.id("geo/crimson_root.geo.json");
    public static final ResourceLocation ANIMS = BHSpells.id("animations/crimson_root_animations.json");

    @Override
    public ResourceLocation getTextureResource(CrimsonRootEntity object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(CrimsonRootEntity object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(CrimsonRootEntity animatable) {
        return ANIMS;
    }
}
