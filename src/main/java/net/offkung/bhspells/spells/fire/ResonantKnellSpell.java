package net.offkung.bhspells.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.capabilities.magic.PlayerRecasts;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.entity.spells.resonant_knell.ResonantKnellDomeAoe;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import java.util.List;

@AutoSpellConfig
public class ResonantKnellSpell extends AbstractSpell {
    private static final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID,"resonant_knell");

    public static final float STAGE_1_DAMAGE = 6.0F;
    public static final float STAGE_2_DAMAGE = 10.0F;
    public static final float STAGE_3_DAMAGE = 16.0F;
    public static final float DAMAGE_PER_LEVEL = 2.0F;
    public static final int BASE_MANA_COST = 75;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final double COOLDOWN_SECONDS = 30.0;

    public static final int TOTAL_RECAST_COUNT = 6;
    public static final int RECAST_WINDOW_TICKS = 300; // 15 seconds per push window
    public static final int COOLDOWN_DURATION_TICKS = (int) (COOLDOWN_SECONDS * 20); // 30 seconds cooldown effect

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public ResonantKnellSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = (int) STAGE_1_DAMAGE;
        this.spellPowerPerLevel = (int) DAMAGE_PER_LEVEL;
        this.castTime = 0;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.ResonantKnell.getBaseMana() + (spellLevel - 1) * SpellConfig.ResonantKnell.getManaPerLevel();
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public int getRecastCount(int spellLevel, LivingEntity entity) {
        return TOTAL_RECAST_COUNT;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity.hasEffect(MobEffectsRegistry.COOLDOWN.get())) {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("ui.bhspells.spell_on_cooldown").withStyle(ChatFormatting.RED), true);
            }
            return false;
        }

        ResonantKnellDomeAoe activeDome = ResonantKnellDomeAoe.getActiveDomeFor(entity);
        if (activeDome != null && activeDome.isExploding()) {
            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("ui.bhspells.resonant_knell_shockwave_active").withStyle(ChatFormatting.GOLD), true);
            }
            return false;
        }
        return true;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        PlayerRecasts recasts = playerMagicData.getPlayerRecasts();
        boolean hasRecast = recasts.hasRecastForSpell(this);

        applyBarrierBuffsToAllies(level, entity);

        if (!hasRecast) {
            ResonantKnellDomeAoe.discardActiveDomesFor(entity);
            playChimeSound(level, entity);

            ResonantKnellDomeAoe dome = new ResonantKnellDomeAoe(level);
            dome.setOwner(entity);
            dome.setPos(entity.getX(), entity.getY(), entity.getZ());
            dome.activateDome(1);
            level.addFreshEntity(dome);

            RecastInstance recastInstance = new RecastInstance(getSpellId(), spellLevel, TOTAL_RECAST_COUNT, RECAST_WINDOW_TICKS, castSource, null);
            recasts.addRecast(recastInstance, playerMagicData);
        } else {
            RecastInstance recastInstance = recasts.getRecastInstance(getSpellId());
            int remaining = recastInstance.getRemainingRecasts();

            switch (remaining) {
                case 5 -> {
                    playPushSound(level, entity, 1.2f, 1.4f);
                    float dmg = (SpellConfig.ResonantKnell.getStage1Damage() + (spellLevel - 1) * SpellConfig.ResonantKnell.getDamagePerLevel()) * getEntityPowerMultiplier(entity);
                    pushEnemies(level, entity, 15.0f, 1.8f, 1.2f, dmg);
                    triggerDomeShockwave(level, entity, 15.0f);
                }
                case 4 -> {
                    playChimeSound(level, entity);
                    openOrCreateDome(level, entity, 2);
                }
                case 3 -> {
                    playPushSound(level, entity, 1.0f, 1.2f);
                    float dmg = (SpellConfig.ResonantKnell.getStage2Damage() + (spellLevel - 1) * SpellConfig.ResonantKnell.getDamagePerLevel()) * getEntityPowerMultiplier(entity);
                    pushEnemies(level, entity, 20.0f, 2.5f, 1.5f, dmg);
                    triggerDomeShockwave(level, entity, 20.0f);
                }
                case 2 -> {
                    playChimeSound(level, entity);
                    openOrCreateDome(level, entity, 3);
                }
                case 1 -> {
                    playNuclearBlastSound(level, entity);
                    float dmg = (SpellConfig.ResonantKnell.getStage3Damage() + (spellLevel - 1) * SpellConfig.ResonantKnell.getDamagePerLevel()) * getEntityPowerMultiplier(entity);
                    pushEnemies(level, entity, 30.0f, 3.8f, 2.0f, dmg);
                    triggerDomeShockwave(level, entity, 30.0f);
                    applyCooldownEffect(entity);
                }
                default -> {
                }
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private void openOrCreateDome(Level level, LivingEntity entity, int stage) {
        ResonantKnellDomeAoe dome = ResonantKnellDomeAoe.getActiveDomeFor(entity);
        if (dome == null || !dome.isAlive() || dome.isRemoved()) {
            dome = new ResonantKnellDomeAoe(level);
            dome.setOwner(entity);
            dome.setPos(entity.getX(), entity.getY(), entity.getZ());
            level.addFreshEntity(dome);
        }
        dome.activateDome(stage);
    }

    private void triggerDomeShockwave(Level level, LivingEntity entity, float radius) {
        ResonantKnellDomeAoe dome = ResonantKnellDomeAoe.getActiveDomeFor(entity);
        if (dome == null || !dome.isAlive() || dome.isRemoved()) {
            dome = new ResonantKnellDomeAoe(level);
            dome.setOwner(entity);
            dome.setPos(entity.getX(), entity.getY(), entity.getZ());
            level.addFreshEntity(dome);
        }
        dome.triggerShockwave(radius);
    }

    private void pushEnemies(Level level, LivingEntity caster, float radius, float horizontalPower, float verticalPower, float damage) {
        AABB aabb = caster.getBoundingBox().inflate(radius);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> e != caster && e.isAlive() && !(e instanceof ArmorStand) && !isAlly(caster, e));

        for (LivingEntity target : targets) {
            double dx = target.getX() - caster.getX();
            double dz = target.getZ() - caster.getZ();
            double distSq = dx * dx + dz * dz;
            if (distSq <= radius * radius && distSq > 0.001) {
                double dist = Math.sqrt(distSq);
                double nx = dx / dist;
                double nz = dz / dist;

                target.setDeltaMovement(target.getDeltaMovement().x + nx * horizontalPower, verticalPower, target.getDeltaMovement().z + nz * horizontalPower);
                target.hurtMarked = true;

                DamageSources.applyDamage(target, damage, getDamageSource(caster));
            }
        }
    }

    public static void applyBarrierBuffs(LivingEntity target, int durationTicks) {
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, durationTicks, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, durationTicks, 1, false, false, true));
        target.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, durationTicks, 0, false, false, true));
    }

    private void applyBarrierBuffsToAllies(Level level, LivingEntity caster) {
        applyBarrierBuffs(caster, RECAST_WINDOW_TICKS);

        float radius = 8.0F;
        AABB aabb = caster.getBoundingBox().inflate(radius);
        List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, aabb, e -> isAlly(caster, e) && caster.distanceToSqr(e) <= (double) (radius * radius));

        for (LivingEntity ally : allies) {
            applyBarrierBuffs(ally, RECAST_WINDOW_TICKS);
        }
    }

    public static boolean isAlly(LivingEntity caster, LivingEntity target) {
        if (target == caster) return true;
        if (!target.isAlive() || target.isSpectator()) return false;
        if (target instanceof ArmorStand) return false;
        if (target.isAlliedTo(caster)) return true;
        if (caster instanceof Player && target instanceof Player) {
            return DamageSources.isFriendlyFireBetween(caster, target);
        }
        if (target instanceof TamableAnimal tamable && tamable.isOwnedBy(caster)) {
            return true;
        }
        return false;
    }

    private void applyCooldownEffect(LivingEntity caster) {
        int durationTicks = (int) (SpellConfig.ResonantKnell.getCooldown() * 20);
        caster.addEffect(new MobEffectInstance(MobEffectsRegistry.COOLDOWN.get(), durationTicks, 0, false, false, true));
    }

    private void playChimeSound(Level level, LivingEntity entity) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BELL_BLOCK, SoundSource.PLAYERS, 1.4f, 1.6f);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.2f, 1.3f);
    }

    private void playPushSound(Level level, LivingEntity entity, float explodePitch, float fireballPitch) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.2f, explodePitch);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.0f, fireballPitch);
    }

    private void playNuclearBlastSound(Level level, LivingEntity entity) {
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 1.8f, 0.65f);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.DRAGON_FIREBALL_EXPLODE, SoundSource.PLAYERS, 1.5f, 0.85f);
        level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.BELL_RESONATE, SoundSource.PLAYERS, 1.5f, 0.9f);
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castData) {
        applyCooldownEffect(serverPlayer);
        ResonantKnellDomeAoe.finishActiveDomesFor(serverPlayer);
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castData);
    }
}
