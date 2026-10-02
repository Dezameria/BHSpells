package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import mod.chloeprime.aaaparticles.common.util.LimitlessResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.util.BHUtil;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class HymnofPurificationSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "hymn_of_purification");

    public static final float BASE_HEAL = 1.0F;
    public static final float HEAL_PER_LEVEL = 0.5F;
    public static final int BASE_MANA_COST = 80;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final double COOLDOWN_SECONDS = 60.0D;

    public static final float RADIUS = 20.0F;
    public static final int DURATION_TICKS = 460;
    public static final int SOUND_DELAY_TICKS = 40;
    public static final int HEAL_INTERVAL_TICKS = 20;
    public static final int NAUSEA_DURATION_TICKS = 40;

    public static final String AREA_ENTITY_ID = "HymnAreaEntityId";
    public static final ResourceLocation SHIBA_COMMISSION_EFFEK_ID = new LimitlessResourceLocation(BHSpells.MODID, "vfx/Shiba_Commission");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.irons_spellbooks.healing", Utils.stringTruncation(getHealAmount(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks(DURATION_TICKS, 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public HymnofPurificationSpell() {
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 1;
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.castTime = 0;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.HymnofPurification.getBaseMana() + (spellLevel - 1) * SpellConfig.HymnofPurification.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.HymnofPurification.getCooldown() * 20);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.empty();
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.none();
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData magicData) {
        if (entity.hasEffect(MobEffectsRegistry.HYMN_OF_PURIFICATION.get())) {
            return false;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, magicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectsRegistry.HYMN_OF_PURIFICATION.get(), DURATION_TICKS, spellLevel - 1, false, false, true));

        if (!level.isClientSide) {
            TargetedAreaEntity visualArea = TargetedAreaEntity.createTargetAreaEntity(level, entity.position(), RADIUS, 0xFFD700, entity);
            visualArea.setDuration(DURATION_TICKS);
            entity.getPersistentData().putInt(AREA_ENTITY_ID, visualArea.getId());

            ResourceLocation emitterName = getEmitterName(entity);
            ParticleEmitterInfo effek = ParticleEmitterInfo.create(level, SHIBA_COMMISSION_EFFEK_ID, emitterName)
                    .position(entity.getX(), entity.getY() + 0.05D, entity.getZ());
            AAALevel.addParticle(level, 64.0, effek);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static float getHealAmount(int spellLevel, LivingEntity caster) {
        float base = SpellConfig.HymnofPurification.getBaseHeal();
        float perLevel = SpellConfig.HymnofPurification.getHealPerLevel();
        return base + (spellLevel - 1) * perLevel;
    }

    public static boolean isAlly(LivingEntity caster, LivingEntity target) {
        if (target == caster) return true;
        if (!target.isAlive() || target.isSpectator()) return false;
        if (target.isAlliedTo(caster)) return true;
        if (caster instanceof Player && target instanceof Player) {
            return DamageSources.isFriendlyFireBetween(caster, target);
        }
        if (target instanceof TamableAnimal tamable && tamable.isOwnedBy(caster)) return true;
        return false;
    }

    public static ResourceLocation getEmitterName(int entityId) {
        return BHSpells.id("hymn_shiba_" + entityId);
    }

    public static ResourceLocation getEmitterName(LivingEntity caster) {
        return getEmitterName(caster.getId());
    }

    public static void stopEffek(LivingEntity entity) {
        if (!entity.level().isClientSide) {
            PacketHandler.sendStopHymnEffek(entity);
        }
    }

    public static void cleanUpArea(LivingEntity entity) {
        if (entity.getPersistentData().contains(AREA_ENTITY_ID)) {
            int areaId = entity.getPersistentData().getInt(AREA_ENTITY_ID);
            entity.getPersistentData().remove(AREA_ENTITY_ID);
            if (entity.level() instanceof ServerLevel serverLevel) {
                Entity area = serverLevel.getEntity(areaId);
                if (area instanceof TargetedAreaEntity targetedArea) {
                    targetedArea.discard();
                }
            }
        }
    }

    public static void cleanUpVisuals(LivingEntity entity) {
        stopEffek(entity);
        cleanUpArea(entity);
    }

    public static void spawnInterruptionSmoke(ServerLevel level, Vec3 center) {
        Vec3 ringPos = center.add(0, 0.2D, 0);
        BHUtil.createHorizontalRingParticles(level, ringPos, ParticleTypes.POOF, 0.8D, 0.25D, 0.5D, 36);
        BHUtil.createHorizontalRingParticles(level, ringPos, ParticleTypes.POOF, 1.8D, 0.35D, 0.7D, 48);
        BHUtil.createHorizontalRingParticles(level, ringPos, ParticleTypes.CAMPFIRE_COSY_SMOKE, 1.0D, 0.15D, 0.35D, 28);
        BHUtil.createSphereParticles(level, center.add(0, 0.6D, 0), ParticleTypes.SMOKE, 0.5D, 0.1D, 0.3D, 24);
    }

    public static void performDebuffCleanse(Level level, LivingEntity caster) {
        float radiusSqr = RADIUS * RADIUS;

        List<LivingEntity> allies = level.getEntitiesOfClass(LivingEntity.class, caster.getBoundingBox().inflate(RADIUS), target -> isAlly(caster, target) && caster.distanceToSqr(target) <= radiusSqr);

        for (LivingEntity ally : allies) {
            var harmfulEffects = ally.getActiveEffects().stream()
                    .map(MobEffectInstance::getEffect)
                    .filter(effect -> effect.getCategory() == MobEffectCategory.HARMFUL)
                    .toList();

            for (var effect : harmfulEffects) {
                ally.removeEffect(effect);
            }
        }

        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1.2F, 1.6F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.PLAYERS, 1.5F, 1.3F);
        level.playSound(null, caster.getX(), caster.getY(), caster.getZ(), SoundEvents.BEACON_ACTIVATE, SoundSource.PLAYERS, 1.0F, 1.8F);
    }
}
