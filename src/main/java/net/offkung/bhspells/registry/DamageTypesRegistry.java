package net.offkung.bhspells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.offkung.bhspells.BHSpells;

public class DamageTypesRegistry {
    public static final ResourceKey<DamageType> WATER_SPELL_BYPASS = create("water_spell_bypass");
    public static final ResourceKey<DamageType> FIRE_SPELL_BYPASS = create("fire_spell_bypass");
    public static final ResourceKey<DamageType> GOLD_SPELL_BYPASS = create("gold_spell_bypass");
    public static final ResourceKey<DamageType> GRASS_SPELL_BYPASS = create("grass_spell_bypass");
    public static final ResourceKey<DamageType> GROUND_SPELL_BYPASS = create("ground_spell_bypass");

    public static final ResourceKey<DamageType> GOLD_MAGIC = create("gold_magic");
    public static final ResourceKey<DamageType> GROUND_MAGIC = create("ground_magic");

    private static ResourceKey<DamageType> create(String name) {
        return ResourceKey.create(Registries.DAMAGE_TYPE, ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, name));
    }
}
