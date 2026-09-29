package net.offkung.bhspells.entity.spells.hazard_area;

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
import java.util.Map;
import java.util.UUID;

public class HazardSplashRenderer extends EntityRenderer<HazardSplash> {
    private static final ResourceLocation DUMMY_TEXTURE = ResourceLocation.fromNamespaceAndPath("minecraft", "textures/entity/pig/pig.png");

    private static final ResourceLocation[] FOG_TEXTURES = {
            fogTexture("haze_soft"),
            fogTexture("haze_patchy"),
            fogTexture("haze_core"),
    };

    public static final float FOG_RADIUS = 10.0f;
    private static final int PUFF_COUNT = 100;
    private static final float MIN_PUFF_SIZE = 4.5f;
    private static final float MAX_PUFF_SIZE = 8.0f;
    private static final float MIN_PUFF_HEIGHT = 0.2f;
    private static final float MAX_PUFF_HEIGHT = 4.5f;
    private static final float MAX_ORBIT_DRIFT_DEG_PER_TICK = 0.10f;
    private static final float MAX_ROLL_DEG_PER_TICK = 0.4f;
    private static final float BOB_AMPLITUDE = 0.5f;
    private static final float MAX_BOB_DEG_PER_TICK = 1.2f;

    private static final int FOG_COLOR_RGB = 0x3FCF5A;
    private static final float FOG_ALPHA = 0.25f;

    private static final int FADE_IN_TICKS = 15;
    private static final int FADE_OUT_TICKS = 40;

    private static ResourceLocation fogTexture(String name) {
        return ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "textures/entity/ring/" + name + ".png");
    }

    private static final RenderStateShard.TransparencyStateShard FOG_TRANSPARENCY =
            new RenderStateShard.TransparencyStateShard("bhspells_fog_transparency", () -> {
                RenderSystem.enableBlend();
                RenderSystem.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
            }, () -> {
                RenderSystem.disableBlend();
                RenderSystem.defaultBlendFunc();
            });

    private static final Map<ResourceLocation, RenderType> RENDER_TYPE_CACHE = new HashMap<>();

    private static RenderType fogRenderType(ResourceLocation texture) {
        return RENDER_TYPE_CACHE.computeIfAbsent(texture, HazardSplashRenderer::buildFogRenderType);
    }

    private static RenderType buildFogRenderType(ResourceLocation texture) {
        RenderType.CompositeState state = RenderType.CompositeState.builder()
                .setShaderState(new RenderStateShard.ShaderStateShard(GameRenderer::getRendertypeEntityTranslucentShader))
                .setTextureState(new RenderStateShard.TextureStateShard(texture, false, false))
                .setTransparencyState(FOG_TRANSPARENCY)
                .setCullState(new RenderStateShard.CullStateShard(false))
                .setLightmapState(new RenderStateShard.LightmapStateShard(true))
                .setOverlayState(new RenderStateShard.OverlayStateShard(true))
                .setWriteMaskState(new RenderStateShard.WriteMaskStateShard(true, false))
                .createCompositeState(false);
        return RenderType.create("bhspells_hazard_fog", DefaultVertexFormat.NEW_ENTITY,
                VertexFormat.Mode.QUADS, 256, false, true, state);
    }

    public HazardSplashRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(HazardSplash entity) {
        return DUMMY_TEXTURE;
    }

    @Override
    public void render(HazardSplash entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float alphaMult = fadeAlpha(entity.tickCount, partialTicks);
        if (alphaMult <= 0.0f) {
            return;
        }

        float r = ((FOG_COLOR_RGB >> 16) & 0xFF) / 255.0f;
        float g = ((FOG_COLOR_RGB >> 8) & 0xFF) / 255.0f;
        float b = (FOG_COLOR_RGB & 0xFF) / 255.0f;
        float a = FOG_ALPHA * alphaMult;

        UUID uuid = entity.getUUID();
        float t = entity.tickCount + partialTicks;
        var cameraOrientation = this.entityRenderDispatcher.cameraOrientation();

        for (int i = 0; i < PUFF_COUNT; i++) {
            renderPuff(uuid, i, t, poseStack, buffer, cameraOrientation, packedLight, r, g, b, a);
        }
    }

    private static void renderPuff(UUID uuid, int index, float t, PoseStack poseStack, MultiBufferSource buffer, org.joml.Quaternionf cameraOrientation, int packedLight, float r, float g, float b, float a) {
        float baseAngle = stableRandom(uuid, index, 0) * (float) (Math.PI * 2.0);
        float distance = Mth.sqrt(stableRandom(uuid, index, 1)) * FOG_RADIUS;
        float baseHeight = MIN_PUFF_HEIGHT + stableRandom(uuid, index, 2) * (MAX_PUFF_HEIGHT - MIN_PUFF_HEIGHT);
        float size = MIN_PUFF_SIZE + stableRandom(uuid, index, 3) * (MAX_PUFF_SIZE - MIN_PUFF_SIZE);
        float orbitDrift = signedUnit(stableRandom(uuid, index, 4)) * MAX_ORBIT_DRIFT_DEG_PER_TICK;
        float roll = stableRandom(uuid, index, 5) * 360.0f;
        float rollSpeed = signedUnit(stableRandom(uuid, index, 6)) * MAX_ROLL_DEG_PER_TICK;
        float bobPhase = stableRandom(uuid, index, 7) * 360.0f;
        float bobSpeed = (0.4f + stableRandom(uuid, index, 8) * 0.6f) * MAX_BOB_DEG_PER_TICK;
        ResourceLocation texture = FOG_TEXTURES[index % FOG_TEXTURES.length];

        float angle = baseAngle + orbitDrift * t * (float) (Math.PI / 180.0);
        float x = Mth.cos(angle) * distance;
        float z = Mth.sin(angle) * distance;
        float y = baseHeight + Mth.sin((bobPhase + bobSpeed * t) * ((float) Math.PI / 180.0f)) * BOB_AMPLITUDE;

        poseStack.pushPose();
        poseStack.translate(x, y, z);
        poseStack.mulPose(cameraOrientation);
        poseStack.mulPose(Axis.ZP.rotationDegrees(roll + rollSpeed * t));

        PoseStack.Pose pose = poseStack.last();
        Matrix4f positionMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        VertexConsumer consumer = buffer.getBuffer(fogRenderType(texture));
        buildQuad(consumer, positionMatrix, normalMatrix, size, packedLight, r, g, b, a);

        poseStack.popPose();
    }

    private static float fadeAlpha(int tickCount, float partialTicks) {
        float t = tickCount + partialTicks;
        float fadeOutStart = HazardSplash.DURATION - FADE_OUT_TICKS;
        float fadeIn = Mth.clamp(t / FADE_IN_TICKS, 0.0f, 1.0f);
        float fadeOut = t > fadeOutStart ? 1.0f - Mth.clamp((t - fadeOutStart) / FADE_OUT_TICKS, 0.0f, 1.0f) : 1.0f;
        return fadeIn * fadeOut;
    }

    private static void buildQuad(VertexConsumer consumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float size, int packedLight, float r, float g, float b, float a) {
        float half = size * 0.5f;
        quadVertex(consumer, positionMatrix, normalMatrix, -half, -half, 0.0f, 0.0f, packedLight, r, g, b, a);
        quadVertex(consumer, positionMatrix, normalMatrix, half, -half, 1.0f, 0.0f, packedLight, r, g, b, a);
        quadVertex(consumer, positionMatrix, normalMatrix, half, half, 1.0f, 1.0f, packedLight, r, g, b, a);
        quadVertex(consumer, positionMatrix, normalMatrix, -half, half, 0.0f, 1.0f, packedLight, r, g, b, a);
    }

    private static void quadVertex(VertexConsumer consumer, Matrix4f positionMatrix, Matrix3f normalMatrix, float x, float y, float u, float v, int packedLight, float r, float g, float b, float a) {
        consumer.vertex(positionMatrix, x, y, 0.0f)
                .color(r, g, b, a)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(normalMatrix, 0.0f, 0.0f, 1.0f)
                .endVertex();
    }

    private static float signedUnit(float unit) {
        return unit * 2.0f - 1.0f;
    }

    private static float stableRandom(UUID uuid, int index, int salt) {
        long h = uuid.getMostSignificantBits() ^ uuid.getLeastSignificantBits()
                ^ (index * 0x9E3779B97F4A7C15L) ^ (salt * 0xC2B2AE3D27D4EB4FL);
        h ^= (h >>> 33);
        h *= 0xff51afd7ed558ccdL;
        h ^= (h >>> 33);
        h *= 0xc4ceb9fe1a85ec53L;
        h ^= (h >>> 33);
        return (float) ((h >>> 11) * 0x1.0p-53);
    }
}
