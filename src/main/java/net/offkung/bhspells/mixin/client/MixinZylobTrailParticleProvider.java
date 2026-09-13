package net.offkung.bhspells.mixin.client;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Pseudo
@Mixin(targets = {
        "com.Zhu_Yii.zylob.client.particle.ACGBloomTrailParticle$Provider",
        "com.Zhu_Yii.zylob.client.particle.TrailParticle$Provider"
}, remap = false)
public class MixinZylobTrailParticleProvider {
    @Inject(method = "createParticle(Lnet/minecraft/core/particles/SimpleParticleType;Lnet/minecraft/client/multiplayer/ClientLevel;DDDDDD)Lnet/minecraft/client/particle/Particle;", at = @At("HEAD"), cancellable = true)
    private void bhspells$cancelZylobTrailOnExtraInvisibility(
            SimpleParticleType typeIn, ClientLevel level,
            double x, double y, double z,
            double xSpeed, double ySpeed, double zSpeed,
            CallbackInfoReturnable<Particle> cir
    ) {
        int eid = (int) Double.doubleToRawLongBits(x);
        Entity entity = level.getEntity(eid);
        if (entity instanceof LivingEntity living && living.hasEffect(MobEffectsRegistry.EXTRA_INVISIBILITY.get())) {
            cir.setReturnValue(null);
        }
    }
}
