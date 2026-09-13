package net.offkung.bhspells.entity.spells.heaven_lion;

import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.ConePart;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.spells.fire.HeavenLionSpell;

public class HeavenLionProjectile extends AbstractConeProjectile {
    private static final int LIFESPAN_TICKS = 10;

    private static final int CLOUD_BURST_END_TICK = 3;

    public HeavenLionProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
    }

    public HeavenLionProjectile(Level level, LivingEntity owner) {
        super(EntityRegistry.HEAVEN_LION_PROJECTILE.get(), level, owner);
        this.subEntities[0] = new ConePart(this, "part1", 3.0F, 3.0F);
        this.subEntities[1] = new ConePart(this, "part2", 6.0F, 5.0F);
        this.subEntities[2] = new ConePart(this, "part3", 9.0F, 7.0F);
        this.subEntities[3] = new ConePart(this, "part4", 12.0F, 10.0F);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && this.age >= LIFESPAN_TICKS) {
            this.discard();
        }
    }

    @Override
    public void spawnParticles() {
        var owner = getOwner();
        if (!level().isClientSide || owner == null) {
            return;
        }

        if (this.age <= CLOUD_BURST_END_TICK) {
            spawnCloudBurst((LivingEntity) owner);
        }
    }

    private void spawnCloudBurst(LivingEntity owner) {
        Vec3 rotation = owner.getLookAngle().normalize();
        var basePos = owner.position().add(rotation.scale(1.6));
        double eyeY = owner.getEyeHeight() * .9f;

        double speed = random.nextDouble() * .35 + .35;

        int steps = 6;
        int particlesPerStep = 26;

        for (int step = 0; step < steps; step++) {
            double distanceAlongCone = 2.0 + step * (20.0 / steps);
            double lateralSpread = Mth.lerp((double) step / (steps - 1), 1.0, 5.0);

            Vec3 stepPos = basePos.add(rotation.scale(distanceAlongCone));
            double x = stepPos.x;
            double y = stepPos.y + eyeY;
            double z = stepPos.z;

            for (int i = 0; i < particlesPerStep; i++) {
                double ox = (Math.random() * 2 * lateralSpread) - lateralSpread;
                double oy = (Math.random() * 2 * lateralSpread) - lateralSpread;
                double oz = (Math.random() * 2 * lateralSpread) - lateralSpread;

                double angularness = .5;
                Vec3 randomVec = new Vec3(Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness, Math.random() * 2 * angularness - angularness).normalize();
                Vec3 result = (rotation.scale(3).add(randomVec)).normalize().scale(speed);

                level().addParticle(ParticleTypes.CLOUD, x + ox, y + oy, z + oz, result.x, result.y, result.z);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (level().isClientSide) return;

        Entity hitEntity = entityHitResult.getEntity();
        if (hitEntity instanceof LivingEntity livingEntity) {
            HeavenLionSpell.applyBlessing(livingEntity);
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
    }
}
