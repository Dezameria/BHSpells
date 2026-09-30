package net.offkung.bhspells.client.event;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.service.DingShenFaService;
import io.redspace.ironsspellbooks.network.casting.CancelCastPacket;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client-side input and action cancellation for players immobilized by Ding Shen Fa.
 * Suppresses walking (WASD), jumping (Space), sneaking (Shift), attacking (Left-Click),
 * and item use/interaction (Right-Click), while allowing the camera to turn freely
 * (compatible with Freecam and standard camera panning).
 */
@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class DingClientEvents {
    private DingClientEvents() {
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onMovementInput(MovementInputUpdateEvent event) {
        if (DingShenFaService.isImmobilized(event.getEntity())) {
            Input input = event.getInput();
            input.forwardImpulse = 0.0F;
            input.leftImpulse = 0.0F;
            input.up = false;
            input.down = false;
            input.left = false;
            input.right = false;
            input.jumping = false;
            input.shiftKeyDown = false;
        }
    }

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onInteractionKeyMapping(InputEvent.InteractionKeyMappingTriggered event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && DingShenFaService.isImmobilized(mc.player)) {
            if (event.isAttack() || event.isUseItem() || event.isPickBlock()) {
                event.setCanceled(true);
                event.setSwingHand(false);
            }
        }
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null && DingShenFaService.isImmobilized(mc.player)) {
            if (ClientMagicData.isCasting()) {
                PacketDistributor.sendToServer(new CancelCastPacket(false));
            }
        }
    }
}
