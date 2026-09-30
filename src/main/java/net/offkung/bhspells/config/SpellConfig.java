package net.offkung.bhspells.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.offkung.bhspells.spells.evocation.DingShenFaSpell;
import net.offkung.bhspells.spells.gold.SavageBiteSpell;
import net.offkung.bhspells.spells.ground.EarthRoarSpell;
import net.offkung.bhspells.spells.ground.ShockingSpell;

import net.offkung.bhspells.spells.aqua.CrimsonRainBathesMoonSpell;
import net.offkung.bhspells.spells.aqua.GlacialFirmamentSpell;
import net.offkung.bhspells.spells.aqua.GlacialVeilSpell;
import net.offkung.bhspells.spells.aqua.ToxicSalvationSpell;
import net.offkung.bhspells.spells.fire.*;
import net.offkung.bhspells.spells.gold.GildedHareSpell;
import net.offkung.bhspells.spells.gold.HymnofPurificationSpell;
import net.offkung.bhspells.spells.gold.ShackleofFearSpell;
import net.offkung.bhspells.spells.gold.SpinStrikeSpell;
import net.offkung.bhspells.spells.ground.JadeAuraSpell;
import net.offkung.bhspells.spells.ground.TigershadeTerrabreakSpell;
import net.offkung.bhspells.spells.lightning.LightningStrikeSpell;
import net.offkung.bhspells.spells.lightning.ThunderStepSpell;
import net.offkung.bhspells.spells.nature.GalePiercerSpell;
import net.offkung.bhspells.spells.nature.RapturousBloomSpell;
import net.offkung.bhspells.spells.nature.VenomousBlossomfallSpell;
import net.offkung.bhspells.spells.nature.WingsofTempestSpell;

public class SpellConfig {
    public static final ForgeConfigSpec SPEC;
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    // ==========================================
    // FIRE SCHOOL
    // ==========================================
    public static class BlazingChakra {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : BlazingChakraSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : BlazingChakraSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : BlazingChakraSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : BlazingChakraSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : BlazingChakraSpell.COOLDOWN_SECONDS;
        }
    }

    public static class SpinStrike {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : SpinStrikeSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : SpinStrikeSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : SpinStrikeSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : SpinStrikeSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : SpinStrikeSpell.COOLDOWN_SECONDS;
        }
    }

    public static class PureWhiteFlameBurst {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.DoubleValue aoeRatio;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : PureWhiteFlameBurstSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : PureWhiteFlameBurstSpell.DAMAGE_PER_LEVEL;
        }

        public static float getAoeRatio() {
            return (SPEC != null && SPEC.isLoaded()) ? aoeRatio.get().floatValue() : PureWhiteFlameBurstSpell.AOE_DAMAGE_RATIO;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : PureWhiteFlameBurstSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : PureWhiteFlameBurstSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : PureWhiteFlameBurstSpell.COOLDOWN_SECONDS;
        }
    }

    public static class GaleDrive {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : GaleDriveSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : GaleDriveSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : GaleDriveSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : GaleDriveSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : GaleDriveSpell.COOLDOWN_SECONDS;
        }
    }

    public static class ResonantKnell {
        public static ForgeConfigSpec.DoubleValue stage1Damage;
        public static ForgeConfigSpec.DoubleValue stage2Damage;
        public static ForgeConfigSpec.DoubleValue stage3Damage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getStage1Damage() {
            return (SPEC != null && SPEC.isLoaded()) ? stage1Damage.get().floatValue() : ResonantKnellSpell.STAGE_1_DAMAGE;
        }

        public static float getStage2Damage() {
            return (SPEC != null && SPEC.isLoaded()) ? stage2Damage.get().floatValue() : ResonantKnellSpell.STAGE_2_DAMAGE;
        }

        public static float getStage3Damage() {
            return (SPEC != null && SPEC.isLoaded()) ? stage3Damage.get().floatValue() : ResonantKnellSpell.STAGE_3_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : ResonantKnellSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : ResonantKnellSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : ResonantKnellSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : ResonantKnellSpell.COOLDOWN_SECONDS;
        }
    }

    public static class CrimsonThornbind {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.DoubleValue rendBaseDamage;
        public static ForgeConfigSpec.DoubleValue rendDamagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : CrimsonThornbindSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : CrimsonThornbindSpell.DAMAGE_PER_LEVEL;
        }

        public static float getRendBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? rendBaseDamage.get().floatValue() : CrimsonThornbindSpell.REND_BASE_DAMAGE;
        }

        public static float getRendDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? rendDamagePerLevel.get().floatValue() : CrimsonThornbindSpell.REND_DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : CrimsonThornbindSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : CrimsonThornbindSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : CrimsonThornbindSpell.COOLDOWN_SECONDS;
        }
    }

    // ==========================================
    // LIGHTNING SCHOOL
    // ==========================================
    public static class LightningStrike {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : LightningStrikeSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : LightningStrikeSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : LightningStrikeSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : LightningStrikeSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : LightningStrikeSpell.COOLDOWN_SECONDS;
        }
    }

    public static class ThunderStep {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : ThunderStepSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : ThunderStepSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : ThunderStepSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : ThunderStepSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : ThunderStepSpell.COOLDOWN_SECONDS;
        }
    }

    // ==========================================
    // GOLD SCHOOL
    // ==========================================
    public static class ShackleofFear {
        public static ForgeConfigSpec.DoubleValue chainHealth;
        public static ForgeConfigSpec.DoubleValue chainHealthPerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getChainHealth() {
            return (SPEC != null && SPEC.isLoaded()) ? chainHealth.get().floatValue() : ShackleofFearSpell.CHAIN_HEALTH;
        }

        public static float getChainHealthPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? chainHealthPerLevel.get().floatValue() : ShackleofFearSpell.CHAIN_HEALTH_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : ShackleofFearSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : ShackleofFearSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : ShackleofFearSpell.COOLDOWN_SECONDS;
        }
    }

    public static class HymnofPurification {
        public static ForgeConfigSpec.DoubleValue baseHeal;
        public static ForgeConfigSpec.DoubleValue healPerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseHeal() {
            return (SPEC != null && SPEC.isLoaded()) ? baseHeal.get().floatValue() : HymnofPurificationSpell.BASE_HEAL;
        }

        public static float getHealPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? healPerLevel.get().floatValue() : HymnofPurificationSpell.HEAL_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : HymnofPurificationSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : HymnofPurificationSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : HymnofPurificationSpell.COOLDOWN_SECONDS;
        }
    }

    public static class GildedHare {
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : GildedHareSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : GildedHareSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : GildedHareSpell.COOLDOWN_SECONDS;
        }
    }

    // ==========================================
    // GROUND SCHOOL
    // ==========================================
    public static class TigershadeTerrabreak {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : TigershadeTerrabreakSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : TigershadeTerrabreakSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : TigershadeTerrabreakSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : TigershadeTerrabreakSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : TigershadeTerrabreakSpell.COOLDOWN_SECONDS;
        }
    }

    public static class JadeAura {
        public static ForgeConfigSpec.DoubleValue baseDuration;
        public static ForgeConfigSpec.DoubleValue durationPerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDuration() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDuration.get().floatValue() : JadeAuraSpell.BASE_DURATION_SECONDS;
        }

        public static float getDurationPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? durationPerLevel.get().floatValue() : JadeAuraSpell.DURATION_PER_LEVEL_SECONDS;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : JadeAuraSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : JadeAuraSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : JadeAuraSpell.COOLDOWN_SECONDS;
        }
    }

    // ==========================================
    // NATURE SCHOOL
    // ==========================================
    public static class WingsofTempest {
        public static ForgeConfigSpec.DoubleValue baseDuration;
        public static ForgeConfigSpec.DoubleValue durationPerLevel;
        public static ForgeConfigSpec.DoubleValue baseRadius;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDuration() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDuration.get().floatValue() : WingsofTempestSpell.BASE_DURATION_SECONDS;
        }

        public static float getDurationPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? durationPerLevel.get().floatValue() : WingsofTempestSpell.DURATION_PER_LEVEL_SECONDS;
        }

        public static float getBaseRadius() {
            return (SPEC != null && SPEC.isLoaded()) ? baseRadius.get().floatValue() : WingsofTempestSpell.BASE_RADIUS;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : WingsofTempestSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : WingsofTempestSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : WingsofTempestSpell.COOLDOWN_SECONDS;
        }
    }

    public static class VenomousBlossomfall {
        public static ForgeConfigSpec.DoubleValue shortDamage;
        public static ForgeConfigSpec.DoubleValue mediumDamage;
        public static ForgeConfigSpec.DoubleValue fullDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getShortDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? shortDamage.get().floatValue() : VenomousBlossomfallSpell.SHORT_DIRECT_DAMAGE;
        }

        public static float getMediumDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? mediumDamage.get().floatValue() : VenomousBlossomfallSpell.MEDIUM_DIRECT_DAMAGE;
        }

        public static float getFullDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? fullDamage.get().floatValue() : VenomousBlossomfallSpell.FULL_DIRECT_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : VenomousBlossomfallSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : VenomousBlossomfallSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : VenomousBlossomfallSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : VenomousBlossomfallSpell.COOLDOWN_SECONDS;
        }
    }

    public static class GalePiercer {
        public static ForgeConfigSpec.DoubleValue normalBaseDamage;
        public static ForgeConfigSpec.DoubleValue normalDamagePerLevel;
        public static ForgeConfigSpec.DoubleValue fullBaseDamage;
        public static ForgeConfigSpec.DoubleValue fullDamagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getNormalBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? normalBaseDamage.get().floatValue() : GalePiercerSpell.NORMAL_BASE_DAMAGE;
        }

        public static float getNormalDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? normalDamagePerLevel.get().floatValue() : GalePiercerSpell.NORMAL_DAMAGE_PER_LEVEL;
        }

        public static float getFullBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? fullBaseDamage.get().floatValue() : GalePiercerSpell.FULL_BASE_DAMAGE;
        }

        public static float getFullDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? fullDamagePerLevel.get().floatValue() : GalePiercerSpell.FULL_DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : GalePiercerSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : GalePiercerSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : GalePiercerSpell.COOLDOWN_SECONDS;
        }
    }

    public static class RapturousBloom {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : RapturousBloomSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : RapturousBloomSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : RapturousBloomSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : RapturousBloomSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : RapturousBloomSpell.COOLDOWN_SECONDS;
        }
    }

    // ==========================================
    // AQUA SCHOOL
    // ==========================================
    public static class CrimsonRainBathesMoon {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : CrimsonRainBathesMoonSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : CrimsonRainBathesMoonSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : CrimsonRainBathesMoonSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : CrimsonRainBathesMoonSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : CrimsonRainBathesMoonSpell.COOLDOWN_SECONDS;
        }
    }

    public static class GlacialVeil {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : GlacialVeilSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : GlacialVeilSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : GlacialVeilSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : GlacialVeilSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : GlacialVeilSpell.COOLDOWN_SECONDS;
        }
    }

    public static class ToxicSalvation {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.DoubleValue baseHeal;
        public static ForgeConfigSpec.DoubleValue healPerLevel;
        public static ForgeConfigSpec.IntValue baseDuration;
        public static ForgeConfigSpec.DoubleValue durationPerLevel;
        public static ForgeConfigSpec.DoubleValue baseRadius;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : ToxicSalvationSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : ToxicSalvationSpell.DAMAGE_PER_LEVEL;
        }

        public static float getBaseHeal() {
            return (SPEC != null && SPEC.isLoaded()) ? baseHeal.get().floatValue() : ToxicSalvationSpell.BASE_HEAL;
        }

        public static float getHealPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? healPerLevel.get().floatValue() : ToxicSalvationSpell.HEAL_PER_LEVEL;
        }

        public static int getBaseDuration() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDuration.get() : ToxicSalvationSpell.BASE_DURATION_SECONDS;
        }

        public static double getDurationPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? durationPerLevel.get() : ToxicSalvationSpell.DURATION_PER_LEVEL_SECONDS;
        }

        public static float getBaseRadius() {
            return (SPEC != null && SPEC.isLoaded()) ? baseRadius.get().floatValue() : ToxicSalvationSpell.BASE_RADIUS;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : ToxicSalvationSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : ToxicSalvationSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : ToxicSalvationSpell.COOLDOWN_SECONDS;
        }
    }

    public static class GlacialFirmament {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : GlacialFirmamentSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : GlacialFirmamentSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : GlacialFirmamentSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : GlacialFirmamentSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : GlacialFirmamentSpell.COOLDOWN_SECONDS;
        }
    }

    // ==========================================
    // EVOCATION SCHOOL
    // ==========================================
    public static class DingShenFa {
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;
        public static ForgeConfigSpec.DoubleValue multiTargetRadius;

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : DingShenFaSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : DingShenFaSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : DingShenFaSpell.COOLDOWN_SECONDS;
        }

        public static double getMultiTargetRadius() {
            return (SPEC != null && SPEC.isLoaded()) ? multiTargetRadius.get() : 0.0D;
        }
    }

    public static class PhantomDodge {
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;
        public static ForgeConfigSpec.IntValue baseDuration;
        public static ForgeConfigSpec.IntValue durationPerLevel;
        public static ForgeConfigSpec.IntValue baseCharges;
        public static ForgeConfigSpec.IntValue chargesPerLevel;

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.COOLDOWN_SECONDS;
        }

        public static int getBaseDuration() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDuration.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.BASE_DURATION_TICKS;
        }

        public static int getDurationPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? durationPerLevel.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.DURATION_PER_LEVEL;
        }

        public static int getBaseCharges() {
            return (SPEC != null && SPEC.isLoaded()) ? baseCharges.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.BASE_CHARGES;
        }

        public static int getChargesPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? chargesPerLevel.get() : net.offkung.bhspells.spells.evocation.PhantomDodgeSpell.CHARGES_PER_LEVEL;
        }
    }

    public static class SpiritualPressure {
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;
        public static ForgeConfigSpec.DoubleValue baseRadius;
        public static ForgeConfigSpec.DoubleValue radiusPerLevel;
        public static ForgeConfigSpec.IntValue globalStreakBudget;
        public static ForgeConfigSpec.IntValue durationTicks;

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : 75;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : 15;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : 35.0D;
        }

        public static float getBaseRadius() {
            return (SPEC != null && SPEC.isLoaded()) ? baseRadius.get().floatValue() : 12.0F;
        }

        public static float getRadiusPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? radiusPerLevel.get().floatValue() : 2.0F;
        }

        public static int getGlobalStreakBudget() {
            return (SPEC != null && SPEC.isLoaded()) ? globalStreakBudget.get() : 256;
        }

        public static int getDurationTicks() {
            return (SPEC != null && SPEC.isLoaded()) ? durationTicks.get() : 300;
        }
    }

    public static class SavageBite {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaDrainPerSecond;
        public static ForgeConfigSpec.DoubleValue cooldown;
        public static ForgeConfigSpec.DoubleValue range;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : SavageBiteSpell.BASE_DAMAGE;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : SavageBiteSpell.BASE_MANA_COST;
        }

        public static int getManaDrainPerSecond() {
            return (SPEC != null && SPEC.isLoaded()) ? manaDrainPerSecond.get() : SavageBiteSpell.MANA_DRAIN_PER_SECOND;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : SavageBiteSpell.COOLDOWN_SECONDS;
        }

        public static double getRange() {
            return (SPEC != null && SPEC.isLoaded()) ? range.get() : SavageBiteSpell.RANGE;
        }
    }

    public static class EarthRoar {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : EarthRoarSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : EarthRoarSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : EarthRoarSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : EarthRoarSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : EarthRoarSpell.COOLDOWN_SECONDS;
        }
    }

    public static class Shocking {
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : ShockingSpell.BASE_DAMAGE;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : ShockingSpell.DAMAGE_PER_LEVEL;
        }

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : ShockingSpell.BASE_MANA_COST;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : ShockingSpell.MANA_COST_PER_LEVEL;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : ShockingSpell.COOLDOWN_SECONDS;
        }
    }

    public static class VengefulPressure {
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;
        public static ForgeConfigSpec.DoubleValue baseRadius;
        public static ForgeConfigSpec.DoubleValue radiusPerLevel;
        public static ForgeConfigSpec.IntValue globalStreakBudget;

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : 75;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : 15;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : 40.0D;
        }

        public static float getBaseRadius() {
            return (SPEC != null && SPEC.isLoaded()) ? baseRadius.get().floatValue() : 16.0F;
        }

        public static float getRadiusPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? radiusPerLevel.get().floatValue() : 2.0F;
        }

        public static int getGlobalStreakBudget() {
            return (SPEC != null && SPEC.isLoaded()) ? globalStreakBudget.get() : 120;
        }
    }

    public static class TempestReiatsu {
        public static ForgeConfigSpec.IntValue baseMana;
        public static ForgeConfigSpec.IntValue manaPerLevel;
        public static ForgeConfigSpec.DoubleValue cooldown;
        public static ForgeConfigSpec.DoubleValue baseDamage;
        public static ForgeConfigSpec.DoubleValue damagePerLevel;
        public static ForgeConfigSpec.DoubleValue baseRadius;
        public static ForgeConfigSpec.DoubleValue radiusPerLevel;
        public static ForgeConfigSpec.IntValue strikeIntervalMin;
        public static ForgeConfigSpec.IntValue strikeIntervalMax;
        public static ForgeConfigSpec.IntValue globalStreakBudget;

        public static int getBaseMana() {
            return (SPEC != null && SPEC.isLoaded()) ? baseMana.get() : 120;
        }

        public static int getManaPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? manaPerLevel.get() : 30;
        }

        public static double getCooldown() {
            return (SPEC != null && SPEC.isLoaded()) ? cooldown.get() : 60.0D;
        }

        public static float getBaseDamage() {
            return (SPEC != null && SPEC.isLoaded()) ? baseDamage.get().floatValue() : 10.0F;
        }

        public static float getDamagePerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? damagePerLevel.get().floatValue() : 2.5F;
        }

        public static float getBaseRadius() {
            return (SPEC != null && SPEC.isLoaded()) ? baseRadius.get().floatValue() : 64.0F;
        }

        public static float getRadiusPerLevel() {
            return (SPEC != null && SPEC.isLoaded()) ? radiusPerLevel.get().floatValue() : 14.0F;
        }

        public static int getStrikeIntervalMin() {
            return (SPEC != null && SPEC.isLoaded()) ? strikeIntervalMin.get() : 10;
        }

        public static int getStrikeIntervalMax() {
            return (SPEC != null && SPEC.isLoaded()) ? strikeIntervalMax.get() : 30;
        }

        public static int getGlobalStreakBudget() {
            return (SPEC != null && SPEC.isLoaded()) ? globalStreakBudget.get() : 240;
        }
    }

    static {
        BUILDER.comment("IronSpell More Spell Tuning Configuration").push("spells");

        // FIRE
        BUILDER.push("blazing_chakra");
        BlazingChakra.baseDamage = BUILDER.comment("Base damage at Level 1").defineInRange("base_damage", 20.0D, 0.0D, 10000.0D);
        BlazingChakra.damagePerLevel = BUILDER.comment("Damage increase per level (when cast via /cast)").defineInRange("damage_per_level", 4.0D, 0.0D, 1000.0D);
        BlazingChakra.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 50, 0, 10000);
        BlazingChakra.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 10, 0, 1000);
        BlazingChakra.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 15.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("spin_strike");
        SpinStrike.baseDamage = BUILDER.comment("Base damage at Level 1").defineInRange("base_damage", 10.0D, 0.0D, 10000.0D);
        SpinStrike.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 1.0D, 0.0D, 1000.0D);
        SpinStrike.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 30, 0, 10000);
        SpinStrike.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        SpinStrike.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 10.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("pure_white_flame_burst");
        PureWhiteFlameBurst.baseDamage = BUILDER.comment("Base damage at Level 1").defineInRange("base_damage", 80.0D, 0.0D, 10000.0D);
        PureWhiteFlameBurst.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 10.0D, 0.0D, 1000.0D);
        PureWhiteFlameBurst.aoeRatio = BUILDER.comment("AoE blast damage multiplier relative to direct damage").defineInRange("aoe_damage_ratio", 0.8D, 0.0D, 10.0D);
        PureWhiteFlameBurst.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 50, 0, 10000);
        PureWhiteFlameBurst.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 10, 0, 1000);
        PureWhiteFlameBurst.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 15.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("gale_drive");
        GaleDrive.baseDamage = BUILDER.comment("Base collision damage at Level 1").defineInRange("base_damage", 10.0D, 0.0D, 10000.0D);
        GaleDrive.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 2.0D, 0.0D, 1000.0D);
        GaleDrive.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        GaleDrive.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        GaleDrive.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 20.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("resonant_knell");
        ResonantKnell.stage1Damage = BUILDER.comment("Push 1 shockwave damage").defineInRange("stage1_damage", 6.0D, 0.0D, 10000.0D);
        ResonantKnell.stage2Damage = BUILDER.comment("Push 2 shockwave damage").defineInRange("stage2_damage", 10.0D, 0.0D, 10000.0D);
        ResonantKnell.stage3Damage = BUILDER.comment("Push 3 final nuclear blast shockwave damage").defineInRange("stage3_damage", 16.0D, 0.0D, 10000.0D);
        ResonantKnell.damagePerLevel = BUILDER.comment("Bonus damage per level").defineInRange("damage_per_level", 2.0D, 0.0D, 1000.0D);
        ResonantKnell.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 75, 0, 10000);
        ResonantKnell.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 0, 0, 1000);
        ResonantKnell.cooldown = BUILDER.comment("Post-barrier cooldown in seconds").defineInRange("cooldown_seconds", 30.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("crimson_thornbind");
        CrimsonThornbind.baseDamage = BUILDER.comment("Base root impact damage at Level 1").defineInRange("base_damage", 12.0D, 0.0D, 10000.0D);
        CrimsonThornbind.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 2.5D, 0.0D, 1000.0D);
        CrimsonThornbind.rendBaseDamage = BUILDER.comment("Base rend (bleed) damage").defineInRange("rend_base_damage", 6.0D, 0.0D, 10000.0D);
        CrimsonThornbind.rendDamagePerLevel = BUILDER.comment("Rend damage increase per level").defineInRange("rend_damage_per_level", 1.5D, 0.0D, 1000.0D);
        CrimsonThornbind.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        CrimsonThornbind.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        CrimsonThornbind.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 25.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        // LIGHTNING
        BUILDER.push("lightning_strike");
        LightningStrike.baseDamage = BUILDER.comment("Base lightning strike damage at Level 1").defineInRange("base_damage", 10.0D, 0.0D, 10000.0D);
        LightningStrike.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 2.0D, 0.0D, 1000.0D);
        LightningStrike.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 50, 0, 10000);
        LightningStrike.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        LightningStrike.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 15.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("thunder_step");
        ThunderStep.baseDamage = BUILDER.comment("Base dash damage at Level 1").defineInRange("base_damage", 10.0D, 0.0D, 10000.0D);
        ThunderStep.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 2.0D, 0.0D, 1000.0D);
        ThunderStep.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 75, 0, 10000);
        ThunderStep.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 15, 0, 1000);
        ThunderStep.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 8.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        // GOLD
        BUILDER.push("shackle_of_fear");
        ShackleofFear.chainHealth = BUILDER.comment("Base golden chain HP").defineInRange("chain_health", 15.0D, 1.0D, 10000.0D);
        ShackleofFear.chainHealthPerLevel = BUILDER.comment("Chain HP increase per level").defineInRange("chain_health_per_level", 3.0D, 0.0D, 1000.0D);
        ShackleofFear.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        ShackleofFear.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 8, 0, 1000);
        ShackleofFear.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 0.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("hymn_of_purification");
        HymnofPurification.baseHeal = BUILDER.comment("Base healing amount per pulse").defineInRange("base_heal", 1.0D, 0.1D, 10000.0D);
        HymnofPurification.healPerLevel = BUILDER.comment("Healing amount increase per level").defineInRange("heal_per_level", 0.5D, 0.0D, 1000.0D);
        HymnofPurification.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 80, 0, 10000);
        HymnofPurification.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 0, 0, 1000);
        HymnofPurification.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 60.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("gilded_hare");
        GildedHare.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 80, 0, 10000);
        GildedHare.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 0, 0, 1000);
        GildedHare.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 60.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        // GROUND
        BUILDER.push("tigershade_terrabreak");
        TigershadeTerrabreak.baseDamage = BUILDER.comment("Base slam damage at Level 1").defineInRange("base_damage", 30.0D, 0.0D, 10000.0D);
        TigershadeTerrabreak.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 3.0D, 0.0D, 1000.0D);
        TigershadeTerrabreak.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        TigershadeTerrabreak.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 0, 0, 1000);
        TigershadeTerrabreak.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 30.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("jade_aura");
        JadeAura.baseDuration = BUILDER.comment("Base aura duration in seconds").defineInRange("base_duration_seconds", 45.0D, 1.0D, 3600.0D);
        JadeAura.durationPerLevel = BUILDER.comment("Duration increase per level in seconds").defineInRange("duration_per_level_seconds", 10.0D, 0.0D, 600.0D);
        JadeAura.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 50, 0, 10000);
        JadeAura.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 25, 0, 1000);
        JadeAura.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 40.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        // NATURE
        BUILDER.push("wings_of_tempest");
        WingsofTempest.baseDuration = BUILDER.comment("Base storm duration in seconds").defineInRange("base_duration_seconds", 14.25D, 1.0D, 3600.0D);
        WingsofTempest.durationPerLevel = BUILDER.comment("Duration increase per level in seconds").defineInRange("duration_per_level_seconds", 2.25D, 0.0D, 600.0D);
        WingsofTempest.baseRadius = BUILDER.comment("Base storm radius").defineInRange("base_radius", 8.0D, 1.0D, 64.0D);
        WingsofTempest.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        WingsofTempest.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 10, 0, 1000);
        WingsofTempest.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 22.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("venomous_blossomfall");
        VenomousBlossomfall.shortDamage = BUILDER.comment("Short charge direct damage").defineInRange("short_damage", 15.0D, 0.0D, 10000.0D);
        VenomousBlossomfall.mediumDamage = BUILDER.comment("Medium charge direct damage").defineInRange("medium_damage", 35.0D, 0.0D, 10000.0D);
        VenomousBlossomfall.fullDamage = BUILDER.comment("Full charge direct damage").defineInRange("full_damage", 50.0D, 0.0D, 10000.0D);
        VenomousBlossomfall.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 5.0D, 0.0D, 1000.0D);
        VenomousBlossomfall.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        VenomousBlossomfall.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        VenomousBlossomfall.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 20.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("gale_piercer");
        GalePiercer.normalBaseDamage = BUILDER.comment("Normal release arrow base damage").defineInRange("normal_base_damage", 12.0D, 0.0D, 10000.0D);
        GalePiercer.normalDamagePerLevel = BUILDER.comment("Normal arrow damage increase per level").defineInRange("normal_damage_per_level", 2.0D, 0.0D, 1000.0D);
        GalePiercer.fullBaseDamage = BUILDER.comment("Full charge arrow base damage").defineInRange("full_base_damage", 28.0D, 0.0D, 10000.0D);
        GalePiercer.fullDamagePerLevel = BUILDER.comment("Full charge arrow damage increase per level").defineInRange("full_damage_per_level", 4.0D, 0.0D, 1000.0D);
        GalePiercer.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        GalePiercer.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        GalePiercer.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 20.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("rapturous_bloom");
        RapturousBloom.baseDamage = BUILDER.comment("Base lotus bloom burst damage at Level 1").defineInRange("base_damage", 24.0D, 0.0D, 10000.0D);
        RapturousBloom.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 4.0D, 0.0D, 1000.0D);
        RapturousBloom.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 45, 0, 10000);
        RapturousBloom.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        RapturousBloom.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 16.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        // AQUA
        BUILDER.push("crimson_rain_bathes_moon");
        CrimsonRainBathesMoon.baseDamage = BUILDER.comment("Spear impact base damage at Level 1").defineInRange("base_damage", 7.0D, 0.0D, 10000.0D);
        CrimsonRainBathesMoon.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 5.0D, 0.0D, 1000.0D);
        CrimsonRainBathesMoon.baseMana = BUILDER.comment("Continuous cast mana cost per tick interval").defineInRange("base_mana", 5, 0, 10000);
        CrimsonRainBathesMoon.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 3, 0, 1000);
        CrimsonRainBathesMoon.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 60.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("glacial_veil");
        GlacialVeil.baseDamage = BUILDER.comment("Frost shockwave base damage at Level 1").defineInRange("base_damage", 20.0D, 0.0D, 10000.0D);
        GlacialVeil.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 3.0D, 0.0D, 1000.0D);
        GlacialVeil.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 50, 0, 10000);
        GlacialVeil.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 10, 0, 1000);
        GlacialVeil.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 20.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("toxic_salvation");
        ToxicSalvation.baseDamage = BUILDER.comment("Toxic mist base damage at Level 1").defineInRange("base_damage", 4.0D, 0.0D, 10000.0D);
        ToxicSalvation.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 1.0D, 0.0D, 1000.0D);
        ToxicSalvation.baseHeal = BUILDER.comment("Healing amount at Level 1").defineInRange("base_heal", 2.0D, 0.0D, 10000.0D);
        ToxicSalvation.healPerLevel = BUILDER.comment("Heal increase per level").defineInRange("heal_per_level", 0.5D, 0.0D, 1000.0D);
        ToxicSalvation.baseDuration = BUILDER.comment("Base duration in seconds").defineInRange("base_duration", 10, 1, 3600);
        ToxicSalvation.durationPerLevel = BUILDER.comment("Duration increase per level in seconds").defineInRange("duration_per_level", 1.5D, 0.0D, 3600.0D);
        ToxicSalvation.baseRadius = BUILDER.comment("Base radius in blocks").defineInRange("base_radius", 5.0D, 0.5D, 64.0D);
        ToxicSalvation.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 40, 0, 10000);
        ToxicSalvation.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        ToxicSalvation.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 22.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("glacial_firmament");
        GlacialFirmament.baseDamage = BUILDER.comment("Ice spike eruption base damage at Level 1").defineInRange("base_damage", 35.0D, 0.0D, 10000.0D);
        GlacialFirmament.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 5.0D, 0.0D, 1000.0D);
        GlacialFirmament.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 60, 0, 10000);
        GlacialFirmament.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 15, 0, 1000);
        GlacialFirmament.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 25.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("ding_shen_fa");
        DingShenFa.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 120, 0, 10000);
        DingShenFa.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 20, 0, 1000);
        DingShenFa.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 30.0D, 0.0D, 3600.0D);
        DingShenFa.multiTargetRadius = BUILDER.comment("AoE multi-target radius in blocks (0 to disable, single-target only)").defineInRange("multi_target_radius", 0.0D, 0.0D, 64.0D);
        BUILDER.pop();

        BUILDER.push("savage_bite");
        SavageBite.baseDamage = BUILDER.comment("Periodic bite damage").defineInRange("base_damage", 6.0D, 0.0D, 10000.0D);
        SavageBite.baseMana = BUILDER.comment("Initial mana cost to initiate lunge").defineInRange("base_mana", 50, 0, 10000);
        SavageBite.manaDrainPerSecond = BUILDER.comment("Continuous mana drain per second while latched").defineInRange("mana_drain_per_sec", 15, 0, 10000);
        SavageBite.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 25.0D, 0.0D, 3600.0D);
        SavageBite.range = BUILDER.comment("Maximum lunge targeting range in blocks").defineInRange("range_blocks", 12.0D, 1.0D, 64.0D);
        BUILDER.pop();

        BUILDER.push("earth_roar");
        EarthRoar.baseDamage = BUILDER.comment("Earth Roar impact base damage at Level 1").defineInRange("base_damage", 35.0D, 0.0D, 10000.0D);
        EarthRoar.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 5.0D, 0.0D, 1000.0D);
        EarthRoar.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 150, 0, 10000);
        EarthRoar.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 25, 0, 1000);
        EarthRoar.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 120.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("shocking");
        Shocking.baseDamage = BUILDER.comment("Shocking beam base DPS at Level 1").defineInRange("base_damage", 8.0D, 0.0D, 10000.0D);
        Shocking.damagePerLevel = BUILDER.comment("Damage increase per level").defineInRange("damage_per_level", 1.5D, 0.0D, 1000.0D);
        Shocking.baseMana = BUILDER.comment("Continuous cast mana cost per second").defineInRange("base_mana", 30, 0, 10000);
        Shocking.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 5, 0, 1000);
        Shocking.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 15.0D, 0.0D, 3600.0D);
        BUILDER.pop();

        BUILDER.push("spiritual_pressure");
        SpiritualPressure.baseMana = BUILDER.comment("Base mana cost to activate").defineInRange("base_mana", 75, 0, 10000);
        SpiritualPressure.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 15, 0, 1000);
        SpiritualPressure.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 35.0D, 0.0D, 3600.0D);
        SpiritualPressure.baseRadius = BUILDER.comment("Base field radius in blocks").defineInRange("base_radius", 12.0D, 1.0D, 64.0D);
        SpiritualPressure.radiusPerLevel = BUILDER.comment("Radius increase per level").defineInRange("radius_per_level", 2.0D, 0.0D, 32.0D);
        SpiritualPressure.globalStreakBudget = BUILDER.comment("Global visual streak particle budget").defineInRange("global_streak_budget", 256, 16, 2048);
        SpiritualPressure.durationTicks = BUILDER.comment("Field duration in ticks (20 ticks = 1 second)").defineInRange("duration_ticks", 300, 20, 7200);
        BUILDER.pop();

        BUILDER.push("vengeful_pressure");
        VengefulPressure.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 75, 0, 10000);
        VengefulPressure.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 15, 0, 1000);
        VengefulPressure.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 40.0D, 0.0D, 3600.0D);
        VengefulPressure.baseRadius = BUILDER.comment("Base radius in blocks").defineInRange("base_radius", 16.0D, 1.0D, 128.0D);
        VengefulPressure.radiusPerLevel = BUILDER.comment("Radius increase per level").defineInRange("radius_per_level", 2.0D, 0.0D, 32.0D);
        VengefulPressure.globalStreakBudget = BUILDER.comment("Global streak budget").defineInRange("global_streak_budget", 120, 0, 1000);
        BUILDER.pop();

        BUILDER.push("tempest_reiatsu");
        TempestReiatsu.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 120, 0, 10000);
        TempestReiatsu.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 30, 0, 1000);
        TempestReiatsu.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 60.0D, 0.0D, 3600.0D);
        TempestReiatsu.baseDamage = BUILDER.comment("Base strike damage").defineInRange("base_damage", 10.0D, 0.0D, 10000.0D);
        TempestReiatsu.damagePerLevel = BUILDER.comment("Strike damage increase per level").defineInRange("damage_per_level", 2.5D, 0.0D, 1000.0D);
        TempestReiatsu.baseRadius = BUILDER.comment("Base radius in blocks").defineInRange("base_radius", 64.0D, 1.0D, 256.0D);
        TempestReiatsu.radiusPerLevel = BUILDER.comment("Radius increase per level").defineInRange("radius_per_level", 14.0D, 0.0D, 64.0D);
        TempestReiatsu.strikeIntervalMin = BUILDER.comment("Min ticks between strikes").defineInRange("strike_interval_min", 10, 1, 200);
        TempestReiatsu.strikeIntervalMax = BUILDER.comment("Max ticks between strikes").defineInRange("strike_interval_max", 30, 1, 400);
        TempestReiatsu.globalStreakBudget = BUILDER.comment("Global streak budget").defineInRange("global_streak_budget", 240, 0, 2000);
        BUILDER.pop();

        // EVOCATION - PHANTOM DODGE
        BUILDER.push("phantom_dodge");
        PhantomDodge.baseMana = BUILDER.comment("Base mana cost").defineInRange("base_mana", 50, 0, 10000);
        PhantomDodge.manaPerLevel = BUILDER.comment("Mana cost increase per level").defineInRange("mana_per_level", 0, 0, 1000);
        PhantomDodge.cooldown = BUILDER.comment("Cooldown in seconds").defineInRange("cooldown_seconds", 50.0D, 0.0D, 3600.0D);
        PhantomDodge.baseDuration = BUILDER.comment("Base duration in ticks (200 = 10s)").defineInRange("base_duration_ticks", 200, 20, 72000);
        PhantomDodge.durationPerLevel = BUILDER.comment("Duration increase per level in ticks (100 = 5s)").defineInRange("duration_per_level_ticks", 100, 0, 72000);
        PhantomDodge.baseCharges = BUILDER.comment("Base dodge charges").defineInRange("base_charges", 3, 1, 100);
        PhantomDodge.chargesPerLevel = BUILDER.comment("Charges increase per level").defineInRange("charges_per_level", 2, 0, 100);
        BUILDER.pop();

        BUILDER.pop(); // spells
        SPEC = BUILDER.build();
    }
}

