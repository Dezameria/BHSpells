package net.offkung.bhspells.entity.spells.gale_drive;

import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class GaleDriveVortexModel extends GeoModel<GaleDriveVortexEntity> {
    private static final ResourceLocation MODEL = BHSpells.id("geo/gale_vortex.geo.json");
    private static final ResourceLocation TEXTURE = BHSpells.id("textures/entity/gale_vortex/gale_vortex.png");
    private static final ResourceLocation ANIMATION = BHSpells.id("animations/gale_vortex.animation.json");

    @Override
    public ResourceLocation getModelResource(GaleDriveVortexEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(GaleDriveVortexEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(GaleDriveVortexEntity animatable) {
        return ANIMATION;
    }
}
