package net.offkung.bhspells.client.event;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.player.ClientMagicData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.Input;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.ScreenEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.offkung.bhspells.registry.MobEffectsRegistry;

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
}
