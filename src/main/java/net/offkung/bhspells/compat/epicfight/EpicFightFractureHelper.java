package net.offkung.bhspells.compat.epicfight;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

/**
 * Backward compatibility facade for fracture spawning.
 * Delegates to {@link EpicFightCompat}.
 */
public final class EpicFightFractureHelper {
    private EpicFightFractureHelper() {
    }

    public static boolean trySpawnFracture(LivingEntity source, Level level, Vec3 samplePosition,
            int searchUp, int searchDown, double radius) {
        return EpicFightCompat.spawnFracture(source, level, samplePosition, searchUp, searchDown, radius);
    }
}
