package net.offkung.bhspells.effect;

import io.redspace.ironsspellbooks.effect.ISyncedMobEffect;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.util.BHUtil;

public class RedChargedEffect extends MagicMobEffect implements ISyncedMobEffect {
    public static final float ATTACK_DAMAGE_BONUS = .3f;
    public static final float SPEED_BONUS = .6f;
    public static final float SPELL_POWER_BONUS = .15f;

    public RedChargedEffect(MobEffectCategory mobEffectCategory, int color) {
        super(mobEffectCategory, color);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level() instanceof ServerLevel serverLevel) {
            BHUtil.spawnRedThunderAura(serverLevel, entity, 2);
        }
    }
}
