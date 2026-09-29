package net.offkung.bhspells.entity.spells.supporting_bamboo;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.client.particle.ColoredCherryParticleOption;

import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.Optional;

public class SupportingBamboo extends AoeEntity implements AntiMagicSusceptible, GeoEntity {

    public static final int LIFETIME_TICKS = 700; // 35 seconds (30 + 5 bonus)
    public static final float RADIUS = 15.0f;
    public static final float HEAL_AMOUNT = 20.0f;
    public static final int WAVE_INTERVAL = 50; // 2.5 seconds
    public static final int CONTINUOUS_BLASTWAVE_DURATION = 10;
    public static final int BLASTWAVE_SPAWN_EVERY = 3;

    // Blastwave colours
    private static final Vector3f GREEN_COLOR = new Vector3f(0.79f, 0.9f, 0.63f);
    private static final Vector3f RETALIATION_COLOR = new Vector3f(0.26f, 0f, 0.13f);
    private static final float WAVE_RADIUS = 15f;

    // Cherry leaf colour (same green tint as GreenSunbeam)
    private static final Vector3f CHERRY_GREEN = new Vector3f(0.17f, 0.56f, 0f);

    /** How many waves have fired — used to calculate stacking strength duration. */
    private int waveCount = 0;
    /** The tick at which the last green wave started its continuous blastwave burst. -1 = no active burst. */
    private int continuousBlastwaveStartTick = -1;
    /** The tick at which the last retaliation wave started its continuous blastwave burst. -1 = no active burst. */
    private int retaliationBlastwaveStartTick = -1;

    // GeckoLib
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation BLOOM = RawAnimation.begin().thenPlayAndHold("Bloom");

    public SupportingBamboo(EntityType<? extends AoeEntity> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
        this.duration = LIFETIME_TICKS;
        this.setRadius(RADIUS);
    }

    public SupportingBamboo(Level level, LivingEntity owner) {
        this(EntityRegistry.SUPPORTING_BAMBOO.get(), level);
        this.setOwner(owner);
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 6, 0);
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target instanceof LivingEntity living && living.isAlive() && !living.isSpectator();
    }

    @Override
    public float getParticleCount() {
        return 0f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void ambientParticles() {}

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || isRemoved()) return;

        // At tick 34, spawn GreenSunbeam at bamboo position (early warm-up)
        if (tickCount == 660) {
            spawnGreenSunbeam();
        }

        // Discard past lifetime (AoeEntity base handles this via duration, but guard here too)
        if (tickCount >= LIFETIME_TICKS) {
            discard();
            return;
        }

        // Continuous green blastwave burst: spawn every BLASTWAVE_SPAWN_EVERY ticks for 3 seconds
        if (continuousBlastwaveStartTick >= 0) {
            int elapsed = tickCount - continuousBlastwaveStartTick;
            if (elapsed <= CONTINUOUS_BLASTWAVE_DURATION) {
                if (tickCount % BLASTWAVE_SPAWN_EVERY == 0) {
                    spawnGreenBlastwave();
                }
            } else {
                continuousBlastwaveStartTick = -1;
            }
        }

        // Continuous retaliation blastwave burst: same timing, dark-purple color
        if (retaliationBlastwaveStartTick >= 0) {
            int elapsed = tickCount - retaliationBlastwaveStartTick;
            if (elapsed <= CONTINUOUS_BLASTWAVE_DURATION) {
                if (tickCount % BLASTWAVE_SPAWN_EVERY == 0) {
                    spawnRetaliationBlastwave();
                }
            } else {
                retaliationBlastwaveStartTick = -1;
            }
        }

        // Wave every WAVE_INTERVAL ticks
        if (tickCount > 0 && tickCount % WAVE_INTERVAL == 0) {
            fireWave();
        }
    }

    // ---- Wave Logic ----
    private void fireWave() {
        waveCount++;
        Vec3 center = position();
        AABB searchBox = new AABB(
                center.x - RADIUS, center.y - 2, center.z - RADIUS,
                center.x + RADIUS, center.y + 8, center.z + RADIUS
        );

        List<LivingEntity> targets = level().getEntitiesOfClass(LivingEntity.class, searchBox, e -> e.isAlive() && !e.isSpectator() && center.distanceTo(e.position()) <= RADIUS);

        for (LivingEntity target : targets) {
            // Heal
            target.heal(HEAL_AMOUNT);

            target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 0, false, false, true));
            target.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 40, 0, false, false, true));

            // Strength I — stacking duration: waveCount * 200 ticks
            int strengthDuration = waveCount * 200;
            MobEffectInstance existingStrength = target.getEffect(MobEffects.DAMAGE_BOOST);
            // Only apply if the new duration would be longer (or it's not active)
            if (existingStrength == null || existingStrength.getDuration() < strengthDuration) {
                target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, strengthDuration, 0, false, false, true));
            }

            // Feature 6: 25 green cherry leaves around each buffed entity
            spawnCherryLeavesAt(target);
        }

        // Feature 3: Start the 3-second continuous blastwave burst (Feature 8)
        continuousBlastwaveStartTick = tickCount;
        // Spawn the first blastwave immediately
        spawnGreenBlastwave();

        // Feature 3: Screen shake every wave
        TOScreenShakeEntity.createScreenShake(level(), center, WAVE_RADIUS, 0.04f, 8, 0, 4, true);

        // Wave sound
        level().playSound(null, center.x, center.y, center.z, SoundRegistry.SUNBEAM_IMPACT.get(), SoundSource.NEUTRAL, 2.0f, 0.9f + random.nextFloat() * 0.2f);
    }

    // Retaliation Wave (called by SupportingBambooEvents)
    public void triggerRetaliationWave() {
        if (level().isClientSide || isRemoved()) return;

        Vec3 center = position();
        AABB searchBox = new AABB(
                center.x - RADIUS, center.y - 2, center.z - RADIUS,
                center.x + RADIUS, center.y + 8, center.z + RADIUS
        );

        // Start the continuous dark-purple blastwave burst (same duration/interval as green waves)
        retaliationBlastwaveStartTick = tickCount;
        spawnRetaliationBlastwave(); // spawn first one immediately

        // Sound — spring wave feels fitting for a bamboo retaliation
        level().playSound(null, center.x, center.y, center.z,
                BHSoundRegistry.SPRING_WAVE.get(), SoundSource.NEUTRAL, 3.0f,
                0.85f + random.nextFloat() * 0.2f);

        // Screen shake + knockback for all entities in the area (excluding owner)
        LivingEntity owner = getOwner();
        List<LivingEntity> nearby = level().getEntitiesOfClass(LivingEntity.class, searchBox,
                e -> e.isAlive() && !e.isSpectator() && center.distanceTo(e.position()) <= RADIUS && e != owner);

        for (LivingEntity entity : nearby) {
            Vec3 toEntity = entity.position().subtract(center);
            double distance = toEntity.length();
            if (distance < 0.01) continue;

            double force = 2.5 * (1.0 - (distance / RADIUS));
            Vec3 pushVec = toEntity.normalize().scale(force);
            entity.setDeltaMovement(entity.getDeltaMovement().add(pushVec.x, 0.7, pushVec.z));
            entity.hurtMarked = true;
        }

        // Screen shake for pushed entities (and in the general area)
        TOScreenShakeEntity.createScreenShake(level(), center, WAVE_RADIUS * 2, 0.08f, 10, 0, 5, true);
    }

    private void spawnGreenBlastwave() {
        Vec3 center = position();
        MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(GREEN_COLOR, WAVE_RADIUS), center.x, center.y + 0.1, center.z, 1, 0, 0, 0, 0, true);
    }

    private void spawnRetaliationBlastwave() {
        Vec3 center = position();
        MagicManager.spawnParticles(level(), new BlastwaveParticleOptions(RETALIATION_COLOR, WAVE_RADIUS), center.x, center.y + 0.1, center.z, 1, 0, 0, 0, 0, true);
    }

    private void spawnCherryLeavesAt(LivingEntity target) {
        ColoredCherryParticleOption greenCherry = new ColoredCherryParticleOption(CHERRY_GREEN, 0.6f);
        MagicManager.spawnParticles(level(), greenCherry, target.getX(), target.getY() + 1.0, target.getZ(), 30, 0, 0, 0, 0.1f, false);
    }

    private void spawnGreenSunbeam() {
        GreenSunbeam sunbeam = new GreenSunbeam(level());
        sunbeam.setPos(getX(), getY(), getZ());
        LivingEntity owner = getOwner();
        if (owner != null) {
            sunbeam.setOwner(owner);
        }
        level().playSound(null, sunbeam.blockPosition(), SoundRegistry.SUNBEAM_WINDUP.get(), SoundSource.NEUTRAL, 3.5f, 1f);
        level().addFreshEntity(sunbeam);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("WaveCount", waveCount);
        tag.putInt("ContinuousBlastwaveStart", continuousBlastwaveStartTick);
        tag.putInt("RetaliationBlastwaveStart", retaliationBlastwaveStartTick);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("WaveCount")) {
            waveCount = tag.getInt("WaveCount");
        }
        if (tag.contains("ContinuousBlastwaveStart")) {
            continuousBlastwaveStartTick = tag.getInt("ContinuousBlastwaveStart");
        }
        if (tag.contains("RetaliationBlastwaveStart")) {
            retaliationBlastwaveStartTick = tag.getInt("RetaliationBlastwaveStart");
        }
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationState<SupportingBamboo> state) {
        state.getController().setAnimation(BLOOM);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        discard();
    }

    @Nullable
    public LivingEntity getOwner() {
        Entity owner = super.getOwner();
        return owner instanceof LivingEntity l ? l : null;
    }
}

