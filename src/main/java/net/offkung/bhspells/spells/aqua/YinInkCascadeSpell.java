package net.offkung.bhspells.spells.aqua;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import mod.chloeprime.aaaparticles.common.util.LimitlessResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.entity.spells.yin_ink_cascade.YinInkCascadeAreaEntity;
import net.offkung.bhspells.network.PacketHandler;

import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class YinInkCascadeSpell extends AbstractSpell {
    public static final String SPELL_ID_STR = "bhspells:yin_ink_cascade";
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "yin_ink_cascade");

    public static final float BASE_DAMAGE = 25.0F;
    public static final float DAMAGE_PER_LEVEL = 5.0F;
    public static final int BASE_MANA_COST = 80;
    public static final int MANA_COST_PER_LEVEL = 10;
    public static final double COOLDOWN_SECONDS = 60.0D;

    public static final float RADIUS = 30.0F;
    public static final int CAST_TIME_TICKS = 100; // 5.0 seconds
    public static final int SURGE_DURATION_TICKS = 100; // 5.0 seconds
    public static final int WITHER_DURATION_TICKS = 280; // 14.0 seconds

    public static final ResourceLocation MRQUESTION_EFFEK_ID = new LimitlessResourceLocation(BHSpells.MODID, "vfx/Mrquestion_commission_02");
    public static final double FORWARD_OFFSET = 2.0D;

    public static ResourceLocation getEmitterName(int entityId) {
        return BHSpells.id("yin_ink_cascade_" + entityId);
    }

    public static ResourceLocation getEmitterName(LivingEntity entity) {
        return getEmitterName(entity.getId());
    }

    public static Vec3 getTargetPosition(LivingEntity entity) {
        Vec3 forward = Vec3.directionFromRotation(0.0F, entity.getYRot()).scale(FORWARD_OFFSET);
        return entity.position().add(forward);
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(RADIUS, 1)),
                Component.translatable("ui.irons_spellbooks.cast_time", Utils.timeFromTicks(CAST_TIME_TICKS, 1)),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(WITHER_DURATION_TICKS, 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public YinInkCascadeSpell() {
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = (int) BASE_DAMAGE;
        this.spellPowerPerLevel = (int) DAMAGE_PER_LEVEL;
        this.castTime = CAST_TIME_TICKS;
        this.baseManaCost = BASE_MANA_COST;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.YinInkCascade.getBaseMana() + (spellLevel - 1) * SpellConfig.YinInkCascade.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.YinInkCascade.getCooldown() * 20);
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.EVOKER_PREPARE_ATTACK);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.GENERIC_SPLASH);
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
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
        if (!level.isClientSide) {
            Vec3 spawnPos = getTargetPosition(entity);
            ResourceLocation emitterName = getEmitterName(entity);
            float rotY = (float) (Math.PI - Math.toRadians(entity.getYRot()));
            ParticleEmitterInfo effek = ParticleEmitterInfo.create(level, MRQUESTION_EFFEK_ID, emitterName)
                    .position(spawnPos.x, spawnPos.y + 0.05D, spawnPos.z)
                    .rotation(0.0F, rotY, 0.0F);
            AAALevel.addParticle(level, 64.0, effek);
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            Vec3 spawnPos = getTargetPosition(entity);

            // Gameplay area entity handling continuous debuffs and finale explosion (pure Effekseer, no vanilla particles)
            YinInkCascadeAreaEntity areaEntity = new YinInkCascadeAreaEntity(level);
            areaEntity.moveTo(spawnPos.x, spawnPos.y, spawnPos.z, entity.getYRot(), 0.0F);
            areaEntity.setOwner(entity);
            areaEntity.setExplosionDamage(getDamage(spellLevel, entity));
            level.addFreshEntity(areaEntity);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        if (cancelled) {
            PacketHandler.sendStopYinInkEffek(entity);
        }
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        float base = SpellConfig.YinInkCascade.getBaseDamage();
        float perLevel = SpellConfig.YinInkCascade.getDamagePerLevel();
        return (base + (spellLevel - 1) * perLevel) * getEntityPowerMultiplier(caster);
    }
}
