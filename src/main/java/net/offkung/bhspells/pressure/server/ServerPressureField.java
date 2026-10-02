package net.offkung.bhspells.pressure.server;

import net.offkung.bhspells.pressure.PressureFieldData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.UUID;

/**
 * Server runtime representation of an active spiritual pressure field or torrential domain.
 */
public class ServerPressureField {
    private final PressureFieldData data;
    private final ServerLevel level;
    private int elapsedTicks;
    private boolean expired;
    private Vec3 lastKnownPosition;
    private long nextStrikeGameTime;
    private long nextScreenShakeGameTime;

    public ServerPressureField(PressureFieldData data, ServerLevel level) {
        this.data = data;
        this.level = level;
        this.elapsedTicks = 0;
        this.expired = false;
        this.lastKnownPosition = data.anchor().isFollowing() ? Vec3.ZERO : data.anchor().getFixedPosition();
        this.nextStrikeGameTime = level.getGameTime() + 20L + (Math.abs(data.seed()) % 30L);
        this.nextScreenShakeGameTime = level.getGameTime();
    }

    public PressureFieldData getData() {
        return data;
    }

    public UUID getFieldId() {
        return data.fieldId();
    }

    public UUID getOwnerUuid() {
        return data.ownerUuid();
    }

    public String getSourceSpellId() {
        return data.sourceSpellId();
    }

    public ServerLevel getLevel() {
        return level;
    }

    public int getElapsedTicks() {
        return elapsedTicks;
    }

    public boolean isExpired() {
        if (expired) {
            return true;
        }

        // Persistent stance fields do not expire from elapsed duration ticks
        if (!data.isPersistent() && elapsedTicks >= data.durationTicks()) {
            return true;
        }

        // Entity-following field expires if owner is absent, dead, or changed dimension
        if (data.anchor().isFollowing()) {
            Entity owner = getOwnerEntity();
            if (owner == null || !owner.isAlive() || owner.level() != level) {
                return true;
            }
        }

        return false;
    }

    public void markExpired() {
        this.expired = true;
    }

    public void incrementTick() {
        this.elapsedTicks++;
    }

    public boolean canTriggerStrike(long gameTime) {
        if (gameTime >= nextStrikeGameTime) {
            return true;
        }
        return false;
    }

    public void scheduleNextStrike(long gameTime, int intervalTicks) {
        this.nextStrikeGameTime = gameTime + intervalTicks;
    }

    public boolean canTriggerScreenShake(long gameTime) {
        if (gameTime >= nextScreenShakeGameTime) {
            nextScreenShakeGameTime = gameTime + 20L; // pulse every 20 ticks (1 second)
            return true;
        }
        return false;
    }

    @Nullable
    public Entity getOwnerEntity() {
        if (data.ownerUuid() == null) {
            return null;
        }
        Entity entity = level.getEntity(data.ownerUuid());
        if (entity == null) {
            entity = level.getPlayerByUUID(data.ownerUuid());
        }
        return entity;
    }

    public Vec3 getCurrentCenter() {
        if (!data.anchor().isFollowing()) {
            Vec3 fixed = data.anchor().getFixedPosition();
            return fixed != null ? fixed : Vec3.ZERO;
        }

        Entity owner = getOwnerEntity();
        if (owner != null && owner.isAlive()) {
            lastKnownPosition = owner.position();
            return lastKnownPosition;
        }

        return lastKnownPosition != null ? lastKnownPosition : Vec3.ZERO;
    }
}
