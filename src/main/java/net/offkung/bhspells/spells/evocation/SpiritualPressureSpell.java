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
 * Spiritual Pressure (灵压 / Reiatsu Field) demonstration spell.
 * Activates a reusable, entity-following spiritual pressure field around the caster.
 * Demonstrates the separation of server gameplay truth and client visual complexity.
 */
@AutoSpellConfig
public class SpiritualPressureSpell extends AbstractSpell {
    private final ResourceLocation spellId = new ResourceLocation(BHSpells.MODID, "spiritual_pressure");

    public static final int BASE_MANA_COST = 75;
    public static final int MANA_COST_PER_LEVEL = 15;
    public static final double COOLDOWN_SECONDS = 35.0;
    public static final float BASE_RADIUS = 12.0F;
    public static final float RADIUS_PER_LEVEL = 2.0F;
    public static final int DURATION_TICKS = 300;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        List<MutableComponent> info = new ArrayList<>();
        float radius = getRadius(spellLevel);
        int duration = SpellConfig.SpiritualPressure.getDurationTicks();
        info.add(Component.translatable("ui.bhspells.spiritual_pressure_radius", Utils.stringTruncation(radius, 1)));
        info.add(Component.translatable("ui.bhspells.spiritual_pressure_duration", Utils.stringTruncation(duration / 20.0F, 1)));
        return info;
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.LEGENDARY)
            .setSchoolResource(SchoolRegistry.EVOCATION_RESOURCE)
            .setMaxLevel(5)
            .setCooldownSeconds(COOLDOWN_SECONDS)
            .build();

    public SpiritualPressureSpell() {
        this.baseManaCost = BASE_MANA_COST;
        this.manaCostPerLevel = MANA_COST_PER_LEVEL;
        this.baseSpellPower = DURATION_TICKS;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
    }

    @Override
    public int getManaCost(int spellLevel) {
        return SpellConfig.SpiritualPressure.getBaseMana() + (spellLevel - 1) * SpellConfig.SpiritualPressure.getManaPerLevel();
    }

    @Override
    public int getSpellCooldown() {
        return (int) (SpellConfig.SpiritualPressure.getCooldown() * 20);
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
        if (net.offkung.bhspells.pressure.PressureToggleHelper.isDomainActive(player, spellId.toString())) {
            return new CastResult(CastResult.Type.SUCCESS);
        }
        return super.canBeCastedBy(spellLevel, castSource, playerMagicData, player);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        // Shift + Cast cancels an active pressure field owned by this caster

            if (!level.isClientSide && ServerPressureManager.hasActiveField(entity.getUUID(), spellId.toString())) {
                ServerPressureManager.stopByOwnerAndSpell(entity.getUUID(), spellId.toString());
                if (entity instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    serverPlayer.displayClientMessage(Component.translatable("ui.bhspells.spiritual_pressure_cancelled"), true);
                    int cdTicks = (int) (SpellConfig.SpiritualPressure.getCooldown() * 20);
                    playerMagicData.getPlayerCooldowns().addCooldown(this, cdTicks);
                    playerMagicData.getPlayerCooldowns().syncToPlayer(serverPlayer);
                }
                return false;
            }

        return super.checkPreCastConditions(level, spellLevel, entity, playerMagicData);
    }

    public float getRadius(int spellLevel) {
        return SpellConfig.SpiritualPressure.getBaseRadius() + (spellLevel - 1) * SpellConfig.SpiritualPressure.getRadiusPerLevel();
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide && level instanceof ServerLevel serverLevel) {
            float radius = getRadius(spellLevel);
            int duration = SpellConfig.SpiritualPressure.getDurationTicks();
            long seed = level.random.nextLong();

            // Select color based on caster UUID hash (allowing distinct colors for distinct players)
            int colorVariant = Math.abs(entity.getUUID().hashCode()) % 4;
            PressureVisualProfile profile = switch (colorVariant) {
                case 1 -> PressureVisualProfile.REIATSU_PINK;
                case 2 -> PressureVisualProfile.REIATSU_CYAN;
                case 3 -> PressureVisualProfile.REIATSU_EMERALD;
                default -> PressureVisualProfile.SPIRITUAL_VIOLET;
            };

            PressureFieldData fieldData = new PressureFieldData(
                    UUID.randomUUID(),
                    entity.getUUID(),
                    PressureAnchor.follow(entity),
                    radius,
                    duration,
                    serverLevel.getGameTime(),
                    seed,
                    profile,
                    1.0F
            );

            ServerPressureManager.startField(serverLevel, fieldData);

            // Audio burst on release
            level.playSound(
                    null, entity.getX(), entity.getY(), entity.getZ(),
                    SoundEvents.WARDEN_SONIC_BOOM, SoundSource.PLAYERS,
                    1.0F, 0.65F
            );
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
