package net.offkung.bhspells.entity.spells.amethyst_decree;

import com.github.L_Ender.cataclysm.effects.EffectStun;
import com.github.L_Ender.cataclysm.init.ModEffect;
import com.hm.efn.registries.EFNMobEffectRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.AoeEntity;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.registry.BHSpellRegistry;
import net.offkung.bhspells.registry.EntityRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class AmethystDecreeAoe extends AoeEntity {
    private final List<UUID> dotTargets = new ArrayList<>();

    public AmethystDecreeAoe(EntityType<? extends Projectile> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
        this.setRadius((float) AmethystDecreeConstants.RADIUS);
    }

    public AmethystDecreeAoe(Level level) {
        this(EntityRegistry.AMETHYST_DECREE_AOE.get(), level);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level().isClientSide() || this.isRemoved()) {
            return;
        }
        if (this.tickCount <= this.getDelay()) {
            return;
        }
        int activeTicks = this.tickCount - this.getDelay();
        if (activeTicks == 1) {
            applyBurst();
        }
        tickDot(activeTicks);

        int totalDotTicks = AmethystDecreeConstants.DOT_INTERVAL_TICKS * AmethystDecreeConstants.DOT_TICK_COUNT;
        if (activeTicks >= totalDotTicks) {
            this.discard();
        }
    }

    @Override
    public void applyEffect(LivingEntity target) {
    }

    @Override
    public float getParticleCount() {
        return 0.0f;
    }

    @Override
    public Optional<ParticleOptions> getParticle() {
        return Optional.empty();
    }

    @Override
    protected boolean canHitEntity(Entity target) {
        if (!(target instanceof LivingEntity living) || !living.isAlive()) {
            return false;
        }
        if (target instanceof Player player && player.isSpectator()) {
            return false;
        }
        Entity owner = this.getOwner();
        if (target == owner) {
            return false;
        }
        return owner == null || !DamageSources.isFriendlyFireBetween(owner, living);
    }

    @Override
    protected boolean canHitTargetForGroundContext(LivingEntity target) {
        return true;
    }

    private void applyBurst() {
        Entity owner = this.getOwner();
        List<LivingEntity> targets = this.level().getEntitiesOfClass(LivingEntity.class, this.getBoundingBox(), this::canHitEntity);
        for (LivingEntity target : targets) {
            DamageSources.applyDamage(target, (float) AmethystDecreeConstants.BURST_DAMAGE, getDamageSource(owner));
            applyRootAndStun(target);
            applyDebuffs(target);
            spawnHitVfx(target);
            spawnTargetCrystal(target);
            dotTargets.add(target.getUUID());
        }
    }

    private void spawnTargetCrystal(LivingEntity target) {
        AmethystDecreeTargetCrystalEntity crystal = new AmethystDecreeTargetCrystalEntity(this.level(), target);
        this.level().addFreshEntity(crystal);
    }

    private void tickDot(int activeTicks) {
        if (dotTargets.isEmpty()) {
            return;
        }
        int interval = AmethystDecreeConstants.DOT_INTERVAL_TICKS;
        if (activeTicks % interval != 0) {
            return;
        }
        int tickIndex = activeTicks / interval;
        if (tickIndex > AmethystDecreeConstants.DOT_TICK_COUNT) {
            return;
        }
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        Entity owner = this.getOwner();
        for (UUID targetId : dotTargets) {
            Entity resolved = serverLevel.getEntity(targetId);
            if (!(resolved instanceof LivingEntity target) || !target.isAlive()) {
                continue;
            }
            DamageSources.applyDamage(target, (float) AmethystDecreeConstants.DOT_DAMAGE_PER_TICK, getDamageSource(owner));
            spawnHitVfx(target);
        }
    }

    private void applyRootAndStun(LivingEntity target) {
        target.addEffect(new MobEffectInstance(EFNMobEffectRegistry.STOP.get(), AmethystDecreeConstants.ROOT_DURATION_TICKS, 0, false, true, true));
        target.addEffect(new MobEffectInstance(ModEffect.EFFECTSTUN.get(), AmethystDecreeConstants.STUN_DURATION_TICKS, 0, false, true, true));
    }

    private void applyDebuffs(LivingEntity target) {
        int duration = AmethystDecreeConstants.DEBUFF_DURATION_TICKS;
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, duration, 1, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, duration, 0, false, true, true));
        target.addEffect(new MobEffectInstance(MobEffects.DIG_SLOWDOWN, duration, 1, false, true, true));
    }

    private DamageSource getDamageSource(Entity owner) {
        return BHSpellRegistry.AMETHYST_DECREE.get().getDamageSource(this, owner);
    }

    private void spawnHitVfx(LivingEntity target) {
        if (!(this.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        serverLevel.sendParticles(ParticleTypes.WITCH, target.getX(), target.getY() + 0.1, target.getZ(), 12, 0.3, 0.05, 0.3, 0.01);
    }
}
