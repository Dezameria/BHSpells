package net.offkung.bhspells.pressure.client;

import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;

/**
 * Aggregates all spiritual pressure fields affecting the local player into a single ScreenPressureState.
 * Identifies primary and secondary sources to allow independent dual-color rendering and interference
 * without running multiple fullscreen passes.
 */
public final class ScreenPressureAggregator {
    private static ScreenPressureState currentState = ScreenPressureState.EMPTY;

    private ScreenPressureAggregator() {
    }

    public static ScreenPressureState getCurrentState() {
        return currentState;
    }

    public static void update(Camera camera, float partialTicks) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) {
            currentState = ScreenPressureState.EMPTY;
            return;
        }

        Vec3 camPos = camera.getPosition();
        var fields = ClientPressureManager.getActiveFields();
        if (fields.isEmpty()) {
            currentState = ScreenPressureState.EMPTY;
            return;
        }

        record SourceInfo(int color, float strength, Vec3 dirToCenter) {}
        List<SourceInfo> influencingSources = new ArrayList<>();

        for (ClientPressureField field : fields) {
            Vec3 center = field.getInterpolatedCenter(partialTicks);
            double dist = center.distanceTo(camPos);
            float radius = field.getData().radius();

            if (dist < radius) {
                float normDist = (float) (dist / radius);
                float distanceFactor = 1.0F - normDist * 0.7F;
                float fade = field.getFadeMultiplier(partialTicks);
                float fieldStrength = field.getData().intensity() * distanceFactor * fade;

                // Self caster aura feedback
                if (player.getUUID().equals(field.getData().ownerUuid())) {
                    fieldStrength *= 0.35F;
                }

                if (fieldStrength > 0.01F) {
                    Vector3f lookF = camera.getLookVector();
                    Vec3 dir = (dist > 0.01D) ? center.subtract(camPos).normalize() : new Vec3(lookF.x(), lookF.y(), lookF.z());
                    influencingSources.add(new SourceInfo(field.getData().getColor(), fieldStrength, dir));
                }
            }
        }

        if (influencingSources.isEmpty()) {
            currentState = ScreenPressureState.EMPTY;
            return;
        }

        // Sort descending by strength
        influencingSources.sort((a, b) -> Float.compare(b.strength, a.strength));

        SourceInfo primary = influencingSources.get(0);
        SourceInfo secondary = influencingSources.size() > 1 ? influencingSources.get(1) : null;

        float primaryStrength = primary.strength;
        int primaryColor = primary.color;

        int secondaryColor = primaryColor;
        float secondaryStrength = 0.0F;
        float interference = 0.0F;

        if (secondary != null) {
            secondaryColor = secondary.color;
            secondaryStrength = secondary.strength;
            // Angular interference between sources
            double dot = primary.dirToCenter.dot(secondary.dirToCenter);
            boolean coLocated = dot > 0.95 || Math.abs(dot - 1.0) < 0.05;
            if (coLocated) {
                // When two colossal domains originate from the same caster, they create intense harmonic clash!
                float gameTime = (mc.level.getGameTime() + partialTicks) / 20.0F;
                interference = 0.75F * Math.min(primaryStrength, secondaryStrength) * (0.8F + 0.2F * Mth.sin(gameTime * 4.0F));
            } else {
                interference = (float) (1.0 - Math.abs(dot)) * Math.min(primaryStrength, secondaryStrength);
            }
        }

        float totalPressure = Mth.clamp(primaryStrength + secondaryStrength * 0.4F, 0.0F, 1.0F);

        // Project primary direction relative to camera look
        Vector3f upF = camera.getUpVector();
        Vector3f leftF = camera.getLeftVector();
        Vec3 up = new Vec3(upF.x(), upF.y(), upF.z());
        Vec3 left = new Vec3(leftF.x(), leftF.y(), leftF.z());

        float dirX = (float) primary.dirToCenter.dot(left);
        float dirY = (float) primary.dirToCenter.dot(up);

        if (secondary != null && Math.abs(dirX) < 0.01F && Math.abs(dirY) < 0.01F) {
            // Harmonic dual aura split: one school pushes slightly to the left, the other to the right!
            float gameTime = (mc.level.getGameTime() + partialTicks) / 20.0F;
            dirX = 0.55F * Mth.sin(gameTime * 2.5F);
        }

        currentState = new ScreenPressureState(
                totalPressure,
                primaryColor,
                primaryStrength,
                secondaryColor,
                secondaryStrength,
                dirX,
                dirY,
                interference,
                influencingSources.size()
        );
    }
}
