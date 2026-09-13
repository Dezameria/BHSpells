package net.offkung.bhspells.event;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.entity.spells.firebird.FireSlashProjectile;

@Mod.EventBusSubscriber
public class FireBodyHitEvent {
    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        Entity directEntity = event.getSource().getDirectEntity();
        if (!(directEntity instanceof LivingEntity caster)) return;
        if (directEntity instanceof FireSlashProjectile) return;
        if (!FireBodyManager.isActive(caster)) return;

        Entity target = event.getEntity();
        if (!(target instanceof LivingEntity livingTarget) || target == caster) return;

        if (FireBodyManager.getLevel(caster) >= 3) {
            event.setAmount(event.getAmount() * 1.5f);
        }

        FireBodyManager.registerHit(caster, livingTarget);
    }
}
