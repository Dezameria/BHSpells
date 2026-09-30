package net.offkung.bhspells.spells.gold;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.service.SavageBiteManager;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

import java.util.List;

/**
 * Savage Bite (กัดขย้ำ) - Gold School Spell.
 * Lunges forward to clamp onto target's leg in an Epic Fight swim animation pose,
 * tethering caster to target's ankle, continuously draining mana, dealing periodic damage,
 * and inflicting Slowness II. Released with Spacebar or on caster/target death or mana loss.
 */
@AutoSpellConfig
public class SavageBiteSpell extends AbstractSpell {
    // ==========================================
    // 1. SPELL ID
    // ==========================================
    public static final String SPELL_ID_STR = "savage_bite";
    public static final ResourceLocation SPELL_RESOURCE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, SPELL_ID_STR);
    private final ResourceLocation spellId = SPELL_RESOURCE;
    private static final ResourceLocation GOLD_SCHOOL_RESOURCE = ResourceLocation.fromNamespaceAndPath("bhspells", "gold");

    // ==========================================
    // 2. TUNING CONSTANTS (Code Defaults)
    // ==========================================
    public static final float BASE_DAMAGE = 2.25F;
    public static final float DAMAGE_PER_LEVEL = 0.0F;
    public static final int BASE_MANA_COST = 20;
    public static final int MANA_COST_PER_LEVEL = 0;
    public static final int MANA_DRAIN_PER_SECOND = 10;
    public static final double COOLDOWN_SECONDS = 12.0D;
    public static final float RANGE = 7.0F;

    public static final TagKey<EntityType<?>> IMMUNITY_TAG = TagKey.create(
            Registries.ENTITY_TYPE,
            ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "savage_bite_immune")
    );

    // ==========================================
    // 3. UNIT INFO / DESCRIPTION (Tooltips)
    // ==========================================
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(SpellConfig.SavageBite.getBaseDamage(), 2)),
                Component.translatable("ui.irons_spellbooks.distance", Utils.stringTruncation(SpellConfig.SavageBite.getRange(), 0)),
                Component.translatable("ui.bhspells.savage_bite_drain", SpellConfig.SavageBite.getManaDrainPerSecond()),
                Component.translatable("ui.irons_spellbooks.slowness_effect", 2)
        );
    }

    // ==========================================
    // 4. DEFAULT CONFIG
    // ==========================================
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(GOLD_SCHOOL_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0.0) // Native cooldown is 0; deferred cooldown applies when latch terminates
            .build();

    // ==========================================
    // 5. CONSTRUCTOR
    // ==========================================
    public SavageBiteSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = (int) BASE_DAMAGE;
        this.spellPowerPerLevel = 0;
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
        return SpellConfig.SavageBite.getBaseMana();
    }

    @Override
    public int getSpellCooldown() {
        return 0; // Cooldown applied upon latch release
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
        SchoolType school = SchoolRegistry.getSchool(GOLD_SCHOOL_RESOURCE);
        return school != null ? school : SchoolRegistry.ENDER.get();
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    // ==========================================
    // 7. PRE-CAST & CAST LOGIC
    // ==========================================
    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        if (SavageBiteManager.hasActiveSession(entity)) {
            return false;
        }
        LivingEntity target = findTarget(entity);
        if (target == null) {
            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("ui.irons_spellbooks.cast_error_target"), true);
            }
            return false;
        }
        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            LivingEntity target = findTarget(entity);
            if (target != null) {
                SavageBiteManager.startLunge(entity, target, spellLevel, this);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    public LivingEntity findTarget(LivingEntity caster) {
        float range = (float) SpellConfig.SavageBite.getRange();
        HitResult hit = Utils.raycastForEntity(caster.level(), caster, range, true, 0.6F);
        if (hit instanceof EntityHitResult entityHit && entityHit.getEntity() instanceof LivingEntity living) {
            if (isValidTarget(caster, living)) {
                return living;
            }
        }
        return null;
    }

    public boolean isValidTarget(LivingEntity caster, LivingEntity target) {
        if (target == null || target == caster || !target.isAlive() || target.isSpectator()) {
            return false;
        }
        if (Utils.shouldHealEntity(caster, target) || caster.isAlliedTo(target)) {
            return false;
        }
        if (target.isMultipartEntity() || target.getType().is(IMMUNITY_TAG)) {
            return false;
        }
        return true;
    }
}
