package net.offkung.bhspells.event;

import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.spells.fire.FieryDanceSpell;
import yesman.epicfight.api.forgeevent.EntityStunEvent;

@Mod.EventBusSubscriber
public class FireBodyStunEvent {
    @SubscribeEvent
    public static void onEntityStun(EntityStunEvent event) {
        LivingEntity target = event.getStunnedEntityPatch().getOriginal();

        if (FireBodyManager.isActive(target) && FireBodyManager.getLevel(target) >= 2) {
            event.setCanceled(true);
        } else if (GoldenMarbleManager.isActive(target) || FieryDanceSpell.isCasting(target)) {
            event.setCanceled(true);
        }
    }
}
