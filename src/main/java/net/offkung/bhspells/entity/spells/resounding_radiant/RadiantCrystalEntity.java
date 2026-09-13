package net.offkung.bhspells.entity.spells.resounding_radiant;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

public class RadiantCrystalEntity extends AoeEntity {
    private static final EntityDataAccessor<Float> DATA_SIZE = SynchedEntityData.defineId(RadiantCrystalEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Integer> DATA_WAIT_TIME = SynchedEntityData.defineId(RadiantCrystalEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DATA_REST_TIME = SynchedEntityData.defineId(RadiantCrystalEntity.class, EntityDataSerializers.INT);
    public static final int RISE_TIME = 4;
    public static final int REST_TIME = 40;
    public static final int LOWER_TIME = 30;
    private final List<Entity> victims;

    public RadiantCrystalEntity(EntityType<? extends AoeEntity> entityType, Level level) {
        super(entityType, level);
        this.victims = new ArrayList<>();
    }

    public RadiantCrystalEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.RADIANT_CRYSTAL.get(), level);
        setOwner(owner);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SIZE, 1f);
        this.entityData.define(DATA_WAIT_TIME, 8);
        this.entityData.define(DATA_REST_TIME, REST_TIME);
    }

    public int getRestTime() {
        return this.entityData.get(DATA_REST_TIME);
    }

    public void setRestTime(int i) {
        this.entityData.set(DATA_REST_TIME, i);
    }

    public float getCrystalSize() {
        return this.entityData.get(DATA_SIZE);
    }

    public void setCrystalSize(float f) {
        this.entityData.set(DATA_SIZE, f);
        this.refreshDimensions();
    }

    public int getWaitTime() {
        return this.entityData.get(DATA_WAIT_TIME);
    }

    public void setWaitTime(int i) {
        this.entityData.set(DATA_WAIT_TIME, i);
    }

    public float getPositionOffset(float partialTick) {
        float f = this.tickCount + partialTick;
        int waitTime = getWaitTime();
        int restTime = getRestTime();
        if (f < waitTime) {
            return -1;
        } else if (f < waitTime + RISE_TIME) {
            f = (f - waitTime) / RISE_TIME;
            return (Mth.sin(f * Mth.PI) / Mth.PI) + f - 1f;
        } else if (f < waitTime + RISE_TIME + restTime) {
            return 0f;
        } else {
            f = Mth.clamp((f - (waitTime + RISE_TIME + restTime)) / LOWER_TIME, 0, 1) + 1;
            return -((Mth.sin(f * Mth.PI) / Mth.PI) + f - 1f);
        }
    }

    @Override
    public void tick() {
        this.refreshDimensions();
        int waitTime = getWaitTime();
        if (tickCount == waitTime) {
            if (!level().isClientSide) {
                float f = getCrystalSize();
                if (!this.isSilent()) {
                    level().playSound(null, this.blockPosition(), BHSoundRegistry.GROUND_BREAKING.get(), SoundSource.NEUTRAL, 1.25f * getCrystalSize(), Mth.randomBetweenInclusive(Utils.random, 6, 12) * .1f);
                }
                MagicManager.spawnParticles(level(), ParticleRegistry.RADIANT_SHATTER.get(), getX(), level().clip(new ClipContext(position().add(0, 2, 0), position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getLocation().y() + 0.1, getZ(), (int) (3 * f * f), 0.1 * f, 0.1 * f, 0.1f * f, 0.12 * f, false);
                MagicManager.spawnParticles(level(), ParticleTypes.GLOW, getX(), level().clip(new ClipContext(position().add(0, 2, 0), position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this)).getLocation().y() + 0.1, getZ(), (int) (2 * f * f), 0.1 * f, 0.1 * f, 0.1f * f, 0.08 * f, false);
            }
        } else if (tickCount > waitTime && tickCount < waitTime + RISE_TIME) {
            AABB damager = this.getBoundingBox();
            damager = new AABB(damager.minX, damager.minY, damager.minZ, damager.maxX, this.getY() + (damager.getYsize() * (getPositionOffset(0) + 1)), damager.maxZ);
            for (Entity entity : level().getEntities(this, damager).stream().filter(target -> canHitEntity(target) && !victims.contains(target)).collect(Collectors.toSet())) {
                if (DamageSources.applyDamage(entity, damage, BHSpellRegistry.SHINING_RADIANT.get().getDamageSource(this, getOwner()))) {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0, this.getCrystalSize() * 0.3, 0));
                    entity.hurtMarked = true;
                }
                victims.add(entity);
                if (entity instanceof ShieldPart || entity instanceof AbstractShieldEntity) {
                    discard();
                    return;
                }
            }
        } else if (tickCount > waitTime + RISE_TIME + getRestTime() + LOWER_TIME) {
            discard();
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putInt("waitTime", this.getWaitTime());
        pCompound.putInt("restTime", this.getRestTime());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        this.setWaitTime(pCompound.getInt("waitTime"));
        if (pCompound.contains("restTime")) {
            this.setRestTime(pCompound.getInt("restTime"));
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {}

    @Override
    public EntityDimensions getDimensions(Pose pPose) {
        return EntityDimensions.scalable(this.getCrystalSize() * 0.3f, this.getCrystalSize() * 0.7f * (this.getPositionOffset(1) + 1));
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public void ambientParticles() {
        return;
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    public void recreateFromPacket(ClientboundAddEntityPacket pPacket) {
        super.recreateFromPacket(pPacket);
        this.xRotO = this.getXRot();
        this.yRotO = this.getYRot();
    }
}
