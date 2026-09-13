package net.offkung.bhspells.event;

import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSword;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;
import yesman.epicfight.particle.EpicFightParticles;

import java.util.*;

@Mod.EventBusSubscriber
public class SwordDashManager {
    private static final Map<UUID, DashData> ACTIVE_DASHES = new HashMap<>();
    private static final Map<UUID, SwordTracker> SWORD_TRACKERS = new HashMap<>();

    private static final double DASH_SPEED = 7.8;
    private static final double ARRIVAL_DISTANCE_SQR = 2.25;
    private static final double HIT_RADIUS = 5.55;
    private static final int DASH_TIMEOUT_TICKS = 40;
    private static final float DASH_DAMAGE = 20.0f;

    private SwordDashManager() {
    }

    public static boolean isDashing(ServerPlayer player) {
        return ACTIVE_DASHES.containsKey(player.getUUID());
    }

    public static void registerSwordCount(UUID playerUUID, int swordCount) {
        SWORD_TRACKERS.put(playerUUID, new SwordTracker(swordCount));
    }

    public static void startDash(ServerPlayer player, PetalWaltzSword sword) {
        if (isDashing(player)) return;
        ACTIVE_DASHES.put(player.getUUID(), new DashData(sword.getId(), sword.position()));
    }

    @SubscribeEvent
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer serverPlayer)) {
            return;
        }

        DashData data = ACTIVE_DASHES.get(serverPlayer.getUUID());
        if (data == null) return;

        Level level = serverPlayer.level();
        Vec3 current = serverPlayer.position();
        Vec3 toTarget = data.targetPos.subtract(current);

        data.timeoutTicks++;
        boolean arrived = toTarget.lengthSqr() <= ARRIVAL_DISTANCE_SQR;
        boolean timedOut = data.timeoutTicks > DASH_TIMEOUT_TICKS;

        if (!arrived && !timedOut) {
            Vec3 dashDir = toTarget.normalize();
            Vec3 currentMotion = serverPlayer.getDeltaMovement();

            Vec3 desiredMotion = dashDir.scale(DASH_SPEED);
            Vec3 blendedMotion = currentMotion.add(desiredMotion.subtract(currentMotion).scale(0.5));

            serverPlayer.setDeltaMovement(blendedMotion.x, Math.max(blendedMotion.y, currentMotion.y * 0.5), blendedMotion.z);
            serverPlayer.fallDistance = 0;
            serverPlayer.hurtMarked = true;

            spawnDashCloudTrail(serverPlayer, blendedMotion);

            AABB hitBox = serverPlayer.getBoundingBox().inflate(HIT_RADIUS);
            for (Entity entity : level.getEntities(serverPlayer, hitBox)) {
                if (entity instanceof LivingEntity livingEntity && entity != serverPlayer && !data.hitEntities.contains(entity.getUUID())) {
                    data.hitEntities.add(entity.getUUID());
                    spawnHorizontalCherry((ServerLevel) level, livingEntity.position(), 36, 0.4);
                    ((ServerLevel) level).sendParticles(EpicFightParticles.BLADE_RUSH_SKILL.get(), livingEntity.position().x, livingEntity.position().y, livingEntity.position().z, 1, 0.0, 1.0, 0.0, 0.0);
                    DamageSources.applyDamage(livingEntity, DASH_DAMAGE, BHSpellRegistry.SIX_PETAL_WALTZ.get().getDamageSource(serverPlayer));
                }
            }
        } else {
            ACTIVE_DASHES.remove(serverPlayer.getUUID());

            serverPlayer.setDeltaMovement(Vec3.ZERO);
            serverPlayer.hurtMarked = true;

            if (level.getEntity(data.swordEntityId) instanceof PetalWaltzSword sword) {
                sword.discardFromDash();
            }

            SwordTracker tracker = SWORD_TRACKERS.get(serverPlayer.getUUID());
            if (tracker != null) {
                tracker.dashedCount++;
                if (tracker.dashedCount >= tracker.totalSwordCount) {
                    serverPlayer.addEffect(new MobEffectInstance(MobEffectsRegistry.PETAL_WALTZ.get(), 20, 0, false, true, false));
                    SWORD_TRACKERS.remove(serverPlayer.getUUID());
                }
            }
        }
    }

    private static void spawnHorizontalCherry(ServerLevel level, Vec3 center, int n, double speed) {
        for (int i = 0; i < n; ++i) {
            double theta = (Math.PI * 2D) * ((double) i / n);
            double dx = Math.sin(theta) * speed;
            double dz = Math.cos(theta) * speed;

            level.sendParticles(ParticleRegistry.SPLATTER_SAKURA.get(), center.x, center.y, center.z, 0, dx, 0.0, dz, 1.0);
        }
    }

    private static void spawnDashCloudTrail(ServerPlayer player, Vec3 dashMotion) {
        Level level = player.level();
        Vec3 behind = dashMotion.normalize().scale(-1.0);
        Vec3 spawnBase = player.position().add(behind.scale(0.6)).add(0, player.getBbHeight() * 0.5, 0);

        for (int i = 0; i < 6; i++) {
            double ox = (level.random.nextDouble() - 0.5) * 0.6;
            double oy = (level.random.nextDouble() - 0.5) * 0.6;
            double oz = (level.random.nextDouble() - 0.5) * 0.6;

            MagicManager.spawnParticles(level, ParticleTypes.CLOUD, spawnBase.x + ox, spawnBase.y + oy, spawnBase.z + oz, 1, 0.1, 0.1, 0.1, 0.01, false);
            MagicManager.spawnParticles(level, ParticleTypes.CHERRY_LEAVES, spawnBase.x + ox, spawnBase.y + oy, spawnBase.z, 5, 0.5, 0.5, 0.5, 1, false);
        }
    }

    private static class DashData {
        final int swordEntityId;
        final Vec3 targetPos;
        final Set<UUID> hitEntities = new HashSet<>();
        int timeoutTicks = 0;

        DashData(int swordEntityId, Vec3 targetPos) {
            this.swordEntityId = swordEntityId;
            this.targetPos = targetPos;
        }
    }

    private static class SwordTracker {
        final int totalSwordCount;
        int dashedCount = 0;

        SwordTracker(int totalSwordCount) {
            this.totalSwordCount = totalSwordCount;
        }
    }
}
