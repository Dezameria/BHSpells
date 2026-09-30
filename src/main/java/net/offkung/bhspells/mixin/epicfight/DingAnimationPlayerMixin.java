package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.AnimationPlayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

/**
 * Freezes AnimationPlayer tick execution and locks prevElapsedTime == elapsedTime
 * for immobilized entities, ensuring 100% frozen rendering without partial-tick jitter.
 */
@Mixin(value = AnimationPlayer.class, remap = false)
public abstract class DingAnimationPlayerMixin {
    @Shadow
    protected float elapsedTime;
    @Shadow
    protected float prevElapsedTime;

    @Inject(method = "tick", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$freezeAnimationPlayer(LivingEntityPatch<?> entitypatch, CallbackInfo ci) {
        if (entitypatch != null) {
            try {
                LivingEntity entity = entitypatch.getOriginal();
                if (DingShenFaService.isImmobilized(entity)) {
                    this.prevElapsedTime = this.elapsedTime;
                    ci.cancel();
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
