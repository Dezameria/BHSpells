package net.offkung.bhspells.client;

import com.asanginxst.epicfightx.client.particle.custom.SpriteParticle;
import io.redspace.ironsspellbooks.render.EnergySwirlLayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
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
import net.offkung.bhspells.entity.spells.aqua_flower.AquaFlowerRenderer;
import net.offkung.bhspells.entity.spells.embracing_bosom.EmbracingBosomRingRenderer;
import net.offkung.bhspells.entity.spells.eternal_purification.LotusPetalRenderer;
import net.offkung.bhspells.entity.spells.eternal_purification.PurificationPillarEntityRenderer;
import net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyRenderer;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleRenderer;
import net.offkung.bhspells.entity.spells.golden_gate.GoldenGateRenderer;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudRenderer;
import net.offkung.bhspells.entity.spells.intrusion_chain.IntrusionChainEntityRenderer;
import net.offkung.bhspells.entity.spells.resounding_radiant.RadiantCrystalRenderer;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSwordRenderer;
import net.offkung.bhspells.entity.spells.stone_crumble.StoneCrumbleRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicAlchemyRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicArrowRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicCastingArrowRenderer;
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
        }
    }

    @SubscribeEvent
    public static void registerParticles(RegisterParticleProvidersEvent event) {
        event.registerSpriteSet(ParticleRegistry.BUBBLE_SPLASH_PARTICLE.get(), BubbleSplashParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GOLD_SPARKLE_PARTICLE.get(), GoldSparkleParticle.Provider::new);
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
        event.registerSpriteSet(ParticleRegistry.BLINK_LEAF.get(), BlinkLeafParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EMBRACE_LEAF.get(), EmbraceLeafParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.EMBRACE_MOTE.get(), EmbraceMoteParticle.Provider::new);
        event.registerSpriteSet((ParticleRegistry.FIRE_HIT_SLASH.get()), (pSprites) -> (new SpriteParticle.SpriteParticleProvider(pSprites, 8)).setSize(2.5F, 2.5F).setQuadSize(1.75F).setRandomRoll(true).setYOffset(1.2F));
        event.registerSpriteSet(ParticleRegistry.RADIANT_SHATTER.get(), RadiantShatterParticle.Provider::new);
    }
}
