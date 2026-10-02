package net.offkung.bhspells.client;

import com.asanginxst.epicfightx.client.particle.custom.SpriteParticle;
import io.redspace.ironsspellbooks.render.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.*;
import net.offkung.bhspells.client.render.BHChargeSpellLayer;
import net.offkung.bhspells.client.render.GalePiercerChargeLayer;
import net.offkung.bhspells.client.render.GildedHarePlayerLayer;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeCasterRingRenderer;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeTargetCrystalRenderer;
import net.offkung.bhspells.entity.spells.amethyst_decree.CrystalUnitModel;
import net.offkung.bhspells.entity.spells.aqua_flower.AquaFlowerRenderer;
import net.offkung.bhspells.entity.spells.crimson_rain_bathes_moon.CrimsonSpearRenderer;
import net.offkung.bhspells.entity.spells.crimson_thornbind.CrimsonRootRenderer;
import net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeRenderer;
import net.offkung.bhspells.entity.spells.embracing_bosom.EmbracingBosomRingRenderer;
import net.offkung.bhspells.entity.spells.eternal_purification.LotusPetalRenderer;
import net.offkung.bhspells.entity.spells.eternal_purification.PurificationPillarEntityRenderer;
import net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyRenderer;
import net.offkung.bhspells.entity.spells.feather_strike.FeatherStrikeRenderer;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleRenderer;
import net.offkung.bhspells.entity.spells.flames_eagle.FlamesEagleEntity;
import net.offkung.bhspells.entity.spells.flames_eagle.FlamesEagleRenderer;
import net.offkung.bhspells.entity.spells.gale_drive.GaleDriveVortexRenderer;
import net.offkung.bhspells.entity.spells.gale_piercer.GaleArrowRenderer;
import net.offkung.bhspells.entity.spells.gale_piercer.WindArrowRenderer;
import net.offkung.bhspells.entity.spells.glacial_firmament.GlacialSpikeRenderer;
import net.offkung.bhspells.entity.spells.glacial_firmament.GlacialTombRenderer;
import net.offkung.bhspells.entity.spells.gold_chain.ArcaneShackleRenderer;
import net.offkung.bhspells.entity.spells.gold_chain.GoldChainRenderer;
import net.offkung.bhspells.entity.spells.golden_gate.GoldenGateRenderer;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudRenderer;
import net.offkung.bhspells.entity.spells.hazard_area.HazardSplashRenderer;
import net.offkung.bhspells.entity.spells.intrusion_chain.IntrusionChainEntityRenderer;
import net.offkung.bhspells.entity.spells.jade_brush_slash.JadeBrushSlashRenderer;
import net.offkung.bhspells.entity.spells.jade_cluster.JadeClusterEntityRenderer;
import net.offkung.bhspells.entity.spells.rapturous_bloom.RapturousBloomRenderer;
import net.offkung.bhspells.entity.spells.red_beryl_bird.RedBerylBirdRenderer;
import net.offkung.bhspells.entity.spells.resonant_knell.ResonantKnellDomeRenderer;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalRenderer;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSwordRenderer;
import net.offkung.bhspells.entity.spells.stone_crumble.StoneCrumbleRenderer;
import net.offkung.bhspells.entity.spells.supporting_bamboo.GreenSunbeam;
import net.offkung.bhspells.entity.spells.supporting_bamboo.GreenSunbeamRenderer;
import net.offkung.bhspells.entity.spells.supporting_bamboo.SupportingBambooRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicAlchemyRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicArrowRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicCastingArrowRenderer;
import net.offkung.bhspells.entity.spells.venomous_blossomfall.AzureVenomNeedleModel;
import net.offkung.bhspells.entity.spells.venomous_blossomfall.AzureVenomNeedleRenderer;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = BHSpells.MODID)
public class BHSpellsClient {
    public static final ResourceLocation RED_CHARGE_TEXTURE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/red_charged/red_charged.png");

    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        BHSpells.LOGGER.info("BHSpells CLIENT SETUP");
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        event.register(BHKeyMappings.DISMOUNT_GOLDEN_CLOUD);
    }

    @SubscribeEvent
     public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ExplosiveLilyRenderer.MODEL_LAYER_LOCATION, ExplosiveLilyRenderer::createBodyLayer);
        event.registerLayerDefinition(RadiantCrystalRenderer.RadiantCrystalModel.LAYER_LOCATION, RadiantCrystalRenderer.RadiantCrystalModel::createBodyLayer);
        event.registerLayerDefinition(CrystalUnitModel.SMALL_LAYER, CrystalUnitModel::createSmallLayer);
        event.registerLayerDefinition(CrystalUnitModel.LARGE_LAYER, CrystalUnitModel::createLargeLayer);
        event.registerLayerDefinition(AzureVenomNeedleModel.LAYER_LOCATION, AzureVenomNeedleModel::createBodyLayer);
        event.registerLayerDefinition(GlacialSpikeRenderer.GlacialSpikeModel.LAYER_LOCATION, GlacialSpikeRenderer.GlacialSpikeModel::createBodyLayer);
        event.registerLayerDefinition(GlacialTombRenderer.GlacialTombModel.LAYER_LOCATION, GlacialTombRenderer.GlacialTombModel::createBodyLayer);
    }

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        ResourceLocation smallArrowTexture = BHSpells.id("textures/entity/magic_arrow/small_magic_arrow.png");

        event.registerEntityRenderer(EntityRegistry.STONE_CRUMBLE_PROJECTILE.get(), StoneCrumbleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EXPLOSIVE_LILY_BALL.get(), ExplosiveLilyRenderer::new);
        event.registerEntityRenderer(EntityRegistry.DARK_RAIN_FALL.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.PURIFICATION_PILLAR.get(), PurificationPillarEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.PURPLE_WAVE_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.MAGIC_ALCHEMY.get(), MagicAlchemyRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SKY_ARROW.get(), MagicCastingArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HUGE_ARROW.get(), context -> new MagicArrowRenderer<>(context, smallArrowTexture, 6.0F));
        event.registerEntityRenderer(EntityRegistry.RAIN_VOLLEY.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RAIN_VOLLEY_ARROW.get(), context -> new MagicArrowRenderer<>(context, smallArrowTexture));
        event.registerEntityRenderer(EntityRegistry.GRAY_SMALL_ARROW.get(), context -> new MagicArrowRenderer<>(context, smallArrowTexture));
        event.registerEntityRenderer(EntityRegistry.PETAL_WALTZ_SWORD.get(), PetalWaltzSwordRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LOTUS_PETAL.get(), LotusPetalRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLDEN_MARBLE.get(), GoldenMarbleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FIRE_SLASH_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLDEN_GATE_ENTITY.get(), GoldenGateRenderer::new);
        event.registerEntityRenderer(EntityRegistry.EMBRACING_BOSOM_AOE.get(), EmbracingBosomRingRenderer::new);
        event.registerEntityRenderer(EntityRegistry.INTRUSION_CHAIN.get(), IntrusionChainEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RED_LIGHTNING_STRIKE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HEAVEN_LION_PROJECTILE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RADIANT_CRYSTAL.get(), RadiantCrystalRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RADIANT_FIELD_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLDEN_CLOUD.get(), GoldenCloudRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RADIUS_SNOW_RING.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AQUA_FLOWER.get(), AquaFlowerRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RED_BERYL_BIRD.get(), RedBerylBirdRenderer::new);
        event.registerEntityRenderer(EntityRegistry.JADE_BRUSH_SLASH.get(), JadeBrushSlashRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SWIRLING_BLOSSOM.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FEATHER_STRIKE.get(), FeatherStrikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.HAZARD_SPLASH.get(), HazardSplashRenderer::new);
        event.registerEntityRenderer(EntityRegistry.SUPPORTING_BAMBOO.get(), SupportingBambooRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GREEN_SUNBEAM.get(), GreenSunbeamRenderer::new);
        event.registerEntityRenderer(EntityRegistry.FLAMES_EAGLE.get(), FlamesEagleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AMETHYST_DECREE_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AMETHYST_DECREE_CASTER_RING.get(), AmethystDecreeCasterRingRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AMETHYST_DECREE_TARGET_CRYSTAL.get(), AmethystDecreeTargetCrystalRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CRYSTAL_HYDRO_DOME_AOE.get(), CrystalHydroDomeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.ARCANE_SHACKLE.get(), ArcaneShackleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLD_CHAIN.get(), GoldChainRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WING_OF_TEMPEST_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CRIMSON_SPEAR.get(), CrimsonSpearRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GALE_DRIVE_VORTEX.get(), GaleDriveVortexRenderer::new);
        event.registerEntityRenderer(EntityRegistry.AZURE_VENOM_NEEDLE.get(), AzureVenomNeedleRenderer::new);
        event.registerEntityRenderer(EntityRegistry.WIND_ARROW.get(), WindArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GALE_ARROW.get(), GaleArrowRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GLACIAL_SPIKE.get(), GlacialSpikeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GLACIAL_TOMB.get(), GlacialTombRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RESONANT_KNELL_DOME.get(), ResonantKnellDomeRenderer::new);
        event.registerEntityRenderer(EntityRegistry.RAPTUROUS_BLOOM.get(), RapturousBloomRenderer::new);
        event.registerEntityRenderer(EntityRegistry.CRIMSON_ROOT.get(), CrimsonRootRenderer::new);
        event.registerEntityRenderer(EntityRegistry.TOXIC_SALVATION_AOE.get(), NoopRenderer::new);
        event.registerEntityRenderer(EntityRegistry.JADE_CLUSTER.get(), JadeClusterEntityRenderer::new);
        event.registerEntityRenderer(EntityRegistry.YIN_INK_CASCADE_AREA.get(), NoopRenderer::new);
    }

    @SubscribeEvent
    public static void registerRenderers(final EntityRenderersEvent.AddLayers event) {
        addLayerToPlayerSkin(event, "default");
        addLayerToPlayerSkin(event, "slim");
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void addLayerToPlayerSkin(EntityRenderersEvent.AddLayers event, String skinName) {
        EntityRenderer<? extends Player> render = event.getSkin(skinName);
        if (render instanceof LivingEntityRenderer livingRenderer) {
            livingRenderer.addLayer(new BHChargeSpellLayer.Vanilla<>(livingRenderer));
            livingRenderer.addLayer(new EnergySwirlLayer.Vanilla(livingRenderer, RED_CHARGE_TEXTURE, MobEffectsRegistry.RED_CHARGED));
            livingRenderer.addLayer(new GalePiercerChargeLayer(livingRenderer));
            livingRenderer.addLayer(new GildedHarePlayerLayer(livingRenderer));
        }
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.BUBBLE_SPLASH_PARTICLE.get(), BubbleSplashParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GREEN_CROSS_PARTICLE.get(), GreenCrossParticle.Provider::new);
        event.registerSpriteSet(ParticleTypes.DUST_COLOR_TRANSITION, CustomDustColorTransition.Provider::new);
        event.registerSpriteSet(ParticleRegistry.OAK_LEAF_PARTICLE.get(), OakLeafParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GREEN_ENCHANTED_HIT_PARTICLE.get(), GreenEnchantedHitParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GREEN_LINE_PARTICLE.get(), GreenLineParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.YELLOW_ELECTRIC_PARTICLE.get(), YellowElectricParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.CUSTOM_ZAP_PARTICLE.get(), CustomZapParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.COLORED_END_ROD_PARTICLE.get(), ColoredEndRodParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GREEN_CAT.get(), GreenCatParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.WHITE_BREATH_PARTICLE.get(), WhiteBreathParticle.Provider::new);
        event.registerSpecial(ParticleRegistry.PURPLE_LIGHTNING.get(), new PurpleLightningParticle.Provider());
        event.registerSpriteSet(ParticleRegistry.SPLATTER_SAKURA.get(), SplatterSakuraParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.COLORED_CHERRY_PARTICLE.get(), ColoredCherryParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.FALLING_LEAF_PARTICLE.get(), FallingLeafParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.BLINK_LEAF.get(), BlinkLeafParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EMBRACE_LEAF.get(), EmbraceLeafParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EMBRACE_MOTE.get(), EmbraceMoteParticle.Provider::new);
        event.registerSpriteSet((ParticleRegistry.FIRE_HIT_SLASH.get()), (pSprites) -> (new SpriteParticle.SpriteParticleProvider(pSprites, 8)).setSize(2.5F, 2.5F).setQuadSize(1.75F).setRandomRoll(true).setYOffset(1.2F));
        event.registerSpriteSet(ParticleRegistry.RADIANT_SHATTER.get(), RadiantShatterParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GOLDEN_CRIT.get(), GoldenCritParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.COLORED_MYCELIUM_PARTICLE.get(), ColoredMyceliumParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GRAY_FLOWER_PARTICLE.get(), GrayFlowerParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.PINK_EMBERS.get(), PinkEmberParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.PINK_DRAGON_FIRE.get(), PinkDragonFireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.PINK_FIRE.get(), PinkFireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.AMETHYST_SHARD.get(), AmethystShardParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.ZAP_CUSTOM.get(), ZapParticleCustom.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SHOCKWAVE_CUSTOM.get(), ShockwaveParticleCustom.Provider::new);
        event.registerSpriteSet(ParticleRegistry.WHITE_FIRE.get(), WhiteFireParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.WHITE_EMBER.get(), WhiteEmberParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.WHITE_FIRE_EMITTER.get(), WhiteFireEmitterParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.RED_PLUM.get(), RedPlumParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GILDED_HARE.get(), GildedHareParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.DING.get(), net.offkung.bhspells.client.particle.DingEntityAfterImageParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.SHOCKING_BEAM.get(), net.offkung.bhspells.client.particle.ShockingBeamParticle.Provider::new);

    }
}