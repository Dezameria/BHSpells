package net.offkung.bhspells.entity.spells.aqua_flower;

import com.gametechbc.traveloptics.init.TravelopticsParticles;
import com.github.alexmodguy.alexscaves.server.misc.ACSoundRegistry;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.Mth;
import net.offkung.bhspells.registry.ParticleRegistry;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.util.BHUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BHSpells.MODID)
public class AquaFlower extends Entity implements GeoEntity {
    public static final float RADIUS = 20.0F;
    public static final int SHIELD_DURATION_TICKS = 400;

    private static final EntityDataAccessor<Integer> LIFESPAN_TICKS = SynchedEntityData.defineId(AquaFlower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> CASTER_ID = SynchedEntityData.defineId(AquaFlower.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Float> SPELL_POWER = SynchedEntityData.defineId(AquaFlower.class, EntityDataSerializers.FLOAT);

    private final RawAnimation IDLE_ANIMATION;
    private final AnimationController<AquaFlower> controller;
    private final AnimatableInstanceCache cache;

    private boolean slowdownApplied = false;
    private boolean speedApplied = false;

    @Nullable
    private UUID casterUUID;
    @Nullable
    private LivingEntity cachedOwner;

    public AquaFlower(EntityType<? extends AquaFlower> entityType, Level level) {
        super(entityType, level);
        this.noPhysics = true;
        this.noCulling = true;
        this.setNoGravity(true);
        this.IDLE_ANIMATION = RawAnimation.begin().thenPlay("animation.purple_flower.new");
        this.controller = new AnimationController<>(this, "controller", 0, this::animationPredicate);
        this.cache = GeckoLibUtil.createInstanceCache(this);
    }

    public AquaFlower(Level level, int lifespanTicks) {
        this(EntityRegistry.AQUA_FLOWER.get(), level);
        this.entityData.set(LIFESPAN_TICKS, lifespanTicks);
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(LIFESPAN_TICKS, 800);
        this.entityData.define(CASTER_ID, 0);
        this.entityData.define(SPELL_POWER, 0.0F);
    }

    public void setOwner(@Nullable LivingEntity owner) {
        if (owner != null) {
            this.entityData.set(CASTER_ID, owner.getId());
            this.casterUUID = owner.getUUID();
            this.cachedOwner = owner;
        }
    }

    @Nullable
    public LivingEntity getOwner() {
        if (this.cachedOwner != null && this.cachedOwner.isAlive()) {
            return this.cachedOwner;
        }
        int id = this.entityData.get(CASTER_ID);
        if (id > 0 && this.level().getEntity(id) instanceof LivingEntity living && living.isAlive()) {
            this.cachedOwner = living;
            return living;
        }
        if (this.casterUUID != null && this.level() instanceof ServerLevel serverLevel && serverLevel.getEntity(this.casterUUID) instanceof LivingEntity living && living.isAlive()) {
            this.cachedOwner = living;
            return living;
        }
        return null;
    }

    public void setSpellPower(float power) {
        this.entityData.set(SPELL_POWER, power);
    }

    public float getSpellPower() {
        return this.entityData.get(SPELL_POWER);
    }

    public boolean isCaster(@Nullable LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        LivingEntity owner = this.getOwner();
        if (owner != null && (entity == owner || entity.getUUID().equals(owner.getUUID()))) {
            return true;
        }
        if (entity.getUUID().equals(this.casterUUID)) {
            return true;
        }
        int id = this.entityData.get(CASTER_ID);
        return id > 0 && entity.getId() == id;
    }

    public boolean isShieldActive() {
        return this.isAlive();
    }

    public boolean isAllyShieldActive() {
        return this.isAlive() && this.tickCount < SHIELD_DURATION_TICKS;
    }

    public boolean isShielded(@Nullable LivingEntity target) {
        if (!this.isAlive() || target == null || !target.isAlive()) {
            return false;
        }
        if (this.distanceTo(target) > RADIUS) {
            return false;
        }
        if (this.isCaster(target)) {
            // The caster always gets the shield during the aqua flower's entire lifetime
            return true;
        }
        // Any living entity nearby gets the shield during the first 20 seconds
        return this.tickCount < SHIELD_DURATION_TICKS;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        return false;
    }

    @Override
    public boolean isInvulnerable() {
        return true;
    }

    @Override
    public void tick() {
        super.tick();

        int lifespan = this.entityData.get(LIFESPAN_TICKS);

        if (this.level().isClientSide) {
            return;
        }

        // 1. Rotating circular ground wave with splash, blue dust color transition, and horizontal rings of water foam
        BHUtil.spawnAquaFlowerGroundWave(this.level(), this.position(), RADIUS, this.tickCount);

        // 2. Full 3D circle sphere water foam particle shield covering protected entities in radius (caster full lifetime, allies first 20s)
        AABB area = this.getBoundingBox().inflate(RADIUS);
        List<LivingEntity> nearby = this.level().getEntitiesOfClass(LivingEntity.class, area, this::isShielded);
        for (LivingEntity target : nearby) {
            if (this.tickCount % 20 == 0) {
                this.level().playSound(null, target.getX(), target.getY(), target.getZ(), ACSoundRegistry.SEA_STAFF_CAST.get(), SoundSource.NEUTRAL, 0.7F, 1.0F);
            }
            BHUtil.spawnAquaFlowerShield(this.level(), target, this.tickCount);
        }

        // 3. After the first 20 seconds (at tick 400), apply MOVEMENT_SLOWDOWN to caster for another 20 seconds
        if (!this.slowdownApplied && this.tickCount >= SHIELD_DURATION_TICKS) {
            this.slowdownApplied = true;
            LivingEntity caster = this.getOwner();
            if (caster != null && caster.isAlive()) {
                int remainingTicks = Math.max(20, lifespan - this.tickCount);
                caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, remainingTicks, 2, false, false, true));
            }
        }

        if (this.tickCount >= lifespan) {
            Vector3f startPurple = new Vector3f(1f, 0.82f, 0.98f);
            Vector3f endPurple = new Vector3f(0.88f, 0.66f, 1f);
            DustColorTransitionOptions purpleDust = new DustColorTransitionOptions(startPurple, endPurple, 3.5f);
            ((ServerLevel) this.level()).sendParticles(purpleDust, this.getX(), this.getY(), this.getZ(), 300, 0.5, 0.5, 0.5, 0.5);
            ((ServerLevel) this.level()).sendParticles(ParticleRegistry.SPLATTER_SAKURA.get(), this.getX(), this.getY(), this.getZ(), 250, 0.5, 0.5, 0.5, 0.5);
            this.level().playSound(null, this.getX(), this.getY(), this.getZ(), SoundRegistry.ROOT_EMERGE.get(), SoundSource.NEUTRAL, 2f, 1f);
            this.discard();
            return;
        }

        if (this.tickCount % 20 == 0) {
            AABB healArea = new AABB(this.getX() - RADIUS, this.getY() - 10.0, this.getZ() - RADIUS, this.getX() + RADIUS, this.getY() + 10.0, this.getZ() + RADIUS);
            List<LivingEntity> entitiesInRadius = this.level().getEntitiesOfClass(LivingEntity.class, healArea, e -> e.isAlive() && this.distanceTo(e) <= RADIUS);

            LivingEntity caster = this.getOwner();

            for (LivingEntity target : entitiesInRadius) {
                if (caster != null && target == caster) {
                    float casterHeal = Math.max(2.0F, 2.0F * Math.max(1.0F, this.getSpellPower() / 10.0F));
                    target.heal(casterHeal);
                } else {
                    target.heal(5.0F);
                }
            }
            BHUtil.spawnRisingSakuraParticles(this.level(), this.position(), RADIUS, 40);
        }

        AABB projArea = this.getBoundingBox().inflate(RADIUS + 2.0);
        List<Projectile> nearbyProjectiles = this.level().getEntitiesOfClass(Projectile.class, projArea, p -> p.isAlive() && !p.isRemoved());
        if (!nearbyProjectiles.isEmpty()) {
            AABB shieldCheckArea = this.getBoundingBox().inflate(RADIUS);
            List<LivingEntity> shieldedEntities = this.level().getEntitiesOfClass(LivingEntity.class, shieldCheckArea, this::isShielded);
            
            if (!shieldedEntities.isEmpty()) {
                for (Projectile projectile : nearbyProjectiles) {
                    for (LivingEntity shielded : shieldedEntities) {
                        Entity projOwner = projectile.getOwner();
                        if (projOwner == shielded || (projOwner instanceof LivingEntity livingOwner && BHUtil.isAllyOrSelf(shielded, livingOwner))) {
                            continue;
                        }

                        if (projectile.distanceTo(shielded) <= 2.2) {
                            BHUtil.spawnProjectileInterceptParticle(this.level(), projectile.position());
                            projectile.discard();
                            break;
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public static void onLivingAttack(LivingAttackEvent event) {
        LivingEntity target = event.getEntity();
        if (target == null || target.level().isClientSide || !target.isAlive()) {
            return;
        }

        AABB checkArea = target.getBoundingBox().inflate(RADIUS);
        List<AquaFlower> nearbyFlowers = target.level().getEntitiesOfClass(AquaFlower.class, checkArea, f -> f.isAlive() && f.isShielded(target));
        if (nearbyFlowers.isEmpty()) {
            return;
        }

        // Intercept and discard projectile
        if (event.getSource().getDirectEntity() instanceof Projectile projectile) {
            BHUtil.spawnProjectileInterceptParticle(target.level(), projectile.position());
            projectile.discard();
            event.setCanceled(true);
            return;
        }

        // Cancel damage from attacks (infinite HP shield), unless void
        if (!event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            Vec3 loc = target.position().add(0, target.getBbHeight() * 0.5, 0);
            Entity attacker = event.getSource().getDirectEntity() != null ? event.getSource().getDirectEntity() : event.getSource().getEntity();
            if (attacker != null) {
                Vec3 dir = attacker.position().subtract(target.position());
                if (dir.lengthSqr() > 0.001) {
                    loc = loc.add(dir.normalize().scale(Math.min(dir.length() * 0.5, target.getBbWidth() * 0.8 + 0.2)));
                }
            }
            BHUtil.spawnProjectileInterceptParticle(target.level(), loc);
        }
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target == null || target.level().isClientSide || !target.isAlive()) {
            return;
        }

        AABB checkArea = target.getBoundingBox().inflate(RADIUS);
        List<AquaFlower> nearbyFlowers = target.level().getEntitiesOfClass(AquaFlower.class, checkArea, f -> f.isAlive() && f.isShielded(target));
        if (!nearbyFlowers.isEmpty() && !event.getSource().is(DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            event.setCanceled(true);
            event.setAmount(0.0F);
            Vec3 loc = target.position().add(0, target.getBbHeight() * 0.5, 0);
            BHUtil.spawnProjectileInterceptParticle(target.level(), loc);
        }
    }

    private PlayState animationPredicate(AnimationState<AquaFlower> state) {
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
    public void remove(@NotNull RemovalReason reason) {
        if (!this.level().isClientSide && !this.speedApplied && reason.shouldDestroy()) {
            this.speedApplied = true;
            LivingEntity caster = this.getOwner();
            if (caster != null && caster.isAlive()) {
                caster.removeEffect(MobEffects.MOVEMENT_SLOWDOWN);
                caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 200, 3, false, false, true));
            }
        }
        super.remove(reason);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag compound) {
        compound.putInt("LifespanTicks", this.entityData.get(LIFESPAN_TICKS));
        compound.putInt("CasterId", this.entityData.get(CASTER_ID));
        compound.putFloat("SpellPower", this.entityData.get(SPELL_POWER));
        compound.putBoolean("SlowdownApplied", this.slowdownApplied);
        compound.putBoolean("SpeedApplied", this.speedApplied);
        if (this.casterUUID != null) {
            compound.putUUID("CasterUUID", this.casterUUID);
        }
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag compound) {
        if (compound.contains("LifespanTicks")) {
            this.entityData.set(LIFESPAN_TICKS, compound.getInt("LifespanTicks"));
        }
        if (compound.contains("CasterId")) {
            this.entityData.set(CASTER_ID, compound.getInt("CasterId"));
        }
        if (compound.contains("SpellPower")) {
            this.entityData.set(SPELL_POWER, compound.getFloat("SpellPower"));
        }
        if (compound.contains("SlowdownApplied")) {
            this.slowdownApplied = compound.getBoolean("SlowdownApplied");
        }
        if (compound.contains("SpeedApplied")) {
            this.speedApplied = compound.getBoolean("SpeedApplied");
        }
        if (compound.hasUUID("CasterUUID")) {
            this.casterUUID = compound.getUUID("CasterUUID");
        }
    }
}
