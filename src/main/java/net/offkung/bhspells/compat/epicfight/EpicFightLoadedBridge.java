package net.offkung.bhspells.compat.epicfight;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.DistExecutor;
import net.offkung.bhspells.compat.api.AnimationRequest;
import net.offkung.bhspells.compat.api.CompatResult;
import net.offkung.bhspells.compat.api.VfxRequest;
import net.offkung.bhspells.compat.epicfight.client.EpicFightLegPoseHelper;
import net.offkung.bhspells.compat.epicfight.client.GildedHareEpicFightRenderCompat;
import net.offkung.bhspells.compat.epicfight.client.JadeAuraEpicFightRenderCompat;
import net.offkung.bhspells.compat.epicfight.common.animation.EpicFightAnimationPlayer;
import net.offkung.bhspells.compat.epicfight.common.animation.IronSpellAnimations;
import net.offkung.bhspells.compat.epicfight.common.particle.EpicFightVfx;

public final class EpicFightLoadedBridge {
    private EpicFightLoadedBridge() {
    }

    static void registerModEvents(IEventBus modEventBus) {
        modEventBus.addListener(IronSpellAnimations::registerAnimations);
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT,
                () -> () -> {
                    GildedHareEpicFightRenderCompat.registerModEvents(modEventBus);
                    JadeAuraEpicFightRenderCompat.registerModEvents(modEventBus);
                });
    }

    static CompatResult playAnimation(AnimationRequest request) {
        return EpicFightAnimationPlayer.play(request);
    }

    static boolean trySpawnFracture(LivingEntity source, Level level, Vec3 samplePosition, int searchUp, int searchDown, double radius) {
        return EpicFightVfx.trySpawnFracture(source, level, samplePosition, searchUp, searchDown, radius);
    }

    static CompatResult spawnVfx(VfxRequest request) {
        return EpicFightVfx.spawnVfx(request);
    }

    static void spawnScatterParticles(ServerLevel level, LivingEntity entity) {
        EpicFightVfx.spawnScatterParticles(level, entity);
    }

    static void spawnScatterParticles(ServerLevel level, double x, double y, double z) {
        EpicFightVfx.spawnScatterParticles(level, x, y, z);
    }

    static boolean isBattleMode(LivingEntity entity) {
        return EpicFightLegPoseHelper.isBattleMode(entity);
    }

    static Vec3 getLegJointWorldPos(LivingEntity entity, boolean isLeft) {
        return EpicFightLegPoseHelper.getLegJointWorldPos(entity, isLeft);
    }
}
