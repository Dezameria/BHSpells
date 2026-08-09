package net.offkung.bhspells.event;

import com.gametechbc.traveloptics.entity.misc.TOScreenShakeEntity;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.registry.BHSoundRegistry;

import java.util.*;

public class GoldenMarbleManager {
    private static final float BASE_ANGULAR_SPEED = 0.15f;
    private static final float BOOSTED_SPEED_MULTIPLIER = 4.5f;
    private static final int BOOST_DURATION_TICKS = 100;
    private static final double REFLECT_CHANCE = 0.40;

    private static final Map<UUID, MarbleGroup> ACTIVE_GROUPS = new HashMap<>();

    private GoldenMarbleManager() {
    }

    public static void registerGroup(LivingEntity owner, List<GoldenMarbleEntity> marbles) {
        MarbleGroup group = new MarbleGroup();
        for (GoldenMarbleEntity marble : marbles) {
            group.marbleIds.add(marble.getId());
        }
        ACTIVE_GROUPS.put(owner.getUUID(), group);
    }

    public static void onMarbleRemoved(UUID ownerId, int marbleId) {
        MarbleGroup group = ACTIVE_GROUPS.get(ownerId);
        if (group == null) return;
        group.marbleIds.remove(Integer.valueOf(marbleId));
        if (group.marbleIds.isEmpty()) {
            ACTIVE_GROUPS.remove(ownerId);
        }
    }

    public static boolean hasActiveGroup(UUID ownerId) {
        return ACTIVE_GROUPS.containsKey(ownerId);
    }

    public static float getSpeedMultiplier(UUID ownerId) {
        MarbleGroup group = ACTIVE_GROUPS.get(ownerId);
        return group != null ? group.speedMultiplier : 1.0f;
    }

    public static float getBaseAngularSpeed() {
        return BASE_ANGULAR_SPEED;
    }

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) return;

        MarbleGroup group = ACTIVE_GROUPS.get(target.getUUID());
        if (group == null) return;
        var source = event.getSource();

        if (source.is(DamageTypes.FELL_OUT_OF_WORLD)) return;
        if (source.is(DamageTypes.STARVE)) return;
        if (source.is(DamageTypes.THORNS)) return;
        if (source.is(DamageTypes.ON_FIRE)) return;
        if (source.is(DamageTypes.IN_FIRE)) return;
        if (source.is(DamageTypes.LAVA)) return;
        if (source.is(DamageTypes.DROWN)) return;
        if (source.is(DamageTypes.FALL)) return;

        Entity attacker = source.getEntity();
        if (!(attacker instanceof LivingEntity)) return;

        boolean reflectSuccess = target.level().getRandom().nextDouble() < REFLECT_CHANCE;
        if (!reflectSuccess) {
            event.setAmount(event.getAmount() * 0.5f);
            return;
        }

        event.setCanceled(true);

        group.speedMultiplier = BOOSTED_SPEED_MULTIPLIER;
        group.boostTicksRemaining = BOOST_DURATION_TICKS;

        spawnReflectEffects(target);
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;

        for (MarbleGroup group : ACTIVE_GROUPS.values()) {
            if (group.boostTicksRemaining > 0) {
                group.boostTicksRemaining--;
                if (group.boostTicksRemaining <= 0) {
                    group.speedMultiplier = 1.0f;
                }
            }
        }
    }

    private static void spawnReflectEffects(LivingEntity target) {
        Vec3 pos = target.position().add(0, target.getBbHeight() * 0.5, 0);
        target.level().playSound(null, pos.x, pos.y, pos.z, BHSoundRegistry.IRON_PARRY.get(), SoundSource.PLAYERS, 2f, 1f);
        TOScreenShakeEntity.createScreenShake(target.level(), pos, 8.0f, 0.02f, 20, 0, 2, true);

        if (target.level() instanceof ServerLevel serverLevel) {
            spawnHorizontalEndRodRing(serverLevel, pos, 36, 0.4);
        }
    }

    private static void spawnHorizontalEndRodRing(ServerLevel level, Vec3 center, int n, double speed) {
        for (int i = 0; i < n; ++i) {
            double theta = (Math.PI * 2D) * ((double) i / n);
            double dx = Math.sin(theta) * speed;
            double dz = Math.cos(theta) * speed;

            level.sendParticles(ParticleRegistry.DRAGON_FIRE_PARTICLE.get(),
                    center.x, center.y, center.z,
                    0,
                    dx, 0.0, dz,
                    1.0);
        }
    }

    private static class MarbleGroup {
        final List<Integer> marbleIds = new ArrayList<>();
        float speedMultiplier = 1.0f;
        int boostTicksRemaining = 0;
    }
}
