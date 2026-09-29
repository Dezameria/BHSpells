package net.offkung.bhspells.util;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeConstants;

public class BHAmethystSounds {
    private static final float PITCH_JITTER = 0.1f;

    private BHAmethystSounds() {
    }

    private static float jitter(RandomSource random, double basePitch) {
        return (float) basePitch + (random.nextFloat() - 0.5f) * PITCH_JITTER;
    }

    public static void playCastStart(Level level, double x, double y, double z, RandomSource random) {
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_RESONATE, SoundSource.PLAYERS, AmethystDecreeConstants.CAST_START_VOLUME, jitter(random, AmethystDecreeConstants.CAST_START_PITCH));
    }

    public static void playRiseChime(Level level, double x, double y, double z, RandomSource random) {
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, AmethystDecreeConstants.RISE_CHIME_VOLUME, jitter(random, AmethystDecreeConstants.RISE_CHIME_PITCH));
    }

    public static void playEruption(Level level, double x, double y, double z, RandomSource random, int layers) {
        float volume = AmethystDecreeConstants.ERUPTION_VOLUME;
        double basePitch = AmethystDecreeConstants.ERUPTION_PITCH;
        for (int i = 0; i < layers; i++) {
            level.playSound(null, x, y, z, SoundEvents.AMETHYST_CLUSTER_PLACE, SoundSource.PLAYERS, volume, jitter(random, basePitch));
        }
    }

    public static void playShatter(Level level, double x, double y, double z, RandomSource random) {
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_CLUSTER_BREAK, SoundSource.PLAYERS, AmethystDecreeConstants.SHATTER_VOLUME, jitter(random, AmethystDecreeConstants.SHATTER_PITCH));
    }

    public static void playDotChime(Level level, double x, double y, double z, RandomSource random) {
        level.playSound(null, x, y, z, SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, AmethystDecreeConstants.DOT_CHIME_VOLUME, jitter(random, AmethystDecreeConstants.DOT_CHIME_PITCH));
    }
}
