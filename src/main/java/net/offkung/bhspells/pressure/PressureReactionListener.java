package net.offkung.bhspells.pressure;

import net.minecraft.world.entity.LivingEntity;

/**
 * Listener interface for observing entity reaction transitions inside pressure fields.
 * Decouples core pressure calculation from external presentation adapters (e.g. Epic Fight).
 */
@FunctionalInterface
public interface PressureReactionListener {
    void onReactionChanged(LivingEntity entity, PressureReaction oldReaction, PressureReaction newReaction);
}
