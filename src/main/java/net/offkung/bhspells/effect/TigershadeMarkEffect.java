package net.offkung.bhspells.effect;

import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.offkung.bhspells.spells.ground.TigershadeTerrabreakSpell;

public class TigershadeMarkEffect extends MobEffect {
    public static final String MARK_CASTER_UUID_TAG = "TigershadeMarkCasterUUID";
    public static final String MARK_CASTER_DIM_TAG = "TigershadeMarkCasterDimension";

    public TigershadeMarkEffect() {
        super(MobEffectCategory.HARMFUL, 0xD2691E);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return false;
    }

    @Override
    public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
        super.removeAttributeModifiers(entity, attributeMap, amplifier);
        TigershadeTerrabreakSpell.onMarkRemoved(entity);
    }
}
