package net.offkung.bhspells.event;

import com.gametechbc.traveloptics.init.TravelopticsSounds;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.client.particle.ColoredEndRodParticleOption;
import net.offkung.bhspells.registry.ParticleRegistry;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber
public class PoisonSwirlManager {
    private static final Map<UUID, SwirlData> ACTIVE_SWIRLS = new HashMap<>();

    private static final double SWIRL_HEIGHT = 0.6;
    private static final int RING_INTERVAL_TICKS = 100;
    private static final int RING_PARTICLE_COUNT = 32;
    private static final double RING_SPEED = 0.22;

    private PoisonSwirlManager() {
    }

    public static void register(LivingEntity target) {
        ACTIVE_SWIRLS.putIfAbsent(target.getUUID(), new SwirlData(target));
    }

    @SubscribeEvent
    public static void onLevelTick(TickEvent.LevelTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.level.isClientSide) return;

        Iterator<Map.Entry<UUID, SwirlData>> it = ACTIVE_SWIRLS.entrySet().iterator();
        while (it.hasNext()) {
            SwirlData data = it.next().getValue();
            LivingEntity target = data.resolveTarget(event.level);

            if (target == null || !target.isAlive() || !target.hasEffect(MobEffects.POISON)) {
                it.remove();
                continue;
            }

            data.tick(target);
        }
    }

    private static void spawnPoisonRing(ServerLevel level, Vec3 center, int n, double speed) {
        ColoredEndRodParticleOption greenRod = new ColoredEndRodParticleOption(0.52F, 1.0F, 0.62F);

        for (int i = 0; i < n; ++i) {
            double theta = (Math.PI * 2D) * ((double) i / n);
            double dx = Math.sin(theta) * speed;
            double dz = Math.cos(theta) * speed;

            level.sendParticles(greenRod, center.x, center.y, center.z, 0, dx, 0.0, dz, 1.0);
            level.sendParticles(ParticleRegistry.OAK_LEAF_PARTICLE.get(), center.x, center.y, center.z, 0, dx, 0.0, dz, 1.0);
        }
    }


    private static class SwirlData {
        private final UUID targetUUID;
        private LivingEntity cachedTarget;
        private int ticksSinceRing = 0;

        SwirlData(LivingEntity target) {
            this.targetUUID = target.getUUID();
            this.cachedTarget = target;
        }

        @Nullable
        LivingEntity resolveTarget(Level level) {
            if (cachedTarget != null && cachedTarget.isAlive() && !cachedTarget.isRemoved()) {
                return cachedTarget;
            }
            if (level instanceof ServerLevel serverLevel) {
                Entity entity = serverLevel.getEntity(targetUUID);
                if (entity instanceof LivingEntity livingEntity) {
                    cachedTarget = livingEntity;
                    return livingEntity;
                }
            }
            return null;
        }

        void tick(LivingEntity target) {
            ++ticksSinceRing;
            if (ticksSinceRing < RING_INTERVAL_TICKS) {
                return;
            }
            ticksSinceRing = 0;

            if (!(target.level() instanceof ServerLevel serverLevel)) {
                return;
            }

            Vec3 center = target.position().add(0, SWIRL_HEIGHT, 0);
            spawnPoisonRing(serverLevel, center, RING_PARTICLE_COUNT, RING_SPEED);
            serverLevel.playSound(null, center.x, center.y, center.z, TravelopticsSounds.AQUA_CAST_2.get(), SoundSource.NEUTRAL, 1.0f, 1.5f);
        }
    }
}
