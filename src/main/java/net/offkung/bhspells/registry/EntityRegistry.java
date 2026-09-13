package net.offkung.bhspells.registry;

import com.gametechbc.traveloptics.entity.projectiles.flood_slash.FloodSlashProjectile;
import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.entity.spells.LightningStrike;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.aqua_flower.AquaFlower;
import net.offkung.bhspells.entity.spells.dark_rainfall.DarkRainFallAoe;
import net.offkung.bhspells.entity.spells.divine_thunder.RedLightningStrike;
import net.offkung.bhspells.entity.spells.embracing_bosom.EmbracingBosomAoe;
import net.offkung.bhspells.entity.spells.eternal_purification.LotusPetal;
import net.offkung.bhspells.entity.spells.eternal_purification.PurificationPillarEntity;
import net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyBall;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.entity.spells.firebird.FireSlashProjectile;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudEntity;
import net.offkung.bhspells.entity.spells.golden_gate.GoldenGateEntity;
import net.offkung.bhspells.entity.spells.heaven_lion.HeavenLionProjectile;
import net.offkung.bhspells.entity.spells.intrusion_chain.IntrusionChainEntity;
import net.offkung.bhspells.entity.spells.purple_wave.PurpleWaveProjectile;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalEntity;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantFieldAoe;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSword;
import net.offkung.bhspells.entity.spells.stone_crumble.StoneCrumbleProjectile;
import net.offkung.bhspells.entity.spells.blessing_snow.RadiusSnowRingEntity;
import net.offkung.bhspells.entity.spells.thousand_arrows.*;

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
}
