package net.offkung.bhspells.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class BlinkLeafParticle extends TextureSheetParticle {
    private final SpriteSet sprites;
    private final int blinkInterval;

    public BlinkLeafParticle(ClientLevel level, double xCoord, double yCoord, double zCoord, SpriteSet spriteSet, BlinkLeafParticleOptions options, double xd, double yd, double zd) {
        super(level, xCoord, yCoord, zCoord, xd, yd, zd);
        this.xd = xd;
        this.yd = yd;
        this.zd = zd;
        this.quadSize *= 0.6F;
        this.friction -= (float) (this.random.nextFloat() * .1);
        this.lifetime = options.getLifetime();
        this.blinkInterval = options.getBlinkInterval();
        this.sprites = spriteSet;
        this.setSpriteFromAge(spriteSet);
        this.gravity = -0.01F;
        this.setColor(options.getR(), options.getG(), options.getB());
    }

    @Override
    public void tick() {
        super.tick();
        this.xd += this.random.nextFloat() / 500.0F * (float) (this.random.nextBoolean() ? 1 : -1);
        this.zd += this.random.nextFloat() / 500.0F * (float) (this.random.nextBoolean() ? 1 : -1);

        animateContinuously();
        updateBlinkAlpha();
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<BlinkLeafParticleOptions> {
        private final SpriteSet sprites;

        public Provider(SpriteSet spriteSet) {
            this.sprites = spriteSet;
        }

        @Override
        public Particle createParticle(BlinkLeafParticleOptions options, ClientLevel level, double x, double y, double z, double dx, double dy, double dz) {
            return new BlinkLeafParticle(level, x, y, z, this.sprites, options, dx, dy, dz);
        }
    }

    @Override
    public int getLightColor(float pPartialTick) {
        return LightTexture.FULL_BRIGHT;
    }

    private void animateContinuously() {
        if (age % 8 == 0) {
            setSprite(sprites.get(this.random));
        }
    }

    private void updateBlinkAlpha() {
        float progress = (float) (age % blinkInterval) / (float) blinkInterval;
        float alpha = Mth.sin(progress * (float) Math.PI);
        this.setAlpha(alpha);
    }
}
