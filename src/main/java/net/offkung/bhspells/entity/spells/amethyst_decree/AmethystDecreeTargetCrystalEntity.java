package net.offkung.bhspells.entity.spells.amethyst_decree;

import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
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
import java.util.UUID;

public class AmethystDecreeTargetCrystalEntity extends AoeEntity {
    private UUID targetId;
    private final int rootDurationTicks;
    private final int totalLifetimeTicks;
    private boolean shatterBurstSpawned;

    private List<CrystalTransform> cachedEncasement;
    private List<CrystalTransform> cachedLegCrystals;
    private List<CrystalTransform> cachedStrikeSpikes;

    public AmethystDecreeTargetCrystalEntity(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.rootDurationTicks = AmethystDecreeConstants.ROOT_DURATION_TICKS;
        this.totalLifetimeTicks = this.rootDurationTicks + AmethystDecreeConstants.DOT_INTERVAL_TICKS * AmethystDecreeConstants.DOT_TICK_COUNT;
    }

    public AmethystDecreeTargetCrystalEntity(Level level, LivingEntity target) {
        this(EntityRegistry.AMETHYST_DECREE_TARGET_CRYSTAL.get(), level);
        this.targetId = target.getUUID();
        this.setPos(target.getX(), target.getY(), target.getZ());
    }

    public int getRootDurationTicks() {
        return this.rootDurationTicks;
    }

    public int getTotalLifetimeTicks() {
        return this.totalLifetimeTicks;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.isRemoved()) {
            return;
        }
        if (this.level().isClientSide()) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel) || this.targetId == null) {
            this.discard();
            return;
        }
        Entity resolved = serverLevel.getEntity(this.targetId);
        if (!(resolved instanceof LivingEntity target) || !target.isAlive()) {
            this.discard();
            return;
        }
        this.setPos(target.getX(), target.getY(), target.getZ());

        if (!this.shatterBurstSpawned && this.tickCount >= this.rootDurationTicks) {
            this.shatterBurstSpawned = true;
            spawnShatterBurst(serverLevel, target);
            BHAmethystSounds.playShatter(serverLevel, target.getX(), target.getY(), target.getZ(), this.random);
        }
        if (this.tickCount == 0) {
            spawnStrikeDebris(serverLevel);
        } else if (this.tickCount % AMBIENT_INTERVAL_TICKS == 0) {
            spawnAmbientShimmer(serverLevel);
        }
        if (this.tickCount >= this.totalLifetimeTicks) {
            this.discard();
        }
    }

    private static final int AMBIENT_INTERVAL_TICKS = 15;

    private void spawnStrikeDebris(ServerLevel serverLevel) {
        for (CrystalTransform t : getOrComputeStrikeSpikes()) {
            double x = this.getX() + t.dx();
            double y = this.getY() + t.dy() + 0.3;
            double z = this.getZ() + t.dz();
            double len = Math.max(0.001, Math.sqrt(t.dx() * t.dx() + t.dz() * t.dz()));
            BHUtil.spawnBurst(serverLevel, x, y, z, t.dx() / len, 0.6, t.dz() / len, 6, 3);
        }
    }

    private void spawnAmbientShimmer(ServerLevel serverLevel) {
        List<CrystalTransform> group = this.tickCount < this.rootDurationTicks ? getOrComputeEncasement() : getOrComputeLegCrystals();
        if (group.isEmpty()) {
            return;
        }
        CrystalTransform t = group.get(this.random.nextInt(group.size()));
        double x = this.getX() + t.dx();
        double y = this.getY() + t.dy() + 0.3;
        double z = this.getZ() + t.dz();
        BHUtil.spawnAmbient(serverLevel, x, y, z, 1, 2);

        if (this.tickCount >= this.rootDurationTicks && this.random.nextInt(3) == 0) {
            BHAmethystSounds.playDotChime(serverLevel, x, y, z, this.random);
        }
    }

    private static void spawnShatterBurst(ServerLevel serverLevel, LivingEntity target) {
        serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 0.2, target.getZ(), 20, 0.35, 0.15, 0.35, 0.02);
    }

    public List<CrystalTransform> getOrComputeEncasement() {
        if (this.cachedEncasement == null) {
            this.cachedEncasement = TargetCrystalLayout.generateEncasement(this.getId());
        }
        return this.cachedEncasement;
    }

    public List<CrystalTransform> getOrComputeLegCrystals() {
        if (this.cachedLegCrystals == null) {
            this.cachedLegCrystals = TargetCrystalLayout.generateLegCrystals(this.getId());
        }
        return this.cachedLegCrystals;
    }

    public List<CrystalTransform> getOrComputeStrikeSpikes() {
        if (this.cachedStrikeSpikes == null) {
            this.cachedStrikeSpikes = TargetCrystalLayout.generateStrikeSpikes(this.getId());
        }
        return this.cachedStrikeSpikes;
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
