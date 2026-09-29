package net.offkung.bhspells.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.network.debug.PlayPlayerAnimationPacket;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import io.redspace.ironsspellbooks.render.animation.AnimationHelper;
import net.minecraft.util.RandomSource;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.event.WingOfWindEvents;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.particle.EpicFightParticles;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class WingOfWindSpell extends AbstractSpell {
    private static final int FLIGHT_BUFF_DURATION = 1200; // 60 seconds
    private static final int IMMOBILIZE_DURATION = 30;     // 30 ticks = 1.5 seconds pause
    public static final int FEATHERS_PER_UNLEASH = 10;
    public static final int TOTAL_UNLEASH_ROUNDS = 6;
    public static final double AIM_MAX_RANGE = 45.0;
    public static final double AIM_MIN_DOT = 0.55;
    public static final double FEATHER_SPAWN_RING_RADIUS = 0.75;
    public static final double FEATHER_SPREAD_DEGREES = 25.0;

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "wing_of_wind");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(16)
            .build();

    public WingOfWindSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 5;
        this.spellPowerPerLevel = 1;
        this.castTime = 0;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.bhspells.wing_of_wind.feathers", FEATHERS_PER_UNLEASH),
                Component.translatable("ui.bhspells.wing_of_wind.recasts", TOTAL_UNLEASH_ROUNDS)
        );
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.EVOCATION_CAST.get());
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return TOTAL_UNLEASH_ROUNDS;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
                // Unleash rounds (total 6 rounds of feather unleashing)
                // 1. Immobilize caster briefly during unleash burst
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.WING_OF_WIND_IMMOBILIZE.get(), IMMOBILIZE_DURATION, 0, false, false, true));
                entity.setDeltaMovement(Vec3.ZERO);
                entity.hasImpulse = true;
                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }

                // Play horizontal swing animation to caster on unleash
                playCasterAnimation(entity, SpellAnimations.SLASH_ANIMATION);

                // Play unleash sounds
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), BHSoundRegistry.UNLEASH_BIRDS.get(), SoundSource.PLAYERS, 1.2F, 1.1F);
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_ATTACK_SWEEP, SoundSource.PLAYERS, 1.0F, 1.2F);

                // 2. Start dynamic unleash barrage: unleashes 10 feathers sequentially from where caster is currently facing & aiming
                float damage = getDamage(spellLevel, entity);
                WingOfWindEvents.startBarrage(entity, damage, spellLevel);
            } else {
                // Round 1: Initial cast -> grants auto elytra flight ability
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.WING_OF_WIND_FLIGHT.get(), FLIGHT_BUFF_DURATION, 0, false, false, true));

                // Immediately activate elytra flight (launch into air)
                entity.setOnGround(false);
                Vec3 look = entity.getLookAngle().normalize();
                Vec3 launch = new Vec3(look.x * 0.85, Math.max(look.y * 0.7 + 0.45, 0.5), look.z * 0.85);
                entity.setDeltaMovement(launch);
                entity.hasImpulse = true;

                if (entity instanceof Player player) {
                    player.startFallFlying();
                }

                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }

                if (level instanceof ServerLevel serverLevel) {
                    serverLevel.sendParticles(EpicFightParticles.FEATHER.get(), entity.getX(), entity.getY() + 0.5, entity.getZ(), 50, 0, 0, 0, 0.15);
                }

                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BAT_TAKEOFF, SoundSource.PLAYERS, 1.2F, 1.0F);

                // Register recast instance with charges
                playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, TOTAL_UNLEASH_ROUNDS, FLIGHT_BUFF_DURATION, castSource, null), playerMagicData);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        super.onClientCast(level, spellLevel, entity, castData);
        if (entity instanceof Player player) {
            MagicData magicData = MagicData.getPlayerMagicData(player);
            if (magicData.getPlayerRecasts().hasRecastForSpell(this)) {
                player.setDeltaMovement(Vec3.ZERO);
                SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION.getForPlayer().ifPresent(animId -> AnimationHelper.animatePlayerStart(player, animId));
            } else {
                Vec3 look = player.getLookAngle().normalize();
                player.setOnGround(false);
                Vec3 launch = new Vec3(look.x * 0.85, Math.max(look.y * 0.7 + 0.45, 0.5), look.z * 0.85);
                player.setDeltaMovement(launch);
                player.startFallFlying();
            }
        }
    }

    private void playCasterAnimation(LivingEntity caster, AnimationHolder animation) {
        if (!(caster instanceof ServerPlayer)) {
            return;
        }
        animation.getForPlayer().ifPresent(animationId -> PacketDistributor.sendToPlayersTrackingEntityAndSelf(caster, new PlayPlayerAnimationPacket(caster.getUUID(), animationId)));
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        // After 6th round of unleashing (or duration expiration), remove flight capability and reset
        serverPlayer.removeEffect(MobEffectsRegistry.WING_OF_WIND_FLIGHT.get());
        if (serverPlayer.isFallFlying()) {
            serverPlayer.stopFallFlying();
        }
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    public static Vec3 spreadDirection(Vec3 direction, RandomSource random) {
        Vec3 arbitrary = Math.abs(direction.y) < 0.99 ? new Vec3(0, 1, 0) : new Vec3(1, 0, 0);
        Vec3 perpendicular1 = direction.cross(arbitrary).normalize();
        Vec3 perpendicular2 = direction.cross(perpendicular1).normalize();

        double maxOffset = Math.tan(Math.toRadians(FEATHER_SPREAD_DEGREES));
        double offset1 = (random.nextDouble() * 2.0 - 1.0) * maxOffset;
        double offset2 = (random.nextDouble() * 2.0 - 1.0) * maxOffset;

        return direction.add(perpendicular1.scale(offset1)).add(perpendicular2.scale(offset2)).normalize();
    }

    public static LivingEntity findAimTarget(Level level, LivingEntity caster, double maxRange, double minDot) {
        Vec3 eyePos = caster.getEyePosition();
        Vec3 look = caster.getLookAngle().normalize();
        AABB box = caster.getBoundingBox().inflate(maxRange);
        List<LivingEntity> list = level.getEntitiesOfClass(LivingEntity.class, box, e ->
                e != caster &&
                e.isAlive() &&
                !e.isSpectator() &&
                !DamageSources.isFriendlyFireBetween(caster, e) &&
                caster.hasLineOfSight(e)
        );

        LivingEntity best = null;
        double bestDot = minDot;

        for (LivingEntity candidate : list) {
            Vec3 toCand = candidate.getBoundingBox().getCenter().subtract(eyePos);
            double dist = toCand.length();
            if (dist > maxRange) continue;
            double dot = look.dot(toCand.normalize());
            if (dot > bestDot) {
                bestDot = dot;
                best = candidate;
            }
        }
        return best;
    }

    private float getDamage(int spellLevel, LivingEntity caster) {
        return getSpellPower(spellLevel, caster);
    }
}
