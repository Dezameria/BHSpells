package net.offkung.bhspells.entity.spells.amethyst_decree;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.util.BHAmethystSounds;
import net.offkung.bhspells.util.BHUtil;

import java.util.List;
import java.util.Optional;

public class AmethystDecreeCasterRingEntity extends AoeEntity {
    public static final int LIFETIME_TICKS = 110;

    private List<CrystalTransform> cachedScatter;
    private List<CrystalTransform> cachedBurstRing;
    private List<CrystalTransform> cachedBurstScatter;

    public AmethystDecreeCasterRingEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setRadius((float) AmethystDecreeConstants.RADIUS);
    }

    public AmethystDecreeCasterRingEntity(Level level) {
        this(EntityRegistry.AMETHYST_DECREE_CASTER_RING.get(), level);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    // emission, not visual rise/sink math.
    private static final int BURST_TICK = 20;
    private static final int SINK_START = 80;
    private static final int AMBIENT_INTERVAL_TICKS = 12;

    private static final int[] RISE_CHIME_TICKS = {5, 11, 16};

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.tickCount >= LIFETIME_TICKS && !this.level().isClientSide()) {
            this.discard();
        }
        if (!this.level().isClientSide() && this.level() instanceof net.minecraft.server.level.ServerLevel serverLevel) {
            if (this.tickCount == BURST_TICK) {
                spawnBurstDebris(serverLevel);
                BHAmethystSounds.playEruption(serverLevel, this.getX(), this.getY(), this.getZ(), this.random, 4);
            } else if (this.tickCount > BURST_TICK && this.tickCount < SINK_START
                    && this.tickCount % AMBIENT_INTERVAL_TICKS == 0) {
                spawnAmbientShimmer(serverLevel);
            } else {
                for (int riseTick : RISE_CHIME_TICKS) {
                    if (this.tickCount == riseTick) {
                        BHAmethystSounds.playRiseChime(serverLevel, this.getX(), this.getY(), this.getZ(), this.random);
                        break;
                    }
                }
            }
        }
    }

    private void spawnBurstDebris(net.minecraft.server.level.ServerLevel serverLevel) {
        for (CrystalTransform t : getOrComputeBurstRing()) {
            spawnBurstAt(serverLevel, t);
        }
        for (CrystalTransform t : getOrComputeBurstScatter()) {
            spawnBurstAt(serverLevel, t);
        }
    }

    private void spawnBurstAt(net.minecraft.server.level.ServerLevel serverLevel, CrystalTransform t) {
        double x = this.getX() + t.dx();
        double y = this.getY() + t.dy() + 0.3;
        double z = this.getZ() + t.dz();
        double dx = t.dx();
        double dz = t.dz();
        double len = Math.max(0.001, Math.sqrt(dx * dx + dz * dz));
        BHUtil.spawnBurst(serverLevel, x, y, z, dx / len, 0.6, dz / len, 6, 3);
    }

    private void spawnAmbientShimmer(net.minecraft.server.level.ServerLevel serverLevel) {
        List<CrystalTransform> scatter = getOrComputeScatter();
        if (scatter.isEmpty()) {
            return;
        }
        for (int i = 0; i < 3; i++) {
            CrystalTransform t = scatter.get(this.random.nextInt(scatter.size()));
            double x = this.getX() + t.dx();
            double y = this.getY() + t.dy() + 0.4;
            double z = this.getZ() + t.dz();
            BHUtil.spawnAmbient(serverLevel, x, y, z, 1, 2);
        }
    }

    public List<CrystalTransform> getOrComputeScatter() {
        if (this.cachedScatter == null) {
            double radius = AmethystDecreeConstants.RADIUS;
            this.cachedScatter = CasterRingLayout.generateScatter(this.getId(), radius, this.level(), this.getX(), this.getY(), this.getZ());
        }
        return this.cachedScatter;
    }

    public List<CrystalTransform> getOrComputeBurstRing() {
        if (this.cachedBurstRing == null) {
            double radius = AmethystDecreeConstants.RADIUS;
            this.cachedBurstRing = CasterRingLayout.generateBurstRing(this.getId(), radius, this.level(), this.getX(), this.getY(), this.getZ());
        }
        return this.cachedBurstRing;
    }

    public List<CrystalTransform> getOrComputeBurstScatter() {
        if (this.cachedBurstScatter == null) {
            double radius = AmethystDecreeConstants.RADIUS;
            this.cachedBurstScatter = CasterRingLayout.generateBurstScatter(this.getId(), radius, this.level(), this.getX(), this.getY(), this.getZ());
        }
        return this.cachedBurstScatter;
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return false;
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return false;
    }

    @Override
    public float getParticleCount() {
        return 0.0f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }
}
