package net.offkung.bhspells.entity.spells.eternal_purification;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.particle.SwirlingParticleOptions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.util.BHUtil;
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
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class PurificationPillarEntity extends Entity implements GeoEntity, AntiMagicSusceptible {
    private final AnimatableInstanceCache cache;
    private static final EntityDataAccessor<Float> DATA_DAMAGE = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_AGE = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_MAX_AGE = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_STAGE = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> DATA_SHOCKWAVE_RADIUS = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> DATA_EMISSIVE_BRIGHTNESS = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> DATA_SHOCKWAVE_EMITTER = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Float> DATA_RESONANCE_SEARCH_RADIUS = SynchedEntityData.defineId(PurificationPillarEntity.class, EntityDataSerializers.FLOAT);
    private double groupCenterX;
    private double groupCenterY;
    private double groupCenterZ;

    private UUID summonerUUID;
    private LivingEntity cachedSummoner;
    private int lastShockwaveAge;
    private int lastStage1ShockwaveAge;
    private int lastStage2ShockwaveAge;
    private int lastStage3ShockwaveAge;
    private int secondBlastwaveAge;
    private int erodeAmplifier;
    private float targetEmissiveBrightness;
    private final RawAnimation IDLE_BLANK_ANIMATION;
    private final RawAnimation PILLAR_RISE_ANIMATION;
    private final RawAnimation PILLAR_DIE_ANIMATION;
    private final AnimationController<PurificationPillarEntity> controller;

    public PurificationPillarEntity(EntityType<? extends Entity> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.cache = GeckoLibUtil.createInstanceCache(this);
        this.lastShockwaveAge = 0;
        this.lastStage1ShockwaveAge = -30;
        this.lastStage2ShockwaveAge = -20;
        this.lastStage3ShockwaveAge = -10;
        this.secondBlastwaveAge = -1;
        this.erodeAmplifier = 0;
        this.targetEmissiveBrightness = 0.0F;
        this.IDLE_BLANK_ANIMATION = RawAnimation.begin().thenLoop("idle_blank");
        this.PILLAR_RISE_ANIMATION = RawAnimation.begin().thenPlay("pillar_rise");
        this.PILLAR_DIE_ANIMATION = RawAnimation.begin().thenPlay("pillar_die");
        this.controller = new AnimationController<>(this, "pillar_controller", 0, this::animationPredicate);
    }

    public PurificationPillarEntity(Level level, LivingEntity summoner) {
        this(EntityRegistry.PURIFICATION_PILLAR.get(), level);
        this.setSummoner(summoner);
    }

    protected void defineSynchedData() {
        this.entityData.define(DATA_DAMAGE, 8.0F);
        this.entityData.define(DATA_AGE, 0);
        this.entityData.define(DATA_MAX_AGE, 600);
        this.entityData.define(DATA_STAGE, 0);
        this.entityData.define(DATA_SHOCKWAVE_RADIUS, 0.0F);
        this.entityData.define(DATA_EMISSIVE_BRIGHTNESS, 0.0F);
        this.entityData.define(DATA_SHOCKWAVE_EMITTER, false);
        this.entityData.define(DATA_RESONANCE_SEARCH_RADIUS, 12.0F);
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("Damage", this.getDamage());
        tag.putInt("Age", this.getAge());
        tag.putInt("MaxAge", this.getMaxAge());
        tag.putInt("Stage", this.getStageValue());
        tag.putFloat("ShockwaveRadius", this.getShockwaveRadius());
        tag.putInt("LastShockwaveAge", this.lastShockwaveAge);
        tag.putFloat("EmissiveBrightness", this.getEmissiveBrightness());
        tag.putInt("ErodeAmplifier", this.getErodeAmplifier());
        if (this.summonerUUID != null) {
            tag.putUUID("Summoner", this.summonerUUID);
        }
        tag.putDouble("GroupCenterX", this.groupCenterX);
        tag.putDouble("GroupCenterY", this.groupCenterY);
        tag.putDouble("GroupCenterZ", this.groupCenterZ);
        tag.putBoolean("ShockwaveEmitter", this.isShockwaveEmitter());
        tag.putFloat("ResonanceSearchRadius", this.getResonanceSearchRadius());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Age")) {
            this.setAge(tag.getInt("Age"));
        }

        if (tag.contains("MaxAge")) {
            this.setMaxAge(tag.getInt("MaxAge"));
        }

        if (tag.contains("Damage")) {
            this.setDamage(tag.getFloat("Damage"));
        }

        if (tag.contains("Stage")) {
            this.setStage(Stage.fromValue(tag.getInt("Stage")));
        }

        if (tag.contains("ShockwaveRadius")) {
            this.setShockwaveRadius(tag.getFloat("ShockwaveRadius"));
        }

        if (tag.contains("LastShockwaveAge")) {
            this.lastShockwaveAge = tag.getInt("LastShockwaveAge");
        }

        if (tag.contains("EmissiveBrightness")) {
            this.setEmissiveBrightness(tag.getFloat("EmissiveBrightness"));
        }

        if (tag.contains("ErodeAmplifier")) {
            this.setErodeAmplifier(tag.getInt("ErodeAmplifier"));
        }

        if (tag.hasUUID("Summoner")) {
            this.summonerUUID = tag.getUUID("Summoner");
        }

        if (tag.contains("GroupCenterX")) {
            this.groupCenterX = tag.getDouble("GroupCenterX");
            this.groupCenterY = tag.getDouble("GroupCenterY");
            this.groupCenterZ = tag.getDouble("GroupCenterZ");
        }

        if (tag.contains("ShockwaveEmitter")) {
            this.setShockwaveEmitter(tag.getBoolean("ShockwaveEmitter"));
        }

        if (tag.contains("ResonanceSearchRadius")) {
            this.setResonanceSearchRadius(tag.getFloat("ResonanceSearchRadius"));
        }
    }

    public enum Stage {
        STAGE_0(0),
        STAGE_1(1),
        STAGE_2(2),
        STAGE_3(3);

        public final int value;

        Stage(int value) {
            this.value = value;
        }

        public static Stage fromValue(int value) {
            for(Stage stage : values()) {
                if (stage.value == value) {
                    return stage;
                }
            }

            return STAGE_0;
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

    public void setDamage(float damage) {
        this.entityData.set(DATA_DAMAGE, damage);
    }

    public float getDamage() {
        return this.entityData.get(DATA_DAMAGE);
    }

    public Stage getStage() {
        return Stage.fromValue(this.entityData.get(DATA_STAGE));
    }

    public int getStageValue() {
        return this.entityData.get(DATA_STAGE);
    }

    public void setStage(Stage stage) {
        this.entityData.set(DATA_STAGE, stage.value);
    }

    public float getShockwaveRadius() {
        return this.entityData.get(DATA_SHOCKWAVE_RADIUS);
    }

    public void setShockwaveRadius(float radius) {
        this.entityData.set(DATA_SHOCKWAVE_RADIUS, radius);
    }

    public int getErodeAmplifier() {
        return this.erodeAmplifier;
    }

    public void setErodeAmplifier(int amplifier) {
        this.erodeAmplifier = Math.max(0, amplifier);
    }

    public float getEmissiveBrightness() {
        return this.entityData.get(DATA_EMISSIVE_BRIGHTNESS);
    }

    public Vec3 getGroupCenter() {
        return new Vec3(this.groupCenterX, this.groupCenterY, this.groupCenterZ);
    }

    public void setEmissiveBrightness(float brightness) {
        this.entityData.set(DATA_EMISSIVE_BRIGHTNESS, brightness);
    }

    public float getResonanceSearchRadius() {
        return this.entityData.get(DATA_RESONANCE_SEARCH_RADIUS);
    }

    public void setResonanceSearchRadius(float radius) {
        this.entityData.set(DATA_RESONANCE_SEARCH_RADIUS, radius);
    }

    public void setGroupCenter(Vec3 center) {
        this.groupCenterX = center.x;
        this.groupCenterY = center.y;
        this.groupCenterZ = center.z;
    }

    public void setShockwaveEmitter(boolean emitter) {
        this.entityData.set(DATA_SHOCKWAVE_EMITTER, emitter);
    }

    public boolean isShockwaveEmitter() {
        return this.entityData.get(DATA_SHOCKWAVE_EMITTER);
    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.summonerUUID = owner.getUUID();
            this.cachedSummoner = owner;
        }
    }

    public LivingEntity getSummoner() {
        if (this.cachedSummoner != null && this.cachedSummoner.isAlive()) {
            return this.cachedSummoner;
        } else if (this.summonerUUID != null && this.level() instanceof ServerLevel) {
            Entity owner = ((ServerLevel)this.level()).getEntity(this.summonerUUID);
            if (owner instanceof LivingEntity) {
                LivingEntity livingEntity = (LivingEntity)owner;
                this.cachedSummoner = livingEntity;
            }

            return this.cachedSummoner;
        } else {
            return null;
        }
    }

    public void tick() {
        super.tick();
        int currentAge = this.getAge();
        this.setAge(currentAge + 1);
        int age = this.getAge();
        int maxAge = this.getMaxAge();
        int stageProgression = age / 10 % 4;
        this.setStage(Stage.fromValue(stageProgression));
        this.updateEmissiveBrightness();
        Vec3 currentMotion = this.getDeltaMovement();
        double newY = this.applyArtificialGravity(currentMotion.y);
        this.setDeltaMovement(currentMotion.x, newY, currentMotion.z);
        this.move(MoverType.SELF, this.getDeltaMovement());
        Vec3 motion = this.getDeltaMovement();
        this.setDeltaMovement(0.0F, motion.y, 0.0F);
        if ((age == 1 || age % 60 == 0) && this.onGround()) {
            this.alignToBlockCenter();
        }

        if (this.level().isClientSide) {
            PurificationPillarParticleManager.playWaterfallCascade(this.level(), this.position(), this.getAge(), 1.7);
        }

        if (age - this.lastShockwaveAge >= 40) {
            this.performOwnShockwave();
            this.lastShockwaveAge = age;
            this.secondBlastwaveAge = age + 2;
        }

        if (!this.level().isClientSide && this.secondBlastwaveAge != -1 && age >= this.secondBlastwaveAge) {
            this.secondBlastwaveAge = -1;
        }

        if (age - this.lastStage1ShockwaveAge >= 40) {
            this.performResonanceShockwave(Stage.STAGE_1);
            this.lastStage1ShockwaveAge = age;
        }

        if (age - this.lastStage2ShockwaveAge >= 40) {
            this.performResonanceShockwave(Stage.STAGE_2);
            this.lastStage2ShockwaveAge = age;
        }

        if (age - this.lastStage3ShockwaveAge >= 40) {
            this.performResonanceShockwave(Stage.STAGE_3);
            this.lastStage3ShockwaveAge = age;
        }

        if (this.isShockwaveEmitter() && age - this.lastShockwaveAge >= 40) {
            this.performOwnShockwave();
            this.lastShockwaveAge = age;
            this.secondBlastwaveAge = age + 2;
        }

        if (!this.level().isClientSide && this.isShockwaveEmitter() && this.secondBlastwaveAge != -1 && age >= this.secondBlastwaveAge) {
            this.secondBlastwaveAge = -1;
        }

        if (age >= maxAge) {
            this.playSound(BHSoundRegistry.BREAK_LARGE.get(), 4.0F, 1.0F);
            if (!this.level().isClientSide) {
                TOScreenShakeEntity.createScreenShake(this.level(), this.position(), this.getShockwaveRadius() * 2.0F, 0.025F, 10, 0, 5, true);
            }

            this.discard();
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
                double centerX = (double)groundBlock.getX() + (double)0.5F;
                double centerZ = (double)groundBlock.getZ() + (double)0.5F;
                double currentY = currentPos.y;
                double distanceFromCenter = Math.sqrt(Math.pow(currentPos.x - centerX, 2.0F) + Math.pow(currentPos.z - centerZ, 2.0F));
                if (distanceFromCenter > 0.1) {
                    this.teleportTo(centerX, currentY, centerZ);
                    Vec3 motion = this.getDeltaMovement();
                    this.setDeltaMovement(0.0F, motion.y, 0.0F);
                }
            }
        }
    }

    private void performOwnShockwave() {
        if (!this.level().isClientSide) {
            LivingEntity owner = this.getSummoner();
            float radius = this.getShockwaveRadius();
            Vec3 center = this.getGroupCenter();
            AABB shockwaveArea = new AABB(center.x - radius, center.y - 1.0, center.z - radius, center.x + radius, center.y + 2.0, center.z + radius);
            this.level().getEntitiesOfClass(LivingEntity.class, shockwaveArea).stream()
                    .filter(e -> e.isAlive() && e.getId() != this.getId() && e != owner)
                    .filter(e -> e.position().distanceTo(center) <= radius)
                    .filter(e -> owner == null || (!BHUtil.isAlly(owner, e) && !BHUtil.isTamed(e)))
                    .forEach(this::applyPurificationResonance);
            this.level().playSound(null, center.x, center.y, center.z, BHSoundRegistry.HIT_MEDIUM_2.get(), SoundSource.HOSTILE, 2.5F, 0.8F);
            TOScreenShakeEntity.createScreenShake(this.level(), center, radius * 2.0F, 0.01F, 8, 0, 4, true);
            MagicManager.spawnParticles(this.level(), new BlastwaveParticleOptions(new Vector3f(0.12f, 0.96f, 0.71f), radius), center.x, center.y + 0.165F, center.z, 1, 0.0F, 0.0F, 0.0F, 0.0F, false);
            this.createBaseShockwaveParticlesAt(center);
        }
    }

    private void createBaseShockwaveParticlesAt(Vec3 center) {
        float radius = this.getShockwaveRadius();
        float radiusScale = radius / 6.0F;
        int basaltCount = Math.round(50.0F * radiusScale);

        for (int i = 0; i < basaltCount; ++i) {
            double angle = (double) i / (double) basaltCount * 2.0F * Math.PI;
            double distance = 1.0F + this.random.nextDouble() * (radius - 1.0F);
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = center.y + 0.1 + this.random.nextDouble() * 0.3;
            double velX = Math.cos(angle) * (0.3 + this.random.nextDouble() * 0.4);
            double velY = 0.1 + this.random.nextDouble() * 0.3;
            double velZ = Math.sin(angle) * (0.3 + this.random.nextDouble() * 0.4);
            MagicManager.spawnParticles(this.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()), x, y, z, 1, velX, velY, velZ, 0.0F, false);
        }

        int debrisCount = Math.round(16.0F * radiusScale);
        for (int i = 0; i < debrisCount; ++i) {
            double x = center.x + (this.random.nextDouble() - 0.5F) * radius * 2.0F;
            double z = center.z + (this.random.nextDouble() - 0.5F) * radius * 2.0F;
            double y = center.y + this.random.nextDouble() * 1.5F;
            double velX = (this.random.nextDouble() - 0.5F) * 0.2;
            double velY = this.random.nextDouble() * 0.4;
            double velZ = (this.random.nextDouble() - 0.5F) * 0.2;
            MagicManager.spawnParticles(this.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.COBBLESTONE.defaultBlockState()), x, y, z, 1, velX, velY, velZ, 0.0F, false);
        }

        int smokeCount = Math.round(12.0F * radiusScale);
        for (int i = 0; i < smokeCount; ++i) {
            double x = center.x + (this.random.nextDouble() - 0.5F) * radius;
            double z = center.z + (this.random.nextDouble() - 0.5F) * radius;
            double y = center.y + 0.5F + this.random.nextDouble();
            MagicManager.spawnParticles(this.level(), ParticleTypes.CAMPFIRE_COSY_SMOKE, x, y, z, 1, 0.0F, 0.02, 0.0F, 0.01, false);
        }
    }

    private void performResonanceShockwave(Stage forceStage) {
        if (!this.level().isClientSide) {
            int targetIndex = forceStage.value - 1;
            float baseRadius = this.getShockwaveRadius();
            float searchRadius = this.getResonanceSearchRadius();
            List<Entity> resonanceEntities = this.level().getEntitiesOfClass(Entity.class, new AABB(this.getX() - (double)searchRadius, this.getY() - (double)20.0F, this.getZ() - (double)searchRadius, this.getX() + (double)searchRadius, this.getY() + (double)20.0F, this.getZ() + (double)searchRadius)).stream().filter((entity) -> entity.getId() != this.getId()).sorted(Comparator.comparingDouble((entity) -> (double)entity.distanceTo(this))).toList();
            if (resonanceEntities.size() > targetIndex) {
                Entity resonanceEntity = resonanceEntities.get(targetIndex);
                Vec3 resonancePosition = resonanceEntity.position();
                LivingEntity owner = this.getSummoner();
                float radiusReduction = 1.0F - (float)forceStage.value * 0.15F;
                float resonanceRadius = baseRadius * radiusReduction;
                float stageMultiplier = 1.0F - (float)forceStage.value * 0.1F;
                float entityMultiplier = this.getEntityResonanceMultiplier(resonanceEntity);
                float baseDamage = this.getDamage();
                float finalDamage = baseDamage * stageMultiplier * entityMultiplier;
                if (resonanceEntity instanceof IPurificationEntity) {
                    IPurificationEntity resonanceImpl = (IPurificationEntity) resonanceEntity;
                    resonanceImpl.onResonanceActivated(this, forceStage, resonanceRadius, baseDamage, finalDamage, owner);
                    this.createSpecialResonanceEffects(resonancePosition, forceStage);
                }

                AABB shockwaveArea = new AABB(resonancePosition.x - (double)resonanceRadius, resonancePosition.y - (double)1.0F, resonancePosition.z - (double)resonanceRadius, resonancePosition.x + (double)resonanceRadius, resonancePosition.y + (double)2.0F, resonancePosition.z + (double)resonanceRadius);
                this.level().getEntitiesOfClass(LivingEntity.class, shockwaveArea).stream()
                        .filter((entity) -> entity.isAlive() && entity.getId() != this.getId())
                        .filter((entity) -> entity.position().distanceTo(resonancePosition) <= (double) resonanceRadius)
                        .filter((entity) -> owner == null || !BHUtil.isAlly(owner, entity) && !BHUtil.isTamed(entity))
                        .forEach((entity) -> {
                            boolean isOwner = entity == owner;
                            if (!isOwner) {
                                this.applyPurificationResonance(entity);
                            }

                            if (resonanceEntity instanceof IPurificationEntity resonanceImpl) {
                                double distanceFromResonanceSource = entity.position().distanceTo(resonancePosition);
                                resonanceImpl.onResonanceShockwave(this, forceStage, owner, entity, this.level(), resonancePosition, resonanceRadius, distanceFromResonanceSource, baseDamage, finalDamage);
                            }
                        });
                this.createResonanceEffects(resonancePosition, resonanceEntity, resonanceRadius, forceStage);
            }
        }
    }

    private void applyPurificationResonance(LivingEntity target) {
        List<MobEffectInstance> activeEffects = new ArrayList<>(target.getActiveEffects());
        for (MobEffectInstance effectInstance : activeEffects) {
            if (effectInstance.getEffect().getCategory() == MobEffectCategory.BENEFICIAL) {
                target.removeEffect(effectInstance.getEffect());
            }
        }

        target.addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 100, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, 100, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 100, 1, false, false, true));
    }

    private float getEntityResonanceMultiplier(Entity entity) {
        if (entity instanceof IPurificationEntity resonanceEntity) {
            return resonanceEntity.getResonanceMultiplier();
        } else {
            return 1.0F;
        }
    }

    private void createSpecialResonanceEffects(Vec3 position, Stage stage) {
        if (!this.level().isClientSide) {
            for(int i = 0; i < 8; ++i) {
                double angle = (double)i / (double)8.0F * (double)2.0F * Math.PI;
                double radius = (double)2.0F + (double)stage.value * (double)0.5F;
                double x = position.x + Math.cos(angle) * radius;
                double z = position.z + Math.sin(angle) * radius;
                MagicManager.spawnParticles(this.level(), ParticleTypes.ENCHANT, x, position.y + (double)1.0F, z, 1, 0.0F, 0.1, 0.0F, 0.02, false);
            }
        }
    }

    private void createResonanceEffects(Vec3 position, Entity resonanceEntity, float radius, Stage stage) {
        TOScreenShakeEntity.createScreenShake(this.level(), resonanceEntity.position(), this.getShockwaveRadius(), 0.007F, 6, 0, 2, true);
        float basePitch = 0.8F;
        float stagePitch = basePitch + (float)stage.value * 0.1F;
        this.level().playSound(null, resonanceEntity.getX(), resonanceEntity.getY(), resonanceEntity.getZ(), BHSoundRegistry.SMALL_ROCK_HIT.get(), SoundSource.AMBIENT, 1.5F, stagePitch);

        if (!this.level().isClientSide) {
            this.createResonanceShockwaveParticles(position, radius, stage);
        }
    }

    private void createResonanceShockwaveParticles(Vec3 center, float radius, Stage stage) {
        float radiusScale = radius / 6.0F;
        int baseParticleCount = Math.round(20.0F * radiusScale);
        int particleCount = Math.max(5, baseParticleCount - stage.value * 3);

        for(int i = 0; i < particleCount; ++i) {
            double angle = (double)i / (double)particleCount * (double)2.0F * Math.PI;
            double distance = (double)0.5F + this.random.nextDouble() * (double)radius * 0.8;
            double x = center.x + Math.cos(angle) * distance;
            double z = center.z + Math.sin(angle) * distance;
            double y = center.y + this.random.nextDouble() * 0.2;
            double velX = Math.cos(angle) * (0.2 + this.random.nextDouble() * 0.2);
            double velY = 0.05 + this.random.nextDouble() * 0.2;
            double velZ = Math.sin(angle) * (0.2 + this.random.nextDouble() * 0.2);
            MagicManager.spawnParticles(this.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BASALT.defaultBlockState()), x, y, z, 1, velX, velY, velZ, 0.0F, false);
        }

        int groundCount = Math.max(2, Math.round(8.0F * radiusScale - (float)(stage.value * 2)));

        for(int i = 0; i < groundCount; ++i) {
            double x = center.x + (this.random.nextDouble() - (double)0.5F) * (double)radius;
            double z = center.z + (this.random.nextDouble() - (double)0.5F) * (double)radius;
            double y = center.y - 0.3 + this.random.nextDouble() * 0.1;
            MagicManager.spawnParticles(this.level(), new BlockParticleOption(ParticleTypes.BLOCK, Blocks.STONE.defaultBlockState()), x, y, z, 1, 0.0F, 0.1, 0.0F, 0.02, false);
        }
    }

    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }

    public boolean isPushable() {
        return false;
    }

    public boolean canBeCollidedWith() {
        return true;
    }

    public boolean isPickable() {
        return false;
    }

    public boolean isOnFire() {
        return false;
    }

    public boolean isPushedByFluid() {
        return false;
    }

    public boolean isNoGravity() {
        return false;
    }

    public boolean hurt(DamageSource pSource, float pAmount) {
        return false;
    }

    private PlayState animationPredicate(AnimationState<PurificationPillarEntity> event) {
        int age = this.getAge();
        int maxAge = this.getMaxAge();
        if (age <= 10) {
            event.getController().setAnimation(this.PILLAR_RISE_ANIMATION);
            return PlayState.CONTINUE;
        } else if (age >= maxAge - 10) {
            event.getController().setAnimation(this.PILLAR_DIE_ANIMATION);
            return PlayState.CONTINUE;
        } else {
            event.getController().setAnimation(this.IDLE_BLANK_ANIMATION);
            return PlayState.CONTINUE;
        }
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(this.controller);
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private void updateEmissiveBrightness() {
        Stage currentStage = this.getStage();
        float targetBrightness;
        switch (currentStage.value) {
            case 1, 2 -> targetBrightness = 0.8F;
            case 3 -> targetBrightness = 0.4F;
            case 4 -> targetBrightness = 0.1F;
            default -> targetBrightness = 0.0F;
        }

        this.targetEmissiveBrightness = targetBrightness;
        float newBrightness = this.getNewBrightness(targetBrightness);
        this.setEmissiveBrightness(newBrightness);
    }

    private float getNewBrightness(float targetBrightness) {
        float currentBrightness = this.getEmissiveBrightness();
        float lerpSpeed = 0.15F;
        float newBrightness = Mth.lerp(lerpSpeed, currentBrightness, targetBrightness);
        if (Math.abs(newBrightness - targetBrightness) < 0.005F) {
            newBrightness = targetBrightness;
        }
        return newBrightness;
    }
}
