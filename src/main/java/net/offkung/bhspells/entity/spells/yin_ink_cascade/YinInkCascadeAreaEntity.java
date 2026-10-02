package net.offkung.bhspells.entity.spells.yin_ink_cascade;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;

import java.util.List;
import java.util.Optional;

public class YinInkCascadeAreaEntity extends AoeEntity implements AntiMagicSusceptible {
    public static final float RADIUS = 30.0F;
    public static final int DURATION = 100;
    public static final int WITHER_DURATION = 280; // 14 seconds

    private float explosionDamage = 25.0F;
    private boolean exploded = false;

    public YinInkCascadeAreaEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setRadius(RADIUS);
        this.setCircular();
        this.duration = DURATION;
    }

    public YinInkCascadeAreaEntity(Level level) {
        this(EntityRegistry.YIN_INK_CASCADE_AREA.get(), level);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 4, 0);
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public float getParticleCount() {
        return 0.0F;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void applyEffect(LivingEntity target) {
        // Custom effect logic is handled in tick() for precise timing and phase transitions
    }

    @Override
    public void tick() {
        LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;

        if (!this.level().isClientSide) {
            // Discard without explosion if the caster is dead or disconnected
            if (owner == null || !owner.isAlive() || !owner.level().dimension().equals(this.level().dimension())) {
                this.discard();
                return;
            }
        }

        super.tick();

        if (!this.level().isClientSide) {
            // Phase 1 (Ticks 0–99): Grey water surge area with continuous Slowness II
            if (this.tickCount < this.duration) {
                if (this.tickCount % 5 == 0) {
                    applySurgeDebuffs(owner);
                }
                if (this.tickCount == 1) {
                    this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 1.5F, 0.8F);
                    this.level().playSound(null, getX(), getY(), getZ(), BHSoundRegistry.WATER_BUBBLE.get(), SoundSource.PLAYERS, 1.5F, 1.0F);
                }
            }

            // Phase 2 (Tick 100): Finale water explosion and Wither debuff (Effekseer handles all visuals)
            if (this.tickCount >= this.duration && !this.exploded) {
                this.exploded = true;
                explodeFinale(owner);
                this.discard();
            }
        }
    }

    private void applySurgeDebuffs(LivingEntity owner) {
        double radiusSqr = RADIUS * RADIUS;
        AABB searchBox = this.getBoundingBox().inflate(RADIUS, 4.0, RADIUS);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity target : targets) {
            if (isValidTarget(owner, target) && target.distanceToSqr(this) <= radiusSqr) {
                // Slowness II for 25 ticks, constantly refreshed while inside zone
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 25, 1, false, false, true));
            }
        }
    }

    private void explodeFinale(LivingEntity owner) {
        // Explosion audio
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 2.0F, 0.8F);
        this.level().playSound(null, getX(), getY(), getZ(), SoundEvents.GENERIC_SPLASH, SoundSource.PLAYERS, 2.0F, 0.5F);

        // Explosion damage + Wither to enemies (pure Effekseer, no vanilla particle clutter)
        double radiusSqr = RADIUS * RADIUS;
        AABB searchBox = this.getBoundingBox().inflate(RADIUS, 6.0, RADIUS);
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);
        for (LivingEntity target : targets) {
            if (isValidTarget(owner, target) && target.distanceToSqr(this) <= radiusSqr) {
                DamageSources.applyDamage(target, this.explosionDamage, BHSpellRegistry.YIN_INK_CASCADE.get().getDamageSource(this, owner));
                target.addEffect(new MobEffectInstance(MobEffects.WITHER, WITHER_DURATION, 0, false, true, true));
            }
        }
    }

    private boolean isValidTarget(LivingEntity owner, LivingEntity target) {
        if (target == null || !target.isAlive() || target.isSpectator()) {
            return false;
        }
        if (owner != null && (target == owner || target.is(owner) || target.getUUID().equals(owner.getUUID()))) {
            return false;
        }
        if (owner != null) {
            if (owner.isAlliedTo(target)) {
                return false;
            }
            if (DamageSources.isFriendlyFireBetween(owner, target)) {
                return false;
            }
        }
        return true;
    }

    public void setExplosionDamage(float explosionDamage) {
        this.explosionDamage = explosionDamage;
    }

    public float getExplosionDamage() {
        return this.explosionDamage;
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putFloat("ExplosionDamage", this.explosionDamage);
        compound.putBoolean("Exploded", this.exploded);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("ExplosionDamage")) {
            this.explosionDamage = compound.getFloat("ExplosionDamage");
        }
        if (compound.contains("Exploded")) {
            this.exploded = compound.getBoolean("Exploded");
        }
    }
}
