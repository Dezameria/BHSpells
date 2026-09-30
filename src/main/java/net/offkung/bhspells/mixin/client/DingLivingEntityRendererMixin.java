package net.offkung.bhspells.mixin.client;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

/**
 * Locks the vanilla model head angles to the latest look direction at the
 * moment of being immobilized,
 * so that when the player turns the camera, the vanilla character model head
 * remains in that exact direction.
 */
@Mixin(LivingEntityRenderer.class)
public abstract class DingLivingEntityRendererMixin {
    @SuppressWarnings("unchecked")
    @Redirect(method = "render(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/EntityModel;setupAnim(Lnet/minecraft/world/entity/Entity;FFFFF)V"))
    private void ironspellMore$lockVanillaHeadAnim(EntityModel<?> model, Entity entity, float limbSwing,
            float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (entity instanceof LivingEntity living && DingShenFaService.isImmobilized(living)) {
            DingShenFaService.FrozenHeadPose frozen = DingShenFaService.getOrCreateFrozenHeadPose(living, 0.0F);
            float yaw = frozen != null ? frozen.netHeadYaw : 0.0F;
            float pitch = frozen != null ? frozen.pitch : 0.0F;
            ((EntityModel<LivingEntity>) (Object) model).setupAnim(living, limbSwing, limbSwingAmount, ageInTicks, yaw,
                    pitch);
        } else if (entity instanceof LivingEntity living) {
            ((EntityModel<LivingEntity>) (Object) model).setupAnim(living, limbSwing, limbSwingAmount, ageInTicks,
                    netHeadYaw, headPitch);
        }
    }
}
