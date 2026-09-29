package net.offkung.bhspells.entity.spells.jade_brush_slash;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.NetworkHooks;
import net.offkung.bhspells.registry.EntityRegistry;

import java.util.Optional;

public class JadeBrushSlash extends AoeEntity {
    private static final EntityDataAccessor<Boolean> DATA_MIRRORED = SynchedEntityData.defineId(JadeBrushSlash.class, EntityDataSerializers.BOOLEAN);
    LivingEntity target;
    public final int ticksPerFrame;
    public final int deathTime;

    public JadeBrushSlash(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        this.ticksPerFrame = 2;
        this.deathTime = 8;
    }

    public JadeBrushSlash(Level level, boolean mirrored) {
        this(EntityRegistry.JADE_BRUSH_SLASH.get(), level);
        if (mirrored) {
            this.getEntityData().set(DATA_MIRRORED, true);
        }

    }

    public void applyEffect(LivingEntity target) {
    }

    public void tick() {
        if (!this.firstTick) {
            this.firstTick = true;
        }

        if (this.tickCount >= 8) {
            this.discard();
        }

    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_MIRRORED, false);
    }

    public boolean isMirrored() {
        return this.getEntityData().get(DATA_MIRRORED);
    }

    public boolean shouldBeSaved() {
        return false;
    }

    public void refreshDimensions() {
    }

    public void ambientParticles() {
    }

    public float getParticleCount() {
        return 0.0F;
    }

    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
