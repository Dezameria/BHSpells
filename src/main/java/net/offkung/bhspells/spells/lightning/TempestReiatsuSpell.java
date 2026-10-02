package net.offkung.bhspells.spells.lightning;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.config.SpellConfig;
import net.offkung.bhspells.pressure.PressureAnchor;
import net.offkung.bhspells.pressure.PressureFieldData;
import net.offkung.bhspells.pressure.PressureVisualProfile;
import net.offkung.bhspells.pressure.server.ServerPressureManager;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Tempest Reiatsu (แรงดันอัสนีบาตสีชมพู / 绯雷威压)
 * Stance toggle spell activating a catastrophic 64 - 120 block domain of pink lightning storm deluge.
 * Envelops nearby players in an unceasing torrential camera-centric pink pressure curtain and periodic lightning strikes.
 */
@AutoSpellConfig
public class TempestReiatsuSpell extends AbstractSpell {
    // ==========================================
    // 1. SPELL ID
    // ==========================================
    public static final String SPELL_ID_STR = "bhspells:tempest_reiatsu";
    private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, "tempest_reiatsu");

    // ==========================================
    // 2. CONSTANTS (Tuning & Code Defaults)
    // ==========================================
    public static final int BASE_MANA_COST = 50;
    public static final int MANA_COST_PER_LEVEL = 30;
    public static final double COOLDOWN_SECONDS = 60.0;
    public static final float BASE_RADIUS = 64.0F;
    public static final float RADIUS_PER_LEVEL = 14.0F;

    // ==========================================
    // 3. UNIT INFO / DESCRIPTION (Tooltips)
    // ==========================================
    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        float radius = getRadius(spellLevel);
        float damage = SpellConfig.TempestReiatsu.getBaseDamage() + (spellLevel - 1) * SpellConfig.TempestReiatsu.getDamagePerLevel();
        info.add(Component.translatable("ui.bhspells.tempest_reiatsu_radius", Utils.stringTruncation(radius, 1)));
        info.add(Component.translatable("ui.bhspells.tempest_reiatsu_damage", Utils.stringTruncation(damage, 1)));
        info.add(Component.translatable("ui.bhspells.tempest_reiatsu_toggle_info"));
        return info;
    }

    // ==========================================
    // 4. DEFAULT CONFIG
    // ==========================================
    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.LIGHTNING_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(0.0) // No cooldown on opening; closing handles cooldown manually
            .build();

    // ==========================================
    // 5. CONSTRUCTOR
    // ==========================================
    public TempestReiatsuSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = 0;
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
        return SpellConfig.TempestReiatsu.getBaseMana() + (spellLevel - 1) * SpellConfig.TempestReiatsu.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return 0; // Cooldown applied only upon toggling off
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
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    public float getRadius(int spellLevel) {
        return SpellConfig.TempestReiatsu.getBaseRadius() + (spellLevel - 1) * SpellConfig.TempestReiatsu.getRadiusPerLevel();
    }

    // ==========================================
    // 7. PRE-CAST & CAST LOGIC
    // ==========================================
    @Override
    public CastResult canBeCastedBy(int spellLevel, CastSource castSource, MagicData playerMagicData, Player player) {
        if (net.offkung.bhspells.pressure.PressureToggleHelper.isDomainActive(player, SPELL_ID_STR)) {
            return new CastResult(CastResult.Type.SUCCESS);
        }
        return super.canBeCastedBy(spellLevel, castSource, playerMagicData, player);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        // Toggle Stance Check: If domain is already active for this caster, close it without mana cost
        if (!level.isClientSide && ServerPressureManager.hasActiveField(entity.getUUID(), SPELL_ID_STR)) {
            ServerPressureManager.stopByOwnerAndSpell(entity.getUUID(), SPELL_ID_STR);
            if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.translatable("ui.bhspells.tempest_reiatsu_closed"), true);
                int cdTicks = (int) (SpellConfig.TempestReiatsu.getCooldown() * 20);
                playerMagicData.getPlayerCooldowns().addCooldown(this, cdTicks);
                playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
            }
            return false;
        }

        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            float radius = getRadius(spellLevel);
            long seed = level.random.nextLong();

            PressureFieldData fieldData = new PressureFieldData(
                    UUID.randomUUID(),
                    entity.getUUID(),
                    SPELL_ID_STR,
                    spellLevel,
                    PressureAnchor.follow(entity),
                    radius,
                    0, // 0 = persistent toggle stance
                    serverLevel.getGameTime(),
                    seed,
                    PressureVisualProfile.TEMPEST_LIGHTNING,
                    1.0F
            );

            ServerPressureManager.startField(serverLevel, fieldData);

            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("ui.bhspells.tempest_reiatsu_opened"), true);
            }

            // Cataclysmic thunder burst on opening
            level.playSound(
                    null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.LIGHTNING_BOLT_THUNDER, SoundSource.PLAYERS,
                    1.4F, 0.9F
            );
            level.playSound(
                    null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS,
                    1.0F, 0.7F
            );
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
