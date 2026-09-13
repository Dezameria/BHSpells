package net.offkung.bhspells.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.spells.aqua.AquaFlowerSpell;
import net.offkung.bhspells.spells.aqua.BlessingSnowSpell;
import net.offkung.bhspells.spells.aqua.StarIceSpell;
import net.offkung.bhspells.spells.fire.FieryDanceSpell;
import net.offkung.bhspells.spells.fire.FireBirdSpell;
import net.offkung.bhspells.spells.fire.HeavenLionSpell;
import net.offkung.bhspells.spells.gold.*;
import net.offkung.bhspells.spells.ground.DemonicSpinSpell;
import net.offkung.bhspells.spells.ground.EmbracingBosomSpell;
import net.offkung.bhspells.spells.ground.FeetStompSpell;
import net.offkung.bhspells.spells.ground.IntrusionChainBuffSpell;
import net.offkung.bhspells.spells.ground.IntrusionChainDebuffSpell;
import net.offkung.bhspells.spells.lightning.DivineThunderSpell;
import net.offkung.bhspells.spells.lightning.UltraShockSpell;
import net.offkung.bhspells.spells.nature.EternalPurificationSpell;
import net.offkung.bhspells.spells.nature.ExplosiveLilySpell;
import net.offkung.bhspells.spells.nature.HealingLilySpell;
import net.offkung.bhspells.spells.nature.SixPetalWaltzSpell;

public class BHSpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, BHSpells.MODID);

    // FIRE
    public static final RegistryObject<AbstractSpell> FIERY_DANCE = registerSpell(new FieryDanceSpell());
    public static final RegistryObject<AbstractSpell> FIREBIRD = registerSpell(new FireBirdSpell());
    public static final RegistryObject<AbstractSpell> HEAVEN_LION = registerSpell(new HeavenLionSpell());

    // AQUA
    public static final RegistryObject<AbstractSpell> ULTRASHOCK = registerSpell(new UltraShockSpell());
    public static final RegistryObject<AbstractSpell> DIVINE_THUNDER = registerSpell(new DivineThunderSpell());
    public static final RegistryObject<AbstractSpell> STAR_ICE = registerSpell(new StarIceSpell());
    public static final RegistryObject<AbstractSpell> BLESSING_SNOW = registerSpell(new BlessingSnowSpell());
    public static final RegistryObject<AbstractSpell> AQUA_FLOWER = registerSpell(new AquaFlowerSpell());

    // GOLD
    public static final RegistryObject<AbstractSpell> STONE_CRUMBLE = registerSpell(new StoneCrumbleSpell());
    public static final RegistryObject<AbstractSpell> THOUSAND_ARROWS = registerSpell(new ThousandArrowsSpell());
    public static final RegistryObject<AbstractSpell> SPIN_STRIKE = registerSpell(new SpinStrikeSpell());
    public static final RegistryObject<AbstractSpell> GOLDEN_GATE = registerSpell(new GoldenGateSpell());
    public static final RegistryObject<AbstractSpell> SKY_EATER = registerSpell(new SkyEaterSpell());
    public static final RegistryObject<AbstractSpell> SHINING_RADIANT = registerSpell(new ShiningRadiantSpell());
    public static final RegistryObject<AbstractSpell> SHAKEN_MONKEY = registerSpell(new ShakenMonkeySpell());

    // GROUND
    public static final RegistryObject<AbstractSpell> DEMONIC_SPIN = registerSpell(new DemonicSpinSpell());
    public static final RegistryObject<AbstractSpell> FEET_STOMP = registerSpell(new FeetStompSpell());
    public static final RegistryObject<AbstractSpell> EMBRACING_BOSOM = registerSpell(new EmbracingBosomSpell());
    public static final RegistryObject<AbstractSpell> INTRUSION_CHAIN_BUFF = registerSpell(new IntrusionChainBuffSpell());
    public static final RegistryObject<AbstractSpell> INTRUSION_CHAIN_DEBUFF = registerSpell(new IntrusionChainDebuffSpell());

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
