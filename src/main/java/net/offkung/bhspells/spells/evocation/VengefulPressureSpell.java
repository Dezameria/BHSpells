package net.offkung.bhspells.spells.evocation;

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
 * Vengeful Pressure (แรงดันวิญญาณอาฆาต / 怨灵威压)
 * Stance toggle spell activating a massive 64 - 120 block domain of malice green spiritual deluge.
 * Envelops nearby players in an unceasing torrential camera-centric pressure curtain.
 */
@AutoSpellConfig
public class VengefulPressureSpell extends AbstractSpell {
    public static final String SPELL_ID_STR = "bhspells:vengeful_pressure";
    private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, "vengeful_pressure");

    public static final int BASE_MANA_COST = 100;
    public static final int MANA_COST_PER_LEVEL = 25;
    public static final double COOLDOWN_SECONDS = 45.0;
    public static final float BASE_RADIUS = 64.0F;
    public static final float RADIUS_PER_LEVEL = 14.0F;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        float radius = getRadius(spellLevel);
        info.add(Component.translatable("ui.bhspells.vengeful_pressure_radius", Utils.stringTruncation(radius, 1)));
        info.add(Component.translatable("ui.bhspells.vengeful_pressure_toggle_info"));
        return info;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(0.0) // No cooldown on opening; closing handles cooldown manually
            .build();

    public VengefulPressureSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.VengefulPressure.getBaseMana() + (spellLevel - 1) * SpellConfig.VengefulPressure.getManaPerLevel();
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
    public DefaultConfig getDefaultConfig() {
        return defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

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
                serverPlayer.displayClientMessage(Component.translatable("ui.bhspells.vengeful_pressure_closed"), true);
                int cdTicks = (int) (SpellConfig.VengefulPressure.getCooldown() * 20);
                playerMagicData.getPlayerCooldowns().addCooldown(this, cdTicks);
                playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
            }
            return false;
        }

        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    public float getRadius(int spellLevel) {
        return SpellConfig.VengefulPressure.getBaseRadius() + (spellLevel - 1) * SpellConfig.VengefulPressure.getRadiusPerLevel();
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
                    PressureVisualProfile.VENGEFUL_MALICE,
                    1.0F
            );

            ServerPressureManager.startField(serverLevel, fieldData);

            if (entity instanceof Player player) {
                player.displayClientMessage(Component.translatable("ui.bhspells.vengeful_pressure_opened"), true);
            }

            // Dread sound burst on opening
            level.playSound(
                    null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.WARDEN_ROAR, SoundSource.PLAYERS,
                    1.2F, 0.75F
            );
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
