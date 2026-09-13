package net.offkung.bhspells.entity.spells.blessing_snow;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class RadiusSnowRingEntity extends AoeEntity {
    public static final int LIFETIME_TICKS = 300; // 15 seconds

    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(RadiusSnowRingEntity.class, EntityDataSerializers.INT);

    private final Map<UUID, HealEntry> healTargets = new HashMap<>();

    public RadiusSnowRingEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.duration = LIFETIME_TICKS;
        this.setCircular();
    }

    public RadiusSnowRingEntity(Level level) {
        this(EntityRegistry.RADIUS_SNOW_RING.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CASTER_ID, 0);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        super.setOwner(entity);
        if (entity != null) {
            this.entityData.set(CASTER_ID, entity.getId());
        }
    }

    @Override
    public Entity getOwner() {
        Entity owner = super.getOwner();
        if (owner != null) {
            return owner;
        }
        int id = this.entityData.get(CASTER_ID);
        if (id > 0) {
            return this.level().getEntity(id);
        }
        return null;
    }

    public void addHealTarget(LivingEntity target, double distance) {
        float totalHeal;
        float healPerSecond;
        if (distance <= 5.0) {
            totalHeal = 300f;
            healPerSecond = 20f;
        } else if (distance <= 10.0) {
            totalHeal = 240f;
            healPerSecond = 16f;
        } else if (distance <= 20.0) {
            totalHeal = 180f;
            healPerSecond = 12f;
        } else if (distance <= 30.0) {
            totalHeal = 120f;
            healPerSecond = 8f;
        } else {
            return;
        }
        healTargets.put(target.getUUID(), new HealEntry(totalHeal, healPerSecond));
    }

    public boolean hasHealTarget(UUID uuid) {
        return healTargets.containsKey(uuid);
    }

    public void removeHealTarget(UUID uuid) {
        healTargets.remove(uuid);
    }

    @Override
    public void tick() {
        super.tick();

        // Follow caster at all times on both client and server
        Entity owner = getOwner();
        if (owner != null && owner.isAlive()) {
            this.setPos(owner.getX(), owner.getY(), owner.getZ());
        }

        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }

        // Apply periodic healing on server: 1 second per specified HP
        if (this.level() instanceof ServerLevel serverLevel) {
            Iterator<Map.Entry<UUID, HealEntry>> it = healTargets.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<UUID, HealEntry> entry = it.next();
                HealEntry healEntry = entry.getValue();
                if (healEntry.totalHealRemaining <= 0) {
                    it.remove();
                    continue;
                }

                healEntry.ticksUntilNextHeal--;
                if (healEntry.ticksUntilNextHeal <= 0) {
                    healEntry.ticksUntilNextHeal = 20; // 1 second (20 ticks)

                    Entity target = serverLevel.getEntity(entry.getKey());
                    if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                        double currentDist = this.distanceTo(livingTarget);

                        // If the selected entity goes away from the RadiusSnowRingEntity, it won't get heal anymore!
                        if (currentDist > this.getRadius()) {
                            continue;
                        }

                        // Determine healing amount based on distance from the caster/ring
                        float healPerSecond;
                        if (currentDist <= 5.0) {
                            healPerSecond = 20f;
                        } else if (currentDist <= 10.0) {
                            healPerSecond = 16f;
                        } else if (currentDist <= 20.0) {
                            healPerSecond = 12f;
                        } else if (currentDist <= 30.0) {
                            healPerSecond = 8f;
                        } else {
                            continue;
                        }

                        float healAmount = Math.min(healPerSecond, healEntry.totalHealRemaining);
                        if (healAmount > 0) {
                            livingTarget.heal(healAmount);
                            healEntry.totalHealRemaining -= healAmount;

                            // Snowflake and heal particles on the entity
                            MagicManager.spawnParticles(level(), ParticleRegistry.CLEANSE_PARTICLE.get(), livingTarget.getX(), livingTarget.getY() + livingTarget.getBbHeight() * 0.5, livingTarget.getZ(), 10, 0.4, 0.4, 0.4, 0, true);
                            serverLevel.playSound(null, livingTarget.getX(), livingTarget.getY(), livingTarget.getZ(), SoundEvents.CONDUIT_ACTIVATE, SoundSource.NEUTRAL, 0.8f, 2.0f);
                        }
                    }
                }
            }
        }

        if (this.tickCount >= LIFETIME_TICKS) {
            this.discard();
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    public float getParticleCount() {
        return 0.2f * getRadius();
    }

    @Override
    protected float particleYOffset() {
        return 0.25f;
    }

    @Override
    protected float getParticleSpeedModifier() {
        return 1.4f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void ambientParticles() {
        if (!this.level().isClientSide)
            return;
        ambientParticles(ParticleHelper.SNOWFLAKE);
        ambientParticles(ParticleHelper.SNOW_DUST);

        // Circular snowflake ring outline
        float radius = getRadius();
        int ringParticles = Math.max(4, (int) (radius * 0.8f));
        for (int i = 0; i < ringParticles; i++) {
            double angle = this.random.nextDouble() * Math.PI * 2.0;
            double px = getX() + radius * Math.cos(angle);
            double pz = getZ() + radius * Math.sin(angle);
            this.level().addParticle(ParticleHelper.SNOWFLAKE, px, getY() + 0.2, pz, 0, 0.01, 0);
        }

        // Random wave pattern of snowflakes rising from ground to sky
        spawnRisingWaveSnowflakes(radius);
    }

    private void spawnRisingWaveSnowflakes(float radius) {
        int waveCount = Math.max(4, (int) (radius * 0.4f));

        for (int i = 0; i < waveCount; i++) {
            double dist = radius * Math.sqrt(this.random.nextDouble());
            double angle = this.random.nextDouble() * Math.PI * 2.0;

            double px = getX() + dist * Math.cos(angle);
            double pz = getZ() + dist * Math.sin(angle);
            double py = getY() + 0.05 + this.random.nextDouble() * 0.15;

            // Wave pattern calculation: distance, angle, and time form a traveling wave
            double wavePhase = (dist * 0.7) + (angle * 3.0) + (this.tickCount * 0.2);
            double waveMod = Math.sin(wavePhase);

            // Upward velocity towards the sky with wave pulsation
            double vy = 0.22 + 0.12 * (waveMod + 1.0) * 0.5 + this.random.nextDouble() * 0.14;

            // Horizontal wave swaying motion as the snowflake rises
            double swayAngle = wavePhase + (this.random.nextDouble() * 0.5);
            double swaySpeed = 0.04 + 0.04 * Math.abs(waveMod);
            double vx = Math.cos(swayAngle) * swaySpeed;
            double vz = Math.sin(swayAngle) * swaySpeed;

            ParticleOptions particle = (this.random.nextBoolean()) ? ParticleHelper.SNOWFLAKE : ParticleTypes.SNOWFLAKE;

            this.level().addParticle(particle, px, py, pz, vx, vy, vz);
        }

        // Expanding ground-to-sky wave ripples
        double waveRadius = ((this.tickCount * 0.35) % radius);
        int rippleCount = Math.max(4, (int) (waveRadius * 1.5f));
        for (int i = 0; i < rippleCount; i++) {
            double rAngle = this.random.nextDouble() * Math.PI * 2.0;
            double rDist = waveRadius + (this.random.nextDouble() - 0.5) * 0.6;
            if (rDist > 0 && rDist <= radius) {
                double rx = getX() + rDist * Math.cos(rAngle);
                double rz = getZ() + rDist * Math.sin(rAngle);
                double ry = getY() + 0.05;

                double rWavePhase = rDist * 0.5 + (this.tickCount * 0.25);
                double rvx = Math.cos(rAngle + rWavePhase) * 0.05;
                double rvz = Math.sin(rAngle + rWavePhase) * 0.05;
                double rvy = 0.30 + this.random.nextDouble() * 0.20;

                this.level().addParticle(ParticleHelper.SNOWFLAKE, rx, ry, rz, rvx, rvy, rvz);
            }
        }
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (!this.level().isClientSide && reason.shouldDestroy()) {
            Entity owner = getOwner();
            if (owner instanceof LivingEntity livingOwner && livingOwner.isAlive()) {
                livingOwner.addEffect(new MobEffectInstance(MobEffectsRegistry.BLESSING_SNOW.get(), 20, 0, false, false, true));
            }
        }
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        super.addAdditionalSaveData(compound);
        compound.putInt("CasterId", this.entityData.get(CASTER_ID));
        ListTag targetsList = new ListTag();
        for (Map.Entry<UUID, HealEntry> entry : healTargets.entrySet()) {
            CompoundTag tag = new CompoundTag();
            tag.putUUID("UUID", entry.getKey());
            tag.putFloat("Remaining", entry.getValue().totalHealRemaining);
            tag.putFloat("PerSecond", entry.getValue().healPerSecond);
            tag.putInt("TicksUntilNext", entry.getValue().ticksUntilNextHeal);
            targetsList.add(tag);
        }
        compound.put("HealTargets", targetsList);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        super.readAdditionalSaveData(compound);
        if (compound.contains("CasterId")) {
            this.entityData.set(CASTER_ID, compound.getInt("CasterId"));
        }
        healTargets.clear();
        if (compound.contains("HealTargets", Tag.TAG_LIST)) {
            ListTag targetsList = compound.getList("HealTargets", Tag.TAG_COMPOUND);
            for (int i = 0; i < targetsList.size(); i++) {
                CompoundTag tag = targetsList.getCompound(i);
                UUID uuid = tag.getUUID("UUID");
                float remaining = tag.getFloat("Remaining");
                float perSecond = tag.contains("PerSecond") ? tag.getFloat("PerSecond") : 20f;
                int ticks = tag.contains("TicksUntilNext") ? tag.getInt("TicksUntilNext") : 20;
                HealEntry entry = new HealEntry(remaining, perSecond);
                entry.ticksUntilNextHeal = ticks;
                healTargets.put(uuid, entry);
            }
        }
    }

    private static class HealEntry {
        float totalHealRemaining;
        final float healPerSecond;
        int ticksUntilNextHeal;

        HealEntry(float totalHeal, float healPerSecond) {
            this.totalHealRemaining = totalHeal;
            this.healPerSecond = healPerSecond;
            this.ticksUntilNextHeal = 20; // 1 second interval
        }
    }
}
