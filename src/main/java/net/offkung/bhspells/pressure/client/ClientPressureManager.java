package net.offkung.bhspells.pressure.client;

import net.offkung.bhspells.pressure.PressureFieldData;
import net.offkung.bhspells.pressure.PressureReaction;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Client-side manager for active spiritual pressure fields.
 * Manages visual field instances, fade interpolation, and entity reactions.
 */
public final class ClientPressureManager {
    private static final Map<UUID, ClientPressureField> ACTIVE_FIELDS = new ConcurrentHashMap<>();
    private static final Map<Integer, PressureReaction> ENTITY_REACTIONS = new ConcurrentHashMap<>();

    private ClientPressureManager() {
    }

    public static void addOrUpdateField(PressureFieldData data) {
        ClientPressureField existing = ACTIVE_FIELDS.get(data.fieldId());
        if (existing == null || existing.isEnding()) {
            ACTIVE_FIELDS.put(data.fieldId(), new ClientPressureField(data));
        }
    }

    public static void removeField(UUID fieldId) {
        ClientPressureField field = ACTIVE_FIELDS.get(fieldId);
        if (field != null) {
            field.markEnding();
        }
    }

    public static void setEntityReaction(int entityId, PressureReaction reaction) {
        if (reaction == PressureReaction.NONE) {
            ENTITY_REACTIONS.remove(entityId);
        } else {
            ENTITY_REACTIONS.put(entityId, reaction);
        }
    }

    public static PressureReaction getEntityReaction(int entityId) {
        return ENTITY_REACTIONS.getOrDefault(entityId, PressureReaction.NONE);
    }

    public static Collection<ClientPressureField> getActiveFields() {
        return ACTIVE_FIELDS.values();
    }

    public static void clientTick() {
        if (ACTIVE_FIELDS.isEmpty()) {
            return;
        }

        List<UUID> finished = new ArrayList<>();
        for (ClientPressureField field : ACTIVE_FIELDS.values()) {
            field.tick();
            if (field.isFinished()) {
                finished.add(field.getData().fieldId());
            }
        }

        for (UUID id : finished) {
            ACTIVE_FIELDS.remove(id);
        }
    }

    public static boolean hasActiveFieldByOwnerAndSpell(UUID ownerUuid, String sourceSpellId) {
        for (ClientPressureField field : ACTIVE_FIELDS.values()) {
            if (field.getData().ownerUuid().equals(ownerUuid) && spellIdMatches(field.getData().sourceSpellId(), sourceSpellId) && !field.isEnding()) {
                return true;
            }
        }
        return false;
    }

    public static void handleLightningStrike(net.minecraft.world.phys.Vec3 pos, int color) {
        net.minecraft.client.Minecraft mc = net.minecraft.client.Minecraft.getInstance();
        if (mc.level == null) {
            return;
        }

        float r = ((color >> 16) & 0xFF) / 255.0F;
        float g = ((color >> 8) & 0xFF) / 255.0F;
        float b = (color & 0xFF) / 255.0F;
        net.minecraft.core.particles.DustParticleOptions dust = new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(r, g, b), 2.2F);

        for (int i = 0; i < 30; i++) {
            double rx = (mc.level.random.nextDouble() - 0.5D) * 3.0D;
            double ry = mc.level.random.nextDouble() * 5.0D;
            double rz = (mc.level.random.nextDouble() - 0.5D) * 3.0D;
            mc.level.addParticle(
                    dust,
                    pos.x + rx, pos.y + ry, pos.z + rz,
                    rx * 0.15D, -0.3D, rz * 0.15D
            );
            mc.level.addParticle(
                    net.minecraft.core.particles.ParticleTypes.ELECTRIC_SPARK,
                    pos.x + rx, pos.y + ry, pos.z + rz,
                    rx * 0.1D, 0.2D, rz * 0.1D
            );
        }

        mc.level.playLocalSound(
                pos.x, pos.y, pos.z,
                net.minecraft.sounds.SoundEvents.LIGHTNING_BOLT_IMPACT,
                net.minecraft.sounds.SoundSource.WEATHER,
                1.2F, 1.2F, false
        );
    }

    public static void clear() {
        ACTIVE_FIELDS.clear();
        ENTITY_REACTIONS.clear();
    }

    public static void clearAll() {
        clear();
    }

    public static boolean spellIdMatches(String a, String b) {
        if (a == null || b == null) return false;
        if (a.equals(b)) return true;
        String pureA = a.contains(":") ? a.substring(a.indexOf(':') + 1) : a;
        String pureB = b.contains(":") ? b.substring(b.indexOf(':') + 1) : b;
        return pureA.equals(pureB);
    }
}