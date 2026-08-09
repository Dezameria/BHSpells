package net.offkung.bhspells.entity.spells.thousand_arrows;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.entity.mobs.AntiMagicSusceptible;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.EntityRegistry;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class MagicAlchemyEntity extends AoeEntity implements GeoEntity, AntiMagicSusceptible {
    private final RawAnimation circleSpin;
    public AnimatableInstanceCache cache;

    public MagicAlchemyEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.circleSpin = RawAnimation.begin().thenPlay("animation.magic_circle.spin");
        this.cache = GeckoLibUtil.createInstanceCache(this);
    }

    public MagicAlchemyEntity(Level level) {
        this(EntityRegistry.MAGIC_ALCHEMY.get(), level);
    }

    protected boolean canHitEntity(Entity pTarget) {
        return !pTarget.isSpectator() && pTarget.isAlive() && pTarget.isPickable();
    }

    public void applyEffect(LivingEntity target) {
    }

    protected Vec3 getInflation() {
        return new Vec3(0.0F, 1.0F, 0.0F);
    }

    public boolean shouldBeSaved() {
        return false;
    }

    public void refreshDimensions() {
    }

    public float getParticleCount() {
        return 0.125F;
    }

    public Optional<ParticleOptions> getParticle() {
        return Optional.of(ParticleTypes.GLOW);
    }

    public void onAntiMagic(MagicData magicData) {
        this.discard();
    }

    private PlayState predicate(AnimationState event) {
        event.getController().setAnimation(this.circleSpin);
        return PlayState.CONTINUE;
    }

    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "controller", 0, this::predicate));
    }

    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
