package net.offkung.bhspells.spells.ground;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.embracing_bosom.EmbracingBosomAoe;
import net.offkung.bhspells.registry.BHSchoolRegistry;

public class EmbracingBosomSpell extends AbstractSpell {
    private static final ResourceLocation SPELL_ID = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "embracing_bosom");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(BHSchoolRegistry.GROUND_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60.0)
            .build();

    public EmbracingBosomSpell() {
        this.baseManaCost = 60;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 10;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return SPELL_ID;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        Vec3 spawnPos = Utils.moveToRelativeGroundLevel(level, entity.position(), 6);
        EmbracingBosomAoe aoe = new EmbracingBosomAoe(level);
        aoe.setOwner(entity);
        aoe.setPos(spawnPos);
        level.addFreshEntity(aoe);
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
