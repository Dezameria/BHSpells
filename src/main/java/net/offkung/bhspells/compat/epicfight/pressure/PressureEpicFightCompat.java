package net.offkung.bhspells.compat.epicfight.pressure;

import net.offkung.bhspells.compat.CompatMods;
import net.offkung.bhspells.pressure.PressureReaction;
import net.minecraft.world.entity.LivingEntity;

/**
 * Public safe bridge for Epic Fight reaction animations.
 * Safe to invoke from ServerPressureManager regardless of whether Epic Fight is installed.
 */
public final class PressureEpicFightCompat {
    private PressureEpicFightCompat() {
    }

    public static void onReactionChange(LivingEntity entity, PressureReaction reaction) {
        if (!CompatMods.isEpicFightLoaded() || entity == null || reaction == PressureReaction.NONE) {
            return;
        }

        PressureAnimationController.handleReaction(entity, reaction);
    }
}
