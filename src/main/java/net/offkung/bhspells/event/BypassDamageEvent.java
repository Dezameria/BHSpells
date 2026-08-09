package net.offkung.bhspells.event;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

public class BypassDamageEvent {
    private static final TagKey<DamageType> BYPASSES_COOLDOWN = TagKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath("minecraft", "bypasses_cooldown"));

    @SubscribeEvent
    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getSource().is(BYPASSES_COOLDOWN)) {
            event.getEntity().invulnerableTime = 0;
        }
    }
}
