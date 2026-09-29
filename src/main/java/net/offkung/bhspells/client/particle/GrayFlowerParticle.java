package net.offkung.bhspells.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.*;
import net.minecraft.util.RandomSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

public class GrayFlowerParticle extends TextureSheetParticle {
    private final float baseSize;
    private float pulseSpeed;
    private final float rotationSpeed;

    protected GrayFlowerParticle(ClientLevel pLevel, SpriteSet pSpriteSet, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed, Vector3f color, float scale) {
        super(pLevel, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed);
        this.setSprite(pSpriteSet.get(this.random.nextInt(12), 12));
        if (color != null) {
            this.setColor(color.x(), color.y(), color.z());
        }
        float baseScale = scale > 0 ? scale : 1.0F;
        float size = (this.random.nextBoolean() ? 0.20F : 0.28F) * baseScale;
        this.baseSize = size;
        this.quadSize = size;
        this.setSize(size, size);
        this.lifetime = (int)((double)16.0F / (Math.random() * 0.6 + 0.4));
        this.hasPhysics = false;
        this.friction = 0.96F;
        this.gravity = 8.0E-5F;
        this.pulseSpeed = this.random.nextFloat() * 0.1F + 0.05F;
        this.rotationSpeed = (float)Math.toRadians(this.random.nextBoolean() ? (double)-40.0F : (double)40.0F);
        this.alpha = 1.0F;
    }

    protected GrayFlowerParticle(ClientLevel pLevel, SpriteSet pSpriteSet, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
        this(pLevel, pSpriteSet, pX, pY, pZ, pXSpeed, pYSpeed, pZSpeed, new Vector3f(1.0F, 1.0F, 1.0F), 1.0F);
    }

    public @NotNull ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_TRANSLUCENT;
    }

    public int getLightColor(float partialTick) {
        return 15728880;
    }

    public void tick() {
        super.tick();
        this.oRoll = this.roll;
        this.roll += this.rotationSpeed / 20.0F;
        float pulse = (float)Math.sin((float)this.age * this.pulseSpeed) * 0.15F + 1.0F;
        this.quadSize = this.baseSize * pulse;
        this.alpha = 0.9F + (float)Math.sin((float)this.age * this.pulseSpeed * 1.5F) * 0.1F;
        if (this.age > this.lifetime - 10) {
            this.alpha = (float)(this.lifetime - this.age) / 10.0F;
            float shrink = (float)(this.lifetime - this.age) / 10.0F;
            this.quadSize = this.baseSize * pulse * (0.3F + shrink * 0.7F);
        }

        this.move(this.xd, this.yd, this.zd);
        this.xd *= this.friction;
        this.yd = this.yd * (double)this.friction - (double)this.gravity;
        this.zd *= this.friction;
    }

    public boolean shouldCull() {
        return false;
    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<GrayFlowerParticleOption> {
        private final SpriteSet sprite;

        public Provider(SpriteSet pSprites) {
            this.sprite = pSprites;
        }

        @Override
        public Particle createParticle(@NotNull GrayFlowerParticleOption pOption, ClientLevel pLevel, double pX, double pY, double pZ, double pXSpeed, double pYSpeed, double pZSpeed) {
            RandomSource random = pLevel.random;
            double dx = (pXSpeed != 0.0) ? pXSpeed : (random.nextDouble() - (double)0.5F) * 0.003;
            double dy = (pYSpeed != 0.0) ? pYSpeed : (random.nextDouble() - 0.4) * 0.002;
            double dz = (pZSpeed != 0.0) ? pZSpeed : (random.nextDouble() - (double)0.5F) * 0.003;
            GrayFlowerParticle particle = new GrayFlowerParticle(pLevel, this.sprite, pX, pY, pZ, dx, dy, dz, pOption.getColor(), pOption.getScale());
            particle.pulseSpeed = random.nextFloat() * 0.12F + 0.06F;
            return particle;
        }
    }
}
