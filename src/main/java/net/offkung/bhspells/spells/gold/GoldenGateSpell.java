package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.golden_gate.GoldenGateEntity;
import net.offkung.bhspells.registry.BHSchoolRegistry;

import java.util.List;

public class GoldenGateSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "golden_gate");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.hp", Utils.stringTruncation(getGateHP(spellLevel, caster), 1)),
                Component.translatable("ui.bhspells.reflect_damage_percent", Utils.stringTruncation(getGateReflectionPercent(spellLevel, caster) * 100, 1))
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(6)
            .setCooldownSeconds(30)
            .build();

    public GoldenGateSpell() {
        this.manaCostPerLevel = 6;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 20;
        this.baseManaCost = 35;
        this.castTime = 0;
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
        GoldenGateEntity goldenGate = new GoldenGateEntity(level, getGateHP(spellLevel, entity), entity, true);
        Vec3 spawn = Utils.raycastForEntity(level, entity, 3, true).getLocation();
        spawn = spawn.add(0, 2.0, 0);
        goldenGate.setPos(spawn);
        goldenGate.setRotation(entity.getYRot());
        goldenGate.setPercentReflectDamage(getGateReflectionPercent(spellLevel, entity));
        level.addFreshEntity(goldenGate);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getGateHP(int spellLevel, LivingEntity caster) {
        return 1 + getSpellPower(spellLevel, caster);
    }

    private float getGateReflectionPercent(int spellLevel, LivingEntity caster) {
        return (float) (10 + (spellLevel * 10)) / 100;
    }
}
