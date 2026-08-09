package net.offkung.bhspells.entity.spells.thousand_arrows;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.EntityRegistry;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;

public class RainVolleyEntity extends AbstractMagicProjectile {
    private static final int TOTAL_WAVES = 25;
    private static final int WAVE_INTERVAL_TICKS = 5;
    private static final int ARROWS_PER_WAVE = 15;
    private static final double RING_RADIUS = 4.0;
    private static final double SPAWN_HEIGHT_ABOVE_GROUND = 12.0;

    private static final int STACK_CAP = 3;

    private final Map<UUID, Integer> stacks = new HashMap<>();
    private final Map<UUID, LivingEntity> stackedEntities = new HashMap<>();

    private double groundY;
    private boolean groundYFound = false;
    private float perArrowDamage = 1.0f;
    private int wavesFired = 0;

    public RainVolleyEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.noPhysics = true;
    }

    public RainVolleyEntity(Level level, LivingEntity owner) {
        this(EntityRegistry.RAIN_VOLLEY.get(), level);
        this.setOwner(owner);
    }

    public void setPerArrowDamage(float damage) {
        this.perArrowDamage = damage;
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            if (!groundYFound) {
                Vec3 origin = this.position();
                HitResult ground = level().clip(new ClipContext(origin, origin.subtract(0, 64, 0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
                this.groundY = ground.getLocation().y;
                this.groundYFound = true;
            }

            if (wavesFired < TOTAL_WAVES && this.tickCount % WAVE_INTERVAL_TICKS == 0) {
                fireWave(wavesFired);
                wavesFired++;
            } else if (wavesFired >= TOTAL_WAVES) {
                fireHugeArrow();
                this.discard();
                return;
            }
        }
        super.tick();
    }

    private void fireWave(int waveIndex) {
        Vec3 center = this.position();
        double angleOffset = (waveIndex * 47.0) % 360.0;
        double waveRadius = RING_RADIUS * Utils.random.nextDouble();

        for (int i = 0; i < ARROWS_PER_WAVE; i++) {
            double angle = Math.toRadians(angleOffset + (360.0 / ARROWS_PER_WAVE) * i);
            double spawnX = center.x + Math.cos(angle) * waveRadius;
            double spawnZ = center.z + Math.sin(angle) * waveRadius;
            double spawnY = this.groundY + SPAWN_HEIGHT_ABOVE_GROUND;

            RainVolleyArrow arrow = new RainVolleyArrow(this.level(), this.getOwner());
            arrow.setDamage(this.perArrowDamage);
            arrow.setPos(spawnX, spawnY, spawnZ);
            arrow.shoot(new Vec3(0, -1.2, 0));
            arrow.setOwner(this.getOwner());
            arrow.setSourceVolley(this);
            level().addFreshEntity(arrow);

            MagicManager.spawnParticles(level(), ParticleTypes.FIREWORK, spawnX, spawnY, spawnZ, 2, .1, .1, .1, .05, false);
        }

        level().playSound(null, center.x, center.y, center.z, SoundRegistry.BOW_SHOOT.get(), SoundSource.NEUTRAL, 2.0f, 1.0f + Utils.random.nextFloat() * .3f);
    }

    public void addStack(LivingEntity target) {
        UUID id = target.getUUID();
        int current = stacks.getOrDefault(id, 0);
        int updated = Math.min(current + 1, STACK_CAP);
        stacks.put(id, updated);
        stackedEntities.put(id, target);

        int totalDurationTicks = TOTAL_WAVES * WAVE_INTERVAL_TICKS;
        int remainingTicks = Math.max(20, totalDurationTicks - this.tickCount + 20); // small buffer past last wave

        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, remainingTicks, 0, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, remainingTicks, 0, false, true, true));
    }

    private void clearAllStacksAndEffects() {
        for (LivingEntity target : stackedEntities.values()) {
            if (target.isAlive()) {
                target.removeEffect(MobEffects.WEAKNESS);
                target.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
            }
        }
        stacks.clear();
        stackedEntities.clear();
    }

    private void fireHugeArrow() {
        LivingEntity owner = getOwner() instanceof LivingEntity le ? le : null;
        Vec3 center = this.position();
        double spawnY = this.groundY + SPAWN_HEIGHT_ABOVE_GROUND + 6;

        HugeArrowEntity huge = new HugeArrowEntity(this.level(), owner);
        huge.setPos(center.x, spawnY, center.z);
        huge.shoot(new Vec3(0, -1.5, 0));
        huge.setTargetStacks(this.stacks);
        level().addFreshEntity(huge);

        clearAllStacksAndEffects();

        level().playSound(null, center.x, center.y, center.z, SoundEvents.EVOKER_CAST_SPELL, SoundSource.NEUTRAL, 3.0f, 0.7f);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("wavesFired", wavesFired);
        tag.putFloat("perArrowDamage", perArrowDamage);
        tag.putDouble("groundY", groundY);
        tag.putBoolean("groundYFound", groundYFound);

        ListTag stackList = new ListTag();
        for (Map.Entry<UUID, Integer> entry : stacks.entrySet()) {
            CompoundTag entryTag = new CompoundTag();
            entryTag.putUUID("Id", entry.getKey());
            entryTag.putInt("Stacks", entry.getValue());
            stackList.add(entryTag);
        }
        tag.put("Stacks", stackList);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.wavesFired = tag.getInt("wavesFired");
        this.perArrowDamage = tag.getFloat("perArrowDamage");
        this.groundY = tag.getDouble("groundY");
        this.groundYFound = tag.getBoolean("groundYFound");

        if (tag.contains("Stacks")) {
            this.stacks.clear();
            ListTag stackList = tag.getList("Stacks", Tag.TAG_COMPOUND);
            for (Tag t : stackList) {
                CompoundTag entryTag = (CompoundTag) t;
                this.stacks.put(entryTag.getUUID("Id"), entryTag.getInt("Stacks"));
            }
        }
    }

    @Override
    public void trailParticles() {
    }

    @Override
    public void impactParticles(double x, double y, double z) {
    }

    @Override
    public float getSpeed() {
        return 0;
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }
}
