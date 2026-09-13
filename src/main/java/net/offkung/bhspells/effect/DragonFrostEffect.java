package net.offkung.bhspells.effect;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class DragonFrostEffect extends MobEffect {
    public static final double PROXIMITY_RADIUS = 1.5;
    public static final double PROXIMITY_RADIUS_SQR = PROXIMITY_RADIUS * PROXIMITY_RADIUS;
    public static final int REQUIRED_NEAR_TICKS = 30 * 20; // 30 seconds = 600 ticks

    public DragonFrostEffect() {
        super(MobEffectCategory.BENEFICIAL, 0x99E2F9);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        LevelCheck:
        if (!livingEntity.level().isClientSide) {
            // 1. Spawn snowflake particle around the player every 1 second (20 ticks)
            if (livingEntity.tickCount % 20 == 0 && livingEntity.level() instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE,
                        livingEntity.getX(),
                        livingEntity.getY() + livingEntity.getBbHeight() * 0.5,
                        livingEntity.getZ(),
                        6, 0.25, 0.4, 0.25, 0.02);
            }

            // 2. Proximity freeze check for entities within 1.5 blocks for 30 seconds
            if (livingEntity instanceof Player player) {
                AABB searchArea = player.getBoundingBox().inflate(PROXIMITY_RADIUS);
                List<LivingEntity> nearbyEntities = player.level().getEntitiesOfClass(LivingEntity.class, searchArea, target ->
                        target != player && target.isAlive() && !target.isSpectator() && !player.isAlliedTo(target) && !DamageSources.isFriendlyFireBetween(player, target) && target.distanceToSqr(player) <= PROXIMITY_RADIUS_SQR
                );

                String uuidStr = player.getStringUUID();
                String nearTicksTag = "bhspells:df_near_ticks_" + uuidStr;
                String lastTickTag = "bhspells:df_last_tick_" + uuidStr;

                for (LivingEntity target : nearbyEntities) {
                    int lastTick = target.getPersistentData().getInt(lastTickTag);
                    int nearTicks = target.getPersistentData().getInt(nearTicksTag);

                    if (player.tickCount - lastTick <= 2) {
                        nearTicks++;
                    } else {
                        nearTicks = 1;
                    }

                    target.getPersistentData().putInt(nearTicksTag, nearTicks);
                    target.getPersistentData().putInt(lastTickTag, player.tickCount);

                    if (nearTicks >= REQUIRED_NEAR_TICKS) {
                        Utils.addFreezeTicks(target, target.getTicksRequiredToFreeze() + 100);
                        if (nearTicks == REQUIRED_NEAR_TICKS && player.level() instanceof ServerLevel serverLevel) {
                            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 20, 0.3, 0.3, 0.3, 0.05);
                            player.level().playSound(null, target.getX(), target.getY(), target.getZ(), SoundEvents.PLAYER_HURT_FREEZE, SoundSource.PLAYERS, 1.0f, 1.0f);
                        }
                    } else if (nearTicks % 20 == 0 && player.level() instanceof ServerLevel serverLevel) {
                        serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, target.getX(), target.getY() + target.getBbHeight() * 0.5, target.getZ(), 2, 0.1, 0.1, 0.1, 0.01);
                    }
                }
            }
        }
    }
}
