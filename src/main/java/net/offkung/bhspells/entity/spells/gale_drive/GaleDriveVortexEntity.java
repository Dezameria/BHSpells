package net.offkung.bhspells.entity.spells.gale_drive;

import com.github.L_Ender.cataclysm.client.particle.StormParticle;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import reascer.wom.particle.WOMParticles;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class GaleDriveVortexEntity extends AoeEntity implements GeoEntity, AntiMagicSusceptible {
    private LivingEntity capturedTarget;
    private UUID capturedTargetUUID;
    private double groundY;

    private final RawAnimation SPIN_ANIMATION = RawAnimation.begin().thenLoop("animation.vortex.idle");
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public GaleDriveVortexEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setCircular();
        this.setRadius(10.0f);
        this.setDuration(140);
        this.reapplicationDelay = 20;
    }

    public GaleDriveVortexEntity(Level level) {
        this(EntityRegistry.GALE_DRIVE_VORTEX.get(), level);
    }

    public void setCapturedTarget(LivingEntity target) {
        this.capturedTarget = target;
        if (target != null) {
            this.capturedTargetUUID = target.getUUID();
            this.groundY = target.getY();
        }
    }

    @Override
    public void tick() {
        if (!this.level().isClientSide) {
            if (this.capturedTarget == null && this.capturedTargetUUID != null && this.level() instanceof ServerLevel serverLevel) {
                Entity found = serverLevel.getEntity(this.capturedTargetUUID);
                if (found instanceof LivingEntity living) {
                    this.capturedTarget = living;
                }
            }

            if (this.capturedTarget != null && this.capturedTarget.isAlive() && !this.capturedTarget.isRemoved()) {
                double targetY = this.groundY + 6.0;
                if (this.capturedTarget instanceof ServerPlayer player) {
                    double dx = this.getX() - player.getX();
                    double dy = targetY - player.getY();
                    double dz = this.getZ() - player.getZ();
                    player.setDeltaMovement(new Vec3(dx, dy, dz).scale(0.35));
                    player.hurtMarked = true;
                } else {
                    this.capturedTarget.teleportTo(this.getX(), targetY, this.getZ());
                    this.capturedTarget.setDeltaMovement(0, 0, 0);
                }
                this.capturedTarget.fallDistance = 6.0f;

                if (this.tickCount > 0 && this.tickCount % 20 == 0) {
                    SpellDamageSource source = BHSpellRegistry.GALE_DRIVE.get().getDamageSource(this, this.getOwner());
                    DamageSources.applyDamage(this.capturedTarget, 2.0f, source);
                    DamageSources.ignoreNextKnockback(this.capturedTarget);

                    if (this.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(WOMParticles.SHARPCUT_SLASH.get(),
                                this.capturedTarget.getX(),
                                this.capturedTarget.getY() + this.capturedTarget.getBbHeight() / 2.0,
                                this.capturedTarget.getZ(),
                                1, 0.0, 0.0, 0.0, 0.0);
                    }

                    this.level().playSound(null, this.capturedTarget.getX(), this.capturedTarget.getY(),
                            this.capturedTarget.getZ(),
                            SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.5f, 1.2f);
                }
            }

            double radius = getRadius();
            double radiusSqr = radius * radius;
            AABB searchBox = this.getBoundingBox().inflate(radius, 6.0, radius);
            List<LivingEntity> nearbyEntities = this.level().getEntitiesOfClass(LivingEntity.class, searchBox);

            for (LivingEntity other : nearbyEntities) {
                if (other != this.capturedTarget && other != this.getOwner() && other.isAlive() && !DamageSources.isFriendlyFireBetween(this.getOwner(), other)) {
                    double dx = this.getX() - other.getX();
                    double dz = this.getZ() - other.getZ();
                    double distSq = dx * dx + dz * dz;

                    if (distSq > 0.5 && distSq <= radiusSqr) {
                        double dist = Math.sqrt(distSq);
                        double pullFactor = 0.16;
                        double tangentFactor = 0.10;
                        double pullX = (dx / dist) * pullFactor;
                        double pullZ = (dz / dist) * pullFactor;
                        double tanX = (-dz / dist) * tangentFactor;
                        double tanZ = (dx / dist) * tangentFactor;

                        Vec3 currentMotion = other.getDeltaMovement();
                        other.setDeltaMovement(currentMotion.x + pullX + tanX, currentMotion.y * 0.5 + 0.03, currentMotion.z + pullZ + tanZ);
                        other.hurtMarked = true;
                    }
                }
            }

            if (this.tickCount % 40 == 0) {
                this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.ELYTRA_FLYING, SoundSource.PLAYERS, 0.5f, 1.4f);
            }

            if (this.tickCount >= this.getDuration()) {
                releaseTarget(true);
                this.discard();
                return;
            }
        }

        super.tick();
    }

    private void releaseTarget(boolean applyFallPenalty) {
        if (this.capturedTarget != null && this.capturedTarget.isAlive() && !this.capturedTarget.isRemoved()) {
            this.capturedTarget.setDeltaMovement(0, -0.8, 0);
            this.capturedTarget.hurtMarked = true;
            this.capturedTarget.fallDistance = 6.0f; // รับ Fall Damage จากความสูง 6 บล็อก
            if (applyFallPenalty) {
                this.capturedTarget.addEffect(new MobEffectInstance(MobEffectsRegistry.GALE_FALL_IMPACT.get(), 100, 0, false, false, false));
            }
        }
    }

    @Override
    public void ambientParticles() {
        if (!this.level().isClientSide) {
            return;
        }

        float radius = getRadius();

        if (this.tickCount % 2 == 0) {
            int stormCount = 2 + this.random.nextInt(2);
            for (int s = 0; s < stormCount; s++) {
                float orbRadius = 1.0f + this.random.nextFloat() * (radius - 1.5f);
                float orbHeight = 0.2f + this.random.nextFloat() * 4.5f;
                this.level().addParticle(new StormParticle.OrbData(0.70f, 0.82f, 0.90f, orbRadius, orbHeight, this.getId()), this.getX(), this.getY(), this.getZ(), 0, 0, 0);
            }
        }

        int particleSpawns = 6;
        for (int i = 0; i < particleSpawns; i++) {
            float r = 1.5f + this.random.nextFloat() * (radius - 1.5f);
            float angle = this.random.nextFloat() * 6.2831855f;
            double px = this.getX() + r * Mth.cos(angle);
            double py = this.getY() + 0.2 + this.random.nextDouble() * 3.5;
            double pz = this.getZ() + r * Mth.sin(angle);

            double tangSpeed = 0.22 + this.random.nextDouble() * 0.12;
            double tangX = -Mth.sin(angle) * tangSpeed;
            double tangZ = Mth.cos(angle) * tangSpeed;

            double inwardSpeed = 0.12 + this.random.nextDouble() * 0.08;
            double inwardX = -Mth.cos(angle) * inwardSpeed;
            double inwardZ = -Mth.sin(angle) * inwardSpeed;

            double vy = 0.06 + this.random.nextDouble() * 0.08;

            this.level().addParticle(ParticleTypes.CLOUD, px, py, pz, tangX + inwardX, vy, tangZ + inwardZ);
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    public float getParticleCount() {
        return 0.5f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        releaseTarget(false);
        this.discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, "controller", 0, state -> {
            state.getController().setAnimation(SPIN_ANIMATION);
            return PlayState.CONTINUE;
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.capturedTargetUUID != null) {
            tag.putUUID("CapturedTargetUUID", this.capturedTargetUUID);
        }
        tag.putDouble("GroundY", this.groundY);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("CapturedTargetUUID")) {
            this.capturedTargetUUID = tag.getUUID("CapturedTargetUUID");
        }
        this.groundY = tag.getDouble("GroundY");
    }
}
