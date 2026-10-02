package net.offkung.bhspells.pressure;

import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.BHSpells;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * In-memory dispatcher registry for pressure reactions.
 * Allows compatibility modules (e.g. Epic Fight, future animation mods) to register listeners
 * without creating circular or direct dependencies from ServerPressureManager to external packages.
 */
public final class PressureReactionDispatcher {
    private static final List<PressureReactionListener> LISTENERS = new CopyOnWriteArrayList<>();

    private PressureReactionDispatcher() {
    }

    public static void registerListener(PressureReactionListener listener) {
        if (listener != null && !LISTENERS.contains(listener)) {
            LISTENERS.add(listener);
        }
    }

    public static void unregisterListener(PressureReactionListener listener) {
        LISTENERS.remove(listener);
    }

    public static void dispatch(LivingEntity entity, PressureReaction oldReaction, PressureReaction newReaction) {
        if (entity == null) {
            return;
        }
        for (PressureReactionListener listener : LISTENERS) {
            try {
                listener.onReactionChanged(entity, oldReaction, newReaction);
            } catch (Throwable t) {
                BHSpells.LOGGER.error("Error dispatching pressure reaction change to listener", t);
            }
        }
    }
}
