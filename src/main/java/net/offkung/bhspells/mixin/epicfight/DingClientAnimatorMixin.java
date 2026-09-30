package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.asset.AssetAccessor;
import yesman.epicfight.api.client.animation.ClientAnimator;
import yesman.epicfight.network.common.AnimatorControlPacket;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Prevents starting, playing, or reserving any new animations on an immobilized entity on the client.
 */
@Mixin(value = ClientAnimator.class, remap = false)
public abstract class DingClientAnimatorMixin {

    @Inject(method = "playAnimation(Lyesman/epicfight/api/asset/AssetAccessor;F)V", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$blockPlayAnimation(AssetAccessor<?> animation, float transitionTime, CallbackInfo ci) {
        ClientAnimator self = (ClientAnimator) (Object) this;
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
        ClientAnimator self = (ClientAnimator) (Object) this;
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
        ClientAnimator self = (ClientAnimator) (Object) this;
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

    @Inject(
        method = "playAnimationAt(Lyesman/epicfight/api/asset/AssetAccessor;FLyesman/epicfight/network/common/AnimatorControlPacket$Layer;Lyesman/epicfight/network/common/AnimatorControlPacket$Priority;)V",
        at = @At("HEAD"),
        cancellable = true
    )
    private void ironspellMore$blockPlayAnimationAt(AssetAccessor<?> animation,
                                                    float transitionTime,
                                                    AnimatorControlPacket.Layer layer,
                                                    AnimatorControlPacket.Priority priority,
                                                    CallbackInfo ci) {
        ClientAnimator self = (ClientAnimator) (Object) this;
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
