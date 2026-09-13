package net.offkung.bhspells.entity.spells.thousand_arrows;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import com.github.L_Ender.cataclysm.init.ModEffect;
import io.redspace.ironsspellbooks.api.entity.NoKnockbackProjectile;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import yesman.epicfight.api.utils.LevelUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class HugeArrowEntity extends AbstractMagicProjectile implements NoKnockbackProjectile {
    private boolean landed = false;
    private int landedTicks = 0;
    private float landedYRot;
    private float landedXRot;
    private Vec3 landedMotion = Vec3.ZERO;
    private static final int DISCARD_DELAY_TICKS = 60;

    private Map<UUID, Integer> targetStacks = Map.of();

    public HugeArrowEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public HugeArrowEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.HUGE_ARROW.get(), level);
        this.setOwner(owner);
    }

    public void setTargetStacks(Map<UUID, Integer> stacks) {
        this.targetStacks = new HashMap<>(stacks);
    }

    private float getDamageForStacks(int stackCount) {
        return switch (stackCount) {
            case 1 -> 20.0f;
            case 2 -> 40.0f;
            case 3 -> 60.0f;
            default -> 0.0f;
        };
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        Entity owner = getOwner();
        if (pTarget == owner) {
            return false;
        }
        if (owner != null && owner.isPassengerOfSameVehicle(pTarget)) {
            return false;
        }
        return pTarget.canBeHitByProjectile() && !pTarget.isSpectator();
    }

    @Override
    public void tick() {
        super.tick();

        if (!level().isClientSide && landed) {
            this.setYRot(landedYRot);
            this.setXRot(landedXRot);
            this.setDeltaMovement(landedMotion);

            landedTicks++;
            if (landedTicks >= DISCARD_DELAY_TICKS) {
                this.discard();
            }
        }
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (level().isClientSide) return;

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            handleEntityImpact((EntityHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            handleGroundImpact(hitResult);
        }
    }

    private void handleEntityImpact(EntityHitResult entityHitResult) {
        Vec3 pos = entityHitResult.getLocation();
        applyAoeDamage(pos);
        spawnImpactEffects(pos);
    }

    private void handleGroundImpact(HitResult hitResult) {
        if (landed) return;
        landed = true;
        landedTicks = 0;

        Vec3 pos = hitResult.getLocation();
        applyAoeDamage(pos);
        spawnImpactEffects(pos);

        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;
        // Nudge below pos: a block-top hit location has an exact integer Y, which makes
        // LevelUtil.circleSlamFracture's internal flooring resolve to the air block above
        // the ground instead of the ground itself, silently no-oping the fracture.
        Vec3 fractureCenter = new Vec3(pos.x, pos.y - 0.1, pos.z);
        LevelUtil.circleSlamFracture(owner, level(), fractureCenter, 6.0, false, false, false);
        TOScreenShakeEntity.createScreenShake(this.level(), pos, 12.0F, 0.07F, 10, 0, 5, true);

        this.landedYRot = this.getYRot();
        this.landedXRot = this.getXRot();

        Vec3 lastMotion = this.getDeltaMovement();
        if (lastMotion.lengthSqr() > 0) {
            this.landedMotion = lastMotion.normalize().scale(0.001);
        } else {
            this.landedMotion = new Vec3(0, -0.001, 0);
        }
        this.setDeltaMovement(landedMotion);
        this.setYRot(landedYRot);
        this.setXRot(landedXRot);
        this.yRotO = landedYRot;
        this.xRotO = landedXRot;

        this.setPos(pos.x, pos.y, pos.z);
    }

    private void applyAoeDamage(Vec3 pos) {
        float radius = 12.0f;
        float radiusSqr = radius * radius;
        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;

        for (Entity entity : level().getEntities(this, this.getBoundingBox().inflate(radius))) {
            if (entity != owner && entity instanceof LivingEntity livingEntity && entity.distanceToSqr(pos) <= radiusSqr) {
                int stackCount = targetStacks.getOrDefault(entity.getUUID(), 0);
                if (stackCount > 0) {
                    DamageSources.ignoreNextKnockback(livingEntity);
                    float dmg = getDamageForStacks(stackCount);
                    DamageSource damageSource;
                    if (owner != null && (owner.isAlliedTo(entity) || entity.isAlliedTo(owner) || DamageSources.isFriendlyFireBetween(owner, entity))) {
                        damageSource = SpellDamageSource.source(this, this, BHSpellRegistry.THOUSAND_ARROWS.get()).setIFrames(0);
                    } else {
                        damageSource = BHSpellRegistry.THOUSAND_ARROWS.get().getDamageSource(this, owner).setIFrames(0);
                    }
                    DamageSources.applyDamage(entity, dmg, damageSource);
                    if (stackCount >= 3) {
                        livingEntity.addEffect(new MobEffectInstance(ModEffect.EFFECTSTUN.get(), 60, 0, false, true, true));
                    }
                }
            }
        }
    }

    private void spawnImpactEffects(Vec3 pos) {
        MagicManager.spawnParticles(level(), ParticleTypes.EXPLOSION_EMITTER, pos.x, pos.y, pos.z, 10, 6, 0, 6, 0, false);
        MagicManager.spawnParticles(level(), ParticleTypes.GLOW, pos.x, pos.y, pos.z, 60, 1.0, 1.0, 1.0, 0.1, false);
        level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.GENERIC_EXPLODE, SoundSource.NEUTRAL, 4.0f, 0.8f);
    }

    @Override
    public void shoot(Vec3 motion) {
        this.setDeltaMovement(motion);
    }

    @Override
    public void trailParticles() {
        Vec3 pos = this.position();
        for (int i = 0; i < 3; i++) {
            level().addParticle(ParticleTypes.GLOW, pos.x + (this.random.nextDouble() - 0.5) * 0.3, pos.y + (this.random.nextDouble() - 0.5) * 0.3, pos.z + (this.random.nextDouble() - 0.5) * 0.3, 0.0, 0.0, 0.0);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 2.5f;
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.of(() -> SoundEvents.GENERIC_EXPLODE);
    }
}
