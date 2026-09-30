package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Freezes playback speed for StaticAnimation (idle, walk, run, sprint, jump, fall poses)
 * on immobilized entities, preventing player/mob default animations from running while Dinged,
 * and skips poseTick so head rotations do not follow camera rotation.
 */
@Mixin(value = StaticAnimation.class, remap = false)
public abstract class DingStaticAnimationMixin {
    @Inject(method = "getPlaySpeed", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$freezeDingStaticAnimation(LivingEntityPatch<?> entityPatch,
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


