package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

/**
 * Beneficial status effect granting phantom evasive charges against incoming attacks.
 * The amplifier represents (charges - 1).
 */
public class PhantomDodgeEffect extends MobEffect {
    public PhantomDodgeEffect(MobEffectCategory category, int color) {
        super(category, color);
    }
}
