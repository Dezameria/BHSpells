package net.offkung.bhspells.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

public class FallingLeafParticle extends TextureSheetParticle {
    FallingLeafParticle(ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, Vector3f color, float scale) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.setColor(color.x(), color.y(), color.z());
        this.quadSize *= scale;
        this.friction = 0.98F;
        this.gravity = 0.3F;
        this.lifetime = 60 + this.random.nextInt(40);
        this.hasPhysics = true;
    }

    @Override
    public void tick() {
        super.tick();
        // Gentle side-to-side sway so the leaf doesn't fall in a perfectly straight line
        this.xd += (this.random.nextFloat() - 0.5F) * 0.01F;
        this.zd += (this.random.nextFloat() - 0.5F) * 0.01F;
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_OPAQUE;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<FallingLeafParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        public Particle createParticle(FallingLeafParticleOption options, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            FallingLeafParticle particle = new FallingLeafParticle(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, options.getColor(), options.getScale());
            particle.pickSprite(this.sprite);
            return particle;
        }
    }
}
