package net.offkung.bhspells.event;

import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingDamageEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.registry.MobEffectsRegistry;

@Mod.EventBusSubscriber
public class EmbracingBosomEvents {
    private static final ThreadLocal<Boolean> APPLYING_SHORTENED_DEBUFF = ThreadLocal.withInitial(() -> false);
    private static final float DAMAGE_REDUCTION_MULTIPLIER = 0.8f;
    private static final float DEBUFF_DURATION_MULTIPLIER = 0.7f;
    private static final int EFFECTIVELY_INFINITE_DURATION_TICKS = 1_000_000;

    @SubscribeEvent
    public static void onLivingDamage(LivingDamageEvent event) {
        LivingEntity target = event.getEntity();
        if (!target.hasEffect(MobEffectsRegistry.EMBRACING_BOSOM.get())) {
            return;
        }
        if (event.getSource().is(DamageTypeTags.BYPASSES_EFFECTS)) {
            return;
        }
        event.setAmount(event.getAmount() * DAMAGE_REDUCTION_MULTIPLIER);
    }

    @SubscribeEvent
    public static void onApplicable(MobEffectEvent.Applicable event) {
        if (APPLYING_SHORTENED_DEBUFF.get()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide) {
            return;
        }
        MobEffectInstance instance = event.getEffectInstance();
        if (instance.isInfiniteDuration() || instance.getDuration() < 0 || instance.getDuration() >= EFFECTIVELY_INFINITE_DURATION_TICKS) {
            return;
        }
        MobEffect effect = instance.getEffect();
        if (effect == MobEffectsRegistry.EMBRACING_BOSOM.get()) {
            return;
        }
        if (effect.getCategory() != MobEffectCategory.HARMFUL) {
            return;
        }
        if (!target.hasEffect(MobEffectsRegistry.EMBRACING_BOSOM.get())) {
            return;
        }
        event.setResult(Event.Result.DENY);
        int shortenedDuration = Math.max(1, Math.round(instance.getDuration() * DEBUFF_DURATION_MULTIPLIER));
        MobEffectInstance shortened = new MobEffectInstance(effect, shortenedDuration, instance.getAmplifier(), instance.isAmbient(), instance.isVisible(), instance.showIcon());
        APPLYING_SHORTENED_DEBUFF.set(true);
        try {
            target.addEffect(shortened);
        } finally {
            APPLYING_SHORTENED_DEBUFF.set(false);
        }
    }
}
