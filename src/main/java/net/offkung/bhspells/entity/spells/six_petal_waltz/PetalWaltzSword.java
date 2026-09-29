package net.offkung.bhspells.entity.spells.six_petal_waltz;

import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AbstractMagicProjectile;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.offkung.bhspells.client.event.SwordDashClientHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.function.Supplier;

public class PetalWaltzSword extends AbstractMagicProjectile implements GeoEntity {
    private static final EntityDataAccessor<Integer> DATA_OWNER_ID = SynchedEntityData.defineId(PetalWaltzSword.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_ACTIVATED = SynchedEntityData.defineId(PetalWaltzSword.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_RETURNING = SynchedEntityData.defineId(PetalWaltzSword.class, EntityDataSerializers.BOOLEAN);

    private static final int RETURN_TIMEOUT_MAX = 60;
    private static final double RETURN_SPEED = 2.2;
    private static final double RETURN_ARRIVAL_DISTANCE_SQR = 1.0;
    private static final float RETURN_HIT_INFLATION = 2.0f;
    private float damage = 20.0f;

    private int waitTimer = -1;
    private LivingEntity owner;
    private int returnTimeoutTicks = 0;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public PetalWaltzSword(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }

    public PetalWaltzSword(Level level, LivingEntity owner) {
        this(EntityRegistry.PETAL_WALTZ_SWORD.get(), level);
        this.setOwner(owner);
        this.owner = owner;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_OWNER_ID, -1);
        this.entityData.define(DATA_ACTIVATED, false);
        this.entityData.define(DATA_RETURNING, false);
    }

    @Override
    public void setOwner(@Nullable Entity owner) {
        super.setOwner(owner);
        if (owner != null) {
            this.entityData.set(DATA_OWNER_ID, owner.getId());
        }
    }

    @Override
    public void trailParticles() {
        Vec3 vec3 = this.position().subtract(this.getDeltaMovement());
        level().addParticle(new DustParticleOptions(new Vector3f(1f, 0.7f, 0.92f), 1.6F), vec3.x, vec3.y, vec3.z, 0.0F, 0.0F, 0.0F);
        level().addParticle(ParticleTypes.CHERRY_LEAVES, vec3.x, vec3.y, vec3.z, 0.0F, 0.0F, 0.0F);
    }

    @Override
    public void impactParticles(double x, double y, double z) {
        DustColorTransitionOptions pinkDust = new DustColorTransitionOptions(new Vector3f(1f, 0.8f, 0.95f), new Vector3f(1f, 0.96f, 0.96f), 2f);
        MagicManager.spawnParticles(level(), pinkDust, x, y, z, 1, 0.4, 0.4, 0.4, 0.2, true);
        MagicManager.spawnParticles(level(), ParticleTypes.CHERRY_LEAVES, x, y, z, 3, 0.4, 0.4, 0.4, 0.3, true);
    }

    @Override
    public float getSpeed() {
        return 1.7F;
    }

    @Override
    public void tick() {
        if (this.tickCount >= AbstractMagicProjectile.EXPIRE_TIME - 1) {
            this.tickCount = 1;
        }
        super.tick();
        if (this.level().isClientSide) return;
        
        if (this.entityData.get(DATA_ACTIVATED)) {
            this.setDeltaMovement(Vec3.ZERO);
            return;
        }
        
        if (this.entityData.get(DATA_RETURNING)) {
            this.tickReturning();
            return;
        }

        if (this.waitTimer > 0) {
            --this.waitTimer;
            if (this.waitTimer <= 410) {
                this.setDeltaMovement(Vec3.ZERO);
            }
            if (this.waitTimer == 0) {
                this.startReturning();
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        if (this.entityData.get(DATA_ACTIVATED)) {
            return false;
        }
        if (this.waitTimer > 0 && this.waitTimer <= 410) {
            return false;
        }
        if (pTarget == this.owner) {
            return false;
        }
        return super.canHitEntity(pTarget);
    }

    @Override
    protected void onHitBlock(BlockHitResult pResult) {
        if (this.level().isClientSide) return;
        if (this.entityData.get(DATA_ACTIVATED) || this.entityData.get(DATA_RETURNING)) return;

        Direction hitDirection = pResult.getDirection();
        Vec3 hitPos = pResult.getLocation();
        Vec3 pullBack = new Vec3(hitDirection.getStepX(), hitDirection.getStepY(), hitDirection.getStepZ()).scale(0.1);
        Vec3 stopPos = hitPos.add(pullBack);

        this.setPos(stopPos.x, stopPos.y, stopPos.z);
        this.setDeltaMovement(Vec3.ZERO);

        if (this.waitTimer < 0 || this.waitTimer > 410) {
            this.waitTimer = 410;
        }

        impactParticles(stopPos.x, stopPos.y, stopPos.z);
    }

    @Override
    public float getHitDetectionInflation() {
        if (this.entityData.get(DATA_RETURNING)) {
            return RETURN_HIT_INFLATION;
        }
        return super.getHitDetectionInflation();
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    @Override
    protected void onHitEntity(EntityHitResult pResult) {
        if (this.level().isClientSide) return;

        Entity hitEntity = pResult.getEntity();
        if (!(hitEntity instanceof LivingEntity livingTarget)) return;
        if (livingTarget == this.owner) return;
        if (!(this.owner instanceof ServerPlayer serverPlayer)) return;

        DamageSources.applyDamage(livingTarget, this.damage, BHSpellRegistry.SIX_PETAL_WALTZ.get().getDamageSource(serverPlayer));
        impactParticles(pResult.getLocation().x, pResult.getLocation().y, pResult.getLocation().z);
    }

    @Override
    protected void onHit(HitResult hitResult) {
        if (this.level().isClientSide) return;

        if (hitResult.getType() == HitResult.Type.ENTITY) {
            onHitEntity((EntityHitResult) hitResult);
        } else if (hitResult.getType() == HitResult.Type.BLOCK) {
            onHitBlock((BlockHitResult) hitResult);
        }
    }

    @Override
    public boolean isCurrentlyGlowing() {
        if (this.level().isClientSide) {
            Boolean glowing = DistExecutor.unsafeCallWhenOn(Dist.CLIENT, () -> () -> SwordDashClientHandler.isSwordGlowing(this, this.entityData.get(DATA_OWNER_ID)));
            return glowing != null && glowing;
        }
        return false;
    }

    @Override
    public int getTeamColor() {
        return 0xFFB3EA;
    }

    public Optional<Supplier<SoundEvent>> getImpactSound() {
        return Optional.empty();
    }

    @Override
    public void onAntiMagic(MagicData magicData) {
    }

    @Override
    public boolean shouldPierceShields() {
        return false;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public boolean isActivated() {
        return this.entityData.get(DATA_ACTIVATED);
    }

    public boolean isReturning() {
        return this.entityData.get(DATA_RETURNING);
    }

    public boolean isOwnedBy(Player player) {
        return player != null && player.getId() == this.entityData.get(DATA_OWNER_ID);
    }

    public void activate() {
        this.entityData.set(DATA_ACTIVATED, true);
        this.setDeltaMovement(Vec3.ZERO);
    }

    public void discardFromDash() {
        this.doBreaking();
        this.discard();
    }

    public void setWaitTimer(int waitTimer) {
        this.waitTimer = waitTimer;
    }

    private void tickReturning() {
        this.returnTimeoutTicks++;
        if (this.owner == null || !this.owner.isAlive() || this.owner.isRemoved()) {
            this.doBreaking();
            this.discard();
            return;
        }
        
        Vec3 targetPos = this.owner.getEyePosition();
        Vec3 diff = targetPos.subtract(this.position());
        
        if (diff.lengthSqr() <= RETURN_ARRIVAL_DISTANCE_SQR || this.returnTimeoutTicks > RETURN_TIMEOUT_MAX) {
            this.doBreaking();
            this.discard();
            return;
        }
        
        this.setDeltaMovement(diff.normalize().scale(RETURN_SPEED));
    }

    private void startReturning() {
        this.entityData.set(DATA_RETURNING, true);
    }

    private void doBreaking() {
        Vec3 center = this.position();
        MagicManager.spawnParticles(this.level(), ParticleTypes.SCRAPE, center.x, center.y, center.z, 8, 0.8, 0.8, 0.8, 0.2, true);
        DustColorTransitionOptions pinkDust = new DustColorTransitionOptions(new Vector3f(1f, 0.8f, 0.95f), new Vector3f(1f, 0.96f, 0.96f), 2f);
        MagicManager.spawnParticles(this.level(), pinkDust, center.x, center.y, center.z, 25, 0.1, 0.1, 0.1, 0.15, true);
        this.level().playSound(null, BlockPos.containing(this.position()), SoundEvents.ITEM_BREAK, SoundSource.NEUTRAL, 2.0F, 0.5F);
    }
}
