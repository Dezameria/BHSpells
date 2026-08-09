package net.offkung.bhspells.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.spells.fire.FieryDanceSpell;
import net.offkung.bhspells.spells.gold.StoneCrumbleSpell;
import net.offkung.bhspells.spells.gold.ThousandArrowsSpell;
import net.offkung.bhspells.spells.ground.FeetStompSpell;
import net.offkung.bhspells.spells.lightning.UltraShockSpell;
import net.offkung.bhspells.spells.nature.EternalPurificationSpell;
import net.offkung.bhspells.spells.nature.ExplosiveLilySpell;
import net.offkung.bhspells.spells.nature.HealingLilySpell;
import net.offkung.bhspells.spells.nature.SixPetalWaltzSpell;

public class BHSpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, BHSpells.MODID);

    // FIRE
    public static final RegistryObject<AbstractSpell> FIERY_DANCE = registerSpell(new FieryDanceSpell());

    // AQUA
    public static final RegistryObject<AbstractSpell> ULTRASHOCK = registerSpell(new UltraShockSpell());

    // GOLD
    public static final RegistryObject<AbstractSpell> STONE_CRUMBLE = registerSpell(new StoneCrumbleSpell());
    public static final RegistryObject<AbstractSpell> THOUSAND_ARROWS = registerSpell(new ThousandArrowsSpell());

    // GROUND
    public static final RegistryObject<AbstractSpell> FEET_STOMP = registerSpell(new FeetStompSpell());

    // NATURE
    public static final RegistryObject<AbstractSpell> EXPLOSIVE_LILY = registerSpell(new ExplosiveLilySpell());
    public static final RegistryObject<AbstractSpell> HEALING_LILY = registerSpell(new HealingLilySpell());
    public static final RegistryObject<AbstractSpell> ETERNAL_PURIFICATION = registerSpell(new EternalPurificationSpell());
    public static final RegistryObject<AbstractSpell> SIX_PETAL_WALTZ = registerSpell(new SixPetalWaltzSpell());

    public static RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}
