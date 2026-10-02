package net.offkung.bhspells.compat.epicfight.pressure;

import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.CompatMods;
import net.offkung.bhspells.pressure.PressureReaction;
import net.offkung.bhspells.pressure.PressureReactionDispatcher;

/**
 * Public safe bridge and listener for Epic Fight reaction animations.
 * Listens to PressureReactionDispatcher without ServerPressureManager needing to import Epic Fight classes.
 */
public final class PressureEpicFightCompat {
    private static boolean linkageFailed;
    private static boolean registered;

    private PressureEpicFightCompat() {
    }

    public static void register() {
        if (!CompatMods.isEpicFightLoaded() || registered) {
            return;
        }
        registered = true;
        try {
            PressureReactionDispatcher.registerListener(PressureEpicFightCompat::onReactionChange);
            BHSpells.LOGGER.info("Registered Epic Fight pressure reaction listener.");
        } catch (Throwable t) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Failed to register Epic Fight pressure listener", t);
        }
    }

    public static void onReactionChange(LivingEntity entity, PressureReaction oldReaction, PressureReaction newReaction) {
        if (!CompatMods.isEpicFightLoaded() || linkageFailed || entity == null) {
            return;
        }

        try {
            PressureAnimationController.handleReaction(entity, oldReaction, newReaction);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("Epic Fight pressure animation failed due to linkage error", error);
        } catch (Throwable t) {
            BHSpells.LOGGER.error("Unexpected error handling Epic Fight pressure reaction", t);
        }
    }
}
