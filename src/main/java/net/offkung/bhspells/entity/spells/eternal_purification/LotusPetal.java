package net.offkung.bhspells.entity.spells.eternal_purification;

import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;
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

public class LotusPetal extends Entity implements GeoEntity {
    private static final EntityDataAccessor<Integer> LIFESPAN_TICKS = SynchedEntityData.defineId(LotusPetal.class, EntityDataSerializers.INT);
    private final RawAnimation IDLE_ANIMATION;
    private final AnimationController<LotusPetal> controller;
    private final AnimatableInstanceCache cache;

    public LotusPetal(EntityType<? extends LotusPetal> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.IDLE_ANIMATION = RawAnimation.begin().thenPlay("animation.lotus_flower1.new");
        this.controller = new AnimationController(this, "controller", 0, this::animationPredicate);
        this.cache = GeckoLibUtil.createInstanceCache(this);
    }

    public LotusPetal(Level level, int lifespanTicks) {
        this(EntityRegistry.LOTUS_PETAL.get(), level);
        this.entityData.set(LIFESPAN_TICKS, lifespanTicks + 20);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LIFESPAN_TICKS, 100);
    }

    @Override
    public void tick() {
        super.tick();

        if (!this.level().isClientSide && this.tickCount >= this.entityData.get(LIFESPAN_TICKS)) {
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.GLOW_SQUID_INK, this.getX(), this.getY(), this.getZ(), 300, 0.5, 0.5, 0.5, 0.5);
            ((ServerLevel) this.level()).sendParticles(ParticleTypes.GLOW, this.getX(), this.getY(), this.getZ(), 250, 0.5, 0.5, 0.5, 0.5);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundEvents.BEACON_DEACTIVATE, SoundSource.NEUTRAL, 2f, 1f);
            this.discard();
        }
    }

    private PlayState animationPredicate(AnimationState<LotusPetal> state) {
        state.getController().setAnimation(this.IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(this.controller);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    @Override
    public boolean isPickable() {
        return false;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("LifespanTicks", this.entityData.get(LIFESPAN_TICKS));
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("LifespanTicks")) {
            this.entityData.set(LIFESPAN_TICKS, compound.getInt("LifespanTicks"));
        }
    }
}
