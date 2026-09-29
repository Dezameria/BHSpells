package net.offkung.bhspells.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.event.YanYingEvents;
import net.offkung.bhweapons.registry.ItemRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.skill.guard.GuardSkill;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.entity.eventlistener.TakeDamageEvent;
import yesman.epicfight.world.item.EpicFightItems;

@Mixin(value = GuardSkill.class, remap = false)
public class MixinGuardSkill {
    @Inject(method = "dealEvent", at = @At("HEAD"))
    private void bhspells$onGuardDealEvent(PlayerPatch<?> playerPatch, TakeDamageEvent.Attack event, boolean advanced, CallbackInfo ci) {
        LivingEntity defender = playerPatch.getOriginal();
        if (defender != null && !defender.level().isClientSide) {
            if (event.getDamageSource().getEntity() != null) {
                if (defender.getMainHandItem().is(EpicFightItems.BOKKEN.get()) || defender.getOffhandItem().is(EpicFightItems.BOKKEN.get())) {
                    YanYingEvents.applySmilesOfFire(defender);
                }
            }
        }
    }
}
