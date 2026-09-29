package net.offkung.bhspells.entity.spells.feather_strike;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.IEntityAdditionalSpawnData;
import net.minecraftforge.network.NetworkHooks;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.DamageSourcesRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.particle.EpicFightParticles;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class FeatherStrike extends AbstractMagicProjectile implements IEntityAdditionalSpawnData {
    private static final int MAX_LIFETIME = 120; // 6 seconds max
    private int delayTicks = 0;
    private Vec3 initialDirection = Vec3.ZERO;
    private boolean hasLaunched = false;

    private float seekAmount = 0.6f;

    public FeatherStrike(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public FeatherStrike(Level level, LivingEntity owner) {
        this(EntityRegistry.FEATHER_STRIKE.get(), level);
        this.setOwner(owner);
    }

    public void setDelayTicks(int delayTicks) {
        this.delayTicks = delayTicks;
    }

    public int getDelayTicks() {
        return this.delayTicks;
    }

    public void setSeekAmount(float seekAmount) {
        this.seekAmount = seekAmount;
    }

    public float getSeekAmount() {
        return this.seekAmount;
    }

    public void setInitialDirection(Vec3 direction) {
        this.initialDirection = direction.normalize();
        double horiz = this.initialDirection.horizontalDistance();
        this.setYRot((float) (Mth.atan2(this.initialDirection.x, this.initialDirection.z) * (180.0F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(this.initialDirection.y, horiz) * (180.0F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    public void setTarget(@Nullable LivingEntity target) {
        if (target != null) {
            this.setHomingTarget(target);
        } else {
            this.cachedHomingTarget = null;
            this.homingTargetUUID = null;
        }
    }

    @Override
    public void shoot(double x, double y, double z, float velocity, float inaccuracy) {
        super.shoot(x, y, z, velocity, inaccuracy);
        this.hasLaunched = true;
        if (this.getDeltaMovement().lengthSqr() > 1.0E-4) {
            this.initialDirection = this.getDeltaMovement().normalize();
        }
    }

    @Override
    public void shoot(Vec3 motion) {
        this.hasLaunched = true;
        this.initialDirection = motion.normalize();
        this.setDeltaMovement(this.initialDirection.scale(getSpeed()));
        Vec3 vel = this.getDeltaMovement();
        double horiz = vel.horizontalDistance();
        this.setYRot((float) (Mth.atan2(vel.x, vel.z) * (180.0F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vel.y, horiz) * (180.0F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public float getSpeed() {
        return 1.7f;
    }

    @Override
    protected void handleEntityHoming() {
        Entity target = getHomingTarget();
        if (target instanceof LivingEntity livingTarget && livingTarget.isAlive() && !livingTarget.isRemoved()) {
            if (this.distanceTo(livingTarget) > 1.5F && this.tickCount < 40) {
                Vec3 targetVec = livingTarget.position().add(0.0D, (double) (0.85F * livingTarget.getBbHeight()), 0.0D).subtract(this.position()).normalize();
                float f = 1.0F - (this.seekAmount - 0.3F) * 0.3F;
                Vec3 blended = this.getDeltaMovement().scale((double) f).add(targetVec.scale((double) this.seekAmount));
                this.setDeltaMovement(blended.scale(0.9D));
            }
        }
    }

    @Override
    public void tick() {
        if (this.delayTicks > 0) {
            --this.delayTicks;
            // Face target while hovering
            Entity currentTarget = getHomingTarget();
            if (currentTarget instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                Vec3 toTarget = livingTarget.getBoundingBox().getCenter().subtract(this.position());
                if (toTarget.lengthSqr() > 1.0E-4) {
                    Vec3 dir = toTarget.normalize();
                    this.initialDirection = dir;
                    double horiz = dir.horizontalDistance();
                    this.setYRot((float) (Mth.atan2(dir.x, dir.z) * (180.0F / (float) Math.PI)));
                    this.setXRot((float) (Mth.atan2(dir.y, horiz) * (180.0F / (float) Math.PI)));
                    this.yRotO = this.getYRot();
                    this.xRotO = this.getXRot();
                }
            }
            if (this.level().isClientSide) {
                // Subtle floating wind particles while hovering
                if (this.random.nextFloat() < 0.3f) {
                    this.level().addParticle(ParticleTypes.CLOUD, this.getX(), this.getY(), this.getZ(), 0, 0.01, 0);
                }
            }
            if (this.delayTicks == 0) {
                launch();
            }
            return;
        }

        if (!this.hasLaunched) {
            launch();
        }

        super.tick();

        if (!this.level().isClientSide && this.tickCount > MAX_LIFETIME) {
            doImpact(this.position());
            this.discard();
        }
    }

    private void launch() {
        this.hasLaunched = true;
        Vec3 dir = this.initialDirection;
        if (dir.lengthSqr() < 1.0E-4) {
            Entity target = getHomingTarget();
            if (target instanceof LivingEntity livingTarget && livingTarget.isAlive()) {
                Vec3 toTarget = livingTarget.getBoundingBox().getCenter().subtract(this.position());
                if (toTarget.lengthSqr() > 1.0E-4) {
                    dir = toTarget.normalize();
                }
            }
        }
        if (dir.lengthSqr() < 1.0E-4) {
            dir = new Vec3(0, 0, 1);
        }
        this.initialDirection = dir.normalize();
        this.setDeltaMovement(this.initialDirection.scale(getSpeed()));
        Vec3 vel = this.getDeltaMovement();
        double horiz = vel.horizontalDistance();
        this.setYRot((float) (Mth.atan2(vel.x, vel.z) * (180.0F / (float) Math.PI)));
        this.setXRot((float) (Mth.atan2(vel.y, horiz) * (180.0F / (float) Math.PI)));
        this.yRotO = this.getYRot();
        this.xRotO = this.getXRot();

        if (!this.level().isClientSide) {
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 0.8F, 1.3F + this.random.nextFloat() * 0.3F);
        }
    }

    @Override
    public void trailParticles() {
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        MagicManager.spawnParticles(this.level(), ParticleTypes.SWEEP_ATTACK, x, y, z, 1, 0.1, 0.1, 0.1, 0.05, false);
        MagicManager.spawnParticles(this.level(), EpicFightParticles.FEATHER.get(), x, y, z, 4, 0.15, 0.15, 0.15, 0.3, false);
    }

    private void doImpact(Vec3 pos) {
        impactParticles(pos.x, pos.y, pos.z);
        this.level().playSound(null, pos.x, pos.y, pos.z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 0.9F, 1.4F);
    }

    @Override
    protected void onHitEntity(@NotNull EntityHitResult pResult) {
        if (this.level().isClientSide) {
            return;
        }
        Entity entity = pResult.getEntity();
        if (canHitEntity(entity)) {
            DamageSources.applyDamage(entity, this.damage, DamageSourcesRegistry.grassSpell(level(), getOwner()));
            doImpact(pResult.getLocation());
            this.discard();
        }
    }

    @Override
    protected void onHitBlock(@NotNull BlockHitResult pResult) {
        if (this.level().isClientSide) {
            return;
        }
        super.onHitBlock(pResult);
        doImpact(pResult.getLocation());
        this.discard();
    }

    @Override
    protected boolean canHitEntity(@NotNull Entity pTarget) {
        return pTarget != getOwner() && super.canHitEntity(pTarget);
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
        this.discard();
    }

    @Override
    public boolean shouldPierceShields() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DelayTicks", this.delayTicks);
        tag.putBoolean("HasLaunched", this.hasLaunched);
        tag.putFloat("SeekAmount", this.seekAmount);
        tag.putDouble("DirX", this.initialDirection.x);
        tag.putDouble("DirY", this.initialDirection.y);
        tag.putDouble("DirZ", this.initialDirection.z);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DelayTicks")) this.delayTicks = tag.getInt("DelayTicks");
        if (tag.contains("HasLaunched")) this.hasLaunched = tag.getBoolean("HasLaunched");
        if (tag.contains("SeekAmount")) this.seekAmount = tag.getFloat("SeekAmount");
        if (tag.contains("DirX")) {
            this.initialDirection = new Vec3(tag.getDouble("DirX"), tag.getDouble("DirY"), tag.getDouble("DirZ"));
        }
    }

    @Override
    public void writeSpawnData(FriendlyByteBuf buffer) {
        buffer.writeInt(this.delayTicks);
        buffer.writeBoolean(this.hasLaunched);
        buffer.writeFloat(this.seekAmount);
        Entity target = getHomingTarget();
        buffer.writeInt(target != null ? target.getId() : -1);
        buffer.writeDouble(this.initialDirection.x);
        buffer.writeDouble(this.initialDirection.y);
        buffer.writeDouble(this.initialDirection.z);
    }

    @Override
    public void readSpawnData(FriendlyByteBuf additionalData) {
        this.delayTicks = additionalData.readInt();
        this.hasLaunched = additionalData.readBoolean();
        this.seekAmount = additionalData.readFloat();
        int targetId = additionalData.readInt();
        if (targetId != -1 && this.level().getEntity(targetId) instanceof LivingEntity livingTarget) {
            this.setHomingTarget(livingTarget);
        }
        this.initialDirection = new Vec3(additionalData.readDouble(), additionalData.readDouble(), additionalData.readDouble());
        if (this.initialDirection.lengthSqr() > 1.0E-4) {
            double horiz = this.initialDirection.horizontalDistance();
            this.setYRot((float) (Mth.atan2(this.initialDirection.x, this.initialDirection.z) * (180.0F / (float) Math.PI)));
            this.setXRot((float) (Mth.atan2(this.initialDirection.y, horiz) * (180.0F / (float) Math.PI)));
            this.yRotO = this.getYRot();
            this.xRotO = this.getXRot();
        }
    }
}
