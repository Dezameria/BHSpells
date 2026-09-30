package net.offkung.bhspells.pressure.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import org.joml.Matrix4f;

/**
 * Single-pass screen-space post-processing effect for spiritual pressure and torrential domains.
 * Combines primary color push, secondary chromatic interference, top/bottom streaming energy bands,
 * vignette, and breathing pulse in ONE unified composite render pass.
 */
public final class ScreenPressurePostProcessor {
    public static final ResourceLocation VIGNETTE_TEXTURE = new ResourceLocation("minecraft", "textures/misc/vignette.png");

    private ScreenPressurePostProcessor() {
    }

    public static void render(GuiGraphics guiGraphics, float partialTicks) {
        ScreenPressureState state = ScreenPressureAggregator.getCurrentState();
        if (!state.isActive()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        float intensity = state.totalPressure();
        float gameTime = (mc.level != null ? mc.level.getGameTime() + partialTicks : 0.0F) / 20.0F;

        // Harmonic breathing pulse
        float pulse = 0.92F + 0.08F * Mth.sin(gameTime * 3.8F);
        float effectiveIntensity = intensity * pulse;

        // Top and bottom surging energy wave modulation
        float topWave = (Mth.sin(gameTime * 4.5F) + 1.0F) * 0.5F;
        float bottomWave = (Mth.cos(gameTime * 4.0F) + 1.0F) * 0.5F;

        // Primary color components
        int pCol = state.primaryColor();
        float pr = FastColor.ARGB32.red(pCol) / 255.0F;
        float pg = FastColor.ARGB32.green(pCol) / 255.0F;
        float pb = FastColor.ARGB32.blue(pCol) / 255.0F;

        // Secondary color components
        int sCol = state.secondaryColor();
        float sr = FastColor.ARGB32.red(sCol) / 255.0F;
        float sg = FastColor.ARGB32.green(sCol) / 255.0F;
        float sb = FastColor.ARGB32.blue(sCol) / 255.0F;

        // Dominant direction push
        float pushX = state.dominantDirX() * 0.35F;

        // Unified Single Composite Pass:
        float leftPrimaryWeight = Mth.clamp(0.5F + pushX, 0.1F, 0.9F);
        float rightPrimaryWeight = 1.0F - leftPrimaryWeight;

        // Corner RGB interpolations
        int cLeftR = (int) (Mth.lerp(leftPrimaryWeight, sr, pr) * 255.0F);
        int cLeftG = (int) (Mth.lerp(leftPrimaryWeight, sg, pg) * 255.0F);
        int cLeftB = (int) (Mth.lerp(leftPrimaryWeight, sb, pb) * 255.0F);

        int cRightR = (int) (Mth.lerp(rightPrimaryWeight, sr, pr) * 255.0F);
        int cRightG = (int) (Mth.lerp(rightPrimaryWeight, sg, pg) * 255.0F);
        int cRightB = (int) (Mth.lerp(rightPrimaryWeight, sb, pb) * 255.0F);

        float alphaBase = Mth.clamp(effectiveIntensity * 0.85F, 0.0F, 0.92F);
        int alphaBottom = (int) (Mth.clamp(alphaBase * (0.85F + 0.15F * bottomWave), 0.0F, 1.0F) * 255.0F);
        int alphaTop = (int) (Mth.clamp(alphaBase * (0.80F + 0.20F * topWave), 0.0F, 1.0F) * 255.0F);

        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.depthMask(false);

        // Single Composite Pass using PositionTexColor shader with Vignette texture
        RenderSystem.setShader(GameRenderer::getPositionTexColorShader);
        RenderSystem.setShaderTexture(0, VIGNETTE_TEXTURE);

        Matrix4f pose = guiGraphics.pose().last().pose();
        BufferBuilder buffer = Tesselator.getInstance().getBuilder();

        buffer.begin(VertexFormat.Mode.QUADS, DefaultVertexFormat.POSITION_TEX_COLOR);
        // Bottom-left
        buffer.vertex(pose, 0.0F, (float) screenHeight, -90.0F).uv(0.0F, 1.0F).color(cLeftR, cLeftG, cLeftB, alphaBottom).endVertex();
        // Bottom-right
        buffer.vertex(pose, (float) screenWidth, (float) screenHeight, -90.0F).uv(1.0F, 1.0F).color(cRightR, cRightG, cRightB, alphaBottom).endVertex();
        // Top-right
        buffer.vertex(pose, (float) screenWidth, 0.0F, -90.0F).uv(1.0F, 0.0F).color(cRightR, cRightG, cRightB, alphaTop).endVertex();
        // Top-left
        buffer.vertex(pose, 0.0F, 0.0F, -90.0F).uv(0.0F, 0.0F).color(cLeftR, cLeftG, cLeftB, alphaTop).endVertex();

        // Subtle chromatic fringe quad when intense interference occurs
        if (state.interference() > 0.05F || intensity > 0.6F) {
            float fringeOffset = 2.5F * state.interference();
            int fringeAlpha = (int) (alphaBase * 0.35F * 255.0F);
            buffer.vertex(pose, fringeOffset, (float) screenHeight, -89.0F).uv(0.0F, 1.0F).color((int) (sr * 255.0F), (int) (sg * 255.0F), (int) (sb * 255.0F), fringeAlpha).endVertex();
            buffer.vertex(pose, (float) screenWidth + fringeOffset, (float) screenHeight, -89.0F).uv(1.0F, 1.0F).color((int) (sr * 255.0F), (int) (sg * 255.0F), (int) (sb * 255.0F), fringeAlpha).endVertex();
            buffer.vertex(pose, (float) screenWidth + fringeOffset, 0.0F, -89.0F).uv(1.0F, 0.0F).color((int) (sr * 255.0F), (int) (sg * 255.0F), (int) (sb * 255.0F), fringeAlpha).endVertex();
            buffer.vertex(pose, fringeOffset, 0.0F, -89.0F).uv(0.0F, 0.0F).color((int) (sr * 255.0F), (int) (sg * 255.0F), (int) (sb * 255.0F), fringeAlpha).endVertex();
        }

        BufferUploader.drawWithShader(buffer.end());

        RenderSystem.depthMask(true);
        RenderSystem.disableBlend();
    }
}
