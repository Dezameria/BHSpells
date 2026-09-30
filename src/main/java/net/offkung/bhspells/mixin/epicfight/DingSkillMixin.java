package net.offkung.bhspells.mixin.epicfight;

import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillContainer;

/**
 * Disables Epic Fight active skills (Dodge, Guard, Weapon Skill, Innate Skill)
 * when the executor is immobilized by Ding.
 */
@Mixin(value = Skill.class, remap = false)
public abstract class DingSkillMixin {
    @Inject(method = "canExecute", at = @At("HEAD"), cancellable = true)
    private void ironspellMore$preventSkillWhileDinged(SkillContainer container, CallbackInfoReturnable<Boolean> cir) {
        if (container != null && container.getExecutor() != null) {
            try {
                LivingEntity entity = container.getExecutor().getOriginal();
                if (DingShenFaService.isImmobilized(entity)) {
                    cir.setReturnValue(false);
                }
            } catch (Throwable ignored) {
            }
        }
    }
}
