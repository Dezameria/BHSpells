package net.offkung.bhspells.spells.aqua;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastData;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.offkung.bhspells.event.StarIceEvents;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetEntityMotionPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.util.BHUtil;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class StarIceSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "star_ice");
    private static final int FREEZE_RADIUS = 10;
    private static final int FLIGHT_BUFF_DURATION = 1200; // 1 minute
    private static final int ABSORPTION_DURATION = 800;   // 40 seconds
    private static final int FREEZE_SECONDS = 5;

    private static final ParticleEmitterInfo FROSTFLAKE_SPREAD = new ParticleEmitterInfo(BHSpells.id("frostflake_spread"));

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public StarIceSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 40;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.bhspells.star_ice.radius", FREEZE_RADIUS),
                Component.translatable("ui.bhspells.star_ice.freeze_duration", FREEZE_SECONDS),
                Component.translatable("ui.bhspells.star_ice.recasts_infinite")
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
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 1;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
            return entity.isFallFlying();
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
                // Recast: Boost velocity forward while elytra flying
                if (entity.isFallFlying()) {
                    Vec3 look = entity.getLookAngle().normalize();
                    Vec3 boost = entity.getDeltaMovement().scale(0.5).add(look.scale(1.6));
                    entity.setDeltaMovement(boost);
                    entity.hasImpulse = true;

                    if (entity instanceof ServerPlayer serverPlayer) {
                        serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                    }

                    if (level instanceof ServerLevel serverLevel) {
                        AAALevel.addParticle(serverLevel, 64.0, FROSTFLAKE_SPREAD.clone().position(entity.getX(), entity.getY(), entity.getZ()));
                    }

                    // Apply buffs on start flight
                    entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, FLIGHT_BUFF_DURATION, 1, false, true, true)); // Strength 2
                    entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, FLIGHT_BUFF_DURATION, 0, false, true, true));    // Haste 1
                    entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, FLIGHT_BUFF_DURATION, 1, false, true, true)); // Regeneration 2
                    entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, 1, false, true, true));     // Absorption 2 (40s)

                    // Apply STAR_ICE effect (grants flight attribute)
                    entity.addEffect(new MobEffectInstance(MobEffectsRegistry.STAR_ICE.get(), FLIGHT_BUFF_DURATION, 0, false, false, true));

                    level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.ICE_CAST.get(), SoundSource.PLAYERS, 1.0f, 1.2f);

                    // Reset remaining recasts to 2 so after AbstractSpell decrements it by 1, it stays at 1 (infinite recasts)
                    playerMagicData.getPlayerRecasts().forceAddRecast(new InfiniteRecastInstance(getSpellId(), spellLevel, FLIGHT_BUFF_DURATION, castSource, 2));
                }
            } else {
                // Initial cast: Freeze players/entities in 10-block radius
                AABB searchBox = entity.getBoundingBox().inflate(FREEZE_RADIUS);
                List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, searchBox, target -> target != entity && target.isAlive() && !target.isSpectator() && target.distanceToSqr(entity) <= (double) (FREEZE_RADIUS * FREEZE_RADIUS) && !DamageSources.isFriendlyFireBetween(entity, target));

                for (LivingEntity target : targets) {
                    Utils.addFreezeTicks(target, target.getTicksRequiredToFreeze() + (FREEZE_SECONDS * 20));
                }

                // Spreading snowflake ring particles around caster, 4 layers from bottom (fastest) to top (slowest)
                if (level instanceof ServerLevel serverLevel) {
                    int ringLayers = 6;
                    int particlesPerRing = 100;
                    double ringRadius = 2;
                    double maxRingSpeed = 2;
                    double minRingSpeed = 0.2;
                    double bottomY = entity.getY() + 0.8;
                    double topY = entity.getY() + entity.getBbHeight();

                    for (int layer = 0; layer < ringLayers; layer++) {
                        double t = (double) layer / (ringLayers - 1);
                        double layerY = Mth.lerp(t, bottomY, topY);
                        double layerSpeed = Mth.lerp(t, maxRingSpeed, minRingSpeed);
                        BHUtil.createHorizontalRingParticles(serverLevel, new Vec3(entity.getX(), layerY, entity.getZ()), ParticleTypes.SNOWFLAKE, ringRadius, layerSpeed, layerSpeed, particlesPerRing);
                    }

                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + 1.0, entity.getZ(), 30, 0.5, 0.5, 0.5, 0.1);
                    serverLevel.sendParticles(ParticleRegistry.SNOW_DUST.get(), entity.getX(), entity.getY() + 1.0, entity.getZ(), 150, 0, 0.5, 0, 0.5);
                }

                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.PLAYER_BREATH, SoundSource.PLAYERS, 1.0f, 1.4f);

                // Apply buffs on start flight
                entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, FLIGHT_BUFF_DURATION, 1, false, true, true)); // Strength 2
                entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, FLIGHT_BUFF_DURATION, 0, false, true, true));    // Haste 1
                entity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, FLIGHT_BUFF_DURATION, 1, false, true, true)); // Regeneration 2
                entity.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, 1, false, true, true));     // Absorption 2 (40s)

                // Apply STAR_ICE effect (grants flight attribute)
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.STAR_ICE.get(), FLIGHT_BUFF_DURATION, 0, false, false, true));

                // Immediately activate elytra flight (lift into air + forward impulse + startFallFlying)
                entity.setOnGround(false);
                Vec3 look = entity.getLookAngle().normalize();
                Vec3 launch = new Vec3(look.x * 1.4, Math.max(look.y * 0.8 + 0.75, 0.75), look.z * 1.4);
                entity.setDeltaMovement(launch);
                entity.hasImpulse = true;

                if (entity instanceof Player player) {
                    player.startFallFlying();
                    player.getPersistentData().putInt(StarIceEvents.CAST_TICK_TAG, player.tickCount);
                    player.getPersistentData().putBoolean(StarIceEvents.AIRBORNE_TAG, false);
                }

                if (entity instanceof ServerPlayer serverPlayer) {
                    serverPlayer.connection.send(new ClientboundSetEntityMotionPacket(serverPlayer));
                }

                // Register recast instance for boosting while flying
                playerMagicData.getPlayerRecasts().addRecast(new InfiniteRecastInstance(getSpellId(), spellLevel, FLIGHT_BUFF_DURATION, castSource, 1), playerMagicData);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onClientCast(Level level, int spellLevel, LivingEntity entity, ICastData castData) {
        super.onClientCast(level, spellLevel, entity, castData);
        if (entity instanceof Player player) {
            Vec3 look = player.getLookAngle().normalize();
            if (player.isFallFlying()) {
                Vec3 boost = player.getDeltaMovement().scale(0.5).add(look.scale(1.6));
                player.setDeltaMovement(boost);
            } else {
                player.setOnGround(false);
                Vec3 launch = new Vec3(look.x * 1.4, Math.max(look.y * 0.8 + 0.75, 0.75), look.z * 1.4);
                player.setDeltaMovement(launch);
                player.startFallFlying();
            }
        }
    }
}
