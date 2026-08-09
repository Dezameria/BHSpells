package net.offkung.bhspells.client;

import io.redspace.ironsspellbooks.IronsSpellbooks;
import io.redspace.ironsspellbooks.render.SpellTargetingLayer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.NoopRenderer;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.event.BHClientPlayerEvents;
import net.offkung.bhspells.client.event.ClientScreenShakeEvent;
import net.offkung.bhspells.client.event.SwordDashClientHandler;
import net.offkung.bhspells.client.particle.*;
import net.offkung.bhspells.client.render.BHChargeSpellLayer;
import net.offkung.bhspells.entity.spells.eternal_purification.LotusPetalRenderer;
import net.offkung.bhspells.entity.spells.eternal_purification.PurificationPillarEntityRenderer;
import net.offkung.bhspells.entity.spells.explosive_lily.ExplosiveLilyRenderer;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleRenderer;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSwordRenderer;
import net.offkung.bhspells.entity.spells.stone_crumble.StoneCrumbleRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicAlchemyRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicArrowRenderer;
import net.offkung.bhspells.entity.spells.thousand_arrows.MagicCastingArrowRenderer;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.ParticleRegistry;

import java.util.Map;

@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.MOD, modid = BHSpells.MODID)
public class BHSpellsClient {
    @SubscribeEvent
    public static void onClientSetup(FMLClientSetupEvent event) {
        MinecraftForge.EVENT_BUS.register(ClientScreenShakeEvent.class);
        MinecraftForge.EVENT_BUS.register(BHClientPlayerEvents.class);
        MinecraftForge.EVENT_BUS.register(SwordDashClientHandler.class);
        BHSpells.LOGGER.info("BHSpells CLIENT SETUP");
    }

    @SubscribeEvent
     public static void onRegisterLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(ExplosiveLilyRenderer.MODEL_LAYER_LOCATION, ExplosiveLilyRenderer::createBodyLayer);
    }

    @SubscribeEvent
    public static void rendererRegister(EntityRenderersEvent.RegisterRenderers event) {
        ResourceLocation smallArrowTexture = IronsSpellbooks.id("textures/entity/small_magic_arrow.png");

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
        event.registerEntityRenderer(EntityRegistry.PETAL_WALTZ_SWORD.get(), PetalWaltzSwordRenderer::new);
        event.registerEntityRenderer(EntityRegistry.LOTUS_PETAL.get(), LotusPetalRenderer::new);
        event.registerEntityRenderer(EntityRegistry.GOLDEN_MARBLE.get(), GoldenMarbleRenderer::new);
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
        event.registerSpriteSet(ParticleRegistry.YELLOW_ZAP_PARTICLE.get(), YellowZapParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.COLORED_END_ROD_PARTICLE.get(), ColoredEndRodParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.GREEN_CAT.get(), GreenCatParticle.Provider::new);
        event.registerSpriteSet(ParticleRegistry.WHITE_BREATH_PARTICLE.get(), WhiteBreathParticle.Provider::new);
        event.registerSpecial(ParticleRegistry.PURPLE_LIGHTNING.get(), new PurpleLightningParticle.Provider());
        event.registerSpriteSet(ParticleRegistry.SPLATTER_SAKURA.get(), SplatterSakuraParticle.Provider::new);
    }
}
