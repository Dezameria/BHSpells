package net.offkung.bhspells.effect;

import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.effect.MagicMobEffect;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.entity.spells.divine_thunder.RedLightningStrike;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.util.BHUtil;

@Mod.EventBusSubscriber
public class RedThunderStormEffect extends MagicMobEffect {
    public static final float DAMAGE = 5f;
    public static final int RADIUS = 20;

    public RedThunderStormEffect(MobEffectCategory pCategory, int pColor) {
        super(pCategory, pColor);
    }

    @Override
    public boolean isDurationEffectTick(int pDuration, int pAmplifier) {
        return true;
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int pAmplifier) {
        if (entity.level() instanceof ServerLevel serverLevel && !entity.hasEffect(MobEffectsRegistry.RED_CHARGED.get())) {
            BHUtil.spawnRedThunderAura(serverLevel, entity, 2);
        }

        if (entity.tickCount % 40 != 0) {
            return;
        }

        var radiusSqr = RADIUS * RADIUS;
        entity.level().getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(RADIUS, 12, RADIUS),
                        livingEntity -> livingEntity != entity &&
                                livingEntity.isAlive() &&
                                horizontalDistanceSqr(livingEntity, entity) < radiusSqr &&
                                livingEntity.isPickable() &&
                                !livingEntity.isSpectator() &&
                                Utils.hasLineOfSight(entity.level(), entity, livingEntity, false)
                )
                .forEach(targetEntity -> {
                    RedLightningStrike redlightningStrike = new RedLightningStrike(entity.level());
                    redlightningStrike.setOwner(entity);
                    redlightningStrike.setDamage(DAMAGE);
                    redlightningStrike.setPos(targetEntity.position());
                    entity.level().addFreshEntity(redlightningStrike);
                });
    }

    private float horizontalDistanceSqr(LivingEntity livingEntity, LivingEntity entity2) {
        var dx = livingEntity.getX() - entity2.getX();
        var dz = livingEntity.getZ() - entity2.getZ();
        return (float) (dx * dx + dz * dz);
    }
}
