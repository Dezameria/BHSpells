package net.offkung.bhspells.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.registries.MobEffectRegistry;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.supporting_bamboo.GreenSunbeam;
import net.offkung.bhspells.entity.spells.supporting_bamboo.SupportingBamboo;

import javax.annotation.Nullable;
import java.util.List;

public class SupportingBambooSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "supporting_bamboo");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks((float) getDuration(), 2)),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(getRadius(), 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(16)
            .build();

    public SupportingBambooSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 2;
        this.castTime = 30;
        this.baseManaCost = 40;
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
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.OVERHEAD_MELEE_SWING_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public void onServerCastTick(Level level, int spellLevel, LivingEntity caster, @Nullable MagicData playerMagicData) {
        // Spawn GreenSunbeam once at the very first tick of casting
        if (playerMagicData != null && playerMagicData.getCastDurationRemaining() == this.castTime - 1) {
            if (!level.isClientSide) {
                GreenSunbeam sunbeam = new GreenSunbeam(level);
                sunbeam.setPos(caster.getX(), caster.getY(), caster.getZ());
                sunbeam.setOwner(caster);
                level.addFreshEntity(sunbeam);
            }
            level.playSound(null, caster.blockPosition(), SoundRegistry.SUNBEAM_WINDUP.get(), SoundSource.NEUTRAL, 3.5f, 1);
        }

        // If the caster is in the air (flying or floating), force them down to the ground
        if (!level.isClientSide && !caster.onGround() && !caster.isInWater() && !caster.isInLava() && !caster.onClimbable()) {
            // Apply a strong downward impulse every tick to drag them to the ground
            caster.setDeltaMovement(caster.getDeltaMovement().x, -1.5, caster.getDeltaMovement().z);
            caster.setNoGravity(false);
            caster.hurtMarked = true;
            // Grant fall damage immunity for 2 seconds so landing doesn't hurt
            caster.addEffect(new MobEffectInstance(MobEffectRegistry.FALL_DAMAGE_IMMUNITY.get(), 40, 0, false, false, true));
        }

        super.onServerCastTick(level, spellLevel, caster, playerMagicData);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity caster, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            SupportingBamboo bamboo = new SupportingBamboo(level, caster);
            bamboo.setOwner(caster);
            bamboo.setPos(caster.getX(), caster.getY(), caster.getZ());
            level.addFreshEntity(bamboo);
        }
        super.onCast(level, spellLevel, caster, castSource, playerMagicData);
    }

    private float getRadius() {
        return 15.0F;
    }

    public int getDuration() {
        return SupportingBamboo.LIFETIME_TICKS;
    }
}

