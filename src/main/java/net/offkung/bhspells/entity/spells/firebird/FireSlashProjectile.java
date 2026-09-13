package net.offkung.bhspells.entity.spells.firebird;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AbstractConeProjectile;
import io.redspace.ironsspellbooks.entity.spells.ConePart;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.*;
import net.offkung.bhspells.event.FireBodyManager;
import net.offkung.bhspells.registry.DamageSourcesRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class FireSlashProjectile extends AbstractConeProjectile implements AntiMagicSusceptible {
    private static final int LIFESPAN_TICKS = 20;
    private static final int DAMAGE_INTERVAL_TICKS = 15;

    private boolean markBased = false;
    private final Map<UUID, Integer> marks = new HashMap<>();
    private final Set<UUID> hitEntities = new HashSet<>();

    public FireSlashProjectile(EntityType<? extends AbstractConeProjectile> entityType, Level level) {
        super(entityType, level);
        this.subEntities[0] = new ConePart(this, "part1", 2.0F, 1.0F);
        this.subEntities[1] = new ConePart(this, "part2", 2.0F, 1.0F);
        this.subEntities[2] = new ConePart(this, "part3", 2.0F, 1.0F);
        this.subEntities[3] = new ConePart(this, "part4", 2.0F, 1.0F);
    }

    public FireSlashProjectile(Level level, LivingEntity owner) {
        super(EntityRegistry.FIRE_SLASH_PROJECTILE.get(), level, owner);
        this.subEntities[0] = new ConePart(this, "part1", 2.0F, 1.0F);
        this.subEntities[1] = new ConePart(this, "part2", 2.0F, 1.0F);
        this.subEntities[2] = new ConePart(this, "part3", 2.0F, 1.0F);
        this.subEntities[3] = new ConePart(this, "part4", 2.0F, 1.0F);
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public void setMarkBased(boolean markBased) {
        this.markBased = markBased;
    }

    public void setMarks(Map<UUID, Integer> marks) {
        this.marks.clear();
        if (marks != null) {
            this.marks.putAll(marks);
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (this.age % DAMAGE_INTERVAL_TICKS == 0) {
                this.setDealDamageActive();
            }
        }

        super.tick();

        if (!level().isClientSide) {
            spawnParticles();
            level().playSound(null, position().x, position().y, position().z, SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.NEUTRAL, 0.4f, 1.3f);
        }

        if (!level().isClientSide && this.age >= LIFESPAN_TICKS) {
            this.discard();
        }
    }

    @Override
    public void spawnParticles() {
        if (level().isClientSide) return;

        Vec3 forward = Vec3.directionFromRotation(this.getXRot(), this.getYRot());
        Vec3 origin = this.position();

        for (int i = 0; i < 3; i++) {
            double forwardOffset = this.random.nextDouble() * 4.0;
            double lateralOffset = (this.random.nextDouble() - 0.5) * 2.0;

            Vec3 lateral = new Vec3(-forward.z, 0, forward.x).normalize();
            Vec3 point = origin.add(forward.scale(forwardOffset)).add(lateral.scale(lateralOffset));

            MagicManager.spawnParticles(level(), ParticleRegistry.FIRE_HIT_SLASH.get(), point.x, point.y, point.z, 1, 4.0, 2.0, 4.0, 1, false);
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        Entity entity = entityHitResult.getEntity();
        if (entity == this.getOwner()) return;
        if (!this.hitEntities.add(entity.getUUID())) return;

        float appliedDamage = this.damage;
        if (this.markBased) {
            int stacks = this.marks.getOrDefault(entity.getUUID(), 0);
            if (stacks == 0 && this.getOwner() instanceof LivingEntity le) {
                stacks = FireBodyManager.getMarkStacks(le, entity);
            }
            appliedDamage = stacks > 0 ? 3.0f * stacks : 3.0f;
        }

        DamageSources.applyDamage(entity, appliedDamage, DamageSourcesRegistry.fireSpell(this.level(), getOwner()));

        entity.setRemainingFireTicks(Math.max(entity.getRemainingFireTicks(), 100));
    }

    public void onAntiMagic(MagicData playerMagicData) {
        this.discard();
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("MarkBased", this.markBased);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("MarkBased")) {
            this.markBased = tag.getBoolean("MarkBased");
        }
    }
}
