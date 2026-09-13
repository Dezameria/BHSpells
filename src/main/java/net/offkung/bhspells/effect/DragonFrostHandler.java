package net.offkung.bhspells.effect;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.registry.MobEffectsRegistry;

public class DragonFrostHandler {
    public static final String JUMPED_TAG = "bhspells:df_jumped";
    public static final String AIR_Y_TAG = "bhspells:df_air_y";
    public static final String WAS_JUMPING_TAG = "bhspells:df_was_jumping";

    public static void onJump(Player player) {
        if (!player.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            return;
        }
        player.getPersistentData().putBoolean(JUMPED_TAG, true);
        player.getPersistentData().putDouble(AIR_Y_TAG, player.getY());
        player.resetFallDistance();

        // Visual / Audio feedback
        Level level = player.level();
        if (level instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY(), player.getZ(), 8, 0.2, 0.05, 0.2, 0.03);
        }
        level.playSound(null, player.getX(), player.getY(), player.getZ(), SoundEvents.SNOW_STEP, SoundSource.PLAYERS, 0.8f, 1.4f);
    }

    public static boolean shouldProvideAirFloor(Player player, Vec3 movement, AABB bb) {
        if (!player.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            return false;
        }
        if (player.isCrouching() || player.isDescending()) {
            return false;
        }
        if (player.isInWater() || player.isInLava()) {
            return false;
        }
        if (player.getAbilities().flying) {
            return false;
        }
        if (!player.getPersistentData().getBoolean(JUMPED_TAG)) {
            return false;
        }

        // When player is rising upwards (jump ascent), do not block upward velocity
        if (movement.y > 0.001) {
            return false;
        }

        // When descending or at apex, provide air floor at feet
        return true;
    }

    public static void onPlayerTick(Player player) {
        if (!player.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            player.getPersistentData().remove(JUMPED_TAG);
            player.getPersistentData().remove(AIR_Y_TAG);
            player.getPersistentData().remove(WAS_JUMPING_TAG);
            return;
        }

        Level level = player.level();

        // If player is crouching, disable air-walking jump state to allow dropping
        if (player.isCrouching()) {
            player.getPersistentData().remove(JUMPED_TAG);
            player.getPersistentData().remove(AIR_Y_TAG);
        }

        // Check if player has landed on real solid ground (non-air, non-fluid)
        if (player.onGround()) {
            BlockPos blockBelowPos = player.blockPosition().below();
            BlockState blockBelow = level.getBlockState(blockBelowPos);
            BlockState blockAtFeet = level.getBlockState(player.blockPosition());

            boolean onRealGround = (!blockBelow.isAir() && blockBelow.getFluidState().isEmpty()) || (!blockAtFeet.isAir() && blockAtFeet.getFluidState().isEmpty());

            if (onRealGround) {
                player.getPersistentData().remove(JUMPED_TAG);
                player.getPersistentData().remove(AIR_Y_TAG);
            }
        }

        // Reset fall distance while air walking
        if (player.getPersistentData().getBoolean(JUMPED_TAG)) {
            player.resetFallDistance();

            // Spawn subtle snowflake particles under feet when moving horizontally on air
            if (player.level() instanceof ServerLevel serverLevel && player.getDeltaMovement().horizontalDistanceSqr() > 0.005) {
                if (player.tickCount % 3 == 0) {
                    serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, player.getX(), player.getY() + 0.05, player.getZ(), 2, 0.15, 0.02, 0.15, 0.01);
                }
            }
        }
    }
}
