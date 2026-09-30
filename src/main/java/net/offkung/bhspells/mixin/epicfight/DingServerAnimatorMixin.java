package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.ServerAnimator;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Prevents starting or reserving any new animations on an immobilized entity on the server.
 */
@Mixin(value = ServerAnimator.class, remap = false)
public abstract class DingServerAnimatorMixin {

    @Inject(method = "playAnimation(Lyesman/epicfight/api/asset/AssetAccessor;F)V", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$blockPlayAnimation(AssetAccessor<?> animation, float transitionTime, CallbackInfo ci) {
        ServerAnimator self = (ServerAnimator) (Object) this;
        LivingEntityPatch<?> patch = self.getEntityPatch();
        if (patch != null) {
            try {
                LivingEntity entity = patch.getOriginal();
                if (DingShenFaService.isImmobilized(entity)) {
                    ci.cancel();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Inject(method = "playAnimationInstantly(Lyesman/epicfight/api/asset/AssetAccessor;)V", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$blockPlayAnimationInstantly(AssetAccessor<?> animation, CallbackInfo ci) {
        ServerAnimator self = (ServerAnimator) (Object) this;
        LivingEntityPatch<?> patch = self.getEntityPatch();
        if (patch != null) {
            try {
                LivingEntity entity = patch.getOriginal();
                if (DingShenFaService.isImmobilized(entity)) {
                    ci.cancel();
                }
            } catch (Throwable ignored) {
            }
        }
    }

    @Inject(method = "reserveAnimation(Lyesman/epicfight/api/asset/AssetAccessor;)V", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$blockReserveAnimation(AssetAccessor<?> animation, CallbackInfo ci) {
        ServerAnimator self = (ServerAnimator) (Object) this;
        LivingEntityPatch<?> patch = self.getEntityPatch();
        if (patch != null) {
            try {
                LivingEntity entity = patch.getOriginal();
                if (DingShenFaService.isImmobilized(entity)) {
                    ci.cancel();
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
