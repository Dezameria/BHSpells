package net.offkung.bhspells.entity.spells.stone_crumble;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;
import software.bernie.geckolib.model.GeoModel;

public class StoneCrumbleModel extends GeoModel<StoneCrumbleProjectile> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/stone_block.png");
    private static final ResourceLocation MODEL = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "geo/ice_block_projectile.geo.json");
    public static final ResourceLocation ANIMS = ResourceLocation.fromNamespaceAndPath(IronsSpellbooks.MODID, "animations/ice_block_animations.json");

    public StoneCrumbleModel() {
    }

    @Override
    public ResourceLocation getTextureResource(StoneCrumbleProjectile object) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getModelResource(StoneCrumbleProjectile object) {
        return MODEL;
    }

    @Override
    public ResourceLocation getAnimationResource(StoneCrumbleProjectile animatable) {
        return ANIMS;
    }
}
