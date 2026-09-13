package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;

public class EmbracingBosomEffect extends MobEffect {
    private static final float HEAL_AMOUNT = 5.0f;
    private static final int HEAL_INTERVAL_TICKS = 20;

    public EmbracingBosomEffect() {
        super(MobEffectCategory.BENEFICIAL, 0xE8A33D);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity livingEntity, int amplifier) {
        if (livingEntity.level().getGameTime() % HEAL_INTERVAL_TICKS != 0) {
            return;
        }
        if (livingEntity.getHealth() < livingEntity.getMaxHealth()) {
            livingEntity.heal(HEAL_AMOUNT);
        }
    }
}
