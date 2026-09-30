package net.offkung.bhspells.pressure;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Defines whether a field stays stationary in world-space or follows a living entity.
 */
public final class PressureAnchor {
    public enum Type {
        STATIONARY,
        FOLLOW_ENTITY
    }

    private final Type type;
    @Nullable
    private final Vec3 fixedPosition;
    @Nullable
    private final UUID entityUuid;
    private final int entityId;

    private PressureAnchor(Type type, @Nullable Vec3 fixedPosition, @Nullable UUID entityUuid, int entityId) {
        this.type = type;
        this.fixedPosition = fixedPosition;
        this.entityUuid = entityUuid;
        this.entityId = entityId;
    }

    public static PressureAnchor stationary(Vec3 position) {
        return new PressureAnchor(Type.STATIONARY, position, null, -1);
    }

    public static PressureAnchor follow(Entity entity) {
        return new PressureAnchor(Type.FOLLOW_ENTITY, null, entity.getUUID(), entity.getId());
    }

    public static PressureAnchor follow(UUID entityUuid, int entityId) {
        return new PressureAnchor(Type.FOLLOW_ENTITY, null, entityUuid, entityId);
    }

    public Type getType() {
        return type;
    }

    public boolean isFollowing() {
        return type == Type.FOLLOW_ENTITY;
    }

    @Nullable
    public Vec3 getFixedPosition() {
        return fixedPosition;
    }

    @Nullable
    public UUID getEntityUuid() {
        return entityUuid;
    }

    public int getEntityId() {
        return entityId;
    }
}
