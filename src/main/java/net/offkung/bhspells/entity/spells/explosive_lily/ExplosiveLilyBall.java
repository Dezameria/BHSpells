package net.offkung.bhspells.entity.spells.explosive_lily;

import com.gametechbc.traveloptics.init.TravelopticsSounds;
import dev.kosmx.playerAnim.core.util.Vector3;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.BlinkLeafParticleOptions;
import net.offkung.bhspells.client.particle.ColoredEndRodParticleOption;
import net.offkung.bhspells.client.particle.GreenCatParticleOption;
import net.offkung.bhspells.event.PoisonSwirlManager;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.offkung.bhspells.util.BHParticleHelper;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Optional;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Supplier;

public class ExplosiveLilyBall extends AbstractMagicProjectile {
    public static final int lifetime = 100;

    private static final int DAMAGE_ATTACH_DURATION = 60;
    private static final int HEAL_ATTACH_DURATION = 160;
    private static final int HEAL_INTERVAL = 40;

    public enum Mode {
        DAMAGE, HEAL
    }

    private static final EntityDataAccessor<Boolean> ATTACHED = SynchedEntityData.defineId(ExplosiveLilyBall.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> HEALING_MODE = SynchedEntityData.defineId(ExplosiveLilyBall.class, EntityDataSerializers.BOOLEAN);
    private static final ParticleEmitterInfo GREEN_RECOVER = new ParticleEmitterInfo(BHSpells.id("green_recover"));

    private UUID targetUUID;
    private Entity cachedTarget;
    private boolean attached = false;
    private int attachTicks = 0;
    private boolean exploded = false;
    private int bounces;

    private float periodicHealAmount = 10.0f;
    private float explosionHealAmount = 30.0f;

    public ExplosiveLilyBall(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public ExplosiveLilyBall(Level level, LivingEntity shooter) {
        this(EntityRegistry.EXPLOSIVE_LILY_BALL.get(), level);
        setOwner(shooter);
    }

    public ExplosiveLilyBall(Level level, LivingEntity shooter, Entity target) {
        this(level, shooter);
        setTarget(target);
    }

    public ExplosiveLilyBall(Level level, LivingEntity shooter, Entity target, Mode mode) {
        this(level, shooter, target);
        setMode(mode);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACHED, false);
        this.entityData.define(HEALING_MODE, false);
    }

    public void setMode(Mode mode) {
        this.entityData.set(HEALING_MODE, mode == Mode.HEAL);
    }

    public boolean isHealing() {
        return this.entityData.get(HEALING_MODE);
    }

    public void setPeriodicHealAmount(float amount) {
        this.periodicHealAmount = amount;
    }

    public void setExplosionHealAmount(float amount) {
        this.explosionHealAmount = amount;
    }

    public Mode getMode() {
        return this.entityData.get(HEALING_MODE) ? Mode.HEAL : Mode.DAMAGE;
    }

    private int getAttachDuration() {
        return isHealing() ? HEAL_ATTACH_DURATION : DAMAGE_ATTACH_DURATION;
    }

    public void setTarget(@Nullable Entity target) {
        if (target != null) {
            this.targetUUID = target.getUUID();
            this.cachedTarget = target;
        }
    }

    @Nullable
    public Entity getTarget() {
        if (this.cachedTarget != null && !this.cachedTarget.isRemoved()) {
            return this.cachedTarget;
        } else if (this.targetUUID != null && this.level() instanceof ServerLevel serverLevel) {
            this.cachedTarget = serverLevel.getEntity(this.targetUUID);
            return this.cachedTarget;
        }
        return null;
    }

    public boolean isAttached() {
        return this.entityData.get(ATTACHED);
    }

    @Override
    public void trailParticles() {
        if (attached || exploded) return;
        Vec3 pos = this.getBoundingBox().getCenter().add(getDeltaMovement());
        Vec3 random = Utils.getRandomVec3(0.28);
        pos = pos.add(getDeltaMovement());
        ColoredEndRodParticleOption greenRod = new ColoredEndRodParticleOption(0.52F, 1.0F, 0.62F);
        level().addParticle(greenRod, pos.x, pos.y, pos.z, random.x, random.y, random.z);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level(), BHParticleHelper.OAK_LEAF, x, y, z, 12, .08, .08, .08, 0.3, false);
    }

    @Override
    public float getSpeed() {
        return 0.6f;
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        return super.canHitEntity(pTarget) && pTarget != getOwner();
    }

    @Override
    public void tick() {
        if (exploded) return;

        if (!attached) {
            Entity target = getTarget();
            if (target != null && target.isAlive()) {
                Vec3 targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
                Vec3 diff = targetPos.subtract(position());
                if (diff.lengthSqr() > 1.0E-4) {
                    Vec3 dir = diff.normalize().scale(getSpeed());
                    setDeltaMovement(dir);
                }
            }
        }

        super.tick();

        if (!attached) {
            if (tickCount > lifetime) {
                discard();
                if (!level().isClientSide) {
                    impactParticles(getX(), this.getBoundingBox().getCenter().y, getZ());
                }
            }
            return;
        }

        Entity target = getTarget();
        Vec3 targetPos = null;
        if (target != null && target.isAlive()) {
            targetPos = target.position().add(0, target.getBbHeight() * 0.5, 0);
            setPos(targetPos);
        }

        if (isHealing()) {
            if (!level().isClientSide && target instanceof LivingEntity livingTarget && livingTarget.isAlive()&& attachTicks > 0 && attachTicks % HEAL_INTERVAL == 0) {
                healEntity(livingTarget, periodicHealAmount);
            }
        } else {
            if (targetPos != null && (level().isClientSide || tickCount % 2 == 0)) {
                Vec3 pPos = targetPos.add((random.nextDouble() - 0.5) * target.getBbWidth(), (random.nextDouble() - 0.5) * target.getBbHeight(), (random.nextDouble() - 0.5) * target.getBbWidth());
                level().addParticle(ParticleRegistry.OAK_LEAF_PARTICLE.get(), pPos.x, pPos.y, pPos.z, 0, 0.02, 0);
            }
        }

        if (!level().isClientSide) {
            attachTicks++;

            int attachDuration = getAttachDuration();
            int secondsLeft = (attachDuration - attachTicks) / 20;
            if (attachTicks % 20 == 0 && secondsLeft > 0 && secondsLeft <= 3) {
                notifyCaster(secondsLeft);
                if (!isHealing() && targetPos != null) {
                    BlinkLeafParticleOptions redBlink = new BlinkLeafParticleOptions(1.0F, 0.0F, 0.0F, 10, 5);
                    MagicManager.spawnParticles(level(), redBlink, targetPos.x, targetPos.y, targetPos.z, 15, 0.3, 0.3, 0.3, 0.05, false);
                }
            }

            if (attachTicks >= attachDuration) {
                explode();
            }
        }
    }


    @Override
    public void handleHitDetection() {
        if (attached || exploded) return;
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 pos = this.position();
        Vec3 vec32 = pos.add(vec3);
        HitResult hitresult = level().clip(new ClipContext(pos, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            onHit(hitresult);
        } else {
            var entities = level().getEntities(this, this.getBoundingBox().inflate(0.25f), this::canHitEntity);
            for (Entity entity : entities) {
                onHit(new EntityHitResult(entity, this.getBoundingBox().getCenter().add(entity.getBoundingBox().getCenter()).scale(0.5f)));
            }
        }
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        if (attached || exploded) return;

        Entity hitEntity = pResult.getEntity();
        Entity target = getTarget();

        if (hitEntity == target || (target == null && canHitEntity(hitEntity))) {
            attach(hitEntity);
        }
    }

    private void attach(Entity entity) {
        attached = true;
        attachTicks = 0;
        setTarget(entity);
        setDeltaMovement(Vec3.ZERO);
        this.entityData.set(ATTACHED, true);

        if (!level().isClientSide) {
            Vec3 pos = position();
            MagicManager.spawnParticles(level(), ParticleRegistry.OAK_LEAF_PARTICLE.get(), pos.x, pos.y, pos.z, 30, 0.4, 0.4, 0.4, 0.05, false);
            BlinkLeafParticleOptions whiteBlink = new BlinkLeafParticleOptions(1.0F, 1.0F, 1.0F, 20, 5);
            MagicManager.spawnParticles(level(), whiteBlink, pos.x, pos.y, pos.z, 15, 0.3, 0.3, 0.3, 0.05, false);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (attached || exploded) return;
        super.onHitBlock(pResult);
        switch (pResult.getDirection()) {
            case UP, DOWN -> this.setDeltaMovement(this.getDeltaMovement().multiply(1, this.isNoGravity() ? -1 : -.8f, 1));
            case EAST, WEST -> this.setDeltaMovement(this.getDeltaMovement().multiply(-1, 1, 1));
            case NORTH, SOUTH -> this.setDeltaMovement(this.getDeltaMovement().multiply(1, 1, -1));
        }
        if (++bounces >= 6) {
            discard();
        }
    }

    private void notifyCaster(int secondsLeft) {
        if (getOwner() instanceof ServerPlayer serverPlayer) {
            String verb = isHealing() ? "§aเบ่งบาน§r" : "§cระเบิด§r";
            serverPlayer.displayClientMessage(Component.literal("ใบบัวจะระเบิด " + verb + " ใน " + secondsLeft + "..."), true);
            serverPlayer.playNotifySound(SoundEvents.UI_BUTTON_CLICK.get(), SoundSource.MASTER, 0.6F, 2.0F);
        }
    }

    private void healEntity(LivingEntity entity, float amount) {
        entity.heal(amount);
        Vec3 pos = entity.position().add(0, entity.getBbHeight() * 0.5, 0);
        AAALevel.addParticle(this.level(), 64.0, GREEN_RECOVER.clone().position(pos.x, pos.y - 0.5, pos.z));
        level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.CONDUIT_ACTIVATE, SoundSource.NEUTRAL, 1, 1);
    }

    private void explode() {
        if (exploded) return;
        exploded = true;

        if (!level().isClientSide) {
            Vec3 center = position();

            if (isHealing()) {
                float radius = 12.0f;
                for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) {
                    if (entity.isSpectator()) continue;
                    float distance = (float) center.distanceTo(entity.position());
                    if (distance > radius) continue;
                    healEntity(entity, explosionHealAmount);
                    ColoredEndRodParticleOption greenRod = new ColoredEndRodParticleOption(0.97F, 0.96F, 0.55F);
                    MagicManager.spawnParticles(level(), greenRod, entity.position().x, entity.position().y, entity.position().z, 30, 0,0, 0, 0.4f, false);
                    MagicManager.spawnParticles(level(), ParticleRegistry.GREEN_CROSS_PARTICLE.get(), entity.position().x, entity.position().y, entity.position().z, 30, 0,0, 0, 0.3f, false);

                    TargetedAreaEntity visualEntity = TargetedAreaEntity.createTargetAreaEntity(level(), entity.position(), 2, 0x85FF9E);
                    visualEntity.setDuration(60);
                    visualEntity.setOwner(entity);
                    visualEntity.setShouldFade(true);
                    level().addFreshEntity(visualEntity);
                }
                level().playSound(null, center.x, center.y, center.z, SoundEvents.PLAYER_LEVELUP, SoundSource.NEUTRAL, 1.5f, 1.2f);
                MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(new Vector3f(0.52F, 1.0F, 0.62F), radius), center.x, center.y - 1.1, center.z, 1, 0, 0, 0, 0, false);
            } else {
                float radius = 8.0f;
                float explosionDamage = getDamage() > 0 ? getDamage() : 20.0f;
                DamageSource damageSource = BHSpellRegistry.EXPLOSIVE_LILY.get().getDamageSource(this, getOwner());

                for (LivingEntity entity : level().getEntitiesOfClass(LivingEntity.class, getBoundingBox().inflate(radius))) {
                    if (entity != getOwner() && !entity.isSpectator() && canHitEntity(entity)) {
                        float distance = (float) center.distanceTo(entity.position());
                        if (distance > radius) continue;
                        DamageSources.applyDamage(entity, explosionDamage, damageSource);
                        entity.addEffect(new MobEffectInstance(MobEffects.POISON, 400, 4, false, false));
                        entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 200, 1, false, false));
                        PoisonSwirlManager.register(entity);
                        MagicManager.spawnParticles(level(), ParticleRegistry.OAK_LEAF_PARTICLE.get(), entity.position().x, entity.position().y, entity.position().z, 60, 0.0, 0.0, 0.0, 0.2, false);
                        DustColorTransitionOptions greenDust = new DustColorTransitionOptions(new Vector3f(0.52f, 1f, 0.62f), new Vector3f(0.97f, 0.96f, 0.55f), 4f);
                        MagicManager.spawnParticles(level(), greenDust, entity.position().x, entity.position().y + 1, entity.position().z, 60, 0,0, 0, 0.25f, false);
                        GreenCatParticleOption cat = new GreenCatParticleOption(new Vector3f(0.52f, 1f, 0.62f), new Vector3f(0.97f, 0.96f, 0.55f), 30);
                        MagicManager.spawnParticles(level(), cat, entity.position().x, entity.position().y + 1.3, entity.position().z, 1, 0,0, 0, 0, false);
                        MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(SchoolRegistry.NATURE.get().getTargetingColor(), radius), center.x, center.y - 1.1, center.z, 1, 0, 0, 0, 0, false);
                    }
                    entity.addEffect(new MobEffectInstance(MobEffectsRegistry.SCREEN_SHAKE.get(), 20, 7, false, false));
                }
                level().playSound(null, center.x, center.y, center.z, TravelopticsSounds.BLAST_STAGE_THREE.get(), SoundSource.NEUTRAL, 2.0f, 1.0f);
            }
        }
        discard();
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.of(SoundRegistry.ACID_ORB_IMPACT);
    }
}
