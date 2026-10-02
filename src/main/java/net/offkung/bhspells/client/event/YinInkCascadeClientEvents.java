package net.offkung.bhspells.client.event;

import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.capabilities.magic.SyncedSpellData;
import mod.chloeprime.aaaparticles.api.client.EffectHolder;
import mod.chloeprime.aaaparticles.api.client.EffectRegistry;
import mod.chloeprime.aaaparticles.api.client.effekseer.ParticleEmitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.spells.aqua.YinInkCascadeSpell;

import java.util.Optional;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class YinInkCascadeClientEvents {

    private YinInkCascadeClientEvents() {
    }

    public static void stopClientEffek(int entityId) {
        ResourceLocation emitterName = YinInkCascadeSpell.getEmitterName(entityId);
        Optional.ofNullable(EffectRegistry.get(YinInkCascadeSpell.MRQUESTION_EFFEK_ID))
                .flatMap(EffectHolder::lazyGet)
                .flatMap(mng -> mng.getNamedEmitter(ParticleEmitter.Type.WORLD, emitterName))
                .ifPresent(ParticleEmitter::stop);
    }

    public static void stopClientEffek(Player player) {
        if (player != null) {
            stopClientEffek(player.getId());
        }
    }

    private static boolean isLocalPlayerCasting() {
        return ClientMagicData.isCasting() && YinInkCascadeSpell.SPELL_ID_STR.equals(ClientMagicData.getCastingSpellId());
    }

    private static boolean isCasting(AbstractClientPlayer player) {
        Minecraft minecraft = Minecraft.getInstance();
        if (player == minecraft.player) {
            return isLocalPlayerCasting();
        }
        SyncedSpellData syncedData = ClientMagicData.getSyncedSpellData(player);
        return syncedData != null && syncedData.isCasting() && YinInkCascadeSpell.SPELL_ID_STR.equals(syncedData.getCastingSpellId());
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) {
            return;
        }

        // During the 5-second cast duration, continuously update the Effekseer emitter's position and orientation to match caster
        for (AbstractClientPlayer player : level.players()) {
            if (isCasting(player)) {
                ResourceLocation emitterName = YinInkCascadeSpell.getEmitterName(player.getId());
                Optional.ofNullable(EffectRegistry.get(YinInkCascadeSpell.MRQUESTION_EFFEK_ID))
                        .flatMap(EffectHolder::lazyGet)
                        .flatMap(mng -> mng.getNamedEmitter(ParticleEmitter.Type.WORLD, emitterName))
                        .ifPresent(emitter -> {
                            Vec3 targetPos = YinInkCascadeSpell.getTargetPosition(player);
                            emitter.setPosition((float) targetPos.x, (float) (targetPos.y + 0.05F), (float) targetPos.z);
                            float rotY = (float) (Math.PI - Math.toRadians(player.getYRot()));
                            emitter.setRotation(0.0F, rotY, 0.0F);
                        });
            }
        }
    }
}
