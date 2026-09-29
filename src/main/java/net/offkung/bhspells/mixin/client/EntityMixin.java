package net.offkung.bhspells.mixin.client;

import net.minecraft.world.entity.Entity;
import net.offkung.bhspells.client.event.ArtOfTruthClientHandler;
import net.offkung.bhspells.client.event.BlessingSnowClientHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Entity.class)
public abstract class EntityMixin {
    @Inject(method = "getTeamColor", at = @At("HEAD"), cancellable = true)
    private void bhspells$blessingSnowGlowColor(CallbackInfoReturnable<Integer> cir) {
        if (BlessingSnowClientHandler.isTargetGlowingForCaster((Entity) (Object) this)) {
            cir.setReturnValue(0x55FFFF);
        } else if (ArtOfTruthClientHandler.isTargetGlowingForCaster((Entity) (Object) this)) {
            cir.setReturnValue(ArtOfTruthClientHandler.GLOW_COLOR);
        }
    }

    @Inject(method = "canSpawnSprintParticle", at = @At("HEAD"), cancellable = true)
    private void bhspells$dragonFrostNoSprintParticle(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity living && living.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            cir.setReturnValue(false);
        }
    }

    @Inject(method = "spawnSprintParticle", at = @At("HEAD"), cancellable = true)
    private void bhspells$dragonFrostCancelSpawnSprintParticle(CallbackInfo ci) {
        if ((Object) this instanceof LivingEntity living && living.hasEffect(MobEffectsRegistry.DRAGON_FROST.get())) {
            ci.cancel();
        }
    }
}
