package net.offkung.bhspells.client.event;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import org.joml.Vector3f;

/**
 * Client-only visual event handler for entities afflicted with the Shocking status effect.
 * Renders short crawling electrical arcs between body points (head, shoulder, arm, chest, leg, ground)
 * during periodic pulse windows, accompanied by sparks and electric pops.
 */
@Mod.EventBusSubscriber(value = Dist.CLIENT, bus = Mod.EventBusSubscriber.Bus.FORGE, modid = BHSpells.MODID)
public final class ShockingEffectClientEvents {
    private static final ResourceLocation WHITE_TEXTURE =
            ResourceLocation.fromNamespaceAndPath("minecraft", "textures/misc/white.png");
    private static final RenderType SHOCK_RENDER_TYPE =
            RenderType.entityTranslucentEmissive(WHITE_TEXTURE, false);

    private static final double MAX_RENDER_DISTANCE_SQR = 48.0D * 48.0D;
    private static final double REDUCED_DETAIL_DISTANCE_SQR = 24.0D * 24.0D;

    private static final int PULSE_CYCLE_TICKS = 24; // 1.2 seconds per cycle
    private static final int PULSE_ACTIVE_TICKS = 7;  // 7 ticks active crawling, 17 ticks quiet

    private static final int[] COLOR_WHITE = new int[]{250, 255, 245};
    private static final int[] COLOR_EMERALD = new int[]{20, 242, 89};
    private static final int[] COLOR_CYAN = new int[]{13, 242, 217};
    private static final int[] COLOR_LIME = new int[]{128, 255, 46};

    private static final DustParticleOptions GREEN_DUST =
            new DustParticleOptions(new Vector3f(0.15F, 1.0F, 0.35F), 0.85F);

    private ShockingEffectClientEvents() {
    }

    @SubscribeEvent
    public static void onRenderLivingPost(RenderLivingEvent.Post<?, ?> event) {
        LivingEntity entity = event.getEntity();
        if (entity == null || !entity.isAlive() || entity.isRemoved()) {
            return;
        }

        if (!entity.hasEffect(MobEffectsRegistry.SHOCKING.get())) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        Camera camera = minecraft.gameRenderer.getMainCamera();
        double distanceSqr = camera.getPosition().distanceToSqr(entity.position());
        if (distanceSqr > MAX_RENDER_DISTANCE_SQR) {
            return;
        }

        int pulseTick = (entity.getId() * 11 + entity.tickCount) % PULSE_CYCLE_TICKS;
        if (pulseTick >= PULSE_ACTIVE_TICKS) {
            return; // Quiet interval between electrical pulses
        }

        float partialTick = event.getPartialTick();
        float pulseProgress = (pulseTick + partialTick) / (float) PULSE_ACTIVE_TICKS;
        float pulseFade = Mth.sin((float) Math.PI * pulseProgress);
        if (pulseFade <= 0.01F) {
            return;
        }

        PoseStack poseStack = event.getPoseStack();
        MultiBufferSource bufferSource = event.getMultiBufferSource();
        VertexConsumer consumer = bufferSource.getBuffer(SHOCK_RENDER_TYPE);

        boolean reducedDetail = distanceSqr > REDUCED_DETAIL_DISTANCE_SQR;
        renderTargetShockArcs(poseStack, consumer, entity, pulseFade, pulseTick, reducedDetail);
    }

    private static void renderTargetShockArcs(
            PoseStack poseStack, VertexConsumer consumer, LivingEntity entity,
            float pulseFade, int pulseTick, boolean reducedDetail) {

        float w = Math.max(0.35F, entity.getBbWidth());
        float h = Math.max(0.60F, entity.getBbHeight());

        // Body anchor points scaled to entity bounding box
        Vector3f head = new Vector3f(0.0F, h * 0.90F, 0.0F);
        Vector3f leftShoulder = new Vector3f(-w * 0.40F, h * 0.72F, 0.0F);
        Vector3f rightShoulder = new Vector3f(w * 0.40F, h * 0.72F, 0.0F);
        Vector3f chest = new Vector3f(0.0F, h * 0.55F, w * 0.16F);
        Vector3f leftArm = new Vector3f(-w * 0.55F, h * 0.42F, 0.0F);
        Vector3f rightArm = new Vector3f(w * 0.55F, h * 0.42F, 0.0F);
        Vector3f leftLeg = new Vector3f(-w * 0.22F, h * 0.20F, 0.0F);
        Vector3f rightLeg = new Vector3f(w * 0.22F, h * 0.20F, 0.0F);
        Vector3f ground = new Vector3f(0.0F, 0.02F, 0.0F);

        // Candidate crawling arc pairs
        Vector3f[][] pairs = new Vector3f[][]{
                {leftShoulder, chest},
                {chest, rightArm},
                {rightShoulder, chest},
                {chest, leftArm},
                {head, leftShoulder},
                {head, rightShoulder},
                {leftLeg, ground},
                {rightLeg, ground},
                {leftArm, ground}
        };

        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();

        int arcsToDraw = reducedDetail ? 2 : 4;
        long seed = (long) entity.getId() * 31L + (pulseTick / 2);
        RandomSource rand = RandomSource.create(seed);

        for (int a = 0; a < arcsToDraw; a++) {
            int pairIdx = (a * 2 + pulseTick + entity.getId()) % pairs.length;
            Vector3f p1 = pairs[pairIdx][0];
            Vector3f p2 = pairs[pairIdx][1];

            drawCrawlingArc(matrix, normal, consumer, p1, p2, rand, w, pulseFade, a);
        }
    }

    private static void drawCrawlingArc(
            Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
            Vector3f start, Vector3f end, RandomSource rand,
            float entityWidth, float pulseFade, int arcIndex) {

        int segments = 4;
        Vector3f prev = new Vector3f(start);
        int[] glowColor = arcIndex % 2 == 0 ? COLOR_EMERALD : COLOR_CYAN;
        int glowAlpha = Math.max(0, Math.min(255, Math.round(180 * pulseFade)));
        int coreAlpha = Math.max(0, Math.min(255, Math.round(250 * pulseFade)));

        for (int i = 1; i <= segments; i++) {
            float t = (float) i / segments;
            Vector3f next = new Vector3f(start).lerp(end, t);

            if (i < segments) {
                float envelope = Mth.sin((float) Math.PI * t);
                float jitter = entityWidth * 0.18F * envelope;
                next.add(
                        (rand.nextFloat() - 0.5F) * jitter,
                        (rand.nextFloat() - 0.5F) * jitter,
                        (rand.nextFloat() - 0.5F) * jitter
                );
            }

            // Glow layer (wider, Emerald/Cyan)
            drawSegmentRibbon(matrix, normal, consumer, prev, next, 0.038F * entityWidth, glowColor, glowAlpha);
            // Core layer (thin, White)
            drawSegmentRibbon(matrix, normal, consumer, prev, next, 0.012F * entityWidth, COLOR_WHITE, coreAlpha);

            prev = next;
        }
    }

    private static void drawSegmentRibbon(
            Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
            Vector3f p1, Vector3f p2, float halfWidth, int[] color, int alpha) {

        Vector3f dir = new Vector3f(p2).sub(p1);
        float lenSq = dir.lengthSquared();
        if (lenSq < 0.00001F) return;
        dir.normalize();

        Vector3f ref = Math.abs(dir.y()) > 0.9F ? new Vector3f(1.0F, 0.0F, 0.0F) : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f side1 = new Vector3f(dir).cross(ref).normalize().mul(halfWidth);
        Vector3f side2 = new Vector3f(dir).cross(side1).normalize().mul(halfWidth);

        // Quad 1: side1
        drawQuadFacePair(matrix, normal, consumer, p1, p2, side1, color, alpha);
        // Quad 2: side2 (perpendicular crossed face for 3D visibility from any camera angle)
        drawQuadFacePair(matrix, normal, consumer, p1, p2, side2, color, alpha);
    }

    private static void drawQuadFacePair(
            Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
            Vector3f p1, Vector3f p2, Vector3f side, int[] color, int alpha) {

        float x1 = p1.x() - side.x(), y1 = p1.y() - side.y(), z1 = p1.z() - side.z();
        float x2 = p1.x() + side.x(), y2 = p1.y() + side.y(), z2 = p1.z() + side.z();
        float x3 = p2.x() + side.x(), y2_2 = p2.y() + side.y(), z3 = p2.z() + side.z();
        float x4 = p2.x() - side.x(), y4 = p2.y() - side.y(), z4 = p2.z() - side.z();

        // Front face
        vertex(matrix, normal, consumer, x1, y1, z1, 0.0F, 0.0F, color, alpha);
        vertex(matrix, normal, consumer, x2, y2, z2, 1.0F, 0.0F, color, alpha);
        vertex(matrix, normal, consumer, x3, y2_2, z3, 1.0F, 1.0F, color, alpha);
        vertex(matrix, normal, consumer, x4, y4, z4, 0.0F, 1.0F, color, alpha);

        // Back face
        vertex(matrix, normal, consumer, x4, y4, z4, 0.0F, 1.0F, color, alpha);
        vertex(matrix, normal, consumer, x3, y2_2, z3, 1.0F, 1.0F, color, alpha);
        vertex(matrix, normal, consumer, x2, y2, z2, 1.0F, 0.0F, color, alpha);
        vertex(matrix, normal, consumer, x1, y1, z1, 0.0F, 0.0F, color, alpha);
    }

    private static void vertex(
            Matrix4f matrix, Matrix3f normal, VertexConsumer consumer,
            float x, float y, float z, float u, float v, int[] color, int alpha) {
        consumer.vertex(matrix, x, y, z)
                .color(color[0], color[1], color[2], alpha)
                .uv(u, v)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(LightTexture.FULL_BRIGHT)
                .normal(normal, 0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @SubscribeEvent
    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        Minecraft minecraft = Minecraft.getInstance();
        ClientLevel level = minecraft.level;
        if (level == null || minecraft.isPaused()) {
            return;
        }

        Camera camera = minecraft.gameRenderer.getMainCamera();
        for (Entity candidate : level.entitiesForRendering()) {
            if (candidate instanceof LivingEntity living
                    && living.isAlive()
                    && !living.isRemoved()
                    && living.hasEffect(MobEffectsRegistry.SHOCKING.get())) {

                double distSqr = camera.getPosition().distanceToSqr(living.position());
                if (distSqr > MAX_RENDER_DISTANCE_SQR) {
                    continue;
                }

                int pulseTick = (living.getId() * 11 + living.tickCount) % PULSE_CYCLE_TICKS;

                // Subtle electric pop sound at start of pulse (every-tick check ensures no skipped sound)
                if (pulseTick == 1 && distSqr < 16.0D * 16.0D) {
                    level.playLocalSound(
                            living.getX(), living.getY() + living.getBbHeight() * 0.5D, living.getZ(),
                            SoundEvents.LIGHTNING_BOLT_IMPACT, SoundSource.PLAYERS,
                            0.12F, 1.95F + living.getRandom().nextFloat() * 0.1F, false
                    );
                }

                // Sparks and green dust during active crawling window (throttled every 2 ticks)
                if (pulseTick < PULSE_ACTIVE_TICKS && living.tickCount % 2 == 0) {
                    double ox = (living.getRandom().nextDouble() - 0.5D) * living.getBbWidth() * 0.8D;
                    double oy = living.getRandom().nextDouble() * living.getBbHeight();
                    double oz = (living.getRandom().nextDouble() - 0.5D) * living.getBbWidth() * 0.8D;

                    level.addParticle(
                            living.getRandom().nextBoolean() ? ParticleTypes.ELECTRIC_SPARK : GREEN_DUST,
                            living.getX() + ox, living.getY() + oy, living.getZ() + oz,
                            ox * 0.05D, 0.02D, oz * 0.05D
                    );
                }
            }
        }
    }
}
