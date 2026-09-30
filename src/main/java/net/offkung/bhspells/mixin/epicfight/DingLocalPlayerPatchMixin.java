package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.client.world.capabilites.entitypatch.player.LocalPlayerPatch;

/**
 * Disables Epic Fight attack animations for the local player when immobilized.
 */
@Mixin(value = LocalPlayerPatch.class, remap = false)
public abstract class DingLocalPlayerPatchMixin {
    @Inject(method = "canPlayAttackAnimation", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$blockLocalAttackAnimation(CallbackInfoReturnable<Boolean> cir) {
        LocalPlayerPatch self = (LocalPlayerPatch) (Object) this;
        if (self.getOriginal() != null && DingShenFaService.isImmobilized(self.getOriginal())) {
            cir.setReturnValue(false);
        }
    }
}
