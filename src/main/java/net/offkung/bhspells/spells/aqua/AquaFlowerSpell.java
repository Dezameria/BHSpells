package net.offkung.bhspells.spells.aqua;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import com.hm.efn.registries.EFNMobEffectRegistry;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.entity.spells.target_area.TargetedAreaEntity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.aqua_flower.AquaFlower;

public class AquaFlowerSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "aqua_flower");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(40)
            .build();

    public AquaFlowerSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 10;
        this.baseManaCost = 40;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            AquaFlower aquaFlower = new AquaFlower(level, 800); // 40 seconds lifetime
            aquaFlower.moveTo(entity.getX(), entity.getY(), entity.getZ(), entity.getYRot(), 0.0F);
            aquaFlower.setOwner(entity);
            float power = this.getSpellPower(spellLevel, entity);
            aquaFlower.setSpellPower(power);
            level.addFreshEntity(aquaFlower);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.ROOT_EMERGE.get(), SoundSource.PLAYERS, 1.5F, 1.2F);
            entity.addEffect(new MobEffectInstance(EFNMobEffectRegistry.HEAVY_RAIN_STUN.get(), 400, 255, false, false, true));

            TargetedAreaEntity visualEntity = TargetedAreaEntity.createTargetAreaEntity(level, aquaFlower.position(), 20, 0x79BAEC);
            visualEntity.setDuration(800);
            visualEntity.setOwner(aquaFlower);
            visualEntity.setShouldFade(true);
            level.addFreshEntity(visualEntity);
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
