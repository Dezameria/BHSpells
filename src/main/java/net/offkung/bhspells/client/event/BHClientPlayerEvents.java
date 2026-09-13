package net.offkung.bhspells.client.event;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudEntity;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.minecraftforge.event.TickEvent;
import net.offkung.bhspells.client.BHKeyMappings;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.client.DismountGoldenCloudPacket;
import yesman.epicfight.api.animation.LivingMotions;
import yesman.epicfight.api.client.forgeevent.UpdatePlayerMotionEvent;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT)
public class BHClientPlayerEvents {
    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onPlayerOpenScreen(ScreenEvent.Opening event) {
        if (ClientMagicData.isCasting()) {
            var spell = SpellRegistry.getSpell(ClientMagicData.getCastingSpellId());
            var player = Minecraft.getInstance().player;

            if (spell != null && !spell.canBeInterrupted(player)) {
                event.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            while (BHKeyMappings.DISMOUNT_GOLDEN_CLOUD.consumeClick()) {
                var player = Minecraft.getInstance().player;
                if (player != null && player.getVehicle() instanceof GoldenCloudEntity) {
                    PacketHandler.INSTANCE.sendToServer(new DismountGoldenCloudPacket());
                }
            }
        }
    }

    @SubscribeEvent
    public static void PMovementEvent(MovementInputUpdateEvent e) {
        Input input = e.getInput();
        Minecraft MC = Minecraft.getInstance();
        if(e.getEntity().hasEffect(MobEffectsRegistry.PERPLEXITY.get())) {
            input.forwardImpulse*=-1;
            input.leftImpulse*=-1;
            input.jumping=MC.options.keyShift.isDown();
            input.shiftKeyDown=MC.options.keyJump.isDown();
        }
    }

    @SubscribeEvent
    public static void onUpdatePlayerMotion(UpdatePlayerMotionEvent.BaseLayer event) {
        if (event.getPlayerPatch() != null && event.getPlayerPatch().getOriginal() != null) {
            var vehicle = event.getPlayerPatch().getOriginal().getVehicle();
            if (vehicle instanceof GoldenCloudEntity || (vehicle != null && !vehicle.shouldRiderSit())) {
                if (event.getMotion() == LivingMotions.SIT || event.getMotion() == LivingMotions.MOUNT) {
                    event.setMotion(LivingMotions.IDLE);
                }
            }
        }
    }
}
