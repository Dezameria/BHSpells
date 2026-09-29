package net.offkung.bhspells.compat;

import net.minecraftforge.fml.ModList;

public final class CompatMods {
    public static final String EPIC_FIGHT = "epicfight";
    public static final String AVALON = "epic_fight_avalon";
    public static final String NIGHTFALL = "efn";
    public static final String AAA_PARTICLES = "aaa_particles";

    private CompatMods() {
    }

    public static boolean isEpicFightLoaded() {
        return ModList.get().isLoaded(EPIC_FIGHT);
    }

    public static boolean isAvalonLoaded() {
        return ModList.get().isLoaded(AVALON);
    }

    public static boolean isNightfallLoaded() {
        return ModList.get().isLoaded(NIGHTFALL);
    }

    public static boolean isAaaParticlesLoaded() {
        return ModList.get().isLoaded(AAA_PARTICLES);
    }
}
