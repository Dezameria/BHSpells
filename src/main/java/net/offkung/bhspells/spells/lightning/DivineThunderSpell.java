package net.offkung.bhspells.spells.lightning;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.effect.RedThunderStormEffect;
import net.offkung.bhspells.registry.MobEffectsRegistry;

import java.util.List;
import java.util.Optional;

public class DivineThunderSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "divine_thunder");

    private static final int RED_CHARGED_DURATION_TICKS = 20 * 60;
    private static final int RED_THUNDERSTORM_DURATION_TICKS = 20 * 60;
    private static final int RED_CHARGED_SPEED_PERCENT = 60;
    private static final int RED_CHARGED_ATTACK_DAMAGE_PERCENT = 30;
    private static final int RED_CHARGED_SPELL_POWER_PERCENT = 15;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.bhspells.divine_thunder.thunderstorm_damage", (int) RedThunderStormEffect.DAMAGE),
                Component.translatable("ui.bhspells.divine_thunder.thunderstorm_radius", RedThunderStormEffect.RADIUS),
                Component.translatable("ui.bhspells.divine_thunder.thunderstorm_length", RED_THUNDERSTORM_DURATION_TICKS / 20),
                Component.translatable("ui.bhspells.divine_thunder.red_charged_length", RED_CHARGED_DURATION_TICKS / 20),
                Component.translatable("ui.bhspells.divine_thunder.speed", RED_CHARGED_SPEED_PERCENT),
                Component.translatable("ui.bhspells.divine_thunder.attack_damage", RED_CHARGED_ATTACK_DAMAGE_PERCENT),
                Component.translatable("ui.bhspells.divine_thunder.spell_power", RED_CHARGED_SPELL_POWER_PERCENT)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(120)
            .build();

    public DivineThunderSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 8;
        this.spellPowerPerLevel = 8;
        this.castTime = 40;
        this.baseManaCost = 70;
    }

    @Override
    public CastType getCastType() {
        return CastType.LONG;
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundRegistry.THUNDERSTORM_PREPARE.get());
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        entity.addEffect(new MobEffectInstance(MobEffectsRegistry.RED_THUNDERSTORM.get(), RED_THUNDERSTORM_DURATION_TICKS, 0, false, false, true));
        entity.addEffect(new MobEffectInstance(MobEffectsRegistry.RED_CHARGED.get(), RED_CHARGED_DURATION_TICKS, 0, false, false, true));
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
