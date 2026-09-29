package net.offkung.bhspells.entity.spells.red_beryl_bird;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.client.particle.ColoredCherryParticleOption;
import net.offkung.bhspells.client.particle.ColoredEndRodParticleOption;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.DamageSourcesRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.function.Supplier;

public class RedBerylBird extends AbstractMagicProjectile implements GeoEntity {
    private static final int LIFETIME_TICKS = 100; // 5 seconds; safety net if it never reaches its target

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation IDLE = RawAnimation.begin().thenLoop("fly");

    public RedBerylBird(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public RedBerylBird(Level level, LivingEntity owner) {
        this(EntityRegistry.RED_BERYL_BIRD.get(), level);
        this.setOwner(owner);
    }

    public void setTarget(LivingEntity target) {
        setHomingTarget(target);
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(getDeltaMovement());
        level().addParticle(ParticleTypes.MYCELIUM, vec3.x, vec3.y, vec3.z, 0, 0, 0);
    }

    public void impactParticles(double x, double y, double z) {
        ColoredCherryParticleOption redCherry = new ColoredCherryParticleOption(new Vector3f(0.98F, 0.62F, 0.62F), 0.4F);
        DustColorTransitionOptions redDust = new DustColorTransitionOptions(new Vector3f(0.98F, 0.62F, 0.62F), new Vector3f(1.0F, 0.0F, 0.0F), 0.8F);
        MagicManager.spawnParticles(level(), redCherry, x, y, z, 1, .4, .4, .4, 0.2, true);
        MagicManager.spawnParticles(level(), redDust, x, y, z, 3, .4, .4, .4, 0.3, true);
    }

    @Override
    public float getSpeed() {
        return 0.5f;
    }

    @Override
    public void tick() {
        super.tick();

        if (level().isClientSide || isRemoved()) {
            return;
        }

        if (getHomingTarget() == null || this.tickCount >= LIFETIME_TICKS) {
            doBreaking();
            discard();
        }
    }

    private void doBreaking() {
        var center = this.position();
        ColoredCherryParticleOption redCherry = new ColoredCherryParticleOption(new Vector3f(0.98F, 0.62F, 0.62F), 0.4F);
        MagicManager.spawnParticles(level(), redCherry, center.x, center.y, center.z, 8, .8, .8, .8, 0.2, true);
        MagicManager.spawnParticles(level(), ParticleTypes.MYCELIUM, center.x, center.y, center.z, 3, .6, .6, .6, 0.2, true);
        level().playSound(null, BlockPos.containing(position()), BHSoundRegistry.BIRD_HITS.get(), SoundSource.NEUTRAL, 2f, .5f);
    }

    @Override
    public void onHitEntity(EntityHitResult entityHitResult) {
        if (level().isClientSide) {
            return;
        }

        Entity entity = entityHitResult.getEntity();
        DamageSources.applyDamage(entity, damage, BHSpellRegistry.JADE_WAVE.get().getDamageSource(this, getOwner()));
        level().playSound(null, BlockPos.containing(position()), BHSoundRegistry.BIRD_HITS.get(), SoundSource.NEUTRAL, 0.2f, 1f);
        doBreaking();
        discard();
    }

    @Override
    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    public void onAntiMagic(MagicData pMagicData) {}

    @Override
    public boolean shouldPierceShields() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "fly_control", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationState<RedBerylBird> state) {
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
