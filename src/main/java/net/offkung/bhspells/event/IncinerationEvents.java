package net.offkung.bhspells.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.registry.MobEffectsRegistry;

@Mod.EventBusSubscriber
public class IncinerationEvents {
    private static final float BONUS_PER_LEVEL = 0.20f;

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        if (!target.isOnFire()) {
            return;
        }
        MobEffectInstance instance = target.getEffect(MobEffectsRegistry.INCINERATION.get());
        if (instance == null) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return;
        }
        float multiplier = 1.0f + BONUS_PER_LEVEL * (instance.getAmplifier() + 1);
        event.setAmount(event.getAmount() * multiplier);
    }
}
