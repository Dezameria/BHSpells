package net.offkung.bhspells.registry;

import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.spells.aqua.*;
import net.offkung.bhspells.spells.fire.*;
import net.offkung.bhspells.spells.gold.*;
import net.offkung.bhspells.spells.ground.*;
import net.offkung.bhspells.spells.lightning.DivineThunderSpell;
import net.offkung.bhspells.spells.lightning.LightningStrikeSpell;
import net.offkung.bhspells.spells.lightning.ThunderStepSpell;
import net.offkung.bhspells.spells.lightning.TempestReiatsuSpell;
import net.offkung.bhspells.spells.lightning.UltraShockSpell;
import net.offkung.bhspells.spells.nature.*;
import net.offkung.bhspells.spells.evocation.*;

public class BHSpellRegistry {
    public static final DeferredRegister<AbstractSpell> SPELLS = DeferredRegister.create(SpellRegistry.SPELL_REGISTRY_KEY, BHSpells.MODID);

        // EVOCATION
    public static final RegistryObject<AbstractSpell> DING_SHEN_FA = registerSpell(new DingShenFaSpell());
    public static final RegistryObject<AbstractSpell> SPIRITUAL_PRESSURE = registerSpell(new SpiritualPressureSpell());
    public static final RegistryObject<AbstractSpell> VENGEFUL_PRESSURE = registerSpell(new VengefulPressureSpell());
    public static final RegistryObject<AbstractSpell> PHANTOM_DODGE = registerSpell(new PhantomDodgeSpell());

    // FIRE
    public static final RegistryObject<AbstractSpell> FIERY_DANCE = registerSpell(new FieryDanceSpell());
    public static final RegistryObject<AbstractSpell> FIREBIRD = registerSpell(new FireBirdSpell());
    public static final RegistryObject<AbstractSpell> HEAVEN_LION = registerSpell(new HeavenLionSpell());
    public static final RegistryObject<AbstractSpell> SMILES_OF_FIRE = registerSpell(new SmilesOfFireSpell());
    public static final RegistryObject<AbstractSpell> BLAZING_CHAKRA = registerSpell(new BlazingChakraSpell());
    public static final RegistryObject<AbstractSpell> PURE_WHITE_FLAME_BURST = registerSpell(new PureWhiteFlameBurstSpell());
    public static final RegistryObject<AbstractSpell> GALE_DRIVE = registerSpell(new GaleDriveSpell());
    public static final RegistryObject<AbstractSpell> RESONANT_KNELL = registerSpell(new ResonantKnellSpell());
    public static final RegistryObject<AbstractSpell> CRIMSON_THORNBIND = registerSpell(new CrimsonThornbindSpell());

    // LIGHTNING
    public static final RegistryObject<AbstractSpell> TEMPEST_REIATSU = registerSpell(new TempestReiatsuSpell());
    public static final RegistryObject<AbstractSpell> LIGHTNING_STRIKE = registerSpell(new LightningStrikeSpell());
    public static final RegistryObject<AbstractSpell> THUNDER_STEP = registerSpell(new ThunderStepSpell());

    // AQUA
    public static final RegistryObject<AbstractSpell> ULTRASHOCK = registerSpell(new UltraShockSpell());
    public static final RegistryObject<AbstractSpell> DIVINE_THUNDER = registerSpell(new DivineThunderSpell());
    public static final RegistryObject<AbstractSpell> STAR_ICE = registerSpell(new StarIceSpell());
    public static final RegistryObject<AbstractSpell> BLESSING_SNOW = registerSpell(new BlessingSnowSpell());
    public static final RegistryObject<AbstractSpell> AQUA_FLOWER = registerSpell(new AquaFlowerSpell());
    public static final RegistryObject<AbstractSpell> HAZARD_AREA = registerSpell(new HazardAreaSpell());
    public static final RegistryObject<AbstractSpell> CRYSTAL_HYDRO_DOME = registerSpell(new CrystalHydroDomeSpell());
    public static final RegistryObject<AbstractSpell> CRIMSON_RAIN_BATHES_MOON = registerSpell(new CrimsonRainBathesMoonSpell());
    public static final RegistryObject<AbstractSpell> GLACIAL_VEIL = registerSpell(new GlacialVeilSpell());
    public static final RegistryObject<AbstractSpell> GLACIAL_FIRMAMENT = registerSpell(new GlacialFirmamentSpell());
    public static final RegistryObject<AbstractSpell> TOXIC_SALVATION = registerSpell(new ToxicSalvationSpell());
    public static final RegistryObject<AbstractSpell> YIN_INK_CASCADE = registerSpell(new YinInkCascadeSpell());

    // GOLD
    public static final RegistryObject<AbstractSpell> SAVAGE_BITE = registerSpell(new SavageBiteSpell());
    public static final RegistryObject<AbstractSpell> STONE_CRUMBLE = registerSpell(new StoneCrumbleSpell());
    public static final RegistryObject<AbstractSpell> THOUSAND_ARROWS = registerSpell(new ThousandArrowsSpell());
    public static final RegistryObject<AbstractSpell> SPIN_STRIKE = registerSpell(new SpinStrikeSpell());
    public static final RegistryObject<AbstractSpell> GOLDEN_GATE = registerSpell(new GoldenGateSpell());
    public static final RegistryObject<AbstractSpell> SKY_EATER = registerSpell(new SkyEaterSpell());
    public static final RegistryObject<AbstractSpell> SHINING_RADIANT = registerSpell(new ShiningRadiantSpell());
    public static final RegistryObject<AbstractSpell> SHAKEN_MONKEY = registerSpell(new ShakenMonkeySpell());
    public static final RegistryObject<AbstractSpell> JADE_WAVE = registerSpell(new JadeWaveSpell());
    public static final RegistryObject<AbstractSpell> GOLDEN_HAND = registerSpell(new GoldenHandSpell());
    public static final RegistryObject<AbstractSpell> WHEEL_OF_KARMA = registerSpell(new WheelOfKarmaSpell());
    public static final RegistryObject<AbstractSpell> AMETHYST_DECREE = registerSpell(new AmethystDecreeSpell());
    public static final RegistryObject<AbstractSpell> SHACKLE_OF_FEAR_SPELL = registerSpell(new ShackleofFearSpell());
    public static final RegistryObject<AbstractSpell> HYMN_OF_PURIFICATION = registerSpell(new HymnofPurificationSpell());
    public static final RegistryObject<AbstractSpell> GILDED_HARE = registerSpell(new GildedHareSpell());
    public static final RegistryObject<AbstractSpell> JADE_CLUSTER = registerSpell(new JadeClusterSpell());

    // GROUND
    public static final RegistryObject<AbstractSpell> EARTH_ROAR = registerSpell(new EarthRoarSpell());
    public static final RegistryObject<AbstractSpell> SHOCKING = registerSpell(new ShockingSpell());
    public static final RegistryObject<AbstractSpell> DEMONIC_SPIN = registerSpell(new DemonicSpinSpell());
    public static final RegistryObject<AbstractSpell> FEET_STOMP = registerSpell(new FeetStompSpell());
    public static final RegistryObject<AbstractSpell> EMBRACING_BOSOM = registerSpell(new EmbracingBosomSpell());
    public static final RegistryObject<AbstractSpell> INTRUSION_CHAIN_BUFF = registerSpell(new IntrusionChainBuffSpell());
    public static final RegistryObject<AbstractSpell> INTRUSION_CHAIN_DEBUFF = registerSpell(new IntrusionChainDebuffSpell());
    public static final RegistryObject<AbstractSpell> TIGERSHADE_TERRABREAK = registerSpell(new TigershadeTerrabreakSpell());
    public static final RegistryObject<AbstractSpell> JADE_AURA = registerSpell(new JadeAuraSpell());

    // NATURE
    public static final RegistryObject<AbstractSpell> EXPLOSIVE_LILY = registerSpell(new ExplosiveLilySpell());
    public static final RegistryObject<AbstractSpell> HEALING_LILY = registerSpell(new HealingLilySpell());
    public static final RegistryObject<AbstractSpell> ETERNAL_PURIFICATION = registerSpell(new EternalPurificationSpell());
    public static final RegistryObject<AbstractSpell> SIX_PETAL_WALTZ = registerSpell(new SixPetalWaltzSpell());
    public static final RegistryObject<AbstractSpell> ART_OF_HEALING = registerSpell(new ArtOfHealingSpell());
    public static final RegistryObject<AbstractSpell> ART_OF_TRUTH = registerSpell(new ArtOfTruthSpell());
    public static final RegistryObject<AbstractSpell> WING_OF_WIND = registerSpell(new WingOfWindSpell());
    public static final RegistryObject<AbstractSpell> SUPPORTING_BAMBOO = registerSpell(new SupportingBambooSpell());
    public static final RegistryObject<AbstractSpell> WINGS_OF_TEMPEST = registerSpell(new WingsofTempestSpell());
    public static final RegistryObject<AbstractSpell> VENOMOUS_BLOSSOMFALL = registerSpell(new VenomousBlossomfallSpell());
    public static final RegistryObject<AbstractSpell> GALE_PIERCER = registerSpell(new GalePiercerSpell());
    public static final RegistryObject<AbstractSpell> RAPTUROUS_BLOOM = registerSpell(new RapturousBloomSpell());

    public static RegistryObject<AbstractSpell> registerSpell(AbstractSpell spell) {
        return SPELLS.register(spell.getSpellName(), () -> spell);
    }

    public static void register(IEventBus eventBus) {
        SPELLS.register(eventBus);
    }
}

