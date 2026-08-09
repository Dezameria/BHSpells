package net.offkung.bhspells.entity.spells.fiery_dance;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.offkung.bhspells.event.GoldenMarbleManager;
import net.offkung.bhspells.registry.EntityRegistry;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.UUID;

public class GoldenMarbleEntity extends Entity {
    private static final EntityDataAccessor<Integer> OWNER_ID = SynchedEntityData.defineId(GoldenMarbleEntity.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPEED_MULTIPLIER = SynchedEntityData.defineId(GoldenMarbleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<Float> ANGLE = SynchedEntityData.defineId(GoldenMarbleEntity.class, EntityDataSerializers.FLOAT);

    @Nullable
    private LivingEntity owner;
    @Nullable private UUID ownerUUID;
    private float angle;
    private final float orbitRadius = 1.4f;
    private final float orbitHeightOffset = 1.1f;
    private int lifetimeTicks = 200;

    public GoldenMarbleEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.setNoGravity(true);
    }

    public GoldenMarbleEntity(Level level, LivingEntity owner, float angleOffset) {
        this(EntityRegistry.GOLDEN_MARBLE.get(), level);
        this.owner = owner;
        this.ownerUUID = owner.getUUID();
        this.angle = angleOffset;
        this.entityData.set(OWNER_ID, owner.getId());
        this.entityData.set(ANGLE, angleOffset);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(OWNER_ID, -1);
        this.entityData.define(SPEED_MULTIPLIER, 1.0f);
        this.entityData.define(ANGLE, 0.0f);
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.owner != null && this.owner.isAlive()) {
            return this.owner;
        }
        if (this.level() instanceof ServerLevel serverLevel) {
            if (this.ownerUUID != null) {
                Entity entity = serverLevel.getEntity(this.ownerUUID);
                if (entity instanceof LivingEntity livingEntity) {
                    this.owner = livingEntity;
                }
            }
        } else {
            int id = this.entityData.get(OWNER_ID);
            if (id != -1) {
                Entity entity = this.level().getEntity(id);
                if (entity instanceof LivingEntity livingEntity) {
                    this.owner = livingEntity;
                }
            }
        }
        return this.owner;
    }

    public void setLifetimeTicks(int ticks) {
        this.lifetimeTicks = ticks;
    }

    @Override
    public void tick() {
        super.tick();

        LivingEntity currentOwner = getOwner();
        if (currentOwner == null || !currentOwner.isAlive()) {
            if (!level().isClientSide) {
                this.discard();
            }
            return;
        }

        float speedMultiplier;
        if (!level().isClientSide) {
            speedMultiplier = GoldenMarbleManager.getSpeedMultiplier(currentOwner.getUUID());
            this.entityData.set(SPEED_MULTIPLIER, speedMultiplier);
            this.angle += GoldenMarbleManager.getBaseAngularSpeed() * speedMultiplier;
            this.entityData.set(ANGLE, this.angle);
        } else {
            speedMultiplier = this.entityData.get(SPEED_MULTIPLIER);
            this.angle = this.entityData.get(ANGLE);
        }

        double x = currentOwner.getX() + Math.cos(angle) * orbitRadius;
        double z = currentOwner.getZ() + Math.sin(angle) * orbitRadius;
        double y = currentOwner.getY() + orbitHeightOffset;
        this.setPos(x, y, z);

        if (!level().isClientSide && this.tickCount > lifetimeTicks) {
            this.discard();
        }
    }

    @Override
    public void lerpTo(double x, double y, double z, float yRot, float xRot, int steps, boolean teleport) {
    }

    @Override
    public void remove(@NotNull RemovalReason reason) {
        if (!level().isClientSide && this.ownerUUID != null) {
            GoldenMarbleManager.onMarbleRemoved(this.ownerUUID, this.getId());
        }
        super.remove(reason);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.hasUUID("Owner")) this.ownerUUID = tag.getUUID("Owner");
        if (tag.contains("Angle")) this.angle = tag.getFloat("Angle");
        if (tag.contains("LifetimeTicks")) this.lifetimeTicks = tag.getInt("LifetimeTicks");
    }

    @Override
    protected void addAdditionalSaveData(@NotNull CompoundTag tag) {
        if (this.ownerUUID != null) tag.putUUID("Owner", this.ownerUUID);
        tag.putFloat("Angle", this.angle);
        tag.putInt("LifetimeTicks", this.lifetimeTicks);
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    @Override
    public @NotNull Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
