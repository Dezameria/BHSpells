package net.offkung.bhspells.compat.epicfight.client;

import net.minecraft.world.entity.LivingEntity;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Isolated client-side helper for sampling Epic Fight biped leg joint transforms.
 * Only loaded when CompatMods.isEpicFightLoaded() is true.
 */
public final class EpicFightLegPoseHelper {
    private EpicFightLegPoseHelper() {
    }

    public static boolean isBattleMode(LivingEntity entity) {
        if (entity == null) {
            return false;
        }
        try {
            LivingEntityPatch<?> patch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (patch instanceof yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch<?> playerPatch) {
                return playerPatch.isEpicFightMode();
            }
            return patch != null;
        } catch (Throwable ignored) {
            return false;
        }
    }
}
