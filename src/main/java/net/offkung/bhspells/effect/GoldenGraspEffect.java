package net.offkung.bhspells.effect;

import com.gametechbc.traveloptics.init.TravelopticsEffects;
import com.gametechbc.traveloptics.init.TravelopticsSounds;
import com.github.L_Ender.cataclysm.entity.effect.ScreenShake_Entity;
import com.github.alexmodguy.alexscaves.server.potion.ACEffectRegistry;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.particle.BlastwaveParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.joml.Vector3f;

public class GoldenGraspEffect extends MobEffect {
    public GoldenGraspEffect() {
        super(MobEffectCategory.BENEFICIAL, 9636843);
    }

    public void applyEffectTick(LivingEntity entity, int amplifier) {
        MobEffectInstance effectInstance = entity.getEffect(this);
        Level level = entity.level();
        if (effectInstance != null) {
            int duration = effectInstance.getDuration();
            if (duration == 2) {
                level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), TravelopticsSounds.TIDAL_GRASP_SMACK.get(), SoundSource.NEUTRAL, 2.0F, 1.0F);
                ScreenShake_Entity.ScreenShake(level, entity.position(), 15.0F, 0.02F, 10, 20);
                AABB effectArea = entity.getBoundingBox().inflate(6.0F);

                for(LivingEntity nearbyEntity : entity.level().getEntitiesOfClass(LivingEntity.class, effectArea)) {
                    if (nearbyEntity.hasEffect(MobEffectsRegistry.GOLDEN_GRASP_HELPER.get())) {
                        nearbyEntity.addEffect(new MobEffectInstance(ACEffectRegistry.STUNNED.get(), 60, 0, false, true));
                        nearbyEntity.addEffect(new MobEffectInstance(TravelopticsEffects.WET.get(), 60, 2, false, true));
                        DamageSources.applyDamage(nearbyEntity, (float)amplifier, BHSpellRegistry.GOLDEN_HAND.get().getDamageSource(entity, entity));
                        if (!entity.level().isClientSide()) {
                            Level lvl = entity.level();
                            if (lvl instanceof ServerLevel) {
                                ServerLevel serverLevel = (ServerLevel)lvl;
                                BlastwaveParticleOptions goldWave = new BlastwaveParticleOptions(new Vector3f(1f, 0.84f, 0.28f), 1.5f);
                                serverLevel.sendParticles(goldWave, nearbyEntity.getX(), nearbyEntity.getY() - 0.3, nearbyEntity.getZ(), 1, 0.0F, 0.0F, 0.0F, 0.0F);
                            }
                        }
                    }
                }
            }
        }

    }

    public boolean isDurationEffectTick(int duration, int amplifier) {
        return true;
    }
}
