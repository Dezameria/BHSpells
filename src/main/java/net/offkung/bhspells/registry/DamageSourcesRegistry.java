package net.offkung.bhspells.registry;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class DamageSourcesRegistry {
    private static DamageSource create(Level level, ResourceKey<DamageType> key, @Nullable Entity attacker) {
        Holder<DamageType> holder = level.registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(key);
        return attacker != null ? new DamageSource(holder, attacker) : new DamageSource(holder);
    }

    public static DamageSource waterSpell(Level level, @Nullable Entity attacker) {
        return create(level, DamageTypesRegistry.WATER_SPELL_BYPASS, attacker);
    }

    public static DamageSource fireSpell(Level level, @Nullable Entity attacker) {
        return create(level, DamageTypesRegistry.FIRE_SPELL_BYPASS, attacker);
    }

    public static DamageSource goldSpell(Level level, @Nullable Entity attacker) {
        return create(level, DamageTypesRegistry.GOLD_SPELL_BYPASS, attacker);
    }

    public static DamageSource grassSpell(Level level, @Nullable Entity attacker) {
        return create(level, DamageTypesRegistry.GRASS_SPELL_BYPASS, attacker);
    }

    public static DamageSource groundSpell(Level level, @Nullable Entity attacker) {
        return create(level, DamageTypesRegistry.GROUND_SPELL_BYPASS, attacker);
    }
}
