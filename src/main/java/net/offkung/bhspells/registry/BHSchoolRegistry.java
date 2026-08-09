package net.offkung.bhspells.registry;

import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.SchoolType;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.util.BHTags;
import io.redspace.ironsspellbooks.registries.SoundRegistry;

public class BHSchoolRegistry {
    private static final DeferredRegister<SchoolType> SCHOOLS = DeferredRegister.create(SchoolRegistry.SCHOOL_REGISTRY_KEY, BHSpells.MODID);

    public static void register(IEventBus eventBus) {
        SCHOOLS.register(eventBus);
    }

    private static RegistryObject<SchoolType> registerSchool(SchoolType schoolType) {
        return SCHOOLS.register(schoolType.getId().getPath(), () -> schoolType);
    }

    public static final ResourceLocation GOLD_RESOURCE = BHSpells.id("gold");
    public static final ResourceLocation GROUND_RESOURCE = BHSpells.id("ground");

    public static final RegistryObject<SchoolType> GOLD = registerSchool(new SchoolType(
            GOLD_RESOURCE,
            BHTags.GOLD_FOCUS,
            Component.translatable("school.bhspells.gold").withStyle(ChatFormatting.GOLD),
            AttributeRegistry.GOLD_SPELL_POWER,
            AttributeRegistry.GOLD_MAGIC_RESIST,
            SoundRegistry.ICE_CAST,
            DamageTypesRegistry.GOLD_MAGIC
    ));

    public static final RegistryObject<SchoolType> GROUND = registerSchool(new SchoolType(
            GROUND_RESOURCE,
            BHTags.GROUND_FOCUS,
            Component.translatable("school.bhspells.ground").withStyle(Style.EMPTY.withColor(0xFF563D2D)),
            AttributeRegistry.GROUND_SPELL_POWER,
            AttributeRegistry.GROUND_MAGIC_RESIST,
            SoundRegistry.NATURE_CAST,
            DamageTypesRegistry.GROUND_MAGIC
    ));
}
