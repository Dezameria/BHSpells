package net.offkung.bhspells.compat.aaaparticles;

import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.CompatMods;

public final class AaaParticlesCompat {
    private static boolean linkageFailed;

    private AaaParticlesCompat() {
    }

    public static boolean isAvailable() {
        return CompatMods.isAaaParticlesLoaded() && !linkageFailed;
    }

    public static boolean addParticle(Level level, double maxDistance, ParticleEmitterInfo info) {
        if (!isAvailable() || level == null || info == null) {
            return false;
        }

        try {
            return AaaParticlesLoadedBridge.addParticle(level, maxDistance, info);
        } catch (LinkageError error) {
            linkageFailed = true;
            BHSpells.LOGGER.error("AAA Particles invocation failed due to linkage error", error);
            return false;
        }
    }
}
