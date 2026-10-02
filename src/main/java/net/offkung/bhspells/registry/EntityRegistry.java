package net.offkung.bhspells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.gale_piercer.GaleArrowEntity;
import net.offkung.bhspells.entity.spells.gale_piercer.WindArrowEntity;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeAoe;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeCasterRingEntity;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeTargetCrystalEntity;
import net.offkung.bhspells.entity.spells.aqua_flower.AquaFlower;
import net.offkung.bhspells.entity.spells.crimson_rain_bathes_moon.CrimsonSpearEntity;
import net.offkung.bhspells.entity.spells.crimson_thornbind.CrimsonRootEntity;
import net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeAoe;
import net.offkung.bhspells.entity.spells.dark_rainfall.DarkRainFallAoe;
import net.offkung.bhspells.entity.spells.divine_thunder.RedLightningStrike;
import net.offkung.bhspells.entity.spells.embracing_bosom.EmbracingBosomAoe;
import net.offkung.bhspells.entity.spells.eternal_purification.LotusPetal;
import net.offkung.bhspells.entity.spells.eternal_purification.PurificationPillarEntity;
import net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyBall;
import net.offkung.bhspells.entity.spells.feather_strike.FeatherStrike;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.entity.spells.firebird.FireSlashProjectile;
import net.offkung.bhspells.entity.spells.flames_eagle.FlamesEagleEntity;
import net.offkung.bhspells.entity.spells.gale_drive.GaleDriveVortexEntity;
import net.offkung.bhspells.entity.spells.glacial_firmament.GlacialSpikeEntity;
import net.offkung.bhspells.entity.spells.glacial_firmament.GlacialTombEntity;
import net.offkung.bhspells.entity.spells.gold_chain.ArcaneShackleProjectile;
import net.offkung.bhspells.entity.spells.gold_chain.GoldChain;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudEntity;
import net.offkung.bhspells.entity.spells.golden_gate.GoldenGateEntity;
import net.offkung.bhspells.entity.spells.hazard_area.HazardSplash;
import net.offkung.bhspells.entity.spells.heaven_lion.HeavenLionProjectile;
import net.offkung.bhspells.entity.spells.intrusion_chain.IntrusionChainEntity;
import net.offkung.bhspells.entity.spells.jade_brush_slash.JadeBrushSlash;
import net.offkung.bhspells.entity.spells.jade_cluster.JadeClusterEntity;
import net.offkung.bhspells.entity.spells.purple_wave.PurpleWaveProjectile;
import net.offkung.bhspells.entity.spells.rapturous_bloom.RapturousBloomEntity;
import net.offkung.bhspells.entity.spells.red_beryl_bird.RedBerylBird;
import net.offkung.bhspells.entity.spells.resonant_knell.ResonantKnellDomeAoe;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalEntity;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantFieldAoe;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSword;
import net.offkung.bhspells.entity.spells.stone_crumble.StoneCrumbleProjectile;
import net.offkung.bhspells.entity.spells.blessing_snow.RadiusSnowRingEntity;
import net.offkung.bhspells.entity.spells.supporting_bamboo.GreenSunbeam;
import net.offkung.bhspells.entity.spells.supporting_bamboo.SupportingBamboo;
import net.offkung.bhspells.entity.spells.swirling_blossom.SwirlingBlossomAoe;
import net.offkung.bhspells.entity.spells.thousand_arrows.*;
import net.offkung.bhspells.entity.spells.toxic_salvation.ToxicSalvationAoe;
import net.offkung.bhspells.entity.spells.venomous_blossomfall.AzureVenomNeedleEntity;
import net.offkung.bhspells.entity.spells.wings_of_tempest.WingofTempestAoe;
import net.offkung.bhspells.entity.spells.yin_ink_cascade.YinInkCascadeAreaEntity;

public class EntityRegistry {
    private static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, BHSpells.MODID);

    public static void register(IEventBus eventBus) {
        ENTITIES.register(eventBus);
    }

    public static final RegistryObject<EntityType<StoneCrumbleProjectile>> STONE_CRUMBLE_PROJECTILE =
            ENTITIES.register("stone_crumble_projectile", () -> EntityType.Builder.<StoneCrumbleProjectile>of(StoneCrumbleProjectile::new, MobCategory.MISC)
                    .sized(1.25f, 1)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "stone_crumble_projectile").toString()));

    public static final RegistryObject<EntityType<ExplosiveLilyBall>> EXPLOSIVE_LILY_BALL =
            ENTITIES.register("explosive_lily_ball", () -> EntityType.Builder.<ExplosiveLilyBall>of(ExplosiveLilyBall::new, MobCategory.MISC)
                    .sized(1.1f, 1.1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "explosive_lily_ball").toString()));

    public static final RegistryObject<EntityType<DarkRainFallAoe>> DARK_RAIN_FALL =
            ENTITIES.register("dark_rain_fall", () -> EntityType.Builder.<DarkRainFallAoe>of(DarkRainFallAoe::new, MobCategory.MISC)
                    .sized(4.0f, 0.8f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "dark_rain_fall").toString()));

    public static final RegistryObject<EntityType<PurificationPillarEntity>> PURIFICATION_PILLAR =
            ENTITIES.register("purification_pillar", () -> EntityType.Builder.<PurificationPillarEntity>of(PurificationPillarEntity::new, MobCategory.MISC)
                    .sized(4.0F, 14.0F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "purification_pillar").toString()));

    public static final RegistryObject<EntityType<PurpleWaveProjectile>> PURPLE_WAVE_PROJECTILE =
            ENTITIES.register("purple_wave_projectile", () -> EntityType.Builder.<PurpleWaveProjectile>of(PurpleWaveProjectile::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "purple_wave_projectile").toString()));

    public static final RegistryObject<EntityType<MagicAlchemyEntity>> MAGIC_ALCHEMY =
            ENTITIES.register("magic_alchemy", () -> EntityType.Builder.<MagicAlchemyEntity>of(MagicAlchemyEntity::new, MobCategory.MISC)
                    .sized(18F, 0.5F)
                    .clientTrackingRange(64)
                    .build((ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "magic_alchemy")).toString()));

    public static final RegistryObject<EntityType<SkyArrowProjectile>> SKY_ARROW =
            ENTITIES.register("sky_arrow", () -> EntityType.Builder.<SkyArrowProjectile>of(SkyArrowProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "sky_arrow").toString()));

    public static final RegistryObject<EntityType<RainVolleyEntity>> RAIN_VOLLEY =
            ENTITIES.register("rain_volley", () -> EntityType.Builder.<RainVolleyEntity>of(RainVolleyEntity::new, MobCategory.MISC)
                    .sized(0.1f, 0.1f).clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "rain_volley").toString()));

    public static final RegistryObject<EntityType<HugeArrowEntity>> HUGE_ARROW =
            ENTITIES.register("huge_arrow", () -> EntityType.Builder.<HugeArrowEntity>of(HugeArrowEntity::new, MobCategory.MISC)
                    .sized(3.0f, 3.0f).clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "huge_arrow").toString()));

    public static final RegistryObject<EntityType<RainVolleyArrow>> RAIN_VOLLEY_ARROW =
            ENTITIES.register("rain_volley_arrow", () -> EntityType.Builder.<RainVolleyArrow>of(RainVolleyArrow::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "rain_volley_arrow").toString()));

    public static final RegistryObject<EntityType<GraySmallArrow>> GRAY_SMALL_ARROW =
            ENTITIES.register("gray_small_arrow", () -> EntityType.Builder.<GraySmallArrow>of(GraySmallArrow::new, MobCategory.MISC)
                    .sized(.5f, .5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "gray_small_arrow").toString()));

    public static final RegistryObject<EntityType<PetalWaltzSword>> PETAL_WALTZ_SWORD =
            ENTITIES.register("petal_waltz_sword", () -> EntityType.Builder.<PetalWaltzSword>of(PetalWaltzSword::new, MobCategory.MISC)
                    .sized(1.2F, 1.8F)
                    .clientTrackingRange(64)
                    .build((ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "petal_waltz_sword")).toString()));

    public static final RegistryObject<EntityType<LotusPetal>> LOTUS_PETAL =
            ENTITIES.register("lotus_petal", () -> EntityType.Builder.<LotusPetal>of(LotusPetal::new, MobCategory.MISC)
                    .sized(3.5F, 1.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "lotus_petal").toString()));

    public static final RegistryObject<EntityType<GoldenMarbleEntity>> GOLDEN_MARBLE =
            ENTITIES.register("golden_marble", () -> EntityType.Builder.<GoldenMarbleEntity>of(GoldenMarbleEntity::new, MobCategory.MISC)
                    .sized(0.3f, 0.3f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "golden_marble").toString()));

    public static final RegistryObject<EntityType<FireSlashProjectile>> FIRE_SLASH_PROJECTILE =
            ENTITIES.register("fire_slash_projectile", () -> EntityType.Builder.<FireSlashProjectile>of(FireSlashProjectile::new, MobCategory.MISC)
                    .sized(2.0F, 0.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "fire_slash_projectile").toString()));

    public static final RegistryObject<EntityType<GoldenGateEntity>> GOLDEN_GATE_ENTITY =
            ENTITIES.register("golden_gate", () -> EntityType.Builder.<GoldenGateEntity>of(GoldenGateEntity::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "golden_gate").toString()));

    public static final RegistryObject<EntityType<EmbracingBosomAoe>> EMBRACING_BOSOM_AOE =
            ENTITIES.register("embracing_bosom_aoe", () -> EntityType.Builder
                    .<EmbracingBosomAoe>of(EmbracingBosomAoe::new, MobCategory.MISC)
                    .sized(12.0f, 1.2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "embracing_bosom_aoe").toString()));

    public static RegistryObject<EntityType<IntrusionChainEntity>> INTRUSION_CHAIN = ENTITIES.register("intrusion_chain",
            () -> EntityType.Builder.<IntrusionChainEntity>of(IntrusionChainEntity::new, MobCategory.MISC)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .setShouldReceiveVelocityUpdates(false)
                    .sized(0.1f, 0.1f).canSpawnFarFromPlayer().fireImmune()
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "intrusion_chain").toString()));

    public static final RegistryObject<EntityType<RedLightningStrike>> RED_LIGHTNING_STRIKE =
            ENTITIES.register("red_lightning_strike", () -> EntityType.Builder.<RedLightningStrike>of(RedLightningStrike::new, MobCategory.MISC)
                    .sized(1f, 1f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "red_lightning_strike").toString()));

    public static final RegistryObject<EntityType<HeavenLionProjectile>> HEAVEN_LION_PROJECTILE =
            ENTITIES.register("heaven_lion_projectile", () -> EntityType.Builder.<HeavenLionProjectile>of(HeavenLionProjectile::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "heaven_lion_projectile").toString()));

    public static final RegistryObject<EntityType<RadiantCrystalEntity>> RADIANT_CRYSTAL =
            ENTITIES.register("radiant_crystal", () -> EntityType.Builder.<RadiantCrystalEntity>of(RadiantCrystalEntity::new, MobCategory.MISC)
                    .sized(0.4f, 0.7f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "radiant_crystal").toString()));

    public static final RegistryObject<EntityType<RadiantFieldAoe>> RADIANT_FIELD_AOE =
            ENTITIES.register("radiant_field_aoe", () -> EntityType.Builder.<RadiantFieldAoe>of(RadiantFieldAoe::new, MobCategory.MISC)
                    .sized(10.0f, 1.2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "radiant_field_aoe").toString()));

    public static final RegistryObject<EntityType<GoldenCloudEntity>> GOLDEN_CLOUD =
            ENTITIES.register("golden_cloud", () -> EntityType.Builder.<GoldenCloudEntity>of(GoldenCloudEntity::new, MobCategory.MISC)
                    .sized(2.0f, 1.0f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "golden_cloud").toString()));

    public static final RegistryObject<EntityType<RadiusSnowRingEntity>> RADIUS_SNOW_RING =
            ENTITIES.register("radius_snow_ring", () -> EntityType.Builder
                    .<RadiusSnowRingEntity>of(RadiusSnowRingEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "radius_snow_ring").toString()));

    public static final RegistryObject<EntityType<AquaFlower>> AQUA_FLOWER =
            ENTITIES.register("aqua_flower", () -> EntityType.Builder.<AquaFlower>of(AquaFlower::new, MobCategory.MISC)
                    .sized(6.5F, 3.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "aqua_flower").toString()));

    public static final RegistryObject<EntityType<RedBerylBird>> RED_BERYL_BIRD =
            ENTITIES.register("red_beryl_bird", () -> EntityType.Builder.<RedBerylBird>of(RedBerylBird::new, MobCategory.MISC)
                    .sized(3.5F, 3.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "red_beryl_bird").toString()));

    public static final RegistryObject<EntityType<JadeBrushSlash>> JADE_BRUSH_SLASH =
            ENTITIES.register("jade_brush_slash", () -> EntityType.Builder.<JadeBrushSlash>of(JadeBrushSlash::new, MobCategory.MISC)
                    .sized(5.0F, 1.0F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "jade_brush_slash").toString()));

    public static final RegistryObject<EntityType<SwirlingBlossomAoe>> SWIRLING_BLOSSOM =
            ENTITIES.register("swirling_blossom_aoe", () -> EntityType.Builder.<SwirlingBlossomAoe>of(SwirlingBlossomAoe::new, MobCategory.MISC)
                    .sized(4f, SwirlingBlossomAoe.HEIGHT)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "swirling_blossom_aoe").toString()));

    public static final RegistryObject<EntityType<FeatherStrike>> FEATHER_STRIKE =
            ENTITIES.register("feather_strike", () -> EntityType.Builder.<FeatherStrike>of(FeatherStrike::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "feather_strike").toString()));

    public static final RegistryObject<EntityType<HazardSplash>> HAZARD_SPLASH =
            ENTITIES.register("hazard_splash", () -> EntityType.Builder.<HazardSplash>of(HazardSplash::new, MobCategory.MISC)
                    .sized(10.0f, 2.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "hazard_splash").toString()));

    public static final RegistryObject<EntityType<SupportingBamboo>> SUPPORTING_BAMBOO =
            ENTITIES.register("supporting_bamboo", () -> EntityType.Builder.<SupportingBamboo>of(SupportingBamboo::new, MobCategory.MISC)
                    .sized(10.0f, 2.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "supporting_bamboo").toString()));

    public static final RegistryObject<EntityType<GreenSunbeam>> GREEN_SUNBEAM =
            ENTITIES.register("green_sunbeam", () -> EntityType.Builder.<GreenSunbeam>of(GreenSunbeam::new, MobCategory.MISC)
                    .sized(1.5f, 14f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "green_sunbeam").toString()));

    public static final RegistryObject<EntityType<FlamesEagleEntity>> FLAMES_EAGLE =
            ENTITIES.register("flames_eagle", () -> EntityType.Builder.<FlamesEagleEntity>of(FlamesEagleEntity::new, MobCategory.MISC)
                    .sized(3.5F, 2.0F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "flames_eagle").toString()));

    public static final RegistryObject<EntityType<AmethystDecreeAoe>> AMETHYST_DECREE_AOE =
            ENTITIES.register("amethyst_decree_aoe", () -> EntityType.Builder.<AmethystDecreeAoe>of(AmethystDecreeAoe::new, MobCategory.MISC)
                    .sized(22.0f, 1.2f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "amethyst_decree_aoe").toString()));

    public static final RegistryObject<EntityType<AmethystDecreeCasterRingEntity>> AMETHYST_DECREE_CASTER_RING =
            ENTITIES.register("amethyst_decree_caster_ring", () -> EntityType.Builder.<AmethystDecreeCasterRingEntity>of(AmethystDecreeCasterRingEntity::new, MobCategory.MISC)
                    .sized(22.0f, 4.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "amethyst_decree_caster_ring").toString()));

    public static final RegistryObject<EntityType<AmethystDecreeTargetCrystalEntity>> AMETHYST_DECREE_TARGET_CRYSTAL =
            ENTITIES.register("amethyst_decree_target_crystal", () -> EntityType.Builder.<AmethystDecreeTargetCrystalEntity>of(AmethystDecreeTargetCrystalEntity::new, MobCategory.MISC)
                    .sized(2.0f, 3.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "amethyst_decree_target_crystal").toString()));

    public static final RegistryObject<EntityType<CrystalHydroDomeAoe>> CRYSTAL_HYDRO_DOME_AOE =
            ENTITIES.register("crystal_hydro_dome_aoe", () -> EntityType.Builder.<CrystalHydroDomeAoe>of(CrystalHydroDomeAoe::new, MobCategory.MISC)
                    .sized(12.0f, 6.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "crystal_hydro_dome_aoe").toString()));

    public static final RegistryObject<EntityType<ArcaneShackleProjectile>> ARCANE_SHACKLE =
            ENTITIES.register("arcane_shackle", () -> EntityType.Builder.<ArcaneShackleProjectile>of(ArcaneShackleProjectile::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "arcane_shackle").toString()));

    public static final RegistryObject<EntityType<GoldChain>> GOLD_CHAIN =
            ENTITIES.register("gold_chain", () -> EntityType.Builder.<GoldChain>of(GoldChain::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "gold_chain").toString()));

    public static final RegistryObject<EntityType<WingofTempestAoe>> WING_OF_TEMPEST_AOE =
            ENTITIES.register("wing_of_tempest_aoe", () -> EntityType.Builder.<WingofTempestAoe>of(WingofTempestAoe::new, MobCategory.MISC)
                    .sized(4.0f, 1.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "wing_of_tempest_aoe").toString()));

    public static final RegistryObject<EntityType<CrimsonSpearEntity>> CRIMSON_SPEAR =
            ENTITIES.register("crimson_spear", () -> EntityType.Builder.<CrimsonSpearEntity>of(CrimsonSpearEntity::new, MobCategory.MISC)
                    .sized(0.5f, 0.5f)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "crimson_spear").toString()));

    public static final RegistryObject<EntityType<GaleDriveVortexEntity>> GALE_DRIVE_VORTEX =
            ENTITIES.register("gale_drive_vortex", () -> EntityType.Builder.<GaleDriveVortexEntity>of(GaleDriveVortexEntity::new, MobCategory.MISC)
                    .sized(4.0f, 6.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "gale_drive_vortex").toString()));

    public static final RegistryObject<EntityType<AzureVenomNeedleEntity>> AZURE_VENOM_NEEDLE =
            ENTITIES.register("azure_venom_needle", () -> EntityType.Builder.<AzureVenomNeedleEntity>of(AzureVenomNeedleEntity::new, MobCategory.MISC)
                    .sized(0.25F, 0.25F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "azure_venom_needle").toString()));

    public static final RegistryObject<EntityType<WindArrowEntity>> WIND_ARROW =
            ENTITIES.register("wind_arrow", () -> EntityType.Builder.<WindArrowEntity>of(WindArrowEntity::new, MobCategory.MISC)
                    .sized(0.3F, 0.3F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "wind_arrow").toString()));

    public static final RegistryObject<EntityType<GaleArrowEntity>> GALE_ARROW =
            ENTITIES.register("gale_arrow", () -> EntityType.Builder.<GaleArrowEntity>of(GaleArrowEntity::new, MobCategory.MISC)
                    .sized(0.4F, 0.4F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "gale_arrow").toString()));

    public static final RegistryObject<EntityType<GlacialSpikeEntity>> GLACIAL_SPIKE =
            ENTITIES.register("glacial_spike", () -> EntityType.Builder.<GlacialSpikeEntity>of(GlacialSpikeEntity::new, MobCategory.MISC)
                    .sized(0.8F, 2.0F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "glacial_spike").toString()));

    public static final RegistryObject<EntityType<GlacialTombEntity>> GLACIAL_TOMB =
            ENTITIES.register("glacial_tomb", () -> EntityType.Builder.<GlacialTombEntity>of(GlacialTombEntity::new, MobCategory.MISC)
                    .sized(1.2F, 2.5F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "glacial_tomb").toString()));

    public static final RegistryObject<EntityType<ResonantKnellDomeAoe>> RESONANT_KNELL_DOME =
        ENTITIES.register("resonant_knell_dome",() -> EntityType.Builder.<ResonantKnellDomeAoe>of(ResonantKnellDomeAoe::new, MobCategory.MISC)
                    .sized(16.0F, 10.0F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "resonant_knell_dome").toString()));

    public static final RegistryObject<EntityType<RapturousBloomEntity>> RAPTUROUS_BLOOM =
            ENTITIES.register("rapturous_bloom", () -> EntityType.Builder.<RapturousBloomEntity>of(RapturousBloomEntity::new, MobCategory.MISC)
                    .sized(6.0F, 2.0F)
                    .clientTrackingRange(64)
                    .updateInterval(1)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "rapturous_bloom").toString()));

    public static final RegistryObject<EntityType<ToxicSalvationAoe>> TOXIC_SALVATION_AOE =
            ENTITIES.register("toxic_salvation_aoe", () -> EntityType.Builder.<ToxicSalvationAoe>of(ToxicSalvationAoe::new, MobCategory.MISC)
                    .sized(4.0f, 1.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "toxic_salvation_aoe").toString()));

    public static final RegistryObject<EntityType<CrimsonRootEntity>> CRIMSON_ROOT =
            ENTITIES.register("crimson_root", () -> EntityType.Builder.<CrimsonRootEntity>of(CrimsonRootEntity::new, MobCategory.MISC)
                    .sized(1.0F, 1.0F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "crimson_root").toString()));

    public static final RegistryObject<EntityType<JadeClusterEntity>> JADE_CLUSTER =
            ENTITIES.register("jade_cluster", () -> EntityType.Builder.<JadeClusterEntity>of(JadeClusterEntity::new, MobCategory.MISC)
                    .sized(1.0F, 3.2F)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "jade_cluster").toString()));

    public static final RegistryObject<EntityType<YinInkCascadeAreaEntity>> YIN_INK_CASCADE_AREA =
            ENTITIES.register("yin_ink_cascade_area", () -> EntityType.Builder.<YinInkCascadeAreaEntity>of(YinInkCascadeAreaEntity::new, MobCategory.MISC)
                    .sized(4.0f, 1.0f)
                    .clientTrackingRange(64)
                    .build(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "yin_ink_cascade_area").toString()));
}