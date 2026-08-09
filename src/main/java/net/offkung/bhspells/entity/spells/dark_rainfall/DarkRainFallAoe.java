package net.offkung.bhspells.entity.spells.dark_rainfall;

import com.gametechbc.traveloptics.init.TravelopticsEffects;
import com.gametechbc.traveloptics.init.TravelopticsSounds;
import com.gametechbc.traveloptics.util.TravelopticsParticleHelper;
import com.github.L_Ender.cataclysm.entity.effect.Boltstrike_Entity;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.joml.Vector3f;

import java.util.*;

public class DarkRainFallAoe extends AoeEntity implements AntiMagicSusceptible {
    private static final EntityDataAccessor<Float> FOG_R = SynchedEntityData.defineId(DarkRainFallAoe.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FOG_G = SynchedEntityData.defineId(DarkRainFallAoe.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FOG_B = SynchedEntityData.defineId(DarkRainFallAoe.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> FOG_SCALE = SynchedEntityData.defineId(DarkRainFallAoe.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Boolean> IS_RAIN = SynchedEntityData.defineId(DarkRainFallAoe.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> IS_BOLT = SynchedEntityData.defineId(DarkRainFallAoe.class, EntityDataSerializers.BOOLEAN);
    private static final ParticleEmitterInfo YELLOW_LIGHTNING = new ParticleEmitterInfo(BHSpells.id("yellow_lightning"));
    private static final float CAST_EFFECT_STRIKE_DAMAGE = 30F;

    private final Set<UUID> struckCastEffectTargets = new HashSet<>();

    private static final int BOLT_STRIKE_INTERVAL_TICKS = 30;
    private static final float BOLT_STRIKE_DAMAGE = 0F;

    private int wetEffectAmplifier;
    private int tickCounter;
    private final List<PendingBoltStrike> pendingBoltStrikes = new ArrayList<>();

    public DarkRainFallAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.wetEffectAmplifier = 0;
        this.duration = 400;
    }

    public DarkRainFallAoe(Level level) {
        this(EntityRegistry.DARK_RAIN_FALL.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FOG_R, 1f);
        this.entityData.define(FOG_G, 1f);
        this.entityData.define(FOG_B, 1f);
        this.entityData.define(FOG_SCALE, 1.5f);
        this.entityData.define(IS_RAIN, true);
        this.entityData.define(IS_BOLT, true);
    }

    public boolean isRain() {
        return this.entityData.get(IS_RAIN);
    }

    public void setRain(boolean isRain) {
        this.entityData.set(IS_RAIN, isRain);
    }

    public boolean isBolt() {
        return this.entityData.get(IS_BOLT);
    }

    public void setBolt(boolean isBolt) {
        this.entityData.set(IS_BOLT, isBolt);
    }

    public float getFogR() {
        return this.entityData.get(FOG_R);
    }

    public float getFogG() {
        return this.entityData.get(FOG_G);
    }

    public float getFogB() {
        return this.entityData.get(FOG_B);
    }

    public float getFogScale() {
        return this.entityData.get(FOG_SCALE);
    }

    public void setFogColor(float r, float g, float b) {
        this.entityData.set(FOG_R, r);
        this.entityData.set(FOG_G, g);
        this.entityData.set(FOG_B, b);
    }

    public void setFogScale(float scale) {
        this.entityData.set(FOG_SCALE, scale);
    }

    public int getWetEffectAmplifier() {
        return this.wetEffectAmplifier;
    }

    public void setWetEffectAmplifier(int wetEffectAmplifier) {
        this.wetEffectAmplifier = wetEffectAmplifier;
    }

    public void tick() {
        super.tick();
        ++this.tickCounter;
        if (!this.level().isClientSide && this.tickCounter == 1 && this.isRain()) {
            this.playSound(TravelopticsSounds.RAINFALL_ACTIVE.get(), 1.5F, 1.0F);
        }

        if (!this.level().isClientSide && this.tickCount % 90 == 0 && this.isRain()) {
            this.playSound(TravelopticsSounds.RAINFALL_ACTIVE.get(), 1.5F, 1.0F);
        }

        if (this.tickCount % 5 == 0 && this.isRain()) {
            this.applyWetEffectToEntities();
            this.extinguishEntitiesFire();
        }

        if (this.isRain()) {
            this.spawnRainParticles();
        }

        if (this.tickCount % 5 == 0) {
            this.spawnCloudParticles();
        }

        if (!this.level().isClientSide && this.isBolt()) {
            if (this.tickCounter % BOLT_STRIKE_INTERVAL_TICKS == 0) {
                this.queueBoltStrike();
            }
            this.processPendingBoltStrikes();
        }

        if (!this.level().isClientSide) {
            this.checkCastEffectTargets();
        }

        if (!this.level().isClientSide && this.tickCount >= this.duration) {
            this.discard();
        }
    }

    private void applyWetEffectToEntities() {
        this.level().getEntities(this, this.getBoundingBox(), (entity) -> entity instanceof LivingEntity).forEach((entity) -> {
            LivingEntity target = (LivingEntity)entity;
            target.addEffect(new MobEffectInstance(TravelopticsEffects.WET.get(), 400, this.getWetEffectAmplifier(), false, false, true));
        });
    }

    private void extinguishEntitiesFire() {
        this.level().getEntities(this, this.getBoundingBox(), (entity) -> entity instanceof LivingEntity).forEach(Entity::clearFire);
    }

    private void spawnRainParticles() {
        double radius = this.getRadius();
        int particleCount = (int)(6.0F * this.getRadius());

        for(int i = 0; i < particleCount; ++i) {
            double randomX = this.getX() + (this.random.nextDouble() * (double)2.0F * radius - radius);
            double randomY = this.getY() + (double)10.0F;
            double randomZ = this.getZ() + (this.random.nextDouble() * (double)2.0F * radius - radius);
            double velocityY = (double)-0.5F - this.random.nextDouble() * (double)0.5F;
            this.level().addParticle(TravelopticsParticleHelper.WATER_DROP, randomX, randomY, randomZ, 0.0F, velocityY, 0.0F);
        }

    }

    private void spawnCloudParticles() {
        double radius = this.getRadius();
        int particleCount = (int)(1.0F * this.getRadius());
        ParticleOptions fog = new FogParticleOptions(new Vector3f(getFogR(), getFogG(), getFogB()), getFogScale());

        for (int i = 0; i < particleCount; ++i) {
            double randomX = this.getX() + (this.random.nextDouble() * 2.0F * radius - radius);
            double randomY = this.getY() + 12.0F;
            double randomZ = this.getZ() + (this.random.nextDouble() * 2.0F * radius - radius);
            double velocityY = -0.5F - this.random.nextDouble() * 0.5F;
            this.level().addParticle(fog, randomX, randomY, randomZ, 0.0F, velocityY, 0.0F);
        }
    }

    private void queueBoltStrike() {
        double radius = this.getRadius();
        double randomX = this.getX() + (this.random.nextDouble() * 2.0F * radius - radius);
        double randomZ = this.getZ() + (this.random.nextDouble() * 2.0F * radius - radius);
        Vec3 targetPos = new Vec3(randomX, this.getY(), randomZ);

        this.pendingBoltStrikes.add(new PendingBoltStrike(targetPos, this.tickCount));
    }

    private void processPendingBoltStrikes() {
        Iterator<PendingBoltStrike> iterator = this.pendingBoltStrikes.iterator();
        while (iterator.hasNext()) {
            PendingBoltStrike pending = iterator.next();
            if (this.tickCount >= pending.spawnAtTick) {
                this.spawnYellowBoltAt(pending.position);
                iterator.remove();
            }
        }
    }

    private void spawnYellowBoltAt(Vec3 position) {
        LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
        Boltstrike_Entity bolt = new Boltstrike_Entity(this.level(), position.x, position.y, position.z, 0.0F, 0, BOLT_STRIKE_DAMAGE, owner);
        bolt.setR(255);
        bolt.setG(255);
        bolt.setB(0);
        this.level().addFreshEntity(bolt);
    }

    private void checkCastEffectTargets() {
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox());
        Set<UUID> currentlyHasEffect = new HashSet<>();
        for (LivingEntity target : nearby) {
            if (target.hasEffect(MobEffectsRegistry.CHECK_CAST.get())) {
                currentlyHasEffect.add(target.getUUID());
                if (!this.struckCastEffectTargets.contains(target.getUUID())) {
                    this.struckCastEffectTargets.add(target.getUUID());
                    this.strikeCastEffectTarget(target);
                }
            }
        }
        this.struckCastEffectTargets.removeIf(id -> !currentlyHasEffect.contains(id));
    }

    private void strikeCastEffectTarget(LivingEntity target) {
        Vec3 pos = target.position();

        AAALevel.addParticle(this.level(), 64.0, YELLOW_LIGHTNING.clone().position(pos.x, pos.y + 1.0, pos.z));

        LivingEntity owner = this.getOwner() instanceof LivingEntity le ? le : null;
        DamageSource damageSource = BHSpellRegistry.ULTRASHOCK.get().getDamageSource(this, owner);
        DamageSources.applyDamage(target, CAST_EFFECT_STRIKE_DAMAGE, damageSource);
        this.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.NEUTRAL, 1, 1);
    }

    public void applyEffect(LivingEntity target) {
    }

    public float getParticleCount() {
        return 0.0F;
    }

    protected float particleYOffset() {
        return 0.25F;
    }

    protected float getParticleSpeedModifier() {
        return 1.4F;
    }

    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    public void onAntiMagic(MagicData magicData) {
        this.discard();
    }

    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("WetEffectAmplifier", this.wetEffectAmplifier);
        compound.putFloat("FogR", getFogR());
        compound.putFloat("FogG", getFogG());
        compound.putFloat("FogB", getFogB());
        compound.putFloat("FogScale", getFogScale());
        compound.putBoolean("IsRain", this.isRain());
        compound.putBoolean("IsBolt", this.isBolt());
    }

    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("WetEffectAmplifier", 3)) {
            this.wetEffectAmplifier = compound.getInt("WetEffectAmplifier");
        }
        if (compound.contains("FogR")) {
            setFogColor(compound.getFloat("FogR"), compound.getFloat("FogG"), compound.getFloat("FogB"));
        }
        if (compound.contains("FogScale")) {
            setFogScale(compound.getFloat("FogScale"));
        }
        if (compound.contains("IsRain")) {
            this.setRain(compound.getBoolean("IsRain"));
        }
        if (compound.contains("IsBolt")) {
            this.setBolt(compound.getBoolean("IsBolt"));
        }
    }

    private static class PendingBoltStrike {
        final Vec3 position;
        final int spawnAtTick;

        PendingBoltStrike(Vec3 position, int spawnAtTick) {
            this.position = position;
            this.spawnAtTick = spawnAtTick;
        }
    }
}
