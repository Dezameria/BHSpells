package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.MovementAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/** Direct equivalent of Wukong's MovementAnimationMixin for the Ding effect. */
@Mixin(value = MovementAnimation.class, remap = false)
public abstract class DingMovementAnimationMixin {
    @Inject(method = "getPlaySpeed", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$freezeDingMovementAnimation(LivingEntityPatch<?> entityPatch,
                                                            DynamicAnimation animation,
                                                            CallbackInfoReturnable<Float> callback) {
        if (entityPatch != null) {
            try {
                LivingEntity entity = entityPatch.getOriginal();
                if (entity != null && entity.getActiveEffectsMap() != null && entity.hasEffect(MobEffectsRegistry.DING.get())) {
                    callback.setReturnValue(0.0F);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
