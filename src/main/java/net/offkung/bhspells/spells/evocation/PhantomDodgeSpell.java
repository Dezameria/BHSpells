package net.offkung.bhspells.spells.evocation;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastResult;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class PhantomDodgeSpell extends AbstractSpell {
    // 1. Spell ID
    public static final String SPELL_ID_STR = "phantom_dodge";
    private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, SPELL_ID_STR);

    // 2. Tuning Constants
    public static final int BASE_MANA_COST = 50;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final double COOLDOWN_SECONDS = 50.0;
    public static final int BASE_DURATION_TICKS = 200; // 10s
    public static final int DURATION_PER_LEVEL = 100; // +5s per level
    public static final int BASE_CHARGES = 3;
    public static final int CHARGES_PER_LEVEL = 2; // +2 charges per level

    // 3. Unit Info / Description
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        info.add(Component.translatable("ui.bhspells.phantom_dodge_duration", Utils.timeFromTicks(getDuration(spellLevel), 1)));
        info.add(Component.translatable("ui.bhspells.phantom_dodge_charges", getCharges(spellLevel)));
        info.add(Component.translatable("ui.bhspells.phantom_dodge_toggle_info"));
        return info;
    }

    // 4. Default Config
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    // 5. Constructor
    public PhantomDodgeSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = BASE_CHARGES;
        this.spellPowerPerLevel = CHARGES_PER_LEVEL;
        this.castTime = 0;
    }

    // 6. Getters / Overrides
    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.PhantomDodge.getBaseMana() + (spellLevel - 1) * SpellConfig.PhantomDodge.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.PhantomDodge.getCooldown() * 20);
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ILLUSIONER_MIRROR_MOVE);
    }

    @Override
    public CastResult canBeCastedBy(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player) {
        if (player.hasEffect(MobEffectsRegistry.PHANTOM_DODGE.get())) {
            return new CastResult(CastResult.Type.SUCCESS);
        }
        return super.canBeCastedBy(spellLevel, castSource, playerMagicData, player);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (entity.hasEffect(MobEffectsRegistry.PHANTOM_DODGE.get())) {
            if (!level.isClientSide) {
                entity.removeEffect(MobEffectsRegistry.PHANTOM_DODGE.get());
                if (entity instanceof Player player) {
                    player.displayClientMessage(Component.translatable("ui.bhspells.phantom_dodge_cancelled"), true);
                    int cdTicks = (int) (SpellConfig.PhantomDodge.getCooldown() * 20);
                    playerMagicData.getPlayerCooldowns().addCooldown(this, cdTicks);
                    if (player instanceof ServerPlayer serverPlayer) {
                        playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
                    }
                }
            }
            return false;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    // 7. Cast Logic
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            int duration = getDuration(spellLevel);
            int charges = getCharges(spellLevel);
            int amplifier = Math.max(0, charges - 1);

            entity.removeEffect(MobEffectsRegistry.PHANTOM_DODGE.get());
            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.PHANTOM_DODGE.get(), duration, amplifier, false, false, true));

            EpicFightCompat.triggerPhantomDodge(entity);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public static int getDuration(int spellLevel) {
        int lvl = Math.max(1, spellLevel);
        return SpellConfig.PhantomDodge.getBaseDuration() + (lvl - 1) * SpellConfig.PhantomDodge.getDurationPerLevel();
    }

    public static int getCharges(int spellLevel) {
        int lvl = Math.max(1, spellLevel);
        return SpellConfig.PhantomDodge.getBaseCharges() + (lvl - 1) * SpellConfig.PhantomDodge.getChargesPerLevel();
    }
}
