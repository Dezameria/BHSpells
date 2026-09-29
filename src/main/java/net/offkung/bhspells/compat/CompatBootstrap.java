package net.offkung.bhspells.compat;

import net.minecraftforge.eventbus.api.IEventBus;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;

public final class CompatBootstrap {
    private static boolean initialized;

    private CompatBootstrap() {
    }

    public static void init(IEventBus modEventBus) {
        if (initialized) {
            return;
        }
        initialized = true;

        if (CompatMods.isEpicFightLoaded()) {
            try {
                EpicFightCompat.registerModEvents(modEventBus);
                BHSpells.LOGGER.info("Epic Fight compatibility initialized successfully.");
            } catch (Throwable t) {
                BHSpells.LOGGER.error("Failed to initialize Epic Fight compatibility", t);
            }
        }

        if (CompatMods.isAvalonLoaded()) {
            BHSpells.LOGGER.info("Epic Fight - Avalon compatibility detected.");
        }

        if (CompatMods.isAaaParticlesLoaded()) {
            BHSpells.LOGGER.info("AAA Particles (Effekseer) compatibility detected.");
        }
    }
}
