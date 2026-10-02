package net.offkung.bhspells.pressure.client;

import net.offkung.bhspells.pressure.PressureFieldData;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Client runtime model for an active pressure field or torrential domain.
 * Handles position interpolation, persistent domain lifecycle, fade-in/fade-out lerp, and procedural streak generation.
 */
public class ClientPressureField {
    private final PressureFieldData data;
    private int clientElapsedTicks = 0;
    private boolean isEnding = false;
    private int endingTicks = 0;
    private static final int FADE_IN_TICKS = 20;
    private static final int FADE_OUT_TICKS = 25;

    private Vec3 lastKnownCenter = Vec3.ZERO;
    private final List<PressureStreak> streaks = new ArrayList<>();

    public ClientPressureField(PressureFieldData data) {
        this.data = data;
        if (!data.anchor().isFollowing() && data.anchor().getFixedPosition() != null) {
            this.lastKnownCenter = data.anchor().getFixedPosition();
        }
        initStreaks();
    }

    private void initStreaks() {
        Random random = new Random(data.seed());
        int count = data.visualProfile().streakCount();
        float radius = data.visualProfile().cameraCurtain() 
                ? data.visualProfile().curtainRadius() 
                : data.radius();

        for (int i = 0; i < count; i++) {
            // Volumetric distribution: power of 0.65 spreads streaks across the entire 3D volume
            double angle = random.nextDouble() * Math.PI * 2.0;
            double r = Math.pow(random.nextDouble(), 0.65) * radius;
            float rx = (float) (r * Math.cos(angle));
            float rz = (float) (r * Math.sin(angle));

            float phaseOffset = random.nextFloat();
            float baseAlpha = data.visualProfile().baseAlpha() * (0.65F + random.nextFloat() * 0.35F);

            // 3D Volumetric Tiers (เธ—เธฑเนเธงเธเนเธฒเธ”เธดเธ / เธเธฃเธญเธเธเธฅเธธเธกเธ—เธฑเนเธงเธญเธฒเธ“เธฒเน€เธเธ•):
            int tierRoll = i % 10;
            float width;
            float length;
            float speed;
            float verticalSpawnOffset;
            float fallDistance;

            if (tierRoll < 5) {
                // Tier 1: Torrential Deluge Rain Needles (50% of streaks) - Rapid downward deluge
                width = Mth.lerp(random.nextFloat(), data.visualProfile().minWidth(), data.visualProfile().maxWidth() * 0.9F);
                length = Mth.lerp(random.nextFloat(), data.visualProfile().minLength(), data.visualProfile().maxLength() * 0.9F);
                speed = Mth.lerp(random.nextFloat(), data.visualProfile().speedMin() * 1.3F, data.visualProfile().speedMax() * 1.5F);
                verticalSpawnOffset = 20.0F + random.nextFloat() * 28.0F; // 20 to 48 blocks high in the sky
                fallDistance = 30.0F + random.nextFloat() * 24.0F;        // Falls 30 to 54 blocks downwards
            } else if (tierRoll < 8) {
                // Tier 2: Mid-Air Atmospheric Surge (30% of streaks) - Surrounds eye level and mid-altitude
                width = Mth.lerp(random.nextFloat(), data.visualProfile().minWidth() * 1.1F, data.visualProfile().maxWidth() * 1.1F);
                length = Mth.lerp(random.nextFloat(), data.visualProfile().minLength() * 1.2F, data.visualProfile().maxLength() * 1.1F);
                speed = Mth.lerp(random.nextFloat(), data.visualProfile().speedMin(), data.visualProfile().speedMax());
                verticalSpawnOffset = 6.0F + random.nextFloat() * 18.0F;  // 6 to 24 blocks above
                fallDistance = 22.0F + random.nextFloat() * 18.0F;        // Falls 22 to 40 blocks downwards
            } else {
                // Tier 3: Massive Sky-to-Earth Pillars (20% of streaks) - Colossal beams bridging heaven and earth
                width = data.visualProfile().maxWidth() * (1.3F + random.nextFloat() * 0.8F);
                length = data.visualProfile().maxLength() * (1.5F + random.nextFloat() * 1.0F); // 18 to 38+ blocks tall
                speed = data.visualProfile().speedMin() * (0.85F + random.nextFloat() * 0.45F);
                verticalSpawnOffset = 32.0F + random.nextFloat() * 22.0F; // 32 to 54 blocks high
                fallDistance = 38.0F + random.nextFloat() * 26.0F;        // Falls 38 to 64 blocks downwards
            }

            streaks.add(new PressureStreak(rx, rz, width, length, speed, baseAlpha, phaseOffset, verticalSpawnOffset, fallDistance));
        }
    }

    public PressureFieldData getData() {
        return data;
    }

    public List<PressureStreak> getStreaks() {
        return streaks;
    }

    public void tick() {
        clientElapsedTicks++;
        if (isEnding) {
            endingTicks++;
        }
    }

    public void startEnding() {
        this.isEnding = true;
    }

    public void markEnding() {
        this.isEnding = true;
    }

    public boolean isEnding() { return isEnding; }

    public boolean isFinished() {
        return isEnding && endingTicks >= FADE_OUT_TICKS;
    }

    public boolean isExpired(long currentGameTime) {
        if (!data.isPersistent() && data.durationTicks() > 0) {
            long safetyLimit = data.startGameTime() + data.durationTicks() + 40L;
            if (currentGameTime > safetyLimit) {
                return true;
            }
        }
        return isEnding && endingTicks >= FADE_OUT_TICKS;
    }

    public float getFadeMultiplier(float partialTicks) {
        float inFactor = 1.0F;
        if (clientElapsedTicks < FADE_IN_TICKS) {
            inFactor = (clientElapsedTicks + partialTicks) / (float) FADE_IN_TICKS;
        }

        float outFactor = 1.0F;
        if (isEnding) {
            outFactor = 1.0F - (endingTicks + partialTicks) / (float) FADE_OUT_TICKS;
            outFactor = Mth.clamp(outFactor, 0.0F, 1.0F);
        }

        return Mth.clamp(inFactor * outFactor, 0.0F, 1.0F);
    }

    public Vec3 getInterpolatedCenter(float partialTicks) {
        if (!data.anchor().isFollowing()) {
            return lastKnownCenter;
        }

        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null) {
            Entity owner = mc.level.getEntity(data.anchor().getEntityId());
            if (owner != null) {
                Vec3 current = owner.getPosition(partialTicks);
                lastKnownCenter = current;
                return current;
            }
        }

        return lastKnownCenter;
    }
}
