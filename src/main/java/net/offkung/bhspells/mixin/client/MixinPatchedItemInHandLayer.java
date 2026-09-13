package net.offkung.bhspells.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.world.entity.LivingEntity;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.PatchedItemInHandLayer;
import yesman.epicfight.client.renderer.patched.layer.PatchedLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

@Mixin(value = PatchedItemInHandLayer.class, remap = false)
public abstract class MixinPatchedItemInHandLayer<E extends LivingEntity, T extends LivingEntityPatch<E>, M extends EntityModel<E>> extends PatchedLayer<E, T, M, RenderLayer<E, M>> {
    @Inject(method = "renderLayer", at = @At("HEAD"), cancellable = true)
    private void bhspells$hideWeaponOnExtraInvisibility(
            T entitypatch, E entityliving, RenderLayer<E, M> vanillaLayer,
            PoseStack postStack, MultiBufferSource buffer, int packedLight,
            OpenMatrix4f[] poses, float bob, float yRot, float xRot, float partialTicks,
            CallbackInfo ci) {
        if (entityliving != null && entityliving.hasEffect(MobEffectsRegistry.EXTRA_INVISIBILITY.get())) {
            ci.cancel();
        }
    }
}
