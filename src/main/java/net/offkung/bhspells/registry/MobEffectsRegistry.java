package net.offkung.bhspells.registry;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.api.registry.AttributeRegistry;
import io.redspace.ironsspellbooks.effect.ChargeEffect;
import io.redspace.ironsspellbooks.effect.ThunderstormEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.effect.*;

public class MobEffectsRegistry {
    public static final DeferredRegister<MobEffect> MOB_EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, BHSpells.MODID);

    public static final RegistryObject<MobEffect> SCREEN_SHAKE = MOB_EFFECTS.register("screen_shake", ScreenShakeEffect::new);
    public static final RegistryObject<MobEffect> CHECK_CAST = MOB_EFFECTS.register("check_cast", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0x5B8DEF){});
    public static final RegistryObject<MobEffect> PERPLEXITY = MOB_EFFECTS.register("perplexity", PerplexityEffect::new);
    public static final RegistryObject<MobEffect> PETAL_WALTZ = MOB_EFFECTS.register("petal_waltz", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFFB5C0){});
    public static final RegistryObject<MobEffect> SPIN_STRIKE = MOB_EFFECTS.register("spin_strike", SpinStrikeEffect::new);
    public static final RegistryObject<MobEffect> EMBRACING_BOSOM = MOB_EFFECTS.register("embracing_bosom", EmbracingBosomEffect::new);
    public static final RegistryObject<MobEffect> FIRE_BODY_LEVEL2 = MOB_EFFECTS.register("fire_body_level2", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFFA500){});
    public static final RegistryObject<MobEffect> INTRUSION_CHAIN = MOB_EFFECTS.register("intrusion_chain", IntrusionChainEffect::new);
    public static final RegistryObject<MobEffect> CHAIN_CANCEL = MOB_EFFECTS.register("chain_cancel", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF0000){});
    public static final RegistryObject<MobEffect> INCINERATION = MOB_EFFECTS.register("incineration", IncinerationEffect::new);
    public static final RegistryObject<MobEffect> EXTRA_INVISIBILITY = MOB_EFFECTS.register("extra_invisibility", ExtraInvisibilityEffect::new);
    public static final RegistryObject<MobEffect> CHAIN_BREAK = MOB_EFFECTS.register("chain_break", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFF0000){});
    public static final RegistryObject<MobEffect> CHAIN_MANA_CHECK = MOB_EFFECTS.register("chain_mana_check", () -> new MobEffect(MobEffectCategory.NEUTRAL, 0xFFFFFF){});
    public static final RegistryObject<MobEffect> CHAIN_DEBUFF_TARGET = MOB_EFFECTS.register("chain_debuff_target", () -> new MobEffect(MobEffectCategory.HARMFUL, 0xFF0000){});
    public static final RegistryObject<MobEffect> CHAIN_BUFF_ALLY = MOB_EFFECTS.register("chain_buff_ally", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0x00FF00){});
    public static final RegistryObject<MobEffect> RED_CHARGED = MOB_EFFECTS.register("red_charged", () -> new RedChargedEffect(MobEffectCategory.BENEFICIAL, 0xFF0000)
            .addAttributeModifier(Attributes.ATTACK_DAMAGE, BHSpells.id("mobeffect_red_charged"), RedChargedEffect.ATTACK_DAMAGE_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(Attributes.MOVEMENT_SPEED, BHSpells.id("mobeffect_red_charged"), RedChargedEffect.SPEED_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL)
            .addAttributeModifier(AttributeRegistry.SPELL_POWER, BHSpells.id("mobeffect_red_charged"), RedChargedEffect.SPELL_POWER_BONUS, AttributeModifier.Operation.MULTIPLY_TOTAL).cast());
    public static final RegistryObject<MobEffect> RED_THUNDERSTORM = MOB_EFFECTS.register("red_thunderstorm", () -> new RedThunderStormEffect(MobEffectCategory.BENEFICIAL, 0xFF0000));
    public static final RegistryObject<MobEffect> DEMONIC_SPIN = MOB_EFFECTS.register("demonic_spin", DemonicSpinEffect::new);
    public static final RegistryObject<MobEffect> STAR_ICE = MOB_EFFECTS.register("star_ice", () -> {
        var effect = new StarIceEffect();
        try {
            var flightAttr = ForgeRegistries.ATTRIBUTES.getValue(StarIceEffect.CAELUS_FALL_FLYING);
            if (flightAttr != null) {
                effect.addAttributeModifier(flightAttr, StarIceEffect.FLIGHT_MODIFIER_UUID.toString(), 1.0D, AttributeModifier.Operation.ADDITION);
            }
        } catch (Throwable ignored) {
        }
        return effect;
    });
    public static final RegistryObject<MobEffect> BLESSING_SNOW_MANA = MOB_EFFECTS.register("blessing_snow_mana", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF){});
    public static final RegistryObject<MobEffect> BLESSING_SNOW = MOB_EFFECTS.register("blessing_snow", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFFFFFF){});
    public static final RegistryObject<MobEffect> DRAGON_FROST = MOB_EFFECTS.register("dragon_frost", DragonFrostEffect::new);
    public static final RegistryObject<MobEffect> GOLDEN_GRASP = MOB_EFFECTS.register("golden_grasp", GoldenGraspEffect::new);
    public static final RegistryObject<MobEffect> GOLDEN_GRASP_HELPER = MOB_EFFECTS.register("golden_grasp_helper", GoldenGraspHelperEffect::new);
    public static final RegistryObject<MobEffect> WING_OF_WIND_FLIGHT = MOB_EFFECTS.register("wing_of_wind_flight", () -> {
        var effect = new WingOfWindFlightEffect();
        try {
            var flightAttr = ForgeRegistries.ATTRIBUTES.getValue(WingOfWindFlightEffect.CAELUS_FALL_FLYING);
            if (flightAttr != null) {
                effect.addAttributeModifier(flightAttr, WingOfWindFlightEffect.FLIGHT_MODIFIER_UUID.toString(), 1.0D, AttributeModifier.Operation.ADDITION);
            }
        } catch (Throwable ignored) {
        }
        return effect;
    });
    public static final RegistryObject<MobEffect> WING_OF_WIND_IMMOBILIZE = MOB_EFFECTS.register("wind_immobilize", WindImmobilizeEffect::new);
    public static final RegistryObject<MobEffect> SMILES_OF_FIRE = MOB_EFFECTS.register("smiles_of_fire", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF8DA1){});
    public static final RegistryObject<MobEffect> SMILES_OF_FIRE_CD = MOB_EFFECTS.register("smiles_of_fire_cd", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF8DA1){});
    public static final RegistryObject<MobEffect> WHEEL_OF_KARMA = MOB_EFFECTS.register("wheel_of_karma", WheelOfKarmaEffect::new);
    public static final RegistryObject<MobEffect> GILDED_HARE = MOB_EFFECTS.register("gilded_hare", GildedHareEffect::new);
    public static final RegistryObject<MobEffect> GILDED_HARE_MARK = MOB_EFFECTS.register("gilded_hare_mark", GildedHareMarkEffect::new);
    public static final RegistryObject<MobEffect> JADE_AURA = MOB_EFFECTS.register("jade_aura", JadeAuraEffect::new);
    public static final RegistryObject<MobEffect> HYMN_OF_PURIFICATION = MOB_EFFECTS.register("hymn_of_purification", HymnofPurificationEffect::new);
    public static final RegistryObject<MobEffect> TIGERSHADE_MARK = MOB_EFFECTS.register("tigershade_mark", TigershadeMarkEffect::new);
    public static final RegistryObject<MobEffect> TIGERSHADE_STANCE = MOB_EFFECTS.register("tigershade_stance", TigershadeStanceEffect::new);
    public static final RegistryObject<MobEffect> COOLDOWN = MOB_EFFECTS.register("cooldown", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF8DA1){});
    public static final RegistryObject<MobEffect> AZURE_VENOMOUS = MOB_EFFECTS.register("azure_venomous", AzureVenomousEffect::new);
    public static final RegistryObject<MobEffect> WHITE_FLAME_BURN = MOB_EFFECTS.register("white_flame_burn", WhiteFlameBurnEffect::new);
    public static final RegistryObject<MobEffect> GALE_DRIVE_DASH = MOB_EFFECTS.register("gale_drive_dash", GaleDriveDashEffect::new);
    public static final RegistryObject<MobEffect> GALE_FALL_IMPACT = MOB_EFFECTS.register("gale_fall_impact", GaleFallImpactEffect::new);
    public static final RegistryObject<MobEffect> JADE_CLUSTER = MOB_EFFECTS.register("jade_cluster", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xFF8DA1){});

        public static final RegistryObject<MobEffect> DING = MOB_EFFECTS.register("ding", () -> new DingEffect(MobEffectCategory.HARMFUL, 0xFFE082));
    public static final RegistryObject<MobEffect> EARTH_ROAR_EMPOWERMENT = MOB_EFFECTS.register("earth_roar_empowerment", () -> new EarthRoarEmpowermentEffect(MobEffectCategory.BENEFICIAL, 0x8B5A2B));
    public static final RegistryObject<MobEffect> EARTH_ROAR_STUN = MOB_EFFECTS.register("earth_roar_stun", () -> new EarthRoarStunEffect(MobEffectCategory.HARMFUL, 0x4A3728));
    public static final RegistryObject<MobEffect> SHOCKING = MOB_EFFECTS.register("shocking", () -> new ShockingEffect(MobEffectCategory.HARMFUL, 0xFFD700));
    public static final RegistryObject<MobEffect> SPIRITUAL_PRESSURE = MOB_EFFECTS.register("spiritual_pressure", () -> new SpiritualPressureEffect(MobEffectCategory.HARMFUL, 0x1A0033));
    public static final RegistryObject<MobEffect> PHANTOM_DODGE = MOB_EFFECTS.register("phantom_dodge", () -> new net.offkung.bhspells.effect.PhantomDodgeEffect(MobEffectCategory.BENEFICIAL, 0x7EC8E3));

    public static void register(IEventBus modEventBus) {
        MOB_EFFECTS.register(modEventBus);
    }
}

