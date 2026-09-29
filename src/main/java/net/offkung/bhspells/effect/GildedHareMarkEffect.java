package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.offkung.bhspells.client.particle.GildedHareVfx;

public class GildedHareMarkEffect extends MobEffect {
    public static final String OWNER_UUID_TAG = "GildedHareOwnerUUID";
    public static final String COMBO_COUNT_TAG = "GildedHareComboCount";
    public static final String LAST_HIT_TICK_TAG = "GildedHareLastHitTick";
    public static final String FINISHER_COOLDOWN_TICK_TAG = "GildedHareFinisherCooldownTick";

    public GildedHareMarkEffect() {
        super(MobEffectCategory.HARMFUL, 0xFFE066);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return amplifier >= 4;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (amplifier >= 4) {
            entity.setDeltaMovement(0, 0, 0);
            entity.hurtMarked = true;
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        if (amplifier >= 4) {
            if (entity.level() instanceof net.minecraft.server.level.ServerLevel serverLevel && entity.isAlive()) {
                GildedHareVfx.spawnCocoonShatterVfx(serverLevel, entity);
            }
            clearComboData(entity);
        }
    }

    public static void clearComboData(LivingEntity entity) {
        var tag = entity.getPersistentData();
        tag.remove(OWNER_UUID_TAG);
        tag.remove(COMBO_COUNT_TAG);
        tag.remove(LAST_HIT_TICK_TAG);
    }
}
