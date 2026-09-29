package net.offkung.bhspells.event;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import yesman.epicfight.world.item.EpicFightItems;

@Mod.EventBusSubscriber
public class YanYingEvents {
    public static final int BUFF_DURATION_TICKS = 10; // 0.5 seconds (20 ticks = 1 second)

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || attacker.level().isClientSide) {
            return;
        }

        ItemStack mainHand = attacker.getMainHandItem();
        ItemStack offHand = attacker.getOffhandItem();
        if (mainHand.is(EpicFightItems.BOKKEN.get()) || offHand.is(EpicFightItems.BOKKEN.get())) {
            applySmilesOfFire(attacker);
        }
    }

    public static void applySmilesOfFire(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(
                MobEffectsRegistry.SMILES_OF_FIRE.get(),
                BUFF_DURATION_TICKS,
                0,
                false,
                false,
                true
        ));
    }
}
