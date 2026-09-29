package net.offkung.bhspells.spells.ground;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.damage.DamageSources;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.TamableAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.client.particle.JadeAuraVfx;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.effect.JadeAuraEffect;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class JadeAuraSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "jade_aura");

    public static final float TARGET_RANGE = 16.0F;
    public static final float BASE_DURATION_SECONDS = 45.0F;
    public static final float DURATION_PER_LEVEL_SECONDS = 10.0F;
    public static final int BASE_MANA_COST = 50;
    public static final int MANA_COST_PER_LEVEL = 25;
    public static final double COOLDOWN_SECONDS = 40.0;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(getDurationTicks(spellLevel, caster), 1)),
                Component.translatable("attribute.modifier.plus.1", Utils.stringTruncation(getPercentAttackDamage(spellLevel), 0), Component.translatable("attribute.name.generic.attack_damage")),
                Component.translatable("attribute.modifier.plus.1", Utils.stringTruncation(getPercentSpeed(spellLevel), 0), Component.translatable("attribute.name.generic.movement_speed")),
                Component.translatable("attribute.modifier.plus.1", Utils.stringTruncation(getPercentSpellPower(spellLevel), 0), Component.translatable("attribute.irons_spellbooks.spell_power")),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(TARGET_RANGE, 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public JadeAuraSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = (int) BASE_DURATION_SECONDS;
        this.spellPowerPerLevel = (int) DURATION_PER_LEVEL_SECONDS;
        this.castTime = 0;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.JadeAura.getBaseMana() + (spellLevel - 1) * SpellConfig.JadeAura.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.JadeAura.getCooldown() * 20);
    }

    public int getDurationTicks(int spellLevel, LivingEntity entity) {
        float base = SpellConfig.JadeAura.getBaseDuration();
        float perLevel = SpellConfig.JadeAura.getDurationPerLevel();
        return (int) ((base + (spellLevel - 1) * perLevel) * 20 * getEntityPowerMultiplier(entity));
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
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundEvents.AMETHYST_BLOCK_CHIME);
    }

    public static float getPercentAttackDamage(int spellLevel) {
        return spellLevel * JadeAuraEffect.ATTACK_DAMAGE_PER_LEVEL * 100.0F;
    }

    public static float getPercentSpeed(int spellLevel) {
        return spellLevel * JadeAuraEffect.SPEED_PER_LEVEL * 100.0F;
    }

    public static float getPercentSpellPower(int spellLevel) {
        return spellLevel * JadeAuraEffect.SPELL_POWER_PER_LEVEL * 100.0F;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            int duration = getDurationTicks(spellLevel, entity);

            LivingEntity targetAlly = findTargetAlly(level, entity, TARGET_RANGE);

            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.JADE_AURA.get(), duration, spellLevel - 1, false, false, true));
            if (level instanceof ServerLevel serverLevel) {
                JadeAuraVfx.spawnCastBurst(serverLevel, entity);
            }

            if (targetAlly != null && targetAlly != entity) {
                targetAlly.addEffect(new MobEffectInstance(MobEffectsRegistry.JADE_AURA.get(), duration, spellLevel - 1, false, false, true));
                if (level instanceof ServerLevel serverLevel) {
                    JadeAuraVfx.spawnCastBurst(serverLevel, targetAlly);
                }
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Nullable
    public static LivingEntity findTargetAlly(Level level, LivingEntity caster, float range) {
        HitResult hitResult = Utils.raycastForEntity(level, caster, range, true, 0.5F);
        if (hitResult instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity livingTarget && isTargetAlly(caster, livingTarget)) {
            return livingTarget;
        }
        return null;
    }

    public static boolean isTargetAlly(LivingEntity caster, LivingEntity target) {
        if (target == null || target == caster) return false;
        if (!target.isAlive() || target.isSpectator()) return false;
        if (target.isAlliedTo(caster)) return true;
        if (caster instanceof Player && target instanceof Player) {
            return DamageSources.isFriendlyFireBetween(caster, target);
        }
        if (target instanceof TamableAnimal tamable && tamable.isOwnedBy(caster)) return true;
        return false;
    }
}
