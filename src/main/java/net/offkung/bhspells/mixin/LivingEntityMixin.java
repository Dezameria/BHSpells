package net.offkung.bhspells.mixin;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.FluidState;
import net.offkung.bhspells.effect.DragonFrostHandler;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin {
    @Inject(method = "canStandOnFluid", at = @At("HEAD"), cancellable = true)
    private void bhspells$canStandOnFluid(FluidState fluidState, CallbackInfoReturnable<Boolean> cir) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (living.hasEffect(MobEffectsRegistry.DRAGON_FROST.get()) && !fluidState.isEmpty()) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "jumpFromGround", at = @At("HEAD"))
    private void bhspells$onJumpFromGround(CallbackInfo ci) {
        LivingEntity living = (LivingEntity) (Object) this;
        if (living instanceof Player player && player.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            DragonFrostHandler.onJump(player);
        }
    }
}
