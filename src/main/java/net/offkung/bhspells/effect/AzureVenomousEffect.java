package net.offkung.bhspells.effect;

import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.spells.nature.VenomousBlossomfallSpell;
import org.joml.Vector3f;

public class AzureVenomousEffect extends MobEffect {
    private static final DustParticleOptions AZURE_DUST = new DustParticleOptions(new Vector3f(0.05F, 0.95F, 0.75F), 1.0F);

    public AzureVenomousEffect() {
        super(MobEffectCategory.HARMFUL, 0x16E6C1);
    }

    @Override
    public boolean isDurationEffectTick(int duration, int amplifier) {
        return duration % VenomousBlossomfallSpell.AZURE_VENOM_DAMAGE_INTERVAL == 0;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        if (entity.level().isClientSide) {
            return;
        }

        entity.hurt(entity.damageSources().magic(), VenomousBlossomfallSpell.AZURE_VENOM_DAMAGE);
        if (entity.level() instanceof ServerLevel serverLevel) {
            serverLevel.sendParticles(AZURE_DUST, entity.getX(), entity.getY() + entity.getBbHeight() * 0.5D, entity.getZ(), 8, entity.getBbWidth() * 0.45D, entity.getBbHeight() * 0.35D, entity.getBbWidth() * 0.45D, 0.025D);
        }
    }
}
