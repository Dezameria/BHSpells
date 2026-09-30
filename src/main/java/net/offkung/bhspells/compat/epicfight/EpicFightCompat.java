package net.offkung.bhspells.compat.epicfight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.IEventBus;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.CompatMods;
import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.api.AnimationRequest;
import net.offkung.bhspells.compat.api.CompatResult;

/**
 * Public safe facade for Epic Fight integration.
 * Safe to call anywhere from spell code or common logic.
 */
public final class EpicFightCompat {
    private static boolean linkageFailed;

    private EpicFightCompat() {
    }

    public static boolean isAvailable() {
        return CompatMods.isEpicFightLoaded() && !linkageFailed;
    }

    public static void registerModEvents(IEventBus modEventBus) {
        if (!isAvailable()) {
            return;
        }

        try {
            EpicFightLoadedBridge.registerModEvents(modEventBus);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight event registration failed due to linkage error", error);
        }
    }

    public static CompatResult playAnimation(AnimationRequest request) {
        if (!isAvailable() || request == null) {
            return CompatResult.UNAVAILABLE;
        }

        try {
            return EpicFightLoadedBridge.playAnimation(request);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight animation playback failed due to linkage error", error);
            return CompatResult.FAILED;
        }
    }

    public static CompatResult playAnimation(LivingEntity entity, AnimationCue cue) {
        return playAnimation(AnimationRequest.of(entity, cue));
    }

    public static CompatResult playAnimation(LivingEntity entity, AnimationCue cue, float transitionDuration) {
        return playAnimation(AnimationRequest.of(entity, cue, transitionDuration));
    }

    public static boolean spawnFracture(LivingEntity source, Level level, Vec3 samplePosition,
            int searchUp, int searchDown, double radius) {
        if (!isAvailable() || level.isClientSide || source == null) {
            return false;
        }

        try {
            return EpicFightLoadedBridge.trySpawnFracture(source, level, samplePosition, searchUp, searchDown, radius);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight fracture spawn failed due to linkage error", error);
            return false;
        }
    }

    public static boolean isBattleMode(LivingEntity entity) {
        if (!isAvailable() || entity == null) {
            return false;
        }

        try {
            return EpicFightLoadedBridge.isBattleMode(entity);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight battle mode check failed due to linkage error", error);
            return false;
        }
    }

    public static boolean isDodging(LivingEntity entity) {
        if (!isAvailable() || entity == null) {
            return false;
        }

        try {
            return EpicFightLoadedBridge.isDodging(entity);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight dodging check failed due to linkage error", error);
            return false;
        }
    }

    public static void stopAnimation(LivingEntity entity) {
        if (!isAvailable() || entity == null) {
            return;
        }

        try {
            EpicFightLoadedBridge.stopAnimation(entity);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight stop animation failed due to linkage error", error);
        }
    }

    public static boolean triggerPhantomDodge(LivingEntity entity) {
        if (!isAvailable() || entity == null) {
            return false;
        }

        try {
            return EpicFightLoadedBridge.triggerPhantomDodge(entity);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight phantom dodge trigger failed due to linkage error", error);
            return false;
        }
    }
}
