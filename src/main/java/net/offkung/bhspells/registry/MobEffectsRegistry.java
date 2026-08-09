package net.offkung.bhspells.registry;

import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.effect.CheckCastEffect;
import net.offkung.bhspells.effect.PerplexityEffect;
import net.offkung.bhspells.effect.PetalWaltzEffect;
import net.offkung.bhspells.effect.ScreenShakeEffect;

public class MobEffectsRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BHSpells.MODID);

    public static final RegistryObject<MobEffect> SCREEN_SHAKE = MOB_EFFECTS.register("screen_shake", ScreenShakeEffect::new);
    public static final RegistryObject<MobEffect> CHECK_CAST = MOB_EFFECTS.register("check_cast", CheckCastEffect::new);
    public static final RegistryObject<MobEffect> PERPLEXITY = MOB_EFFECTS.register("perplexity", PerplexityEffect::new);
    public static final RegistryObject<MobEffect> PETAL_WALTZ = MOB_EFFECTS.register("petal_waltz", PetalWaltzEffect::new);
}
