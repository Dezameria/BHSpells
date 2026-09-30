package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.LinkAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Freezes playback speed for LinkAnimation (transition/blend poses) on immobilized entities.
 */
@Mixin(value = LinkAnimation.class, remap = false)
public abstract class DingLinkAnimationMixin {
    @Inject(method = "getPlaySpeed", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$freezeDingLinkAnimation(LivingEntityPatch<?> entityPatch,
                                                       DynamicAnimation animation,
                                                       CallbackInfoReturnable<Float> callback) {
        if (entityPatch != null) {
            try {
                LivingEntity entity = entityPatch.getOriginal();
                if (DingShenFaService.isImmobilized(entity)) {
                    callback.setReturnValue(0.0F);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
