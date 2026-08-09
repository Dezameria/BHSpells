package net.offkung.bhspells.entity.spells.stone_crumble;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class StoneCrumbleRenderer extends GeoEntityRenderer<StoneCrumbleProjectile> {
    public StoneCrumbleRenderer(EntityRendererProvider.Context context) {
        super(context, new StoneCrumbleModel());
        this.shadowRadius = 1.5f;
    }
}
