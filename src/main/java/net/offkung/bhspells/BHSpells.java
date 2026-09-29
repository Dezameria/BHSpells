package net.offkung.bhspells;

import com.mojang.logging.LogUtils;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.offkung.bhspells.compat.CompatBootstrap;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudEntity;
import net.offkung.bhspells.event.*;
import net.offkung.bhspells.event.entity.BypassDamageEvent;
import net.offkung.bhspells.event.EmbracingBosomEvents;
import net.offkung.bhspells.event.FireBodyHitEvent;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.registry.*;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(BHSpells.MODID)
public class BHSpells {
    public static final String MODID = "bhspells";
    public static final Logger LOGGER = LogUtils.getLogger();

    public BHSpells(FMLJavaModLoadingContext context) {
        IEventBus modEventBus = context.getModEventBus();
        modEventBus.addListener(this::commonSetup);

        MobEffectsRegistry.register(modEventBus);
        BHSoundRegistry.register(modEventBus);
        EntityRegistry.register(modEventBus);
        BHSpellRegistry.register(modEventBus);
        ParticleRegistry.register(modEventBus);
        AttributeRegistry.register(modEventBus);
        BHSchoolRegistry.register(modEventBus);
        ItemRegistry.register(modEventBus);
        BHSpellsTabRegistry.register(modEventBus);
        CompatBootstrap.init(modEventBus);
        context.registerConfig(ModConfig.Type.SERVER, SpellConfig.SPEC, "bhspells-server.toml");

        MinecraftForge.EVENT_BUS.register(this);
    }

    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(PacketHandler::registerPackets);
        LOGGER.info("BHSpells COMMON SETUP");
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("BHSpells SERVER STARTING");
    }

    public static ResourceLocation id(@NotNull String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
