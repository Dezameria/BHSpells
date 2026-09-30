package net.offkung.bhspells.pressure.client;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.minecraft.client.Camera;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Single shared batched renderer for all active spiritual pressure fields and torrential domains.
 * Employs weather/rain rendering principles: procedural crossed quads, distance LOD,
 * zero-raycast heightmap caching, and camera-centric curtain mode for 64-120 block domains.
 */
public final class PressureFieldRenderer {
    private static final ResourceLocation STREAK_TEXTURE = new ResourceLocation(BHSpells.MODID, "textures/vfx/pressure_streak.png");
    private static final int MAX_RENDERED_FIELDS = 6;

    private static final RenderType PRESSURE_STREAK_RENDER_TYPE = RenderType.create(
            "pressure_streak",
            DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
            VertexFormat.Mode.QUADS,
            2048,
            false,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getPositionColorTexLightmapShader))
                    .setTextureState(new RenderStateShard.TextureStateShard(STREAK_TEXTURE, false, false))
                    .setTransparencyState(new RenderStateShard.TransparencyStateShard("translucent_transparency",
                            () -> {
                                com.mojang.blaze3d.systems.RenderSystem.enableBlend();
                                com.mojang.blaze3d.systems.RenderSystem.blendFuncSeparate(
                                        com.mojang.blaze3d.platform.GlStateManager.SourceFactor.SRC_ALPHA,
                                        com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE,
                                        com.mojang.blaze3d.platform.GlStateManager.SourceFactor.ONE,
                                        com.mojang.blaze3d.platform.GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA
                                );
                            },
                            () -> {
                                com.mojang.blaze3d.systems.RenderSystem.disableBlend();
                                com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
                            }
                    ))
                    .setCullState(new RenderStateShard.CullStateShard(false))
                    .setLightmapState(new RenderStateShard.LightmapStateShard(false))
                    .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                    .createCompositeState(false)
    );

    private PressureFieldRenderer() {
    }

    public static void render(ClientLevel level, PoseStack poseStack, MultiBufferSource bufferSource, Camera camera, float partialTicks) {
        var fields = ClientPressureManager.getActiveFields();
        if (fields.isEmpty()) {
            return;
        }

        Vec3 camPos = camera.getPosition();
        float gameTimeSeconds = (level.getGameTime() + partialTicks) / 20.0F;

        // Rank fields by distance to camera
        List<ClientPressureField> sortedFields = new ArrayList<>(fields);
        sortedFields.sort(Comparator.comparingDouble(f -> f.getInterpolatedCenter(partialTicks).distanceToSqr(camPos)));

        int remainingBudget = Math.max(
                SpellConfig.SpiritualPressure.getGlobalStreakBudget(),
                Math.max(SpellConfig.VengefulPressure.getGlobalStreakBudget(), SpellConfig.TempestReiatsu.getGlobalStreakBudget())
        );
        int fieldCount = Math.min(sortedFields.size(), MAX_RENDERED_FIELDS);

        VertexConsumer builder = bufferSource.getBuffer(PRESSURE_STREAK_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        float[] lengthBuffer = new float[1];

        for (int fIdx = 0; fIdx < fieldCount && remainingBudget > 0; fIdx++) {
            ClientPressureField field = sortedFields.get(fIdx);
            Vec3 center = field.getInterpolatedCenter(partialTicks);
            double distToCamera = center.distanceTo(camPos);
            float radius = field.getData().radius();
            boolean isCurtain = field.getData().visualProfile().cameraCurtain();
            boolean insideField = distToCamera <= radius;

            // Distance culling & LOD:
            // For camera curtain mode: if inside the 64-120m domain, effective distance is 0 (full 100% LOD)
            double effectiveDist = (isCurtain && insideField) ? 0.0D : Math.max(0.0D, distToCamera - radius);
            if (effectiveDist > 64.0D) {
                continue;
            }

            int lodStep;
            if (effectiveDist < 24.0D) {
                lodStep = 1;
            } else if (effectiveDist < 40.0D) {
                lodStep = 2;
            } else {
                lodStep = 4;
            }

            float fade = field.getFadeMultiplier(partialTicks) * field.getData().intensity();
            if (fade <= 0.01F) {
                continue;
            }

            int color = field.getData().getColor();
            int r = FastColor.ARGB32.red(color);
            int g = FastColor.ARGB32.green(color);
            int b = FastColor.ARGB32.blue(color);

            List<PressureStreak> streaks = field.getStreaks();
            int streaksToRender = Math.min(streaks.size() / lodStep, remainingBudget);

            // World-snapped origin for camera curtain mode gives 360-degree deluge around player without jitter
            Vec3 streakOrigin = (isCurtain && insideField)
                    ? new Vec3(Math.floor(camPos.x), camPos.y, Math.floor(camPos.z))
                    : center;

            double[] yRange = new double[2];

            for (int sIdx = 0; sIdx < streaks.size() && streaksToRender > 0; sIdx += lodStep) {
                PressureStreak streak = streaks.get(sIdx);
                double worldX = streakOrigin.x + streak.getOffsetX();
                double worldZ = streakOrigin.z + streak.getOffsetZ();

                int groundY = streak.getOrUpdateGroundY(level, (int) worldX, (int) worldZ, gameTimeSeconds);
                double refY = (isCurtain && insideField) ? camPos.y : Math.max(center.y, groundY);

                float streakAlpha = streak.calculateMotionAndAlpha(gameTimeSeconds, refY, groundY, yRange) * fade;
                if (streakAlpha <= 0.02F) {
                    continue;
                }

                double baseY = yRange[0];
                double topY = yRange[1];
                if (topY <= baseY + 0.1D) {
                    continue;
                }

                // Relative coordinates to camera
                float relX = (float) (worldX - camPos.x);
                float relZ = (float) (worldZ - camPos.z);
                float relY0 = (float) (baseY - camPos.y);
                float relY1 = (float) (topY - camPos.y);

                float halfW = streak.getWidth() * 0.5F;
                int a = (int) (streakAlpha * 255.0F);
                int light = 0x00F000F0; // Full emissive brightness for supernatural reiatsu

                // Downward UV scrolling for rushing deluge effect
                float vScroll = (gameTimeSeconds * streak.getSpeed() * 2.5F + streak.getPhaseOffset()) % 1.0F;
                float v0 = vScroll;
                float v1 = vScroll + 1.0F;

                // Quad 1: X-plane strip
                builder.vertex(matrix, relX - halfW, relY0, relZ).color(r, g, b, a).uv(0.0F, v1).uv2(light).endVertex();
                builder.vertex(matrix, relX + halfW, relY0, relZ).color(r, g, b, a).uv(1.0F, v1).uv2(light).endVertex();
                builder.vertex(matrix, relX + halfW, relY1, relZ).color(r, g, b, a).uv(1.0F, v0).uv2(light).endVertex();
                builder.vertex(matrix, relX - halfW, relY1, relZ).color(r, g, b, a).uv(0.0F, v0).uv2(light).endVertex();

                // Quad 2: Z-plane strip (Crossed Quads for 360-degree visibility)
                builder.vertex(matrix, relX, relY0, relZ - halfW).color(r, g, b, a).uv(0.0F, v1).uv2(light).endVertex();
                builder.vertex(matrix, relX, relY0, relZ + halfW).color(r, g, b, a).uv(1.0F, v1).uv2(light).endVertex();
                builder.vertex(matrix, relX, relY1, relZ + halfW).color(r, g, b, a).uv(1.0F, v0).uv2(light).endVertex();
                builder.vertex(matrix, relX, relY1, relZ - halfW).color(r, g, b, a).uv(0.0F, v0).uv2(light).endVertex();

                remainingBudget--;
                streaksToRender--;
            }
        }
    }
}
