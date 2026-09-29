package net.offkung.bhspells.spells.aqua;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.damage.SpellDamageSource;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeAoe;
import net.offkung.bhspells.entity.spells.crystal_hydro_dome.CrystalHydroDomeConstants;

import javax.annotation.Nullable;
import java.util.List;

public class CrystalHydroDomeSpell extends AbstractSpell {
    private static final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "crystal_hydro_dome");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0.0)
            .build();

    public CrystalHydroDomeSpell() {
        this.baseManaCost = 0;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = CrystalHydroDomeConstants.DURATION_TICKS;
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
        return CastType.CONTINUOUS;
    }

    @Override
    public SpellDamageSource getDamageSource(@Nullable Entity projectile, Entity attacker) {
        return super.getDamageSource(projectile, attacker).setIFrames(0);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (playerMagicData.getCastDurationRemaining() != playerMagicData.getCastDuration()) {
            return;
        }

        Vec3 center = entity.position();
        openCleanseAndHeal(level, center);
        entity.addTag(CrystalHydroDomeConstants.DOME_TAG);

        CrystalHydroDomeAoe aoe = new CrystalHydroDomeAoe(level);
        aoe.setOwner(entity);
        aoe.setPos(center);
        level.addFreshEntity(aoe);

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onServerCastComplete(Level level, int spellLevel, LivingEntity entity, MagicData playerMagicData, boolean cancelled) {
        super.onServerCastComplete(level, spellLevel, entity, playerMagicData, cancelled);
        if (cancelled) {
            CrystalHydroDomeAoe.endActiveDomeFor(entity);
        } else {
            CrystalHydroDomeAoe.naturalEndFor(entity);
        }
    }

    private void openCleanseAndHeal(Level level, Vec3 center) {
        AABB box = CrystalHydroDomeAoe.searchBox(center, 0);
        List<LivingEntity> targets = level.getEntitiesOfClass(LivingEntity.class, box, e -> !(e instanceof ArmorStand) && e.isAlive() && CrystalHydroDomeAoe.isInside(center, e.position()));
        for (LivingEntity target : targets) {
            CrystalHydroDomeAoe.cleanseHarmfulEffects(target);
            target.heal((float) CrystalHydroDomeConstants.OPEN_HEAL);
        }
    }
}
