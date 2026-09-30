package net.offkung.bhspells.effect;

import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Beneficial lifecycle marker for Earth Roar's empowerment phase (60 seconds / 1,200 ticks).
 * On natural expiry (duration == 1), transitions the entity into the 60-second exhaustion phase
 * with Weakness II, Slowness II, Nausea I, and Mining Fatigue I.
 * If cleansed early (milk, anti-magic, death), debuffs are NOT applied.
 */
public class EarthRoarEmpowermentEffect extends MobEffect {
    public static final int EXHAUSTION_DURATION_TICKS = 1200;

    public EarthRoarEmpowermentEffect(MobEffectCategory category, int color) {
        super(category, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        MobEffectInstance effect = entity.getEffect(MobEffectsRegistry.EARTH_ROAR_EMPOWERMENT.get());
        if (effect != null && effect.getDuration() == 1) {
            // Natural expiration transition: apply exhaustion debuffs without potion swirl particles (visible: false, showIcon: true)
            entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, EXHAUSTION_DURATION_TICKS, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, EXHAUSTION_DURATION_TICKS, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.CONFUSION, EXHAUSTION_DURATION_TICKS, 0, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, EXHAUSTION_DURATION_TICKS, 0, false, false, true));

            entity.level().playSound(null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.ELDER_GUARDIAN_CURSE, SoundSource.PLAYERS, 0.8F, 0.9F);

            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("ui.bhspells.earth_roar_exhaustion"), true);
            }
        }
    }
}
