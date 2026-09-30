package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingEvent;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.api.utils.AttackResult;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.damagesource.EpicFightDamageSource;

/**
 * Blocks attacks and keeps the 3D model orientation locked in place for immobilized players,
 * while allowing the player's camera/screen to rotate freely.
 */
@Mixin(value = PlayerPatch.class, remap = false)
public abstract class DingPlayerPatchMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$blockPlayerAttack(EpicFightDamageSource damageSource,
                                                 Entity target,
                                                 InteractionHand hand,
                                                 CallbackInfoReturnable<AttackResult> cir) {
        PlayerPatch<?> self = (PlayerPatch<?>) (Object) this;
        if (self.getOriginal() != null && DingShenFaService.isImmobilized(self.getOriginal())) {
            cir.setReturnValue(AttackResult.blocked(0.0F));
        }
    }

    @Inject(method = "disableModelYRot", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$preventDisableModelYRot(boolean sync, CallbackInfo ci) {
        PlayerPatch<?> self = (PlayerPatch<?>) (Object) this;
        if (self.getOriginal() != null && DingShenFaService.isImmobilized(self.getOriginal())) {
            ci.cancel();
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    private void ironspellMore$lockModelYRotAtHead(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        PlayerPatch<?> self = (PlayerPatch<?>) (Object) this;
        if (self.getOriginal() != null && DingShenFaService.isImmobilized(self.getOriginal())) {
            self.setModelYRot(self.getYRot(), false);
            self.setYRotO(self.getYRot());
        }
    }

    @Inject(method = "tick", at = @At("TAIL"))
    private void ironspellMore$lockModelYRot(LivingEvent.LivingTickEvent event, CallbackInfo ci) {
        PlayerPatch<?> self = (PlayerPatch<?>) (Object) this;
        if (self.getOriginal() != null && DingShenFaService.isImmobilized(self.getOriginal())) {
            self.setModelYRot(self.getYRot(), false);
            self.setYRotO(self.getYRot());
        }
    }
}


