package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.effect.WheelOfKarmaEffect;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhweapons.registry.AnimationRegistry;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import java.util.List;

public class WheelOfKarmaSpell extends AbstractSpell {
    private static final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "wheel_of_karma");
    public static final int DURATION_TICKS = 300; // 15 seconds

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public WheelOfKarmaSpell() {
        this.baseManaCost = 60;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.bhspells.wheel_of_karma.projectile_discard"),
                Component.translatable("ui.bhspells.wheel_of_karma.radius", 10),
                Component.translatable("ui.bhspells.wheel_of_karma.duration", DURATION_TICKS / 20)
        );
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            entity.getPersistentData().putBoolean(WheelOfKarmaEffect.FINISHED_TAG, false);

            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.WHEEL_OF_KARMA.get(), DURATION_TICKS, 0, false, false, true));
            entity.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), DURATION_TICKS + 60, 4, false, false, true));

            LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
            if (entityPatch instanceof PlayerPatch<?> playerPatch) {
                playerPatch.toEpicFightMode(true);
            }
            if (entityPatch != null) {
                entityPatch.playAnimationSynchronized(AnimationRegistry.STAFF_CHARYBDIS_LOOP_FAST, 0.0f);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
