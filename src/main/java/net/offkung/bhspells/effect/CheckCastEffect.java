package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

public class CheckCastEffect extends MobEffect {
    public CheckCastEffect() {
        super(MobEffectCategory.HARMFUL, 0x800080);
    }
}
