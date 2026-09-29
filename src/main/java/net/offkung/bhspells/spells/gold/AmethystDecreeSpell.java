package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeAoe;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeCasterRingEntity;
import net.offkung.bhspells.entity.spells.amethyst_decree.AmethystDecreeConstants;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.util.BHAmethystSounds;

import javax.annotation.Nullable;
import java.util.List;

public class AmethystDecreeSpell extends AbstractSpell {
    private static final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "amethyst_decree");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", AmethystDecreeConstants.BURST_DAMAGE));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public AmethystDecreeSpell() {
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 20;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return spellId;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        return this.getCastTime(spellLevel);
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_RAISED_HAND;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.TOUCH_GROUND_ANIMATION;
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        AmethystDecreeAoe aoe = new AmethystDecreeAoe(level);
        aoe.setOwner(entity);
        aoe.setPos(entity.position());
        level.addFreshEntity(aoe);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerPreCast(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData) {
        super.onServerPreCast(level, spellLevel, entity, playerMagicData);
        AmethystDecreeCasterRingEntity ring = new AmethystDecreeCasterRingEntity(level);
        ring.setPos(entity.position());
        level.addFreshEntity(ring);
        BHAmethystSounds.playCastStart(level, entity.getX(), entity.getY(), entity.getZ(), entity.getRandom());
    }
}
