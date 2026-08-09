package net.offkung.bhspells.entity.spells.thousand_arrows;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class SkyArrowProjectile extends AbstractMagicProjectile {
    private Vec3 destination = Vec3.ZERO;
    private float perArrowDamage = 4.0f;

    public SkyArrowProjectile(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public SkyArrowProjectile(Level level, LivingEntity owner) {
        this(EntityRegistry.SKY_ARROW.get(), level);
        this.setOwner(owner);
    }

    public void setDestination(Vec3 destination) {
        this.destination = destination;
    }

    public void setPerArrowDamage(float damage) {
        this.perArrowDamage = damage;
    }

    @Override
    public void shoot(Vec3 motion) {
        this.setDeltaMovement(motion);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && (this.position().distanceToSqr(destination) < 1.5 || this.tickCount > 100)) {
            explode();
        }
    }

    private void explode() {
        Vec3 pos = this.position();
        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;

        MagicManager.spawnParticles(level(), ParticleTypes.GLOW, pos.x, pos.y, pos.z, 50, 0.6, 0.6, 0.6, 0.06, false);
        level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.BEACON_ACTIVATE, SoundSource.NEUTRAL, 2.0f, 1.3f);

        HitResult groundHit = level().clip(new ClipContext(pos, pos.subtract(0, 64, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        double groundY = groundHit.getLocation().y;
        Vec3 spawnPos = new Vec3(pos.x, groundY + 20.0, pos.z);

        MagicAlchemyEntity alchemy = new MagicAlchemyEntity(level());
        alchemy.setPos(spawnPos.x, spawnPos.y - 7, spawnPos.z);
        alchemy.setOwner(owner);
        alchemy.setDuration(160);
        level().addFreshEntity(alchemy);

        applyPerplexityToGroundEnemies(pos.x, groundY, pos.z, owner);

        RainVolleyEntity rain = new RainVolleyEntity(level(), owner);
        rain.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
        rain.setPerArrowDamage(this.perArrowDamage);
        level().addFreshEntity(rain);

        this.discard();
    }

    private void applyPerplexityToGroundEnemies(double x, double groundY, double z, @Nullable LivingEntity owner) {
        double radius = 12.0;
        AABB area = new AABB(x - radius, groundY - 1.0, z - radius, x + radius, groundY + 3.0, z + radius);

        for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, area)) {
            if (entity != owner && entity.isAlive()) {
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.PERPLEXITY.get(), 140, 0, false, true, true));
            }
        }
    }

    @Override
    public void trailParticles() {
        this.level().addParticle(ParticleTypes.GLOW, true, this.getX(), this.getY(), this.getZ(), 0, 0, 0);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 1.0f;
    }

    @Override
    protected void onHit(HitResult hitResult) {
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }
}
