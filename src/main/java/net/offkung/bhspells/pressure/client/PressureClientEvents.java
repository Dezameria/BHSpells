package net.offkung.bhspells.pressure.client;

import net.offkung.bhspells.BHSpells;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.client.event.RenderLevelStageEvent;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.client.gui.overlay.VanillaGuiOverlay;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

/**
 * Client event subscriber for spiritual pressure rendering, screen post-processing, and camera feedback.
 */
@Mod.EventBusSubscriber(modid = BHSpells.MODID, value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class PressureClientEvents {
    private PressureClientEvents() {
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase == TickEvent.Phase.END) {
            ClientPressureManager.clientTick();
        }
    }

    @SubscribeEvent
    public static void onRenderLevelStage(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        Camera camera = event.getCamera();
        float partialTick = event.getPartialTick();

        // Update screen state aggregator from current camera perspective
        ScreenPressureAggregator.update(camera, partialTick);

        // Render world-space procedural Reiatsu streaks
        PressureFieldRenderer.render(
                mc.level,
                event.getPoseStack(),
                mc.renderBuffers().bufferSource(),
                camera,
                partialTick
        );
    }

    @SubscribeEvent
    public static void onRenderGuiOverlay(RenderGuiOverlayEvent.Post event) {
        // Render screen-space post-processing over the final GUI / vignette pass
        if (event.getOverlay().id().equals(VanillaGuiOverlay.VIGNETTE.id())) {
            ScreenPressurePostProcessor.render(event.getGuiGraphics(), event.getPartialTick());
        }
    }

    @SubscribeEvent
    public static void onComputeCameraAngles(ViewportEvent.ComputeCameraAngles event) {
        ScreenPressureState state = ScreenPressureAggregator.getCurrentState();
        if (!state.isActive() || state.totalPressure() < 0.25F) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        float intensity = state.totalPressure();
        float gameTime = (mc.level.getGameTime() + (float) event.getPartialTick()) * 1.5F;

        // Subtle, non-nauseating micro camera vibration (clamped to +-0.05 yaw and +-0.08 pitch)
        float yawJitter = (Mth.sin(gameTime * 17.0F) * 0.035F + Mth.cos(gameTime * 29.0F) * 0.02F) * intensity;
        float pitchJitter = (Mth.cos(gameTime * 23.0F) * 0.045F + Mth.sin(gameTime * 13.0F) * 0.03F) * intensity;

        yawJitter = Mth.clamp(yawJitter, -0.05F, 0.05F);
        pitchJitter = Mth.clamp(pitchJitter, -0.08F, 0.08F);

        event.setYaw(event.getYaw() + yawJitter);
        event.setPitch(event.getPitch() + pitchJitter);
    }

    @SubscribeEvent
    public static void onLoggingOut(net.minecraftforge.client.event.ClientPlayerNetworkEvent.LoggingOut event) {
        ClientPressureManager.clearAll();
    }
}
