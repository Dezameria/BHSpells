package net.offkung.bhspells.client.event;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class HymnofPurificationClientEvents {
    private static final Map<UUID, HymnSoundInstance> ACTIVE_SOUNDS = new HashMap<>();

    private HymnofPurificationClientEvents() {
    }

    public static boolean isPerforming(Player player) {
        return player != null && player.hasEffect(MobEffectsRegistry.HYMN_OF_PURIFICATION.get());
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
                if (!ACTIVE_SOUNDS.containsKey(player.getUUID())) {
                    HymnSoundInstance sound = new HymnSoundInstance(player, BHSoundRegistry.HYMN_OF_PURIFICATION.get());
                    ACTIVE_SOUNDS.put(player.getUUID(), sound);
                    minecraft.getSoundManager().play(sound);
                }
            }
        }

        Iterator<Map.Entry<UUID, HymnSoundInstance>> iterator = ACTIVE_SOUNDS.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<UUID, HymnSoundInstance> entry = iterator.next();
            UUID playerId = entry.getKey();
            HymnSoundInstance sound = entry.getValue();

            Player player = level.getPlayerByUUID(playerId);
            if (player == null || player.isRemoved() || !isPerforming(player) || sound.isStopped()) {
                sound.stopPlaying();
                minecraft.getSoundManager().stop(sound);
                iterator.remove();
            }
        }
    }

    public static void stopAllSounds(Minecraft minecraft) {
        for (HymnSoundInstance sound : ACTIVE_SOUNDS.values()) {
            sound.stopPlaying();
            if (minecraft != null && minecraft.getSoundManager() != null) {
                minecraft.getSoundManager().stop(sound);
            }
        }
        ACTIVE_SOUNDS.clear();
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
