package net.offkung.bhspells.client.particle;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.ParticleRenderType;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.client.particle.TextureSheetParticle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;

/**
 * Direct port of Wukong's {@code DingEntityAfterImageParticle}, registered in
 * this mod simply as {@code ding}. The entity id is packed in x-speed.
 * The glyph itself is spawned once above the target's head and is discarded
 * when that entity is no longer present or has died.
 */
public final class DingEntityAfterImageParticle extends TextureSheetParticle {
    private final int entityId;

    private DingEntityAfterImageParticle(ClientLevel level, double x, double y, double z,
                                         SpriteSet sprites, int entityId) {
        super(level, x, y, z);
        this.entityId = entityId;
        this.setSize(2.5F, 2.5F);
        this.quadSize *= 2.85F;
        this.lifetime = 100;
        this.gravity = 0.0F;
        this.hasPhysics = false;
        this.setColor(1.0F, 1.0F, 1.0F);
        this.pickSprite(sprites);
    }

    @Override
    public boolean shouldCull() {
        return false;
    }

    @Override
    public int getLightColor(float partialTick) {
        return 15728880;
    }

    @Override
    public void tick() {
        super.tick();
        Entity entity = this.level.getEntity(this.entityId);
        if (!(entity instanceof LivingEntity living) || living.getHealth() == 0.0F) {
            this.remove();
        }
    }

    @Override
    public ParticleRenderType getRenderType() {
        return ParticleRenderType.PARTICLE_SHEET_LIT;
    }

    public static final class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet sprites;

        public Provider(SpriteSet sprites) {
            this.sprites = sprites;
        }

        @Override
        public Particle createParticle(SimpleParticleType type, ClientLevel level, double x, double y, double z,
                                       double xSpeed, double ySpeed, double zSpeed) {
            int entityId = (int) Double.doubleToLongBits(xSpeed);
            Entity entity = level.getEntity(entityId);
            if (!(entity instanceof LivingEntity living) || living.getHealth() == 0.0F) {
                return null;
            }
            return new DingEntityAfterImageParticle(level, x, y, z, this.sprites, entityId);
        }
    }
}
