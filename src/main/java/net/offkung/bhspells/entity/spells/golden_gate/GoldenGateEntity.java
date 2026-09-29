package net.offkung.bhspells.entity.spells.golden_gate;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.entity.mobs.IMagicSummon;
import io.redspace.ironsspellbooks.entity.spells.AbstractShieldEntity;
import io.redspace.ironsspellbooks.entity.spells.ShieldPart;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.util.OwnerHelper;
import net.mcreator.dungeonsandcombat.init.DungeonsAndCombatModParticleTypes;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
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

public class GoldenGateEntity extends AbstractShieldEntity implements GeoEntity, IMagicSummon {
    public static final float GATE_SCALE_WIDTH = 6f;
    public static final float GATE_SCALE_HEIGHT = 4f;

    private static final float PARTS_PER_UNIT = 3.667f;

    private static final EntityDataAccessor<Boolean> DATA_IS_OPENING = SynchedEntityData.defineId(GoldenGateEntity.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> DATA_IS_CLOSING = SynchedEntityData.defineId(GoldenGateEntity.class, EntityDataSerializers.BOOLEAN);

    protected ShieldPart[] subEntities;
    protected final Vec3[] subPositions;
    protected final int LIFETIME;
    protected int width;
    protected int height;
    protected int age;

    public GoldenGateEntity(EntityType<?> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        width = Math.round(GATE_SCALE_WIDTH * PARTS_PER_UNIT);
        height = Math.round(GATE_SCALE_HEIGHT * PARTS_PER_UNIT);
        subEntities = new ShieldPart[width * height];
        subPositions = new Vec3[width * height];
        this.setHealth(10);
        LIFETIME = 15 * 20;
        createShield();
    }

    public GoldenGateEntity(Level level, float health, LivingEntity caster, boolean playOpenAnimation) {
        this(EntityRegistry.GOLDEN_GATE_ENTITY.get(), level);
        this.setHealth(health);
        setSummoner(caster);
        if (playOpenAnimation)
            triggerOpenAnimation();
    }

    protected UUID summonerUUID;
    protected LivingEntity cachedSummoner;
    private int openTick = 15;
    private int closeTick = 10;
    public float percentReflectDamage;

    public void setPercentReflectDamage(float percent) {
        this.percentReflectDamage = percent;
    }

    @Override
    public LivingEntity getSummoner() {
        return OwnerHelper.getAndCacheOwner(level(), cachedSummoner, summonerUUID);
    }

    public void setSummoner(@Nullable LivingEntity summoner) {
        if (summoner != null) {
            this.summonerUUID = summoner.getUUID();
            this.cachedSummoner = summoner;
        }
    }

    @Override
    public void onUnSummon() {
        this.isAnimatingClose();
        this.entityData.set(DATA_IS_CLOSING, true);
    }

    @Override
    protected void createShield() {
        float spacing = 0.5f;
        for (int x = 0; x < width; x++) {
            for (int y = 0; y < height; y++) {
                int i = x * height + y;
                subEntities[i] = new ShieldPart(this, "part" + (i + 1), spacing, spacing, true);
                subPositions[i] = new Vec3((x - width / 2f) * spacing + spacing * 0.5f, (y - height / 2f) * spacing, 0);
            }
        }
    }

    public void setRotation(float y) {
        this.setYRot(y);
        this.yRotO = y;
    }

    @Override
    public void takeDamage(DamageSource source, float amount, @Nullable Vec3 location) {
        if (!this.isInvulnerableTo(source)) {
            this.setHealth(this.getHealth() - amount);
            if (!level().isClientSide && location != null) {
                MagicManager.spawnParticles(level(), ParticleTypes.WAX_ON, location.x, location.y, location.z, 8, .1, .1, .1, .5, false);
                MagicManager.spawnParticles(level(), DungeonsAndCombatModParticleTypes.BLESSED_SPARKLE.get(), location.x, location.y, location.z, 3, .1, .1, .1, .3, false);
                level().playSound(null, location.x, location.y, location.z, SoundRegistry.FORCE_IMPACT.get(), SoundSource.NEUTRAL, .8f, 1f);
            }
        }
    }

    @Override
    public void tick() {
        hurtThisTick = false;
        if (isAnimatingOpen()) {
            if (--openTick < 0) {
                entityData.set(DATA_IS_OPENING, false);
                this.setXRot(0);
                this.setOldPosAndRot();
            }
        }
        if (getHealth() <= 0) {
            destroy();
        } else if (++age > LIFETIME) {
            onUnSummon();
        } else {
            for (int i = 0; i < subEntities.length; i++) {
                var subEntity = subEntities[i];

                Vec3 pos = subPositions[i].xRot(Mth.DEG_TO_RAD * -this.getXRot()).yRot(Mth.DEG_TO_RAD * -this.getYRot()).add(this.position());
                subEntity.setPos(pos);
                subEntity.xo = pos.x;
                subEntity.yo = pos.y;
                subEntity.zo = pos.z;
                subEntity.xOld = pos.x;
                subEntity.yOld = pos.y;
                subEntity.zOld = pos.z;
            }
        }
        if (isAnimatingClose()) {
            if (--closeTick < 0) {
                entityData.set(DATA_IS_CLOSING, false);
                this.discard();
            }
        }
    }

    @Override
    public PartEntity<?>[] getParts() {
        return this.subEntities;
    }

    @Override
    protected void destroy() {
        if (!this.level().isClientSide) {
            level().playSound(null, getX(), getY(), getZ(), SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.NEUTRAL, 2, .65f);
        }
        super.destroy();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_IS_OPENING, false);
        this.entityData.define(DATA_IS_CLOSING, false);
    }

    public boolean isAnimatingOpen() {
        return entityData.get(DATA_IS_OPENING);
    }

    public boolean isAnimatingClose() {
        return entityData.get(DATA_IS_CLOSING);
    }

    public void triggerOpenAnimation() {
        entityData.set(DATA_IS_OPENING, true);
    }

    //ANIMATION
    @Override
    public double getTick(Object o) {
        return this.tickCount;
    }

    private final RawAnimation OPEN = RawAnimation.begin().thenPlay("open");
    private final RawAnimation IDLE = RawAnimation.begin().thenPlay("idle");
    private final RawAnimation CLOSE = RawAnimation.begin().thenPlay("close");

    private final AnimationController<GoldenGateEntity> openController = new AnimationController<>(this, "open_control", 0, this::openPredicate);
    private final AnimationController<GoldenGateEntity> idleController = new AnimationController<>(this, "idle_control", 0, this::idlePredicate);
    private final AnimationController<GoldenGateEntity> closeController = new AnimationController<>(this, "close_control", 0, this::closePredicate);

    private PlayState openPredicate(AnimationState event) {
        if (!isAnimatingOpen())
            return PlayState.STOP;
        if (event.getController().getAnimationState() == AnimationController.State.STOPPED) {
            event.getController().setAnimation(OPEN);
        }
        return PlayState.CONTINUE;
    }

    private PlayState idlePredicate(AnimationState event) {
        if (isAnimatingClose() || isAnimatingClose())
            return PlayState.STOP;
        else {
            event.getController().setAnimation(IDLE);
            return PlayState.CONTINUE;
        }
    }

    private PlayState closePredicate(AnimationState event) {
        if (isAnimatingClose()) {
            event.getController().setAnimation(CLOSE);
        }
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllerRegistrar) {
        controllerRegistrar.add(openController);
        controllerRegistrar.add(idleController);
        controllerRegistrar.add(closeController);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    public AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
}
