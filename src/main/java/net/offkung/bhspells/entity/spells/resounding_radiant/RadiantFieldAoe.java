package net.offkung.bhspells.entity.spells.resounding_radiant;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.EntityRegistry;
import org.joml.Vector3f;

import java.util.Optional;

public class RadiantFieldAoe extends AoeEntity {
    public static final int LIFETIME_TICKS = 20 * 20;
    public static final float FIELD_RADIUS = 5.0f;

    private static final int BLASTWAVE_INTERVAL_TICKS = 20;
    private static final int DEBUFF_INTERVAL_TICKS = 40;
    private static final int DEBUFF_DURATION_TICKS = 40;
    private static final int CASTER_BUFF_DURATION_TICKS = 60;
    private static final Vector3f RADIANT_GREEN = new Vector3f(1f, 0.87f, 0.29f);

    public RadiantFieldAoe(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.duration = LIFETIME_TICKS;
        this.reapplicationDelay = DEBUFF_INTERVAL_TICKS;
        this.setRadius(FIELD_RADIUS);
        this.setCircular();
    }

    public RadiantFieldAoe(Level level, LivingEntity owner) {
        this(EntityRegistry.RADIANT_FIELD_AOE.get(), level);
        this.setOwner(owner);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 4, 0);
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved() || this.level().isClientSide) {
            return;
        }
        if (this.tickCount <= this.getDelay()) {
            return;
        }
        int activeTicks = this.tickCount - this.getDelay();
        if (activeTicks % BLASTWAVE_INTERVAL_TICKS == 0) {
            spawnBlastwave();
        }
        if (activeTicks % DEBUFF_INTERVAL_TICKS == 0) {
            buffCaster();
        }
    }

    private void spawnBlastwave() {
        MagicManager.spawnParticles(this.level(), new BlastwaveParticleOptions(RADIANT_GREEN, FIELD_RADIUS), this.getX(), this.getY() + 0.15, this.getZ(), 1, 0, 0, 0, 0, true);
    }

    private void buffCaster() {
        if (this.getOwner() instanceof LivingEntity caster && caster.isAlive()) {
            caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, CASTER_BUFF_DURATION_TICKS, 0, false, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, CASTER_BUFF_DURATION_TICKS, 1, false, false, true));
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, DEBUFF_DURATION_TICKS, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, DEBUFF_DURATION_TICKS, 1, false, false, true));
    }

    @Override
    public void ambientParticles() {
    }

    @Override
    public float getParticleCount() {
        return 0f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }
}
