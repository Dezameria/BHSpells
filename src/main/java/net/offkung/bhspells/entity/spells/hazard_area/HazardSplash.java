package net.offkung.bhspells.entity.spells.hazard_area;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.entity.spells.poison_cloud.PoisonCloud;
import io.redspace.ironsspellbooks.util.ParticleHelper;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;

import java.util.List;
import java.util.Optional;

public class HazardSplash extends AoeEntity {
    public static final float RADIUS = 5.0f; // 10x10 blocks area (radius 5 = diameter 10)
    public static final int DURATION = 40 * 20; // 40 seconds (800 ticks)
    public static final int REAPPLICATION_DELAY = 2 * 20; // 2 seconds (40 ticks)
    public static final int EFFECT_DURATION = 40 * 20; // 40 seconds (800 ticks)
    public static final int CASTER_BUFF_DURATION = 40 * 20; // 40 seconds (800 ticks)

    public HazardSplash(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setRadius(RADIUS);
        this.duration = DURATION;
        this.reapplicationDelay = REAPPLICATION_DELAY;
        this.effectDuration = EFFECT_DURATION;
    }

    public HazardSplash(Level level) {
        this(EntityRegistry.HAZARD_SPLASH.get(), level);
    }

    boolean playedParticles;

    @Override
    public void tick() {
        super.tick();

        if (!playedParticles) {
            playedParticles = true;
            if (level().isClientSide) {
                for (int i = 0; i < 150; i++) {
                    Vec3 pos = new Vec3(Utils.getRandomScaled(.5f), Utils.getRandomScaled(.2f), this.random.nextFloat() * getRadius()).yRot(this.random.nextFloat() * 360);
                    Vec3 motion = new Vec3(
                            Utils.getRandomScaled(.06f),
                            this.random.nextDouble() * -.8 - .5,
                            Utils.getRandomScaled(.06f)
                    );

                    level().addParticle(ParticleHelper.ACID, getX() + pos.x, getY() + pos.y + getBoundingBox().getYsize(), getZ() + pos.z, motion.x, motion.y, motion.z);
                }
            } else {
                MagicManager.spawnParticles(level(), ParticleHelper.POISON_CLOUD, getX(), getY() + getBoundingBox().getYsize(), getZ(), 9, getRadius() * .7f, .2f, getRadius() * .7f, 1, true);
            }
        }

        if (tickCount == 4) {
            if (!level().isClientSide) {
                checkHits();
                MagicManager.spawnParticles(level(), ParticleHelper.POISON_CLOUD, getX(), getY(), getZ(), 9, getRadius() * .7f, .2f, getRadius() * .7f, 1, true);
            }
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
        DamageSources.applyDamage(target, getDamage(), BHSpellRegistry.HAZARD_AREA.get().getDamageSource(this, getOwner()));
        target.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0));
        target.addEffect(new MobEffectInstance(MobEffects.GLOWING, 60, 0));
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, 2, 0);
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public float getParticleCount() {
        return 0.2f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.of(ParticleHelper.POISON_CLOUD);
    }

    @Override
    public void ambientParticles() {
        super.ambientParticles();
        if (this.random.nextFloat() < 0.3f) {
            Vec3 pos = new Vec3(Utils.getRandomScaled(getRadius() * 0.85f), 0.1f, Utils.getRandomScaled(getRadius() * 0.85f));
            level().addParticle(ParticleHelper.ACID, getX() + pos.x, getY() + pos.y, getZ() + pos.z, 0, 0.02, 0);
        }
    }

    public static void applyCasterBuffs(LivingEntity caster) {
        if (caster != null && caster.isAlive() && !caster.level().isClientSide) {
            caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, CASTER_BUFF_DURATION, 1, false, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, CASTER_BUFF_DURATION, 1, false, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, CASTER_BUFF_DURATION, 1,false, false, true));
        }
    }
}
