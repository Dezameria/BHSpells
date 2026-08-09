package net.offkung.bhspells.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.attribute.MagicPercentAttribute;
import io.redspace.ironsspellbooks.api.attribute.MagicRangedAttribute;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class AttributeRegistry {
    private static final DeferredRegister<Attribute> ATTRIBUTES = DeferredRegister.create(Registries.ATTRIBUTE, BHSpells.MODID);

    public static void register(IEventBus eventBus) {
        ATTRIBUTES.register(eventBus);
    }

    public static final RegistryObject<Attribute> GOLD_MAGIC_RESIST = newResistanceAttribute("gold");
    public static final RegistryObject<Attribute> GROUND_MAGIC_RESIST = newResistanceAttribute("ground");

    public static final RegistryObject<Attribute> GOLD_SPELL_POWER = newPowerAttribute("gold");
    public static final RegistryObject<Attribute> GROUND_SPELL_POWER = newPowerAttribute("ground");

    @SubscribeEvent
    public static void modifyEntityAttributes(EntityAttributeModificationEvent e) {
        e.getTypes().forEach(entity -> ATTRIBUTES.getEntries().forEach(attribute -> e.add(entity, attribute.get())));
    }

    private static RegistryObject<Attribute> newResistanceAttribute(String id) {
        return ATTRIBUTES.register(id + "_magic_resist", () -> (new MagicPercentAttribute("attribute.bhspells." + id + "_magic_resist", 1.0D, -100, 100).setSyncable(true)));
    }

    private static RegistryObject<Attribute> newPowerAttribute(String id) {
        return ATTRIBUTES.register(id + "_spell_power", () -> (new MagicPercentAttribute("attribute.bhspells." + id + "_spell_power", 1.0D, -100, 100).setSyncable(true)));
    }
}
