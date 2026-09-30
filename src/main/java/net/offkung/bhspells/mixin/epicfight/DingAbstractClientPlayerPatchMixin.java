package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import yesman.epicfight.api.animation.Pose;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.client.world.capabilites.entitypatch.player.AbstractClientPlayerPatch;

/**
 * Locks the player's 3D model head to the latest look direction at the moment of being immobilized,
 * by temporarily supplying the frozen look angles to Epic Fight's native poseTick and restoring them afterwards.
 * This completely avoids custom matrix distortion or head stretching.
 */
@Mixin(value = AbstractClientPlayerPatch.class, remap = false)
public abstract class DingAbstractClientPlayerPatchMixin {
    @Unique
    private static final ThreadLocal<float[]> ironspellMore$SAVED_HEAD_ANGLES = new ThreadLocal<>();

    @Inject(method = "poseTick", at = @At("HEAD"))
    private void ironspellMore$beforeHeadPoseTick(DynamicAnimation animation, Pose pose, float prevElapsedTime, float partialTicks, CallbackInfo ci) {
        AbstractClientPlayerPatch<?> self = (AbstractClientPlayerPatch<?>) (Object) this;
        if (self.getOriginal() != null && DingShenFaService.isImmobilized(self.getOriginal())) {
            DingShenFaService.FrozenHeadPose frozen = DingShenFaService.getOrCreateFrozenHeadPose(self.getOriginal(), self.getYRot());
            if (frozen != null) {
                LivingEntity entity = self.getOriginal();
                ironspellMore$SAVED_HEAD_ANGLES.set(new float[]{entity.yHeadRot, entity.yHeadRotO, entity.getXRot(), entity.xRotO});
                entity.yHeadRot = frozen.headYRot;
                entity.yHeadRotO = frozen.headYRot;
                entity.setXRot(frozen.pitch);
                entity.xRotO = frozen.pitch;
            }
        }
    }

    @Inject(method = "poseTick", at = @At("TAIL"))
    private void ironspellMore$afterHeadPoseTick(DynamicAnimation animation, Pose pose, float prevElapsedTime, float partialTicks, CallbackInfo ci) {
        float[] saved = ironspellMore$SAVED_HEAD_ANGLES.get();
        if (saved != null) {
            ironspellMore$SAVED_HEAD_ANGLES.remove();
            AbstractClientPlayerPatch<?> self = (AbstractClientPlayerPatch<?>) (Object) this;
            if (self.getOriginal() != null) {
                LivingEntity entity = self.getOriginal();
                entity.yHeadRot = saved[0];
                entity.yHeadRotO = saved[1];
                entity.setXRot(saved[2]);
                entity.xRotO = saved[3];
            }
        }
    }
}
