package net.offkung.bhspells.entity.spells.purple_wave;

import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.ConePart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.offkung.bhspells.registry.DamageSourcesRegistry;
import net.offkung.bhspells.registry.EntityRegistry;

public class PurpleWaveProjectile extends AbstractConeProjectile {
    private static final EntityDataAccessor<Integer> LIFESPAN_TICKS = SynchedEntityData.defineId(PurpleWaveProjectile.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> DAMAGE_INTERVAL_TICKS = SynchedEntityData.defineId(PurpleWaveProjectile.class, EntityDataSerializers.INT);

    public PurpleWaveProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public PurpleWaveProjectile(Level level, LivingEntity owner) {
        super(EntityRegistry.PURPLE_WAVE_PROJECTILE.get(), level, owner);
        this.entityData.set(LIFESPAN_TICKS, 200);
        this.entityData.set(DAMAGE_INTERVAL_TICKS, 10);
        this.damage = 10.0f;
        this.subEntities[0] = new ConePart(this, "part1", 2.0F, 1.0F);
        this.subEntities[1] = new ConePart(this, "part2", 2.0F, 1.0F);
        this.subEntities[2] = new ConePart(this, "part3", 2.0F, 1.0F);
        this.subEntities[3] = new ConePart(this, "part4", 2.0F, 1.0F);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(LIFESPAN_TICKS, 200);
        this.entityData.define(DAMAGE_INTERVAL_TICKS, 10);
    }

    public void setLifespanTicks(int ticks) {
        this.entityData.set(LIFESPAN_TICKS, ticks);
    }

    public int getLifespanTicks() {
        return this.entityData.get(LIFESPAN_TICKS);
    }

    public void setDamageIntervalTicks(int ticks) {
        this.entityData.set(DAMAGE_INTERVAL_TICKS, Math.max(1, ticks));
    }

    public int getDamageIntervalTicks() {
        return this.entityData.get(DAMAGE_INTERVAL_TICKS);
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (this.age % this.getDamageIntervalTicks() == 0) {
                this.setDealDamageActive();
            }
        }

        super.tick();

        if (!level().isClientSide && this.age >= this.getLifespanTicks()) {
            this.discard();
        }
    }

    @Override
    public void spawnParticles() {
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        DamageSources.applyDamage(entity, this.damage, DamageSourcesRegistry.groundSpell(level(), getOwner()));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("LifespanTicks", this.getLifespanTicks());
        tag.putInt("DamageIntervalTicks", this.getDamageIntervalTicks());
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("LifespanTicks")) {
            this.setLifespanTicks(tag.getInt("LifespanTicks"));
        }
        if (tag.contains("DamageIntervalTicks")) {
            this.setDamageIntervalTicks(tag.getInt("DamageIntervalTicks"));
        }
    }
}
