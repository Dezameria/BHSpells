package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.client.model.EntityModel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import yesman.epicfight.client.renderer.patched.entity.PatchedLivingEntityRenderer;

/**
 * Ensures vanilla model layers (like custom helmets or modded layers) attached to Epic Fight entities
 * keep their locked head angles from the moment of Ding rather than turning when camera pans while Dinged.
 */
@Mixin(value = PatchedLivingEntityRenderer.class, remap = false)
public abstract class DingPatchedLivingEntityRendererMixin {
    @SuppressWarnings("unchecked")
    @Redirect(
        method = "prepareVanillaModel",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V",
            remap = true
        )
    )
    private void ironspellMore$lockVanillaLayerHeadAnim(EntityModel<?> model, Entity entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof LivingEntity living && DingShenFaService.isImmobilized(living)) {
            DingShenFaService.FrozenHeadPose frozen = DingShenFaService.getOrCreateFrozenHeadPose(living, 0.0F);
            float yaw = frozen != null ? frozen.netHeadYaw : 0.0F;
            float pitch = frozen != null ? frozen.pitch : 0.0F;
            ((EntityModel<LivingEntity>) (Object) model).setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, yaw, pitch);
        } else if (entity instanceof LivingEntity living) {
            ((EntityModel<LivingEntity>) (Object) model).setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        }
    }
}
