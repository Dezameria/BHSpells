package net.offkung.bhspells.client.particle;

import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import org.joml.Vector3f;

/**
 * Pure geometric and algorithmic renderer for the "Emerald Arc Discharge / Unstable Magical Electricity" VFX.
 * Generates segmented jagged lightning, multi-layered cores, branching arcs, hand origin energy core,
 * fragmented shock rings, travelling pulses, radial impact bursts, and residual electrical arcs.
 */
public final class ShockingLightningGeometry {
    public static final int FULL_BRIGHT = 15728880;
    public static final float TAU = (float) (Math.PI * 2.0D);

    // Color Palette Constants
    public static final float WHITE_R = 0.98F, WHITE_G = 1.00F, WHITE_B = 0.96F;
    public static final float LIME_R = 0.50F, LIME_G = 1.00F, LIME_B = 0.18F;
    public static final float EMERALD_R = 0.08F, EMERALD_G = 0.95F, EMERALD_B = 0.35F;
    public static final float CYAN_R = 0.05F, CYAN_G = 0.95F, CYAN_B = 0.85F;

    private ShockingLightningGeometry() {
    }

    /**
     * Renders the complete Emerald Arc Discharge sequence.
     */
    public static void renderDischarge(
            VertexConsumer consumer,
            float u0, float v0, float u1, float v1,
            Vector3f start, Vector3f end,
            long visualSeed, float age, float partialTick,
            float scale, int lifetime, boolean reducedDetail) {

        Vector3f axis = new Vector3f(end).sub(start);
        float length = axis.length();
        if (length < 0.001F) {
            return;
        }

        Vector3f direction = new Vector3f(axis).normalize();
        Vector3f reference = Math.abs(direction.y()) > 0.92F
                ? new Vector3f(1.0F, 0.0F, 0.0F)
                : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f right = new Vector3f(direction).cross(reference).normalize();
        Vector3f up = new Vector3f(right).cross(direction).normalize();

        float time = age + partialTick;
        float progress = Mth.clamp(time / lifetime, 0.0F, 1.0F);
        float globalFade = 1.0F - smoothstep(progress);

        // 4 discrete states A, B, C, D cycling rapidly over time (not per frame noise)
        int state = ((int) (age * 1.5F)) % 4;
        long stateSeed = visualSeed ^ (0x9E3779B97F4A7C15L * (state + 1));
        RandomSource stateRand = RandomSource.create(stateSeed);

        // Surge pulse travelling along the arc from origin to impact
        float surgeProgress = easeOutCubic(Mth.clamp(time / 4.2F, 0.0F, 1.0F));

        int mainSegments = reducedDetail ? 12 : 18;

        // 1. Generate main jagged arc vertices
        Vector3f[] mainPoints = generateMainArcPoints(start, end, direction, right, up,
                mainSegments, stateRand, scale);

        // 2. Render Hand Origin Energy Core & Muzzle Shock Rings
        renderHandOrigin(consumer, u0, v0, u1, v1, start, direction, right, up,
                time, globalFade, scale, visualSeed, state, reducedDetail);

        // 3. Render 3-Layer Main Jagged Arc with Travelling Pulse
        renderMainArcLayers(consumer, u0, v0, u1, v1, mainPoints, right, up,
                surgeProgress, globalFade, scale, state);

        // 4. Render Branching Lightning Arcs
        renderBranches(consumer, u0, v0, u1, v1, mainPoints, direction, right, up,
                age, surgeProgress, globalFade, scale, visualSeed, state, reducedDetail);

        // 5. Render Impact Effects (Flash, Shock Ring, Radial Arcs, Residual Arcs)
        renderImpact(consumer, u0, v0, u1, v1, end, direction, right, up,
                time, surgeProgress, globalFade, scale, visualSeed, state, reducedDetail);
    }

    /**
     * Generates segmented jagged lightning path with non-linear displacement envelope.
     */
    private static Vector3f[] generateMainArcPoints(
            Vector3f start, Vector3f end, Vector3f dir, Vector3f right, Vector3f up,
            int segments, RandomSource rand, float scale) {

        Vector3f[] points = new Vector3f[segments + 1];
        points[0] = new Vector3f(start);
        points[segments] = new Vector3f(end);

        for (int i = 1; i < segments; i++) {
            float t = (float) i / segments;
            Vector3f base = new Vector3f(start).lerp(end, t);

            // Envelope: 0 at start and end, maximum in the middle
            float envelope = Mth.sin((float) Math.PI * t);

            // Jagged displacement in 3D
            float jitterR = (rand.nextFloat() - 0.5F) * 0.55F * scale * envelope;
            float jitterU = (rand.nextFloat() - 0.5F) * 0.55F * scale * envelope;
            float jitterD = (rand.nextFloat() - 0.5F) * 0.15F * scale * envelope;

            base.fma(jitterR, right)
                .fma(jitterU, up)
                .fma(jitterD, dir);

            points[i] = base;
        }

        return points;
    }

    /**
     * Hand Energy Core:
     * High-brightness core (white center, lime rim, emerald outer glow)
     * Swirling small arcs, fragmented distorted shock rings, and expanding discharge ring.
     */
    private static void renderHandOrigin(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f origin, Vector3f dir, Vector3f right, Vector3f up,
            float time, float globalFade, float scale, long seed, int state, boolean reducedDetail) {

        float flashIntensity = Mth.clamp(1.4F - time * 0.22F, 0.0F, 1.0F) * globalFade;
        if (flashIntensity <= 0.001F) {
            return;
        }

        // Layer 1: White Hot Core Center (Crossed billboards)
        drawCrossBillboard(consumer, u0, v0, u1, v1, origin, dir, right, up,
                0.09F * scale, WHITE_R, WHITE_G, WHITE_B, 0.98F * flashIntensity);

        // Layer 2: Lime-Green Rim
        drawCrossBillboard(consumer, u0, v0, u1, v1, origin, dir, right, up,
                0.18F * scale, LIME_R, LIME_G, LIME_B, 0.80F * flashIntensity);

        // Layer 3: Emerald Green Outer Glow
        drawCrossBillboard(consumer, u0, v0, u1, v1, origin, dir, right, up,
                0.32F * scale, EMERALD_R, EMERALD_G, EMERALD_B, 0.55F * flashIntensity);

        // Transparent Fragmented & Distorted Shock Rings around origin
        int ringCount = reducedDetail ? 1 : 3;
        for (int ring = 0; ring < ringCount; ring++) {
            float ringRadius = (0.16F + ring * 0.10F) * scale;
            float tiltPhase = (seed ^ (ring * 1024L)) % 100 / 100.0F * TAU + time * 0.4F;
            Vector3f ringRight = new Vector3f(right).mul(Mth.cos(tiltPhase)).add(new Vector3f(up).mul(Mth.sin(tiltPhase)));
            Vector3f ringUp = new Vector3f(dir).cross(ringRight).normalize();

            drawFragmentedRing(consumer, u0, v0, u1, v1, origin, ringRight, ringUp,
                    ringRadius, 0.008F * scale, EMERALD_R, EMERALD_G, CYAN_B,
                    0.50F * flashIntensity, ring + state);
        }

        // Rapidly expanding muzzle shock ring upon discharge
        float muzzleExpand = easeOutCubic(Mth.clamp(time / 2.8F, 0.0F, 1.0F));
        float muzzleFade = flashIntensity * (1.0F - smoothstep(muzzleExpand));
        if (muzzleFade > 0.01F) {
            float expandRadius = (0.15F + muzzleExpand * 0.65F) * scale;
            Vector3f muzzleCenter = new Vector3f(origin).fma(muzzleExpand * 0.4F, dir);
            drawRing(consumer, u0, v0, u1, v1, muzzleCenter, right, up,
                    expandRadius, 0.011F * scale, CYAN_R, CYAN_G, CYAN_B, 0.70F * muzzleFade, 16);
        }

        // Swirling small lightning arcs around origin
        if (!reducedDetail) {
            for (int swirl = 0; swirl < 2; swirl++) {
                float swirlAngle = time * 1.8F + swirl * (float) Math.PI;
                Vector3f swirlP1 = new Vector3f(origin).fma(Mth.cos(swirlAngle) * 0.20F * scale, right)
                        .fma(Mth.sin(swirlAngle) * 0.20F * scale, up);
                Vector3f swirlP2 = new Vector3f(origin).fma(Mth.cos(swirlAngle + 1.2F) * 0.14F * scale, right)
                        .fma(Mth.sin(swirlAngle + 1.2F) * 0.14F * scale, up)
                        .fma(0.18F * scale, dir);
                drawTube(consumer, u0, v0, u1, v1, swirlP1, swirlP2, 0.012F * scale,
                        LIME_R, LIME_G, LIME_B, 0.85F * flashIntensity);
            }
        }
    }

    /**
     * Renders the 3 coincident layers of the main arc with dynamic thickness and pulse brightening.
     */
    private static void renderMainArcLayers(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f[] points, Vector3f right, Vector3f up,
            float surgeProgress, float globalFade, float scale, int state) {

        int segCount = points.length - 1;

        for (int i = 0; i < segCount; i++) {
            Vector3f p1 = points[i];
            Vector3f p2 = points[i + 1];

            float t = (i + 0.5F) / segCount;

            // Distance to travelling surge pulse
            float distToPulse = Math.abs(t - surgeProgress);
            float pulseBoost = (float) Math.exp(-distToPulse * distToPulse * 28.0F);

            // Thickness variation along the arc
            float thicknessMod = 0.82F + 0.32F * Mth.sin(i * 1.4F + state * 2.1F) + 0.50F * pulseBoost;

            // Layer 3: Outer Electrical Glow (Cyan / Cyan-Green)
            float glowRadius = 0.10F * scale * thicknessMod;
            float glowAlpha = (0.24F + 0.28F * pulseBoost) * globalFade;
            drawTube(consumer, u0, v0, u1, v1, p1, p2, glowRadius,
                    CYAN_R, CYAN_G, CYAN_B, glowAlpha);

            // Layer 2: Emerald Arc (Main body, Emerald Green)
            float emeraldRadius = 0.042F * scale * thicknessMod;
            float emeraldAlpha = (0.75F + 0.22F * pulseBoost) * globalFade;
            drawTube(consumer, u0, v0, u1, v1, p1, p2, emeraldRadius,
                    EMERALD_R, EMERALD_G, EMERALD_B, emeraldAlpha);

            // Layer 1: White Hot Core (Very thin, White/White-green, brightest)
            float coreRadius = 0.016F * scale * (0.80F + 0.40F * pulseBoost);
            float coreAlpha = (0.92F + 0.08F * pulseBoost) * globalFade;
            drawTube(consumer, u0, v0, u1, v1, p1, p2, coreRadius,
                    WHITE_R, WHITE_G, WHITE_B, coreAlpha);
        }
    }

    /**
     * Renders branching arcs that fork off the main arc.
     * Branches have short seeded 2-5 tick visibility windows, random lengths, and sub-branches.
     */
    private static void renderBranches(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f[] mainPoints, Vector3f dir, Vector3f right, Vector3f up,
            float age, float surgeProgress, float globalFade, float scale,
            long seed, int state, boolean reducedDetail) {

        int segCount = mainPoints.length - 1;
        int branchCandidates = reducedDetail ? 3 : 7;
        int step = segCount / (branchCandidates + 1);

        for (int b = 0; b < branchCandidates; b++) {
            int anchorIdx = (b + 1) * step;
            if (anchorIdx >= mainPoints.length - 1) break;

            float t = (float) anchorIdx / segCount;
            float distToPulse = Math.abs(t - surgeProgress);
            boolean nearPulse = distToPulse < 0.16F;

            // Seeded 2 to 5 tick visibility window
            RandomSource branchSeedRand = RandomSource.create(seed ^ (0xB5A4F793D21109L * (b + 1)));
            int branchStartTick = (int) (Math.abs(branchSeedRand.nextLong()) % 6);
            int branchDuration = 2 + (int) (Math.abs(branchSeedRand.nextLong()) % 4); // 2 to 5 ticks
            boolean activeInWindow = (age >= branchStartTick && age <= (branchStartTick + branchDuration));

            if (!nearPulse && !activeInWindow) {
                continue;
            }

            Vector3f branchStart = mainPoints[anchorIdx];
            RandomSource branchRand = RandomSource.create(seed ^ (0xB5A4F793D21109L * (b + 1) + state));

            // Branch direction angled away from main direction
            float branchAngle = branchRand.nextFloat() * TAU;
            float branchSpread = 0.35F + branchRand.nextFloat() * 0.45F;
            Vector3f branchDir = new Vector3f(dir).mul(0.65F)
                    .fma(Mth.cos(branchAngle) * branchSpread, right)
                    .fma(Mth.sin(branchAngle) * branchSpread, up)
                    .normalize();

            float branchLength = (0.65F + branchRand.nextFloat() * 1.10F) * scale;
            int branchSegments = 3 + branchRand.nextInt(3);

            Vector3f current = new Vector3f(branchStart);
            Vector3f subForkPoint = null;
            Vector3f subForkDir = null;

            for (int s = 1; s <= branchSegments; s++) {
                float segT = (float) s / branchSegments;
                Vector3f next = new Vector3f(branchStart).fma(segT * branchLength, branchDir);

                // Add lateral jitter
                float jitter = (branchRand.nextFloat() - 0.5F) * 0.22F * scale;
                next.fma(jitter, right).fma(jitter, up);

                // Draw branch tube (Emerald / Cyan-Green, White-tipped)
                float branchRadius = 0.015F * scale * (1.0F - segT * 0.5F);
                float bR = segT > 0.8F ? WHITE_R : (b % 2 == 0 ? EMERALD_R : CYAN_R);
                float bG = segT > 0.8F ? WHITE_G : (b % 2 == 0 ? EMERALD_G : CYAN_G);
                float bB = segT > 0.8F ? WHITE_B : (b % 2 == 0 ? EMERALD_B : CYAN_B);
                float bAlpha = 0.75F * globalFade;

                drawTube(consumer, u0, v0, u1, v1, current, next, branchRadius, bR, bG, bB, bAlpha);

                if (s == 2) {
                    subForkPoint = new Vector3f(current);
                    subForkDir = new Vector3f(branchDir)
                            .fma((branchRand.nextFloat() - 0.5F) * 0.8F, right)
                            .fma((branchRand.nextFloat() - 0.5F) * 0.8F, up)
                            .normalize();
                }

                current = next;
            }

            // Draw sub-branch if available (within same active window)
            if (subForkPoint != null && subForkDir != null && !reducedDetail) {
                Vector3f subNext = new Vector3f(subForkPoint).fma(0.45F * scale, subForkDir);
                drawTube(consumer, u0, v0, u1, v1, subForkPoint, subNext, 0.009F * scale,
                        CYAN_R, CYAN_G, CYAN_B, 0.65F * globalFade);
            }
        }
    }

    /**
     * Renders the Impact VFX at the end position:
     * White-green flash, 3D spherical radial lightning arcs, electrical explosion shock ring, and residual arcs.
     */
    private static void renderImpact(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f impactPos, Vector3f dir, Vector3f right, Vector3f up,
            float time, float surgeProgress, float globalFade, float scale,
            long seed, int state, boolean reducedDetail) {

        if (surgeProgress < 0.65F) {
            return;
        }

        float impactReveal = smoothstep(Mth.clamp((surgeProgress - 0.65F) / 0.35F, 0.0F, 1.0F));
        float impactFade = globalFade * impactReveal;
        if (impactFade <= 0.001F) {
            return;
        }

        // Offset impact visual slightly back along direction to prevent z-fighting with block face
        Vector3f impactCenter = new Vector3f(impactPos).fma(-0.04F * scale, dir);

        // 1. White-Green Flash Center (Crossed billboards)
        drawCrossBillboard(consumer, u0, v0, u1, v1, impactCenter, dir, right, up,
                0.22F * scale, WHITE_R, WHITE_G, WHITE_B, 0.95F * impactFade);
        drawCrossBillboard(consumer, u0, v0, u1, v1, impactCenter, dir, right, up,
                0.40F * scale, EMERALD_R, EMERALD_G, EMERALD_B, 0.70F * impactFade);

        // 2. Shock Ring expanding on impact plane
        float ringProgress = Mth.clamp((time - 2.5F) / 4.0F, 0.0F, 1.0F);
        if (ringProgress > 0.0F) {
            float ringRadius = (0.20F + ringProgress * 0.95F) * scale;
            float ringAlpha = (1.0F - smoothstep(ringProgress)) * impactFade;
            drawRing(consumer, u0, v0, u1, v1, impactCenter, right, up,
                    ringRadius, 0.012F * scale, CYAN_R, CYAN_G, CYAN_B, 0.78F * ringAlpha, 16);
        }

        // 3. 3D Spherical Radial Lightning Arcs bursting from impact point
        int radialArcCount = reducedDetail ? 4 : 7;
        RandomSource impactRand = RandomSource.create(seed ^ (0x8F512C41D831L + state));
        for (int r = 0; r < radialArcCount; r++) {
            // Uniform sampling on 3D sphere
            float u = impactRand.nextFloat() * 2.0F - 1.0F; // cos(phi) from -1 to 1
            float theta = impactRand.nextFloat() * TAU;
            float sinPhi = (float) Math.sqrt(Math.max(0.0F, 1.0F - u * u));
            Vector3f arcDir = new Vector3f(
                    sinPhi * (float) Math.cos(theta),
                    u,
                    sinPhi * (float) Math.sin(theta)
            ).normalize();

            // If pointing back into wall, reflect forward into world
            if (arcDir.dot(dir) < -0.2F) {
                arcDir.fma(-1.3F * arcDir.dot(dir), dir).normalize();
            }

            float arcLength = (0.35F + impactRand.nextFloat() * 0.65F) * scale;
            Vector3f p1 = new Vector3f(impactCenter);
            Vector3f pMid = new Vector3f(impactCenter).fma(arcLength * 0.5F, arcDir)
                    .fma((impactRand.nextFloat() - 0.5F) * 0.18F * scale, right);
            Vector3f p2 = new Vector3f(impactCenter).fma(arcLength, arcDir);

            float arcAlpha = 0.80F * impactFade;
            drawTube(consumer, u0, v0, u1, v1, p1, pMid, 0.014F * scale, WHITE_R, WHITE_G, WHITE_B, arcAlpha);
            drawTube(consumer, u0, v0, u1, v1, pMid, p2, 0.010F * scale, EMERALD_R, EMERALD_G, EMERALD_B, arcAlpha);
        }

        // 4. Residual Arcs: twitching short arcs near impact in later ticks (time >= 4.0F)
        if (time >= 4.0F) {
            float residualFade = globalFade * (1.0F - smoothstep(Mth.clamp((time - 4.0F) / 6.0F, 0.0F, 1.0F)));
            int resCount = reducedDetail ? 2 : 3;
            for (int res = 0; res < resCount; res++) {
                float resAngle = (seed + res * 137L + state * 47L) % 100 / 100.0F * TAU;
                Vector3f resP1 = new Vector3f(impactCenter)
                        .fma(Mth.cos(resAngle) * 0.15F * scale, right)
                        .fma(Mth.sin(resAngle) * 0.15F * scale, up);
                Vector3f resP2 = new Vector3f(impactCenter)
                        .fma(Mth.cos(resAngle + 1.1F) * 0.35F * scale, right)
                        .fma(Mth.sin(resAngle + 1.1F) * 0.35F * scale, up)
                        .fma((res % 2 == 0 ? 0.15F : -0.15F) * scale, dir);
                drawTube(consumer, u0, v0, u1, v1, resP1, resP2, 0.009F * scale,
                        LIME_R, LIME_G, LIME_B, 0.85F * residualFade);
            }
        }
    }

    /**
     * Renders a fragmented ring with gaps and distortion to convey an unstable magnetic field.
     */
    private static void drawFragmentedRing(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f center, Vector3f right, Vector3f up,
            float radius, float tubeRadius,
            float red, float green, float blue, float alpha, int state) {

        int steps = 14;
        for (int i = 0; i < steps; i++) {
            // Create intentional fragmentation gaps
            if ((i + state) % 4 == 0) {
                continue;
            }

            float angle1 = i * TAU / steps;
            float angle2 = (i + 1) * TAU / steps;

            // Distortion / jitter on radius
            float r1 = radius * (0.92F + 0.16F * Mth.sin(i * 1.7F + state));
            float r2 = radius * (0.92F + 0.16F * Mth.sin((i + 1) * 1.7F + state));

            Vector3f p1 = new Vector3f(center).fma(Mth.cos(angle1) * r1, right).fma(Mth.sin(angle1) * r1, up);
            Vector3f p2 = new Vector3f(center).fma(Mth.cos(angle2) * r2, right).fma(Mth.sin(angle2) * r2, up);

            drawTube(consumer, u0, v0, u1, v1, p1, p2, tubeRadius, red, green, blue, alpha);
        }
    }

    /**
     * Renders a smooth circular ring.
     */
    private static void drawRing(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f center, Vector3f right, Vector3f up,
            float radius, float tubeRadius,
            float red, float green, float blue, float alpha, int steps) {

        for (int i = 0; i < steps; i++) {
            float angle1 = i * TAU / steps;
            float angle2 = (i + 1) * TAU / steps;

            Vector3f p1 = new Vector3f(center).fma(Mth.cos(angle1) * radius, right).fma(Mth.sin(angle1) * radius, up);
            Vector3f p2 = new Vector3f(center).fma(Mth.cos(angle2) * radius, right).fma(Mth.sin(angle2) * radius, up);

            drawTube(consumer, u0, v0, u1, v1, p1, p2, tubeRadius, red, green, blue, alpha);
        }
    }

    /**
     * Renders a true crossed billboard at the given point (2 perpendicular intersecting quads).
     * Remains visible from any camera orientation.
     */
    private static void drawCrossBillboard(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f center, Vector3f dir, Vector3f right, Vector3f up,
            float radius, float red, float green, float blue, float alpha) {

        Vector3f r = new Vector3f(right).mul(radius);
        Vector3f u = new Vector3f(up).mul(radius);
        Vector3f d = new Vector3f(dir).mul(radius);

        // Quad 1: (Right, Up) plane
        Vector3f q1_p1 = new Vector3f(center).add(r).add(u);
        Vector3f q1_p2 = new Vector3f(center).sub(r).add(u);
        Vector3f q1_p3 = new Vector3f(center).sub(r).sub(u);
        Vector3f q1_p4 = new Vector3f(center).add(r).sub(u);
        makeQuad(consumer, u0, v0, u1, v1, q1_p1, q1_p2, q1_p3, q1_p4, red, green, blue, alpha);

        // Quad 2: (Dir, Up) plane (perpendicular to Quad 1)
        Vector3f q2_p1 = new Vector3f(center).add(d).add(u);
        Vector3f q2_p2 = new Vector3f(center).sub(d).add(u);
        Vector3f q2_p3 = new Vector3f(center).sub(d).sub(u);
        Vector3f q2_p4 = new Vector3f(center).add(d).sub(u);
        makeQuad(consumer, u0, v0, u1, v1, q2_p1, q2_p2, q2_p3, q2_p4, red, green, blue, alpha);
    }

    /**
     * Renders a 3D 4-sided tube between two points.
     */
    public static void drawTube(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f first, Vector3f second, float radius,
            float red, float green, float blue, float alpha) {

        if (alpha <= 0.001F || radius <= 0.0001F) {
            return;
        }

        Vector3f direction = new Vector3f(second).sub(first);
        float lenSq = direction.lengthSquared();
        if (lenSq < 0.00001F) {
            return;
        }
        direction.normalize();

        Vector3f reference = Math.abs(direction.y()) > 0.92F
                ? new Vector3f(1.0F, 0.0F, 0.0F)
                : new Vector3f(0.0F, 1.0F, 0.0F);
        Vector3f right = new Vector3f(direction).cross(reference).normalize().mul(radius);
        Vector3f up = new Vector3f(right).cross(direction).normalize().mul(radius);

        Vector3f c1 = new Vector3f(first).add(right).add(up);
        Vector3f c2 = new Vector3f(first).sub(right).add(up);
        Vector3f c3 = new Vector3f(first).sub(right).sub(up);
        Vector3f c4 = new Vector3f(first).add(right).sub(up);

        Vector3f c5 = new Vector3f(second).add(right).add(up);
        Vector3f c6 = new Vector3f(second).sub(right).add(up);
        Vector3f c7 = new Vector3f(second).sub(right).sub(up);
        Vector3f c8 = new Vector3f(second).add(right).sub(up);

        makeQuad(consumer, u0, v0, u1, v1, c1, c2, c6, c5, red, green, blue, alpha);
        makeQuad(consumer, u0, v0, u1, v1, c2, c3, c7, c6, red, green, blue, alpha);
        makeQuad(consumer, u0, v0, u1, v1, c3, c4, c8, c7, red, green, blue, alpha);
        makeQuad(consumer, u0, v0, u1, v1, c4, c1, c5, c8, red, green, blue, alpha);
    }

    private static void makeQuad(
            VertexConsumer consumer, float u0, float v0, float u1, float v1,
            Vector3f p1, Vector3f p2, Vector3f p3, Vector3f p4,
            float r, float g, float b, float a) {

        consumer.vertex(p1.x(), p1.y(), p1.z()).uv(u0, v0).color(r, g, b, a).uv2(FULL_BRIGHT).endVertex();
        consumer.vertex(p2.x(), p2.y(), p2.z()).uv(u1, v0).color(r, g, b, a).uv2(FULL_BRIGHT).endVertex();
        consumer.vertex(p3.x(), p3.y(), p3.z()).uv(u1, v1).color(r, g, b, a).uv2(FULL_BRIGHT).endVertex();
        consumer.vertex(p4.x(), p4.y(), p4.z()).uv(u0, v1).color(r, g, b, a).uv2(FULL_BRIGHT).endVertex();
    }

    public static float smoothstep(float value) {
        return value * value * (3.0F - 2.0F * value);
    }

    public static float easeOutCubic(float value) {
        float inv = 1.0F - value;
        return 1.0F - inv * inv * inv;
    }
}
