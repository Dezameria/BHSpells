package net.offkung.bhspells.mixin;

import net.minecraftforge.common.ForgeConfigSpec;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

// Prevent Invalid Player Data on Dev Environment Server
@Mixin(value = ForgeConfigSpec.ConfigValue.class, remap = false)
public abstract class ForgeConfigSpecValueMixin<T> {
    @Shadow
    private ForgeConfigSpec spec;

    @Shadow
    public abstract T getDefault();

    @Inject(method = "get", at = @At("HEAD"), cancellable = true)
    private void bhspells$safeGetConfigValue(CallbackInfoReturnable<T> cir) {
        if (this.spec != null && !this.spec.isLoaded()) {
            cir.setReturnValue(this.getDefault());
        }
    }
}
