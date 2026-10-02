package net.offkung.bhspells.client.event;

import mod.chloeprime.aaaparticles.api.client.EffectHolder;
import mod.chloeprime.aaaparticles.api.client.EffectRegistry;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.spells.gold.HymnofPurificationSpell;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class HymnofPurificationClientEvents {
    private static final Map<UUID, HymnSoundInstance> ACTIVE_SOUNDS = new HashMap<>();
    private static final Map<UUID, Integer> PERFORMER_TICKS = new HashMap<>();

    private HymnofPurificationClientEvents() {
    }

    public static boolean isPerforming(Player player) {
        return player != null && player.hasEffect(MobEffectsRegistry.HYMN_OF_PURIFICATION.get());
    }

    public static void stopClientEffek(int entityId) {
        ResourceLocation emitterName = HymnofPurificationSpell.getEmitterName(entityId);
        Optional.ofNullable(EffectRegistry.get(HymnofPurificationSpell.SHIBA_COMMISSION_EFFEK_ID))
                .flatMap(EffectHolder::lazyGet)
                .flatMap(mng -> mng.getNamedEmitter(ParticleEmitter.Type.WORLD, emitterName))
                .ifPresent(emitter -> emitter.stop());
    }

    public static void stopClientEffek(Player player) {
        if (player != null) {
            stopClientEffek(player.getId());
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) {
            stopAllSounds(minecraft);
            return;
        }

        for (AbstractClientPlayer player : level.players()) {
            if (isPerforming(player)) {
                UUID playerId = player.getUUID();
                int ticks = PERFORMER_TICKS.getOrDefault(playerId, 0) + 1;
                var effect = player.getEffect(MobEffectsRegistry.HYMN_OF_PURIFICATION.get());
                if (effect != null) {
                    int elapsed = HymnofPurificationSpell.DURATION_TICKS - effect.getDuration();
                    if (elapsed > ticks) {
                        ticks = elapsed;
                    }
                }
                PERFORMER_TICKS.put(playerId, ticks);

                if (ticks >= HymnofPurificationSpell.SOUND_DELAY_TICKS && !ACTIVE_SOUNDS.containsKey(playerId)) {
                    HymnSoundInstance sound = new HymnSoundInstance(player, BHSoundRegistry.HYMN_OF_PURIFICATION.get());
                    ACTIVE_SOUNDS.put(playerId, sound);
                    minecraft.getSoundManager().play(sound);
                }
            }
        }

        Iterator<Map.Entry<UUID, Integer>> performerIterator = PERFORMER_TICKS.entrySet().iterator();
        while (performerIterator.hasNext()) {
            Map.Entry<UUID, Integer> entry = performerIterator.next();
            UUID playerId = entry.getKey();
            Player player = level.getPlayerByUUID(playerId);
            if (player == null || player.isRemoved() || !isPerforming(player)) {
                performerIterator.remove();
                HymnSoundInstance sound = ACTIVE_SOUNDS.remove(playerId);
                if (sound != null) {
                    sound.stopPlaying();
                    minecraft.getSoundManager().stop(sound);
                }
            }
        }

        Iterator<Map.Entry<UUID, HymnSoundInstance>> soundIterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (soundIterator.hasNext()) {
            Map.Entry<UUID, HymnSoundInstance> entry = soundIterator.next();
            HymnSoundInstance sound = entry.getValue();
            if (sound.isStopped()) {
                soundIterator.remove();
            }
        }
    }

    public static void stopAllSounds(Minecraft minecraft) {
        for (Map.Entry<UUID, HymnSoundInstance> entry : ACTIVE_SOUNDS.entrySet()) {
            entry.getValue().stopPlaying();
            if (minecraft != null && minecraft.getSoundManager() != null) {
                minecraft.getSoundManager().stop(entry.getValue());
            }
        }
        ACTIVE_SOUNDS.clear();
        PERFORMER_TICKS.clear();
        if (minecraft != null && minecraft.getSoundManager() != null) {
            minecraft.getSoundManager().stop(BHSoundRegistry.HYMN_OF_PURIFICATION.get().getLocation(), SoundSource.PLAYERS);
        }
    }

    public static class HymnSoundInstance extends AbstractTickableSoundInstance {
        private final Player player;

        public HymnSoundInstance(Player player, SoundEvent soundEvent) {
            super(soundEvent, SoundSource.PLAYERS, SoundInstance.createUnseededRandom());
            this.player = player;
            this.looping = false;
            this.delay = 0;
            this.volume = 1.0F;
            this.pitch = 1.0F;
            this.x = player.getX();
            this.y = player.getY();
            this.z = player.getZ();
        }

        public void stopPlaying() {
            this.stop();
        }

        @Override
        public void tick() {
            if (this.player.isRemoved() || !isPerforming(this.player)) {
                this.stop();
                return;
            }
            this.x = this.player.getX();
            this.y = this.player.getY();
            this.z = this.player.getZ();
        }
    }
}
