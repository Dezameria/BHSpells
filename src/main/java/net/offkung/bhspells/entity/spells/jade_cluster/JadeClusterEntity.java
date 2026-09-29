package net.offkung.bhspells.entity.spells.jade_cluster;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkHooks;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.event.JadeClusterClientHandler;
import net.offkung.bhspells.client.particle.ColoredEndRodParticleOption;
import net.offkung.bhspells.client.particle.ColoredMyceliumParticleOptions;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.warphan.iss_magicfromtheeast.util.MFTEParticleHelper;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class JadeClusterEntity extends Entity implements GeoEntity, AntiMagicSusceptible {
    private final AnimatableInstanceCache cache;
    public static final int VISUAL_COLOR = 3994796;
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_AGE = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_RADIUS = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_PULSE_BRIGHTNESS = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Optional<UUID>> DATA_OWNER_UUID = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.OPTIONAL_UUID);
    private static final EntityDataAccessor<Integer> DATA_VISUAL_ENTITY_ID = SynchedEntityData.defineId(JadeClusterEntity.class, EntityDataSerializers.INT);

    private static final RawAnimation IDLE_ANIM = RawAnimation.begin().thenLoop("idle");
    private static final Set<JadeClusterEntity> ACTIVE_CLUSTERS = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private static final ParticleEmitterInfo JADE_CLUSTER_RING = new ParticleEmitterInfo(BHSpells.id("jade_cluster_ring"));

    private UUID ownerUUID;
    private LivingEntity cachedOwner;
    private float damageMultiplier = 1.0F;
    @Nullable
    private TargetedAreaEntity visualEntity;
    private final AnimationController<JadeClusterEntity> controller;

    public JadeClusterEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.cache = GeckoLibUtil.createInstanceCache(this);
        this.controller = new AnimationController<>(this, "jade_cluster_controller", 0, this::animationPredicate);
    }

    public JadeClusterEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.JADE_CLUSTER.get(), level);
        this.setOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(DATA_AGE, 0);
        this.entityData.define(DATA_MAX_AGE, 300); // 15 seconds
        this.entityData.define(DATA_RADIUS, 10.0F); // radius of 10
        this.entityData.define(DATA_PULSE_BRIGHTNESS, 0.0F);
        this.entityData.define(DATA_OWNER_ID, -1);
        this.entityData.define(DATA_OWNER_UUID, Optional.empty());
        this.entityData.define(DATA_VISUAL_ENTITY_ID, -1);
    }

    @Override
    public void onAddedToWorld() {
        super.onAddedToWorld();
        if (!this.level().isClientSide) {
            ACTIVE_CLUSTERS.add(this);
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (this.visualEntity != null && !this.visualEntity.isRemoved()) {
            this.visualEntity.discard();
        }
        if (!this.level().isClientSide) {
            ACTIVE_CLUSTERS.remove(this);
            this.checkCooldownOnCleanup();
        }
    }

    private void checkCooldownOnCleanup() {
        UUID ownerId = this.getOwnerUUID();
        if (ownerId != null && getActiveClusterCount(ownerId) == 0) {
            LivingEntity owner = this.getOwner();
            if (owner instanceof ServerPlayer serverPlayer) {
                var cooldowns = MagicData.getPlayerMagicData(serverPlayer).getPlayerCooldowns();
                var spell = BHSpellRegistry.JADE_CLUSTER.get();
                if (cooldowns.getCooldownPercent(spell) <= 0.05F) {
                    cooldowns.addCooldown(spell, 30 * 20);
                }
            }
        }
    }

    public static int getActiveClusterCount(UUID casterId) {
        if (casterId == null) return 0;
        int count = 0;
        for (JadeClusterEntity cluster : ACTIVE_CLUSTERS) {
            if (cluster.isAlive() && !cluster.isRemoved()) {
                UUID ownerId = cluster.getOwnerUUID();
                if (ownerId == null && cluster.getOwner() != null) {
                    ownerId = cluster.getOwner().getUUID();
                }
                if (casterId.equals(ownerId)) {
                    count++;
                }
            }
        }
        return count;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Age", this.getAge());
        tag.putInt("MaxAge", this.getMaxAge());
        tag.putFloat("Radius", this.getRadius());
        tag.putFloat("DamageMultiplier", this.damageMultiplier);
        tag.putFloat("PulseBrightness", this.getPulseBrightness());
        if (this.ownerUUID != null) {
            tag.putUUID("Owner", this.ownerUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Age")) {
            this.setAge(tag.getInt("Age"));
        }
        if (tag.contains("MaxAge")) {
            this.setMaxAge(tag.getInt("MaxAge"));
        }
        if (tag.contains("Radius")) {
            this.setRadius(tag.getFloat("Radius"));
        }
        if (tag.contains("DamageMultiplier")) {
            this.damageMultiplier = tag.getFloat("DamageMultiplier");
        }
        if (tag.contains("PulseBrightness")) {
            this.setPulseBrightness(tag.getFloat("PulseBrightness"));
        }
        if (tag.hasUUID("Owner")) {
            this.ownerUUID = tag.getUUID("Owner");
            this.entityData.set(DATA_OWNER_UUID, Optional.of(this.ownerUUID));
        }
    }

    public int getAge() {
        return this.entityData.get(DATA_AGE);
    }

    public void setAge(int age) {
        this.entityData.set(DATA_AGE, age);
    }

    public int getMaxAge() {
        return this.entityData.get(DATA_MAX_AGE);
    }

    public void setMaxAge(int maxAge) {
        this.entityData.set(DATA_MAX_AGE, maxAge);
    }

    public float getRadius() {
        return this.entityData.get(DATA_RADIUS);
    }

    public void setRadius(float radius) {
        this.entityData.set(DATA_RADIUS, radius);
    }

    public float getDamageMultiplier() {
        return this.damageMultiplier;
    }

    public void setDamageMultiplier(float multiplier) {
        this.damageMultiplier = multiplier > 0.0F ? multiplier : 1.0F;
    }

    public float getPulseBrightness() {
        return this.entityData.get(DATA_PULSE_BRIGHTNESS);
    }

    public void setPulseBrightness(float brightness) {
        this.entityData.set(DATA_PULSE_BRIGHTNESS, brightness);
    }

    public void setVisualEntity(@Nullable TargetedAreaEntity visualEntity) {
        this.visualEntity = visualEntity;
        this.entityData.set(DATA_VISUAL_ENTITY_ID, visualEntity != null ? visualEntity.getId() : -1);
    }

    public int getVisualEntityId() {
        return this.entityData.get(DATA_VISUAL_ENTITY_ID);
    }

    @Nullable
    public TargetedAreaEntity getVisualEntity() {
        if (this.visualEntity == null && this.level().isClientSide) {
            int id = this.getVisualEntityId();
            if (id > 0 && this.level().getEntity(id) instanceof TargetedAreaEntity area) {
                this.visualEntity = area;
            }
        }
        return this.visualEntity;
    }

    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.ownerUUID = owner.getUUID();
            this.cachedOwner = owner;
            this.entityData.set(DATA_OWNER_UUID, Optional.of(owner.getUUID()));
            this.entityData.set(DATA_OWNER_ID, owner.getId());
        } else {
            this.ownerUUID = null;
            this.cachedOwner = null;
            this.entityData.set(DATA_OWNER_UUID, Optional.empty());
            this.entityData.set(DATA_OWNER_ID, -1);
        }
    }

    public UUID getOwnerUUID() {
        if (this.ownerUUID != null) return this.ownerUUID;
        Optional<UUID> opt = this.entityData.get(DATA_OWNER_UUID);
        return opt.orElse(null);
    }

    public boolean isOwnedBy(@Nullable Player player) {
        if (player == null) return false;
        int ownerId = this.entityData.get(DATA_OWNER_ID);
        if (ownerId != -1 && player.getId() == ownerId) {
            return true;
        }
        UUID uuid = this.getOwnerUUID();
        return uuid != null && uuid.equals(player.getUUID());
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && this.cachedOwner.isAlive()) {
            return this.cachedOwner;
        } else if (this.ownerUUID != null && this.level() instanceof ServerLevel serverLevel) {
            Entity owner = serverLevel.getEntity(this.ownerUUID);
            if (owner instanceof LivingEntity livingEntity) {
                this.cachedOwner = livingEntity;
            }
            return this.cachedOwner;
        } else {
            return null;
        }
    }

    @Override
    public void tick() {
        super.tick();
        int currentAge = this.getAge();
        this.setAge(currentAge + 1);
        int age = this.getAge();
        int maxAge = this.getMaxAge();

        Vec3 currentMotion = this.getDeltaMovement();
        double newY = this.applyArtificialGravity(currentMotion.y);
        this.setDeltaMovement(currentMotion.x, newY, currentMotion.z);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0F, motion.y, 0.0F);

        if ((age == 1 || age % 60 == 0) && this.onGround()) {
            this.alignToBlockCenter();
        }

        this.updatePulseBrightness();

        if (this.level().isClientSide) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> JadeClusterClientHandler.handleAmbientParticles(this));
        }

        if (age >= maxAge && !this.level().isClientSide) {
            this.explode();
        }
    }

    private double applyArtificialGravity(double currentY) {
        if (this.onGround()) {
            return Math.max(currentY * 0.8, -0.05);
        } else {
            double gravity = -0.1;
            double terminalVelocity = -1.5F;
            double newY = currentY + gravity;
            return Math.max(newY, terminalVelocity);
        }
    }

    private void alignToBlockCenter() {
        if (this.onGround()) {
            Vec3 currentPos = this.position();
            BlockPos groundBlock = this.blockPosition().below();
            BlockState groundState = this.level().getBlockState(groundBlock);
            if (!groundState.isAir() && groundState.isSolidRender(this.level(), groundBlock)) {
                double centerX = (double)groundBlock.getX() + 0.5D;
                double centerZ = (double)groundBlock.getZ() + 0.5D;
                double currentY = currentPos.y;
                double distanceFromCenter = Math.sqrt(Math.pow(currentPos.x - centerX, 2.0D) + Math.pow(currentPos.z - centerZ, 2.0D));
                if (distanceFromCenter > 0.1) {
                    this.teleportTo(centerX, currentY, centerZ);
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(0.0F, motion.y, 0.0F);
                }
            }
        }
    }

    public void spawnAmbientParticles() {
        if (this.random.nextFloat() < 0.1F) {
            AABB box = this.getBoundingBox();
            double x = box.minX + this.random.nextDouble() * (box.maxX - box.minX);
            double y = box.minY + this.random.nextDouble() * (box.maxY - box.minY);
            double z = box.minZ + this.random.nextDouble() * (box.maxZ - box.minZ);
            this.level().addParticle(ParticleTypes.GLOW, x, y, z, 0.0F, 0.0F, 0.0F);
        }
    }

    public float calculateExplosionDamage() {
        float ageInSeconds = (float) this.getAge() / 20.0F;
        float calculatedDamage = 20.0F + ageInSeconds * 2.67F;
        if (calculatedDamage > 60.0F || this.getAge() >= this.getMaxAge()) {
            calculatedDamage = 60.0F;
        }
        return calculatedDamage * (this.damageMultiplier > 0.0F ? this.damageMultiplier : 1.0F);
    }

    public void explode() {
        if (this.isRemoved()) return;

        if (this.visualEntity != null && !this.visualEntity.isRemoved()) {
            this.visualEntity.discard();
        }

        float damage = this.calculateExplosionDamage();
        float radius = this.getRadius();
        Vec3 center = this.position();
        LivingEntity owner = this.getOwner();
        AABB damageArea = new AABB(center.x - radius, center.y - radius, center.z - radius,
                center.x + radius, center.y + radius, center.z + radius);

        for (LivingEntity target : this.level().getEntitiesOfClass(LivingEntity.class, damageArea)) {
            if (target != owner && target.isAlive() && !target.isSpectator()) {
                double distance = target.distanceTo(this);
                if (distance <= radius && (owner == null || (!owner.isAlliedTo(target) && !DamageSources.isFriendlyFireBetween(owner, target)))) {
                    target.invulnerableTime = 0;
                    DamageSources.applyDamage(target, damage, BHSpellRegistry.JADE_CLUSTER.get().getDamageSource(this, owner));
                }
            }
        }

        AAALevel.addParticle(this.level(), 64.0, JADE_CLUSTER_RING.clone().position(center.x, center.y + 0.05, center.z).scale(2.7f));
        MagicManager.spawnParticles(this.level(), new BlastwaveParticleOptions(new Vector3f(0.24F, 0.96F, 0.67F), radius), center.x, center.y + 0.165F, center.z, 1, 0.2F, 0.0F, 0.0F, 0.0F, false);
        this.spawnExplosionParticles(center);
        TOScreenShakeEntity.createScreenShake(this.level(), center, radius * 1.5F, 0.05F, 10, 0, 5, true);
        this.playSound(SoundEvents.RESPAWN_ANCHOR_DEPLETE.get(), 1.5F, 1.0F);
        this.playSound(SoundEvents.GENERIC_EXPLODE, 1.5F, 1.0F);
        this.discard();
    }

    private void spawnExplosionParticles(Vec3 center) {
        MagicManager.spawnParticles(this.level(), ParticleTypes.GLOW, center.x, center.y + 1.0D, center.z, 150, 0.3, 0.3, 0.3, 0.7, false);
        MagicManager.spawnParticles(this.level(), MFTEParticleHelper.JADE_SHATTER, center.x, center.y + 1.0D, center.z, 150, 0.25, 0.25, 0.25, 0.5, false);
        MagicManager.spawnParticles(this.level(), new ColoredMyceliumParticleOptions(new Vector3f(0.24F, 0.96F, 0.67F), 1.0F), center.x, center.y + 1.0D, center.z, 150, 0.25, 0.25, 0.25, 0.5, false);
        MagicManager.spawnParticles(this.level(), new ColoredEndRodParticleOption(0.24F, 0.96F, 0.67F), center.x, center.y + 1.0D, center.z, 150, 0.25, 0.25, 0.25, 0.6, false);
    }

    private void updatePulseBrightness() {
        float currentPulse = this.getPulseBrightness();
        float baseLevel = this.getBaseBrightnessLevel();
        if (currentPulse > baseLevel) {
            float decayRate = 0.05F;
            float newPulse = Math.max(currentPulse - decayRate, baseLevel);
            this.setPulseBrightness(newPulse);
        } else if (currentPulse < baseLevel) {
            this.setPulseBrightness(baseLevel);
        }
    }

    private float getBaseBrightnessLevel() {
        int maxAge = this.getMaxAge();
        return maxAge <= 0 ? 0.0F : Math.min((float) this.getAge() / (float) maxAge, 1.0F);
    }

    public float getEmissiveBrightness() {
        return this.getPulseBrightness();
    }

    @Override
    public void onAntiMagic(MagicData playerMagicData) {
        this.explode();
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isNoGravity() {
        return false;
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    private PlayState animationPredicate(AnimationState<JadeClusterEntity> event) {
        event.getController().setAnimation(IDLE_ANIM);
        return PlayState.CONTINUE;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(this.controller);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
