package net.offkung.bhspells.entity.spells.swirling_blossom;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import io.redspace.ironsspellbooks.particle.SwirlingParticleOptions;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.PacketDistributor;
import net.offkung.bhspells.client.particle.ColoredCherryParticleOption;
import net.offkung.bhspells.client.particle.GrayFlowerParticleOption;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.server.ArtOfTruthTargetGlowSyncPacket;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import org.joml.Vector3f;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SwirlingBlossomAoe extends AoeEntity {
    private static final EntityDataAccessor<Boolean> DATA_PURPLE_VARIANT = SynchedEntityData.defineId(SwirlingBlossomAoe.class, EntityDataSerializers.BOOLEAN);

    public static final float HEIGHT = 7f;
    public static final float DEFAULT_RADIUS = 20.0f;
    public static final int DEFAULT_DURATION = 1200; // 60 seconds
    public static final int REAPPLICATION_DELAY = 40; // 2 seconds
    public static final int EFFECT_DURATION = 60; // 3 seconds per refresh
    public static final float INNER_RADIUS_RATIO = 0.30f; // Leaves ~8 blocks open in center
    public static final float OUTER_RADIUS_RATIO = 0.92f; // Keeps swirl comfortably inside ~17 blocks of the 20-block radius

    private UUID truthTargetUuid;
    private int truthTargetId = -1;

    public SwirlingBlossomAoe(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.setNoGravity(true);
        this.setCircular();
        this.setRadius(DEFAULT_RADIUS);
        this.duration = DEFAULT_DURATION;
        this.reapplicationDelay = REAPPLICATION_DELAY;
    }

    public SwirlingBlossomAoe(Level level) {
        this(EntityRegistry.SWIRLING_BLOSSOM.get(), level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_PURPLE_VARIANT, false);
    }

    public boolean isPurpleVariant() {
        return this.entityData.get(DATA_PURPLE_VARIANT);
    }

    public void setPurpleVariant(boolean purpleVariant) {
        this.entityData.set(DATA_PURPLE_VARIANT, purpleVariant);
    }

    public void setTruthTarget(LivingEntity target) {
        if (target != null) {
            this.truthTargetUuid = target.getUUID();
            this.truthTargetId = target.getId();
        }
    }

    @Nullable
    public LivingEntity getTruthTarget() {
        if (this.truthTargetUuid != null && level() instanceof ServerLevel serverLevel) {
            Entity entity = serverLevel.getEntity(this.truthTargetUuid);
            if (entity instanceof LivingEntity living) {
                return living;
            }
        }
        return null;
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.putBoolean("PurpleVariant", isPurpleVariant());
        if (this.truthTargetUuid != null) {
            pCompound.putUUID("TruthTarget", this.truthTargetUuid);
        }
        pCompound.putInt("TruthTargetId", this.truthTargetId);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("PurpleVariant")) {
            setPurpleVariant(pCompound.getBoolean("PurpleVariant"));
        }
        if (pCompound.hasUUID("TruthTarget")) {
            this.truthTargetUuid = pCompound.getUUID("TruthTarget");
        }
        if (pCompound.contains("TruthTargetId")) {
            this.truthTargetId = pCompound.getInt("TruthTargetId");
        }
    }

    @Override
    public float getParticleCount() {
        return 0;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    protected Vec3 getInflation() {
        return new Vec3(0, HEIGHT, 0);
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    @Override
    public void applyEffect(LivingEntity target) {
        if (isPurpleVariant()) {
            return;
        }
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 2, false, false, true));
        if (this.getOwner() instanceof LivingEntity caster && caster.isAlive() && caster != target) {
            caster.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, EFFECT_DURATION, 1, false, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.REGENERATION, EFFECT_DURATION, 2, false, false, true));
        }
    }

    @Override
    public void tick() {
        super.tick();
        this.move(MoverType.SELF, getDeltaMovement());
        if ((tickCount - 1) % 10 == 0) {
            playSound(SoundRegistry.BLACK_HOLE_LOOP.get(), 1, random.nextIntBetweenInclusive(15, 20) * .1f);
        }
        if (tickCount % 20 == 0) {
            if (level().collidesWithSuffocatingBlock(this, AABB.ofSize(this.position().add(0, 0.5, 0), 1.5, 0.5, 1.5))) {
                Vec3 ground = Utils.moveToRelativeGroundLevel(level(), this.position().add(0, 1, 0), 1);
                this.move(MoverType.SELF, ground.subtract(this.position()));
            } else {
                if (Utils.raycastForBlock(level(), position(), position().add(0, -0.5, 0), ClipContext.Fluid.NONE).getType() == HitResult.Type.MISS) {
                    this.move(MoverType.SELF, new Vec3(0, -0.5, 0));
                }
            }
        }
    }

    @Override
    public void onRemovedFromWorld() {
        super.onRemovedFromWorld();
        if (!level().isClientSide && isPurpleVariant()) {
            if (this.truthTargetId != -1 && this.getOwner() instanceof ServerPlayer casterPlayer) {
                PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> casterPlayer), new ArtOfTruthTargetGlowSyncPacket(this.truthTargetId, false));
            }
        }
    }

    @Override
    protected boolean canHitEntity(Entity pTarget) {
        return pTarget instanceof LivingEntity living && living.isAlive() && !living.isSpectator();
    }

    private static final Vector3f COLOR_YELLOW = new Vector3f(1.0f, 0.88f, 0.32f);
    private static final Vector3f COLOR_PINK = new Vector3f(1.0f, 0.68f, 0.88f);
    private static final Vector3f COLOR_PURPLE = new Vector3f(0.65f, 0.40f, 0.88f);

    private static final float SCALE_SMALL = 0.50f;
    private static final float SCALE_MEDIUM = 1.50f;
    private static final float SCALE_BIG = 2.0f;

    private static final GrayFlowerParticleOption[] PINK_FLOWERS = new GrayFlowerParticleOption[] {
            new GrayFlowerParticleOption(COLOR_PINK, SCALE_SMALL),
            new GrayFlowerParticleOption(COLOR_PINK, SCALE_MEDIUM),
            new GrayFlowerParticleOption(COLOR_PINK, SCALE_BIG)
    };

    private static final GrayFlowerParticleOption[] PURPLE_FLOWERS = new GrayFlowerParticleOption[] {
            new GrayFlowerParticleOption(COLOR_PURPLE, SCALE_SMALL),
            new GrayFlowerParticleOption(COLOR_PURPLE, SCALE_MEDIUM),
            new GrayFlowerParticleOption(COLOR_PURPLE, SCALE_BIG)
    };

    private static final GrayFlowerParticleOption[] YELLOW_FLOWERS = new GrayFlowerParticleOption[] {
            new GrayFlowerParticleOption(COLOR_YELLOW, SCALE_SMALL),
            new GrayFlowerParticleOption(COLOR_YELLOW, SCALE_MEDIUM),
            new GrayFlowerParticleOption(COLOR_YELLOW, SCALE_BIG)
    };

    private GrayFlowerParticleOption getRandomFlowerParticle() {
        float roll = random.nextFloat();
        GrayFlowerParticleOption[] pool;
        if (roll < 0.65f) {
            pool = PINK_FLOWERS;
        } else if (roll < 0.88f) {
            pool = PURPLE_FLOWERS;
        } else {
            pool = YELLOW_FLOWERS;
        }
        return pool[random.nextInt(pool.length)];
    }

    private static final ColoredCherryParticleOption[] CHERRY_VARIANTS = new ColoredCherryParticleOption[] {
            new ColoredCherryParticleOption(new Vector3f(1.0f, 0.72f, 0.96f), 0.65f),
            new ColoredCherryParticleOption(new Vector3f(1.0f, 0.82f, 0.94f), 0.55f),
            new ColoredCherryParticleOption(new Vector3f(0.98f, 0.62f, 0.88f), 0.75f)
    };

    @Override
    public void ambientParticles() {
        if (!level().isClientSide) {
            return;
        }
        Vec3 pos = position();
        float radius = getRadius();
        int sakuraCount = (int) (2 * (radius * radius / 64f));
        int cherryCount = (int) (16 * (radius * radius / 64f));
        int flowerCount = Math.max(1, (int) (4 * (radius * radius / 64f)));

        for (int i = 0; i < sakuraCount; i++) {
            swirlingParticle(radius, pos, ParticleRegistry.SPLATTER_SAKURA.get());
        }
        for (int i = 0; i < cherryCount; i++) {
            ColoredCherryParticleOption cherry = CHERRY_VARIANTS[random.nextInt(CHERRY_VARIANTS.length)];
            swirlingParticle(radius, pos, cherry);
        }
        for (int i = 0; i < flowerCount; i++) {
            swirlingParticle(radius, pos, getRandomFlowerParticle());
        }
    }

    private void swirlingParticle(float radius, Vec3 pos, ParticleOptions particle) {
        float swirlRadius = radius * (INNER_RADIUS_RATIO + (OUTER_RADIUS_RATIO - INNER_RADIUS_RATIO) * random.nextFloat());
        float diameter = swirlRadius * 3.0f;
        float angularSpeed = 3.5f * (random.nextFloat() + 0.5f);
        Vec3 center = pos.add(Utils.getRandomVec3(0.5f)).add(0, 1.5f + random.nextFloat() * (HEIGHT - 1.5f), 0);
        Vec3 up = new Vec3(0, 1, 0).add(Utils.getRandomVec3(0.15)).normalize();
        double upwardSpeed = 0.02 + random.nextDouble() * 0.03;
        level().addParticle(new SwirlingParticleOptions(particle, up, new Vec3(0, 0, 1), new Vec3(diameter, diameter, angularSpeed), new Vec3(0, 0, 0)), true, center.x, center.y, center.z, 0, upwardSpeed, 0);
    }
}
