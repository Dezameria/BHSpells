package net.offkung.bhspells.event;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustColorTransitionOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.joml.Vector3f;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.offkung.bhspells.registry.BHSpellRegistry;
import yesman.epicfight.api.utils.LevelUtil;

import java.util.*;

@Mod.EventBusSubscriber
public class ShakenMonkeyDashManager {
    private static final Map<UUID, DashData> ACTIVE_DASHES = new HashMap<>();

    private static final double DASH_SPEED = 2.0;
    private static final double DASH_RANGE = 8.0;
    private static final double DASH_DROP = 6.0;
    private static final double ARRIVAL_DISTANCE_SQR = 4.0;
    private static final int DASH_TIMEOUT_TICKS = 200; // 10 seconds timeout for dash
    private static final int CLOUD_BLESS_DURATION = 60;
    private static final double DAMAGE_RADIUS = 8.0;
    private static final int SLOWNESS_DURATION = 100; // 5 seconds
    private static final int SLOWNESS_AMPLIFIER = 2; // Slowness III (0-indexed)

    private ShakenMonkeyDashManager() {
    }

    public static boolean isDashing(ServerPlayer player) {
        return ACTIVE_DASHES.containsKey(player.getUUID());
    }

    public static void applyCloudBless(ServerPlayer player) {
        player.addEffect(new MobEffectInstance(MobEffectRegistry.FALL_DAMAGE_IMMUNITY.get(), CLOUD_BLESS_DURATION, 0, false, false, true));
    }

    public static void startDash(ServerPlayer player, float spellPower) {
        applyCloudBless(player);
        if (isDashing(player)) return;

        // Raycast forward-downward to find the ground target
        Vec3 start = player.position();
        Vec3 lookDir = player.getLookAngle();
        // Project forward and downward: use the player's horizontal look direction + downward
        Vec3 horizontalLook = new Vec3(lookDir.x, 0, lookDir.z).normalize();
        // Target is forward (scaled by range) and down at a fixed slope, independent of world height
        Vec3 targetDir = horizontalLook.scale(DASH_RANGE).add(0, -DASH_DROP, 0);
        Vec3 end = start.add(targetDir);

        // Raycast to find the actual ground block
        BlockHitResult hitResult = player.level().clip(new ClipContext(start, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        Vec3 targetPos;
        if (hitResult.getType() == HitResult.Type.BLOCK) {
            targetPos = hitResult.getLocation();
        } else {
            // Fallback: no obstruction along the dash cone — find the ground below the forward point
            Vec3 forwardPoint = start.add(horizontalLook.scale(DASH_RANGE));
            BlockPos groundPos = BlockPos.containing(forwardPoint);
            while (groundPos.getY() > player.level().getMinBuildHeight() && player.level().isEmptyBlock(groundPos)) {
                groundPos = groundPos.below();
            }
            targetPos = Vec3.atCenterOf(groundPos.above());
        }

        ACTIVE_DASHES.put(player.getUUID(), new DashData(targetPos, spellPower));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        UUID uuid = serverPlayer.getUUID();
        DashData data = ACTIVE_DASHES.get(uuid);
        if (data == null) return;

        Level level = serverPlayer.level();
        Vec3 current = serverPlayer.position();
        Vec3 toTarget = data.targetPos.subtract(current);

        data.timeoutTicks++;
        boolean arrived = toTarget.lengthSqr() <= ARRIVAL_DISTANCE_SQR;
        boolean timedOut = data.timeoutTicks > DASH_TIMEOUT_TICKS;
        boolean hitGround = data.timeoutTicks > 2 && (
                serverPlayer.onGround() ||
                serverPlayer.verticalCollision ||
                !level.noCollision(serverPlayer, serverPlayer.getBoundingBox().expandTowards(0, -1.0, 0))
        );

        if (!arrived && !timedOut && !hitGround) {
            // Move towards target
            Vec3 dashDir = toTarget.normalize();
            Vec3 desiredMotion = dashDir.scale(DASH_SPEED);

            serverPlayer.setDeltaMovement(desiredMotion);
            serverPlayer.fallDistance = 0.0F;
            serverPlayer.hurtMarked = true;
        } else {
            // Arrived, hit ground, or timed out — apply slam effects
            ACTIVE_DASHES.remove(uuid);

            Vec3 slamPos = serverPlayer.position();
            HitResult groundCheck = level.clip(new ClipContext(
                    slamPos,
                    slamPos.add(0, -2.5, 0),
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    serverPlayer
            ));
            if (groundCheck.getType() == HitResult.Type.BLOCK) {
                slamPos = groundCheck.getLocation();
            }

            performSlam(serverPlayer, slamPos, data.spellPower);
        }
    }

    public static void performSlam(ServerPlayer serverPlayer, Vec3 slamPos, float spellPower) {
        Level level = serverPlayer.level();

        // Stop momentum and reset fall distance
        serverPlayer.setDeltaMovement(Vec3.ZERO);
        serverPlayer.hurtMarked = true;
        serverPlayer.fallDistance = 0.0F;

        Vec3 fractureCenter = new Vec3(slamPos.x, slamPos.y - 0.1, slamPos.z);
        LevelUtil.circleSlamFracture(serverPlayer, level, fractureCenter, 6.0, false, false, false);
        TOScreenShakeEntity.createScreenShake(level, slamPos, 12.0F, 0.07F, 10, 0, 5, true);

        // Yellow sand-colored dust transition particles
        DustColorTransitionOptions dustOptions = new DustColorTransitionOptions(
                new Vector3f(0.93F, 0.84F, 0.56F),
                new Vector3f(0.76F, 0.60F, 0.32F),
                2.5F
        );
        MagicManager.spawnParticles(level, dustOptions, slamPos.x, slamPos.y + 0.2, slamPos.z, 100, 2.0, 0.5, 2.0, 0.2, true);

        MagicManager.spawnParticles(level, ParticleTypes.CAMPFIRE_COSY_SMOKE, slamPos.x, slamPos.y + 0.1, slamPos.z, 150, 2.5, 0.5, 2.5, 0.3, true);
        MagicManager.spawnParticles(level, ParticleTypes.CLOUD, slamPos.x, slamPos.y + 0.1, slamPos.z, 150, 2.5, 0.5, 2.5, 0.3, true);

        // AoE damage & slowness
        double radiusSqr = DAMAGE_RADIUS * DAMAGE_RADIUS;
        List<LivingEntity> damagedEntities = new ArrayList<>();

        for (Entity entity : level.getEntities(serverPlayer, serverPlayer.getBoundingBox().inflate(DAMAGE_RADIUS))) {
            if (entity instanceof LivingEntity livingEntity && entity != serverPlayer && entity.distanceToSqr(slamPos) <= radiusSqr) {
                DamageSources.applyDamage(livingEntity, spellPower, BHSpellRegistry.SHAKEN_MONKEY.get().getDamageSource(serverPlayer));
                damagedEntities.add(livingEntity);
            }
        }

        // Apply Slowness III to caster
        serverPlayer.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, false, true));

        // Apply Slowness III to all damaged entities
        for (LivingEntity damaged : damagedEntities) {
            damaged.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, SLOWNESS_DURATION, SLOWNESS_AMPLIFIER, false, false, true));
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedOut(PlayerEvent.PlayerLoggedOutEvent event) {
        ACTIVE_DASHES.remove(event.getEntity().getUUID());
    }

    private static class DashData {
        final Vec3 targetPos;
        final float spellPower;
        int timeoutTicks = 0;

        DashData(Vec3 targetPos, float spellPower) {
            this.targetPos = targetPos;
            this.spellPower = spellPower;
        }
    }
}
