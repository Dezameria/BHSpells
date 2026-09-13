package net.offkung.bhspells.entity.spells.embracing_bosom;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.offkung.bhspells.BHSpells;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class EmbracingBosomRingRenderer extends EntityRenderer<EmbracingBosomAoe> {
    private static final ResourceLocation DUMMY_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/pig/pig.png");
    private static final String RING_TEX_PATH = "textures/entity/ring/";
    private static final int AMBER_TINT_HEX = 0xC96A14;
    private static final int SEGMENTS = 48;

    private static final int FADE_OUT_TICKS = 20;
    private static final float FADE_OUT_SHRINK = 0.18f;

    private static final int RING_CONVERGE_TICKS = 15;
    private static final float RING_CONVERGE_START_RADIUS_MULT = 1.7f;
    private static final float RING_CONVERGE_SPIN_MULT = 3.0f;
    private static final float RING_CONVERGE_STAGGER_TICKS = 4.0f;

    private static final List<EmbracingBrosomRingLayer> LAYERS = List.of(
            new EmbracingBrosomRingLayer(ring("haze_soft"), 6.0f, 0.0f, 1.8f, 360f, 0.32f, 0.02f),
            new EmbracingBrosomRingLayer(ring("haze_patchy"), 5.4f, 0.0f, -2.7f, 360f, 0.29f, 0.04f),
            new EmbracingBrosomRingLayer(ring("vortex"), 6.0f, 0.0f, -7.2f, 360f, 0.62f, 0.06f),
            new EmbracingBrosomRingLayer(ring("cyclone"), 4.2f, 0.0f, 12.6f, 360f, 0.57f, 0.08f),
            new EmbracingBrosomRingLayer(ring("highlight_rim"), 6.2f, 0.0f, -5.4f, 360f, 0.55f, 0xF5B34E, 0.10f));

    private static final float MIN_LAYER_RADIUS = (float) LAYERS.stream().mapToDouble(EmbracingBrosomRingLayer::radius).min().orElse(0.0);
    private static final float MAX_LAYER_RADIUS = (float) LAYERS.stream().mapToDouble(EmbracingBrosomRingLayer::radius).max().orElse(1.0);

    private static ResourceLocation ring(String name) {
        return ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, RING_TEX_PATH + name + ".png");
    }

    private static final RenderStateShard.TransparencyStateShard ADDITIVE_ALPHA_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("bhspells_additive_alpha", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    private static final Map<ResourceLocation, RenderType> RENDER_TYPE_CACHE = new HashMap<>();

    private static RenderType ringRenderType(ResourceLocation texture) {
        return RENDER_TYPE_CACHE.computeIfAbsent(texture, EmbracingBosomRingRenderer::buildRingRenderType);
    }

    private static RenderType buildRingRenderType(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentEmissiveShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(ADDITIVE_ALPHA_TRANSPARENCY)
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(false))
                .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                .createCompositeState(false);
        return RenderType.create("bhspells_ring", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    public EmbracingBosomRingRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(EmbracingBosomAoe entity) {
        return DUMMY_TEXTURE;
    }

    @Override
    public void render(EmbracingBosomAoe entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        int activeTicks = Math.max(0, entity.tickCount - entity.getDelay());

        float fadeOutAlpha = lifecycleFadeOutAlpha(activeTicks, partialTicks);
        if (fadeOutAlpha <= 0.0f) {
            return;
        }
        float shrink = lifecycleShrink(activeTicks, partialTicks);

        UUID uuid = entity.getUUID();
        for (int i = 0; i < LAYERS.size(); i++) {
            renderLayer(LAYERS.get(i), i, uuid, entity.tickCount, activeTicks, partialTicks, fadeOutAlpha, shrink, poseStack, buffer);
        }
    }

    private static float lifecycleFadeOutAlpha(int activeTicks, float partialTicks) {
        float t = activeTicks + partialTicks;
        float fadeOutStart = EmbracingBosomAoe.LIFETIME_TICKS - FADE_OUT_TICKS;
        if (t > fadeOutStart) {
            return 1.0f - Mth.clamp((t - fadeOutStart) / FADE_OUT_TICKS, 0.0f, 1.0f);
        }
        return 1.0f;
    }

    private static float lifecycleShrink(int activeTicks, float partialTicks) {
        float t = activeTicks + partialTicks;
        float fadeOutStart = EmbracingBosomAoe.LIFETIME_TICKS - FADE_OUT_TICKS;
        if (t > fadeOutStart) {
            float p = Mth.clamp((t - fadeOutStart) / FADE_OUT_TICKS, 0.0f, 1.0f);
            return 1.0f - FADE_OUT_SHRINK * p;
        }
        return 1.0f;
    }

    private static float layerConvergeProgress(EmbracingBrosomRingLayer layer, int activeTicks, float partialTicks) {
        float globalT = activeTicks + partialTicks;
        float localT = Math.max(0.0f, globalT - staggerOffsetTicks(layer));
        return Mth.clamp(localT / RING_CONVERGE_TICKS, 0.0f, 1.0f);
    }

    private static float staggerOffsetTicks(EmbracingBrosomRingLayer layer) {
        float range = MAX_LAYER_RADIUS - MIN_LAYER_RADIUS;
        if (range < 1.0e-4f) {
            return 0.0f;
        }
        return (layer.radius() - MIN_LAYER_RADIUS) / range * RING_CONVERGE_STAGGER_TICKS;
    }

    private static float easeOutCubic(float p) {
        float inv = 1.0f - p;
        return 1.0f - inv * inv * inv;
    }

    private static float convergeSpinBonusDegrees(float rotationSpeed, int activeTicks, float partialTicks) {
        float t = Math.min(activeTicks + partialTicks, (float) RING_CONVERGE_TICKS);
        if (t <= 0.0f) {
            return 0.0f;
        }
        float p = t / RING_CONVERGE_TICKS;
        float oneMinusP = 1.0f - p;
        float oneMinusP4 = oneMinusP * oneMinusP * oneMinusP * oneMinusP;
        float integral = (RING_CONVERGE_TICKS / 4.0f) * (1.0f - oneMinusP4);
        return rotationSpeed * (RING_CONVERGE_SPIN_MULT - 1.0f) * integral;
    }

    private static void renderLayer(EmbracingBrosomRingLayer layer, int layerIndex, UUID uuid, int tickCount, int activeTicks, float partialTicks, float fadeOutAlpha, float shrink, PoseStack poseStack, MultiBufferSource buffer) {
        float jitter = stableJitterDegrees(uuid, layerIndex, layer.startAngleJitter());
        float baseAngleDeg = jitter + layer.rotationSpeed() * (tickCount + partialTicks);
        float spinBonusDeg = convergeSpinBonusDegrees(layer.rotationSpeed(), activeTicks, partialTicks);
        float angleDeg = baseAngleDeg + spinBonusDeg;

        float convergeP = layerConvergeProgress(layer, activeTicks, partialTicks);
        float convergeEased = easeOutCubic(convergeP);
        float convergeMult = RING_CONVERGE_START_RADIUS_MULT
                + (1.0f - RING_CONVERGE_START_RADIUS_MULT) * convergeEased;

        float outer = layer.radius() * convergeMult * shrink;
        float inner = layer.innerRadius() * convergeMult * shrink;

        int tintRGB = layer.resolveTint(AMBER_TINT_HEX);
        float r = ((tintRGB >> 16) & 0xFF) / 255.0f;
        float g = ((tintRGB >> 8) & 0xFF) / 255.0f;
        float b = (tintRGB & 0xFF) / 255.0f;
        // Alpha fade-in driven by the exact same eased progress as the radius, so they resolve
        // together, per spec.
        float a = layer.alpha() * convergeEased * fadeOutAlpha;

        poseStack.pushPose();
        poseStack.translate(0.0, layer.yOffset(), 0.0);
        poseStack.mulPose(Axis.YP.rotationDegrees(angleDeg));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f positionMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = buffer.getBuffer(ringRenderType(layer.texture()));
        buildAnnulus(consumer, positionMatrix, normalMatrix, outer, inner, r, g, b, a);

        poseStack.popPose();
    }

    private static void buildAnnulus(VertexConsumer consumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float outer, float inner, float r, float g, float b, float a) {
        float uvScale = outer > 1.0e-4f ? 1.0f / (2.0f * outer) : 0.0f;

        float prevCos = Mth.cos(0.0f);
        float prevSin = 0.0f;
        for (int i = 1; i <= SEGMENTS; i++) {
            float angleRad = (float) (i * (2.0 * Math.PI) / SEGMENTS);
            float cos = Mth.cos(angleRad);
            float sin = Mth.sin(angleRad);

            quadVertex(consumer, positionMatrix, normalMatrix, inner * prevCos, inner * prevSin, r, g, b, a, uvScale);
            quadVertex(consumer, positionMatrix, normalMatrix, outer * prevCos, outer * prevSin, r, g, b, a, uvScale);
            quadVertex(consumer, positionMatrix, normalMatrix, outer * cos, outer * sin, r, g, b, a, uvScale);
            quadVertex(consumer, positionMatrix, normalMatrix, inner * cos, inner * sin, r, g, b, a, uvScale);

            prevCos = cos;
            prevSin = sin;
        }
    }

    private static void quadVertex(VertexConsumer consumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float z, float r, float g, float b, float a, float uvScale) {
        float u = x * uvScale + 0.5f;
        float v = z * uvScale + 0.5f;
        consumer.vertex(positionMatrix, x, 0.0f, z)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normalMatrix, 0.0f, 1.0f, 0.0f)
                .endVertex();
    }

    private static float stableJitterDegrees(UUID uuid, int layerIndex, float amplitudeDegrees) {
        if (amplitudeDegrees <= 0.0f) {
            return 0.0f;
        }
        long h = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits() ^ (layerIndex * 0x9E3779B97F4A7C15L);
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);
        float unit = (float) ((h >>> 11) * 0x1.0p-53); // [0, 1)
        return unit * amplitudeDegrees;
    }
}
