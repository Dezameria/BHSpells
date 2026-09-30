package net.offkung.bhspells.spells.evocation;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.api.CompatResult;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhspells.service.DingShenFaService;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.AutoSpellConfig;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellAnimations;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@AutoSpellConfig
public class DingShenFaSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, "ding_shen_fa");

    // ==========================================
    // SPELL TUNING CONSTANTS (Code Defaults)
    // ==========================================
    public static final int BASE_MANA_COST = 50;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final double COOLDOWN_SECONDS = 50.0;
    public static final int DURATION_TICKS = 192; // 9.6s
    public static final double RANGE = 50.0;
    public static final double DEFAULT_MULTI_TARGET_RADIUS = 0.0D;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        info.add(Component.translatable("ui.bhspells.ding_shen_fa_duration", Utils.stringTruncation(DURATION_TICKS / 20.0F, 1)));
        info.add(Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(RANGE, 0)));
        double radius = SpellConfig.DingShenFa.getMultiTargetRadius();
        if (radius > 0.0D) {
            info.add(Component.translatable("ui.bhspells.ding_shen_fa_aoe", Utils.stringTruncation(radius, 1)));
        }
        return info;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public DingShenFaSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = DURATION_TICKS;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.DingShenFa.getBaseMana() + (spellLevel - 1) * SpellConfig.DingShenFa.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.DingShenFa.getCooldown() * 20);
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        if (EpicFightCompat.isAvailable()) {
            return Optional.empty();
        }
        return Optional.of(BHSoundRegistry.XULI_DING_SOU.get());
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        // Shift + Cast cancels / lifts Ding on all targets previously immobilized by this caster
        if (entity.isShiftKeyDown()) {
            if (!level.isClientSide) {
                boolean released = DingShenFaService.releaseAllByCaster(entity);
                if (released) {
                    if (entity instanceof Player player) {
                        player.displayClientMessage(Component.translatable("ui.bhspells.ding_shen_fa_cancelled"), true);
                    }
                    playerMagicData.getPlayerCooldowns().addCooldown(this, 30);
                }
            }
            return false;
        }

        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        CompatResult animationResult = EpicFightCompat.playAnimation(entity, AnimationCue.DING_SHEN_FA);
        if (!level.isClientSide && !animationResult.isApplied()) {
            DingShenFaService.cast(entity);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
