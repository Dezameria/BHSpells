package net.offkung.bhspells.entity.spells.flames_eagle;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.DamageSourcesRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.offkung.bhspells.util.BHParticleHelper;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

public class FlamesEagleEntity extends AbstractMagicProjectile implements GeoEntity, IEntityAdditionalSpawnData {
    public static final int MAX_LIFETIME = 120; // 6 seconds
    private static final EntityDataAccessor<Boolean> DATA_EMPOWERED = SynchedEntityData.defineId(FlamesEagleEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation IDLE = RawAnimation.begin().thenLoop("fly");
    private final Set<UUID> hitEntities = new HashSet<>();

    private LivingEntity homingTarget;
    private Vec3 targetPos;
    private boolean isRedirected = false;

    public FlamesEagleEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.damage = 40.0F;
    }

    public FlamesEagleEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.FLAMES_EAGLE.get(), level);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_EMPOWERED, false);
    }

    public boolean isEmpowered() {
        return this.entityData.get(DATA_EMPOWERED);
    }

    public void setEmpowered(boolean empowered) {
        this.entityData.set(DATA_EMPOWERED, empowered);
        this.refreshDimensions();
    }

    public boolean hasHitEntity(Entity entity) {
        return hitEntities.contains(entity.getUUID());
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> key) {
        super.onSyncedDataUpdated(key);
        if (DATA_EMPOWERED.equals(key)) {
            this.refreshDimensions();
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return super.getDimensions(pose).scale(isEmpowered() ? 2.0F : 1.0F);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return this.damage;
    }

    public void shoot(Vec3 direction) {
        Vec3 dir = direction.normalize();
        this.setDeltaMovement(dir.scale(getSpeed()));
        double horiz = dir.horizontalDistance();
        float xRot = -((float) (Mth.atan2(horiz, dir.y) * (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(dir.z, dir.x) * (180F / (float) Math.PI)) + 90.0F);
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
        this.hasImpulse = true;
    }

    public void redirectTowards(@Nullable LivingEntity targetEntity, @Nullable Vec3 fallbackPos) {
        this.homingTarget = targetEntity;
        this.targetPos = fallbackPos;
        this.isRedirected = true;
        this.hasImpulse = true;

        if (level() instanceof ServerLevel serverLevel) {
            float multiplier = isEmpowered() ? 2.0f : 1.0f;
            MagicManager.spawnParticles(serverLevel, BHParticleHelper.PINK_FIRE_EMITTER, getX(), getY() + getBbHeight() * 0.5, getZ(), (int) (2 * multiplier), 0.5 * multiplier, 0.5 * multiplier, 0.5 * multiplier, 0.2, true);}
    }

    public void redirect(Vec3 newDirection) {
        redirectTowards(null, this.position().add(newDirection.normalize().scale(64.0)));
    }

    @Override
    public void handleHitDetection() {
        Vec3 vec3 = this.getDeltaMovement();
        Vec3 pos = this.position();
        Vec3 vec32 = pos.add(vec3);
        HitResult hitresult = level().clip(new ClipContext(pos, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (hitresult.getType() != HitResult.Type.MISS) {
            onHit(hitresult);
            return;
        }

        for (Entity entity : level().getEntities(this, this.getBoundingBox().inflate(0.5), this::canHitEntity)) {
            damageEntity(entity);
            if (this.isRemoved()) {
                return;
            }
        }
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity entity) {
        return entity != getOwner() && super.canHitEntity(entity) && !DamageSources.isFriendlyFireBetween(entity, getOwner());
    }

    private void damageEntity(Entity entity) {
        if (!level().isClientSide && canHitEntity(entity) && hitEntities.add(entity.getUUID())) {
            DamageSources.applyDamage(entity, this.damage, DamageSourcesRegistry.fireSpell(level(), getOwner()));
            if (entity instanceof LivingEntity living) {
                // Slowness 2 (amplifier 1) for 4 seconds (80 ticks)
                living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                // Weakness 2 (amplifier 1) for 4 seconds (80 ticks)
                living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
            }
            Vec3 hitPos = new Vec3(entity.getX(), entity.getY() + entity.getBbHeight() * 0.5, entity.getZ());
            impactParticles(hitPos.x, hitPos.y, hitPos.z);
            level().playSound(null, entity.getX(), entity.getY(), entity.getZ(), BHSoundRegistry.BIRD_HITS.get(), SoundSource.NEUTRAL, 1.5f, 0.8f);
            spawnScreenShake(hitPos);
            if (isEmpowered()) {
                damageNearbyEntities(hitPos, 7.0);
            }
            applyCooldownToCaster();
            cancelRecast();
            this.discard();
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        damageEntity(entityHitResult.getEntity());
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        super.onHitBlock(pResult);
        if (!level().isClientSide) {
            Vec3 hitPos = pResult.getLocation();
            impactParticles(hitPos.x, hitPos.y, hitPos.z);
            level().playSound(null, hitPos.x, hitPos.y, hitPos.z, SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, 1.0f, 0.6f);
            spawnScreenShake(hitPos);
            if (isEmpowered()) {
                damageNearbyEntities(hitPos, 7.0);
            }
            applyCooldownToCaster();
            cancelRecast();
            this.discard();
        }
    }

    private void damageNearbyEntities(Vec3 center, double radius) {
        AABB aabb = new AABB(
                center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius
        );
        level().playSound(null, center.x, center.y, center.z, SoundRegistry.FIRE_ERUPTION_SLAM.get(), SoundSource.NEUTRAL, 1.5f, 0.9f);
        for (Entity nearby : level().getEntities(this, aabb, this::canHitEntity)) {
            if (nearby.distanceToSqr(center) <= radius * radius && hitEntities.add(nearby.getUUID())) {
                DamageSources.applyDamage(nearby, this.damage, DamageSourcesRegistry.fireSpell(level(), getOwner()));
                if (nearby instanceof LivingEntity living) {
                    living.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 80, 1));
                    living.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 1));
                }
                impactParticles(nearby.getX(), nearby.getY() + nearby.getBbHeight() * 0.5, nearby.getZ());
            }
        }
    }

    private void spawnScreenShake(Vec3 pos) {
        float radius = isEmpowered() ? 12.0F : 8.0F;
        float magnitude = isEmpowered() ? 0.09F : 0.07F;
        TOScreenShakeEntity.createScreenShake(level(), pos, radius, magnitude, 10, 0, 5, true);
    }

    private void applyCooldownToCaster() {
        if (!level().isClientSide && getOwner() instanceof LivingEntity caster) {
            caster.addEffect(new MobEffectInstance(MobEffectsRegistry.SMILES_OF_FIRE_CD.get(), 20, 0, false, false, false));
        }
    }

    private void cancelRecast() {
        if (getOwner() instanceof Player player) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData.getPlayerRecasts().hasRecastForSpell(BHSpellRegistry.SMILES_OF_FIRE.get())) {
                var recast = magicData.getPlayerRecasts().getRecastInstance(BHSpellRegistry.SMILES_OF_FIRE.get().getSpellId());
                if (recast != null) {
                    magicData.getPlayerRecasts().removeRecast(recast, RecastResult.USED_ALL_RECASTS);
                }
            }
        }
    }

    private void clearEmpoweredFromCaster() {
        if (this.isEmpowered() && !level().isClientSide && getOwner() instanceof LivingEntity caster) {
            caster.removeEffect(MobEffectsRegistry.SMILES_OF_FIRE.get());
        }
    }

    @Override
    public void remove(RemovalReason reason) {
        if (!level().isClientSide) {
            applyCooldownToCaster();
            cancelRecast();
            clearEmpoweredFromCaster();
        }
        super.remove(reason);
    }

    @Override
    protected void handleEntityHoming() {
        // Custom smooth steering handled in tick()
    }

    @Override
    protected void rotateWithMotion() {
        Vec3 motion = this.getDeltaMovement();
        if (motion.lengthSqr() > 1.0E-4) {
            double horiz = motion.horizontalDistance();
            float xRot = -((float) (Mth.atan2(horiz, motion.y) * (180F / (float) Math.PI)) - 90.0F);
            float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (180F / (float) Math.PI)) + 90.0F);
            this.setYRot(yRot);
            this.setXRot(xRot);
        }
    }

    @Override
    public void tick() {
        // Smooth homing / steering towards aim target (server-authoritative)
        if (!level().isClientSide && this.isRedirected) {
            Vec3 targetPoint = null;
            if (this.homingTarget != null && this.homingTarget.isAlive() && !this.homingTarget.isRemoved() && !hasHitEntity(this.homingTarget)) {
                targetPoint = this.homingTarget.getBoundingBox().getCenter();
            } else if (this.targetPos != null) {
                targetPoint = this.targetPos;
            } else {
                this.isRedirected = false;
            }

            if (targetPoint != null) {
                Vec3 currentMovement = this.getDeltaMovement();
                double speed = currentMovement.length();
                if (speed < 1.0E-4) {
                    speed = getSpeed();
                }
                Vec3 currentDir = currentMovement.normalize();
                Vec3 toTarget = targetPoint.subtract(this.position());
                double dist = toTarget.length();
                if (this.homingTarget == null && (dist < 1.5 || currentDir.dot(toTarget.normalize()) > 0.985)) {
                    this.targetPos = null;
                    this.isRedirected = false;
                } else if (dist > 0.1) {
                    Vec3 desiredDir = toTarget.normalize();
                    if (currentDir.dot(desiredDir) < -0.999) {
                        desiredDir = desiredDir.add(0.001, 0.001, 0).normalize();
                    }
                    float turnSpeed = Mth.clamp((float) (0.20f + (dist < 8.0 ? (8.0 - dist) * 0.015f : 0.0f)), 0.20f, 0.32f);
                    Vec3 newDir = Utils.slerp(turnSpeed, currentDir, desiredDir);
                    this.setDeltaMovement(newDir.scale(speed));
                }
            }
        }

        super.tick();

        if (!level().isClientSide) {
            if (this.tickCount >= MAX_LIFETIME) {
                Vec3 expirePos = this.position();
                impactParticles(expirePos.x, expirePos.y, expirePos.z);
                spawnScreenShake(expirePos);
                if (isEmpowered()) {
                    damageNearbyEntities(expirePos, 7.0);
                }
                applyCooldownToCaster();
                cancelRecast();
                this.discard();
            }
        }
    }

    @Override
    public void trailParticles() {
        float width = getBbWidth();
        float height = getBbHeight();
        Vec3 center = this.position().add(0, height * 0.5, 0);

        // 1. Ambient body particles covering the eagle
        int fireCount = isEmpowered() ? 10 : 5;
        for (int i = 0; i < fireCount; i++) {
            double ox = (random.nextDouble() - 0.5) * width * 0.7;
            double oy = (random.nextDouble() - 0.5) * height * 0.7;
            double oz = (random.nextDouble() - 0.5) * width * 0.7;
            level().addParticle(
                    ParticleRegistry.PINK_FIRE.get(),
                    getX() + ox,
                    getY() + height * 0.5 + oy,
                    getZ() + oz,
                    (random.nextDouble() - 0.5) * 0.02,
                    0.01 + random.nextDouble() * 0.02,
                    (random.nextDouble() - 0.5) * 0.02
            );
        }

        int emberCount = isEmpowered() ? 4 : 2;
        for (int i = 0; i < emberCount; i++) {
            double ox = (random.nextDouble() - 0.5) * width * 0.5;
            double oy = (random.nextDouble() - 0.5) * height * 0.5;
            double oz = (random.nextDouble() - 0.5) * width * 0.5;
            level().addParticle(
                    ParticleRegistry.PINK_EMBERS.get(),
                    getX() + ox,
                    getY() + height * 0.5 + oy,
                    getZ() + oz,
                    0, 0, 0
            );
        }

        // Calculate orthonormal basis (u, v) perpendicular to flight direction dir
        Vec3 motion = getDeltaMovement();
        Vec3 dir = motion.lengthSqr() > 1.0E-4 ? motion.normalize() : new Vec3(0, 0, 1);
        Vec3 up = Math.abs(dir.y) < 0.95 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 u = dir.cross(up).normalize();
        Vec3 v = dir.cross(u).normalize();

        // 2. Vertical rotating ring on the flames eagle
        double ringRadius = isEmpowered() ? 3.5 : 1.8;
        int numNodes = isEmpowered() ? 4 : 3;
        double baseAngle = this.tickCount * 0.08;
        int arcSteps = isEmpowered() ? 3 : 2;

        for (int k = 0; k < numNodes; k++) {
            double nodeAngle = baseAngle + (Math.PI * 2.0 / numNodes) * k;
            Vec3 offset = u.scale(Math.cos(nodeAngle) * ringRadius).add(v.scale(Math.sin(nodeAngle) * ringRadius));
            Vec3 pos = center.add(offset);
            Vec3 rotVel = u.scale(-Math.sin(nodeAngle) * 0.06).add(v.scale(Math.cos(nodeAngle) * 0.06));
            Vec3 vel = motion.scale(0.6).add(rotVel);

            // Orbiting focal flame nodes
            level().addParticle(ParticleRegistry.PINK_DRAGON_FIRE.get(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);

            // Connecting arc particles to complete the ring outline
            for (int step = 1; step <= arcSteps; step++) {
                double arcAngle = nodeAngle + (Math.PI * 2.0 / (numNodes * (arcSteps + 1))) * step;
                Vec3 arcOffset = u.scale(Math.cos(arcAngle) * ringRadius).add(v.scale(Math.sin(arcAngle) * ringRadius));
                Vec3 arcPos = center.add(arcOffset);
                Vec3 arcRotVel = u.scale(-Math.sin(arcAngle) * 0.04).add(v.scale(Math.cos(arcAngle) * 0.04));
                Vec3 arcVel = motion.scale(0.5).add(arcRotVel);
                level().addParticle(ParticleRegistry.PINK_EMBERS.get(), arcPos.x, arcPos.y, arcPos.z, arcVel.x, arcVel.y, arcVel.z);
            }
        }

        // 3. Spreading vertical ring as a trace where the flames eagle is going (every 8 ticks)
        if (this.tickCount % 20 == 0) {
            Vec3 traceCenter = center.subtract(dir.scale(1.0));
            double traceStartRadius = isEmpowered() ? 2.5 : 1.3;
            double spreadSpeed = isEmpowered() ? 0.32 : 0.22;
            int traceCount = isEmpowered() ? 24 : 16;

            for (int j = 0; j < traceCount; j++) {
                double angle = (Math.PI * 2.0 / traceCount) * j;
                Vec3 radialDir = u.scale(Math.cos(angle)).add(v.scale(Math.sin(angle)));
                Vec3 pos = traceCenter.add(radialDir.scale(traceStartRadius));
                Vec3 vel = radialDir.scale(spreadSpeed);

                level().addParticle(ParticleRegistry.PINK_FIRE.get(), pos.x, pos.y, pos.z, vel.x, vel.y, vel.z);
                level().addParticle(ParticleRegistry.PINK_EMBERS.get(), pos.x, pos.y, pos.z, vel.x * 0.8, vel.y * 0.8, vel.z * 0.8);
                if (j % 2 == 0) {
                    level().addParticle(BHParticleHelper.PINK_FIRE, pos.x, pos.y, pos.z, vel.x * 1.1, vel.y * 1.1, vel.z * 1.1);
                }
            }
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(level(), ParticleRegistry.PINK_EMBERS.get(), x, y, z, 120, 0, 0, 0, 0.4, true);
        MagicManager.spawnParticles(level(), BHParticleHelper.PINK_SPARKS, x, y, z, 60, 0, 0, 0, 0.3, true);
    }

    @Override
    public float getSpeed() {
        return 0.5f;
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData pMagicData) {
        applyCooldownToCaster();
        cancelRecast();
        this.discard();
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationState<FlamesEagleEntity> state) {
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Empowered", this.isEmpowered());
        tag.putFloat("Damage", this.damage);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Empowered")) {
            this.setEmpowered(tag.getBoolean("Empowered"));
        }
        if (tag.contains("Damage")) {
            this.damage = tag.getFloat("Damage");
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        super.writeSpawnData(buffer);
        buffer.writeBoolean(this.isEmpowered());
        buffer.writeFloat(this.damage);
        buffer.writeDouble(this.getDeltaMovement().x);
        buffer.writeDouble(this.getDeltaMovement().y);
        buffer.writeDouble(this.getDeltaMovement().z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        super.readSpawnData(additionalData);
        this.setEmpowered(additionalData.readBoolean());
        this.damage = additionalData.readFloat();
        double mx = additionalData.readDouble();
        double my = additionalData.readDouble();
        double mz = additionalData.readDouble();
        this.setDeltaMovement(mx, my, mz);
        double horiz = Math.sqrt(mx * mx + mz * mz);
        float xRot = -((float) (Mth.atan2(horiz, my) * (180F / (float) Math.PI)) - 90.0F);
        float yRot = -((float) (Mth.atan2(mz, mx) * (180F / (float) Math.PI)) + 90.0F);
        this.setYRot(yRot);
        this.setXRot(xRot);
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
