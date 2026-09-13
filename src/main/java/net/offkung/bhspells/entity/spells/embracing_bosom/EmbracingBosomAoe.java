package net.offkung.bhspells.entity.spells.embracing_bosom;

import com.gametechbc.traveloptics.api.particle.AdvancedCylinderParticleManager;
import com.gametechbc.traveloptics.api.particle.ParticleDirection;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.client.particle.EmbraceLeafParticleOption;
import net.offkung.bhspells.client.particle.EmbraceMoteParticleOption;
import net.offkung.bhspells.registry.EntityRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.joml.Vector3f;

import java.util.List;
import java.util.Optional;

public class EmbracingBosomAoe extends AoeEntity {
    private static final float RADIUS = 6.0f;
    public static final int LIFETIME_TICKS = 160;
    private static final int BUFF_DURATION_TICKS = 40;

    private static final int AMBER_TINT_HEX = 0xF0B23D;
    private static final Vector3f AMBER_TINT = new Vector3f(((AMBER_TINT_HEX >> 16) & 0xFF) / 255f, ((AMBER_TINT_HEX >> 8) & 0xFF) / 255f, (AMBER_TINT_HEX & 0xFF) / 255f);

    private static final DustParticleOptions AMBER_DUST = new DustParticleOptions(AMBER_TINT, 1.0f);

    private static final int CONVERGE_BURST_TICKS = 8;
    private static final int CONVERGE_PARTICLES_PER_TICK = 6;
    private static final float CONVERGE_END_RADIUS = 0.4f;
    private static final double CONVERGE_HEIGHT = 1.0;
    private static final double CONVERGE_SPEED = 0.15;

    private static final int COLUMN_INTERVAL_TICKS = 5;
    private static final int COLUMN_END_ROD_PER_CALL = 1;
    private static final int COLUMN_DUST_PER_CALL = 1;
    private static final double COLUMN_RADIUS = 1.5;
    private static final double COLUMN_HEIGHT = 2.5;

    private static final double COLUMN_END_ROD_SPEED = 0.04;

    private static final double COLUMN_DUST_SPEED = 0.02;

    private static final int LEAF_INTERVAL_TICKS = 8;
    private static final int MOTE_INTERVAL_TICKS = 5;
    private static final int MOTE_PER_BURST = 4;
    private static final int PARTICLE_FADE_TAPER_TICKS = 20;

    public EmbracingBosomAoe(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.duration = LIFETIME_TICKS;
        this.setRadius(RADIUS);
    }

    public EmbracingBosomAoe(Level level) {
        this(EntityRegistry.EMBRACING_BOSOM_AOE.get(), level);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide || this.isRemoved()) {
            return;
        }
        if (this.tickCount <= this.getDelay()) {
            return;
        }
        int activeTicks = this.tickCount - this.getDelay();
        if (activeTicks <= CONVERGE_BURST_TICKS) {
            spawnConvergeBurstTick(activeTicks);
        }
        if (activeTicks % COLUMN_INTERVAL_TICKS == 0) {
            spawnColumnTick();
        }
        spawnAmbientParticlesTick(activeTicks);
        applyBuffToNearbyTargets();
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        return target instanceof Player player && player.isAlive() && !player.isSpectator();
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    private void applyBuffToNearbyTargets() {
        List<Player> targets = this.level().getEntitiesOfClass(Player.class, this.getBoundingBox(), this::canHitEntity);
        for (Player target : targets) {
            target.addEffect(new MobEffectInstance(MobEffectsRegistry.EMBRACING_BOSOM.get(), BUFF_DURATION_TICKS, 0, false, false, true));
        }
    }

    private void spawnConvergeBurstTick(int activeTicks) {
        float progress = (activeTicks - 1) / (float) (CONVERGE_BURST_TICKS - 1);
        float radius = RADIUS + (CONVERGE_END_RADIUS - RADIUS) * progress;
        AdvancedCylinderParticleManager.spawnParticles(this.level(), this.position(), CONVERGE_PARTICLES_PER_TICK, AMBER_DUST, ParticleDirection.INWARD, radius, CONVERGE_HEIGHT, 0.0, 0.0, 0.0, CONVERGE_SPEED, false);
    }

    private void spawnColumnTick() {
        AdvancedCylinderParticleManager.spawnParticles(this.level(), this.position(), COLUMN_END_ROD_PER_CALL, ParticleTypes.END_ROD, ParticleDirection.UPWARD, COLUMN_RADIUS, COLUMN_HEIGHT, 0.0, 0.0, 0.0, COLUMN_END_ROD_SPEED, false);
        AdvancedCylinderParticleManager.spawnParticles(this.level(), this.position(), COLUMN_DUST_PER_CALL, AMBER_DUST, ParticleDirection.UPWARD, COLUMN_RADIUS, COLUMN_HEIGHT, 0.0, 0.0, 0.0, COLUMN_DUST_SPEED, false);
    }

    private void spawnAmbientParticlesTick(int activeTicks) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        float taper = particleFadeTaper(activeTicks);
        if (taper <= 0.0f) {
            return;
        }
        if (activeTicks % LEAF_INTERVAL_TICKS == 0 && this.random.nextFloat() < taper) {
            spawnAmbientParticle(serverLevel, new EmbraceLeafParticleOption(AMBER_TINT));
        }
        if (activeTicks % MOTE_INTERVAL_TICKS == 0 && this.random.nextFloat() < taper) {
            for (int i = 0; i < MOTE_PER_BURST; i++) {
                spawnAmbientParticle(serverLevel, new EmbraceMoteParticleOption(AMBER_TINT));
            }
        }
    }

    private static float particleFadeTaper(int activeTicks) {
        int fadeStart = LIFETIME_TICKS - PARTICLE_FADE_TAPER_TICKS;
        if (activeTicks <= fadeStart) {
            return 1.0f;
        }
        return Math.max(0.0f, 1.0f - (activeTicks - fadeStart) / (float) PARTICLE_FADE_TAPER_TICKS);
    }

    private void spawnAmbientParticle(ServerLevel serverLevel, ParticleOptions options) {
        double angle = this.random.nextDouble() * Math.PI * 2.0;
        double dist = Math.sqrt(this.random.nextDouble()) * RADIUS;
        double x = this.getX() + Math.cos(angle) * dist;
        double z = this.getZ() + Math.sin(angle) * dist;
        double y = this.getY() + 0.05;
        serverLevel.sendParticles(options, x, y, z, 1, 0.0, 0.0, 0.0, 0.0);
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
