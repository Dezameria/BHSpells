package net.offkung.bhspells.spells.gold;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import com.gametechbc.traveloptics.api.utils.TOGeneralUtils;
import com.gametechbc.traveloptics.init.TravelopticsSounds;
import com.gametechbc.traveloptics.particle.reverse_blastwave.ReverseBlastwaveParticleOptions;
import com.gametechbc.traveloptics.spells.TravelopticsSpellAnimations;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.network.particles.OakskinParticlesPacket;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.setup.PacketDistributor;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.joml.Vector3f;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Optional;

public class GoldenHandSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "golden_hand");

    private static final int OAKSKIN_DURATION = 400; // 20 seconds (400 ticks)
    private static final int OAKSKIN_AMPLIFIER = 7;   // Level 8 OakSkin (+50% Damage reduction)
    private static final int SLOWNESS_AMPLIFIER = 0;  // Slowness I (-15% to -20% speed)
    private static final int ABSORPTION_DURATION = 400; // 20 seconds (400 ticks)
    private static final int ABSORPTION_AMPLIFIER = 12; // 26 golden hearts (52 HP)

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return TOGeneralUtils.buildAquaSpellInfo(2, true,
                Component.translatable("ui.traveloptics.damage", Utils.stringTruncation(this.getDamage(spellLevel, caster), 1)),
                Component.translatable("ui.traveloptics.stun_length", Utils.timeFromTicks(60.0F, 1)),
                Component.translatable("ui.traveloptics.range", Utils.stringTruncation(30.0F, 2)),
                Component.translatable("ui.irons_spellbooks.damage_reduction", Utils.stringTruncation(50, 0)),
                Component.translatable("attribute.modifier.take.1", Utils.stringTruncation(20.0D, 0), Component.translatable("attribute.name.generic.movement_speed")).withStyle(ChatFormatting.RED),
                Component.translatable("ui.irons_spellbooks.absorption", 50),
                Component.translatable("ui.irons_spellbooks.effect_length", Utils.timeFromTicks(OAKSKIN_DURATION, 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public GoldenHandSpell() {
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 20;
        this.baseManaCost = 35;
        this.castTime = 0;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(TravelopticsSounds.TIDAL_GRASP_PULL.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.empty();
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return TravelopticsSpellAnimations.TIDAL_GRASP;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return TravelopticsSpellAnimations.TIDAL_GRASP_SMACK;
    }

    @Override
    public boolean canBeInterrupted(@Nullable Player player) {
        return false;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return this.getCastTime(spellLevel);
    }

    @Override
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData playerMagicData) {
        return Utils.preCastTargetHelper(level, caster, playerMagicData, this, 30, 0.2F);
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData playerMagicData) {
        assert playerMagicData != null;

        ICastData castData = playerMagicData.getAdditionalCastData();
        if (castData instanceof TargetEntityCastData) {
            TargetEntityCastData targetData = (TargetEntityCastData)castData;
            Entity targetEntity = targetData.getTarget((ServerLevel)level);
            if (targetEntity != null && targetEntity instanceof LivingEntity) {
                LivingEntity livingTarget = (LivingEntity)targetEntity;
                livingTarget.addEffect(new MobEffectInstance(MobEffectsRegistry.GOLDEN_GRASP_HELPER.get(), 5, 0, false, false, true));
            }
        }
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData playerMagicData) {
        ICastData castData = playerMagicData.getAdditionalCastData();
        if (castData instanceof TargetEntityCastData targetData) {
            Entity targetEntity = targetData.getTarget((ServerLevel)level);
            if (targetEntity != null && targetEntity instanceof LivingEntity livingTarget) {
                Vec3 casterPos = caster.position();
                Vec3 targetPos = livingTarget.position();
                Vec3 pullVector = casterPos.subtract(targetPos).normalize();
                Vec3 newTargetPos = casterPos.subtract(pullVector.scale(2.0F));
                livingTarget.addEffect(new MobEffectInstance(MobEffectsRegistry.GOLDEN_GRASP_HELPER.get(), 15, 0, false, false, true));
                livingTarget.teleportTo(newTargetPos.x, newTargetPos.y, newTargetPos.z);
            }

            caster.addEffect(new MobEffectInstance(MobEffectsRegistry.GOLDEN_GRASP.get(), 13, this.getDamage(spellLevel, caster), false, false, true));
        }

        if (!level.isClientSide) {
            caster.addEffect(new MobEffectInstance(MobEffectRegistry.OAKSKIN.get(), OAKSKIN_DURATION, OAKSKIN_AMPLIFIER, false, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, OAKSKIN_DURATION, SLOWNESS_AMPLIFIER, false, false, true));
            caster.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, ABSORPTION_AMPLIFIER, false, false, true));
        }

        super.onCast(level, spellLevel, caster, castSource, playerMagicData);
    }

    private int getDamage(int spellLevel, LivingEntity caster) {
        return (int)((double)10.0F + (double)this.getSpellPower(spellLevel, caster) * (double)3.0F);
    }

    @Override
    public boolean stopSoundOnCancel() {
        return true;
    }
}
