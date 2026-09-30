package net.offkung.bhspells.spells.ground;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.compat.api.AnimationCue;
import net.offkung.bhspells.compat.epicfight.EpicFightCompat;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhspells.service.EarthRoarDashManager;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Earth Roar (ปฐพีคำราม)
 * Damage spell executing a high-speed piercing 40-block earth dash kick.
 * Grants a 60-second empowerment buff (Resistance II, Haste I, Strength II), followed by
 * a 60-second exhaustion debuff upon natural expiry.
 */
@AutoSpellConfig
public class EarthRoarSpell extends AbstractSpell {
    // ==========================================
    // 1. SPELL ID
    // ==========================================
    public static final String SPELL_ID_STR = "bhspells:earth_roar";
    public static final ResourceLocation SPELL_RESOURCE = BHSpells.id("earth_roar");
    private final ResourceLocation spellId = SPELL_RESOURCE;
    private static final ResourceLocation GROUND_SCHOOL_RESOURCE = ResourceLocation.fromNamespaceAndPath("bhspells", "ground");

    // ==========================================
    // 2. CONSTANTS (Tuning & Code Defaults)
    // ==========================================
    public static final float BASE_DAMAGE = 35.0F;
    public static final float DAMAGE_PER_LEVEL = 0.0F;
    public static final int BASE_MANA_COST = 80;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final double COOLDOWN_SECONDS = 120.0D;
    public static final float DASH_DISTANCE = 40.0F;
    public static final int STUN_DURATION_TICKS = 100; // 5.0 seconds
    public static final int BUFF_DURATION_TICKS = 1200; // 60 seconds
    public static final int EXHAUSTION_DURATION_TICKS = 1200; // 60 seconds

    // ==========================================
    // 3. UNIT INFO / DESCRIPTION (Tooltips)
    // ==========================================
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(DASH_DISTANCE, 0)),
                Component.translatable("ui.bhspells.earth_roar_stun", Utils.timeFromTicks(STUN_DURATION_TICKS, 1)),
                Component.translatable("ui.bhspells.earth_roar_buff_duration", Utils.timeFromTicks(BUFF_DURATION_TICKS, 1)),
                Component.translatable("ui.bhspells.earth_roar_debuff_duration", Utils.timeFromTicks(EXHAUSTION_DURATION_TICKS, 1))
        );
    }

    // ==========================================
    // 4. DEFAULT CONFIG
    // ==========================================
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(GROUND_SCHOOL_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    // ==========================================
    // 5. CONSTRUCTOR
    // ==========================================
    public EarthRoarSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = (int) BASE_DAMAGE;
        this.spellPowerPerLevel = (int) DAMAGE_PER_LEVEL;
        this.castTime = 0;
    }

    // ==========================================
    // 6. GETTERS & OVERRIDES
    // ==========================================
    @Override
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.EarthRoar.getBaseMana() + (spellLevel - 1) * SpellConfig.EarthRoar.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.EarthRoar.getCooldown() * 20);
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
    public SchoolType getSchoolType() {
        SchoolType school = SchoolRegistry.getSchool(GROUND_SCHOOL_RESOURCE);
        return school != null ? school : SchoolRegistry.EVOCATION.get();
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    public float getDamage(int spellLevel, LivingEntity caster) {
        float base = SpellConfig.EarthRoar.getBaseDamage();
        float perLevel = SpellConfig.EarthRoar.getDamagePerLevel();
        return (base + (spellLevel - 1) * perLevel) * getEntityPowerMultiplier(caster);
    }

    // ==========================================
    // 7. PRE-CAST & CAST LOGIC
    // ==========================================
    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            // Apply 60-second empowerment buffs without potion swirl particles (visible: false, showIcon: true)
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, BUFF_DURATION_TICKS, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DIG_SPEED, BUFF_DURATION_TICKS, 0, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, BUFF_DURATION_TICKS, 1, false, false, true));
            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.EARTH_ROAR_EMPOWERMENT.get(), BUFF_DURATION_TICKS, 0, false, false, true));

            // Start 36-tick server dash state machine
            EarthRoarDashManager.startDash(entity, spellLevel, this);
        }

        // Trigger Epic Fight synchronized charge animation (kneel)
        EpicFightCompat.playAnimation(entity, AnimationCue.EARTH_ROAR_CHARGE, 0.15F);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
