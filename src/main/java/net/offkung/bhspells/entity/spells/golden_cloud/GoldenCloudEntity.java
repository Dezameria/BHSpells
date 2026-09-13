package net.offkung.bhspells.entity.spells.golden_cloud;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.SummonManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.particle.FogParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.offkung.bhspells.mixin.LivingEntityAccessor;
import net.offkung.bhspells.registry.EntityRegistry;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.UUID;

public class GoldenCloudEntity extends PathfinderMob implements IMagicSummon, GeoEntity {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private final RawAnimation IDLE = RawAnimation.begin().thenLoop("tail");

    public GoldenCloudEntity(EntityType<? extends PathfinderMob> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
    }

    public GoldenCloudEntity(Level level, LivingEntity caster) {
        this(EntityRegistry.GOLDEN_CLOUD.get(), level);
        setSummoner(caster);
    }

    @Override
    public void registerGoals() {
        this.goalSelector.addGoal(0, new FloatGoal(this));
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0)
                .add(ForgeMod.STEP_HEIGHT_ADDITION.get(), 1)
                .add(Attributes.MOVEMENT_SPEED, 0.5D);
    }

    @Nullable
    public LivingEntity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        if (entity instanceof Mob) {
            return (Mob) entity;
        } else {
            entity = this.getFirstPassenger();
            if (entity instanceof Player) {
                return (Player) entity;
            }

            return null;
        }
    }

    @Override
    public void tick() {
        if (!level().isClientSide) {
            MagicManager.spawnParticles(level(), new FogParticleOptions(new Vector3f(1f, 0.96f, 0.7f), 0.5f), this.getX(), this.getY() - 0.25f, this.getZ(), 2, 0.4, 0.2, 0.4, 2.0, false);
        }

        super.tick();
    }

    public void setSummoner(@Nullable LivingEntity owner) {
        if (owner == null) return;
        SummonManager.setOwner(this, owner);
    }

    @Override
    public void onUnSummon() {
        if (!level().isClientSide) {
            MagicManager.spawnParticles(level(), ParticleTypes.SCRAPE, getX(), getY(), getZ(), 25, .5, .5, .5, .25, false);
            setRemoved(RemovalReason.DISCARDED);
        }
    }

    @Override
    public void die(DamageSource pDamageSource) {
        this.onDeathHelper();
        super.die(pDamageSource);
    }

    @Override
    public void onRemovedFromWorld() {
        this.onRemovedHelper(this);
        super.onRemovedFromWorld();
    }

    @Override
    public boolean isAlliedTo(Entity entity) {
        return super.isAlliedTo(entity) || this.isAlliedHelper(entity);
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (!source.is(DamageTypeTags.BYPASSES_INVULNERABILITY))
            return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isPushedByFluid() {
        return false;
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    @Override
    public boolean canBeLeashed(Player pPlayer) {
        return false;
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
        if (this.isVehicle()) {
            return super.mobInteract(pPlayer, pHand);
        }
        if (pPlayer == getSummoner()) {
            this.doPlayerRide(pPlayer);
        }
        return InteractionResult.sidedSuccess(this.level().isClientSide);
    }

    public void doPlayerRide(Player pPlayer) {
        if (!this.level().isClientSide) {
            pPlayer.setYRot(this.getYRot());
            pPlayer.setXRot(this.getXRot());
            pPlayer.startRiding(this);
        }
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (this.isAlive()) {
            if (this.isVehicle() && this.getControllingPassenger() instanceof Player player) {
                this.setYRot(player.getYRot());
                this.yRotO = this.getYRot();
                this.setXRot(player.getXRot());
                this.xRotO = this.getXRot();
                this.setRot(this.getYRot(), this.getXRot());
                this.yBodyRot = this.getYRot();
                this.yHeadRot = this.getYRot();

                float forward = player.zza;
                float strafe = player.xxa;

                Vec3 moveVec = Vec3.ZERO;

                // Forward / Backward in 3D look direction (pitch and yaw)
                if (forward != 0.0F) {
                    Vec3 look = player.getLookAngle();
                    moveVec = moveVec.add(look.scale(forward));
                }

                // Left / Right strafing perpendicular to look yaw
                if (strafe != 0.0F) {
                    float yawRad = (float) Math.toRadians(player.getYRot());
                    Vec3 left = new Vec3(Math.cos(yawRad), 0, Math.sin(yawRad));
                    moveVec = moveVec.add(left.scale(strafe));
                }

                // Ascend with jump (spacebar) like creative flight
                if (player instanceof LivingEntityAccessor accessor && accessor.bhspells$isJumping()) {
                    moveVec = moveVec.add(0.0, 1.0, 0.0);
                }

                float speed = (float) this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 1.6F;
                if (moveVec.lengthSqr() > 1.0E-4D) {
                    Vec3 targetVelocity = moveVec.normalize().scale(speed);
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.5D).add(targetVelocity.scale(0.5D)));
                } else {
                    this.setDeltaMovement(this.getDeltaMovement().scale(0.7D));
                    if (this.getDeltaMovement().lengthSqr() < 1.0E-4D) {
                        this.setDeltaMovement(Vec3.ZERO);
                    }
                }

                this.move(MoverType.SELF, this.getDeltaMovement());
                this.resetFallDistance();
                player.resetFallDistance();
                return;
            }
        }
        super.travel(travelVector);
    }

    @Override
    protected void positionRider(Entity passanger, MoveFunction moveFunction) {
        if (this.hasPassenger(passanger)) {
            moveFunction.accept(passanger, this.getX(), this.getY() + this.getPassengersRidingOffset() + passanger.getMyRidingOffset() + 0.5f, this.getZ());
        }
    }

    // GeckoLib Animation
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(new AnimationController<>(this, "tail_control", 0, this::idlePredicate));
    }

    private PlayState idlePredicate(AnimationState<GoldenCloudEntity> state) {
        state.getController().setAnimation(IDLE);
        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
