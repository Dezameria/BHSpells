package net.offkung.bhspells.spells.aqua;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.blessing_snow.RadiusSnowRingEntity;
import net.offkung.bhspells.registry.MobEffectsRegistry;

public class BlessingSnowSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "blessing_snow");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(4)
            .setCooldownSeconds(30)
            .build();

    public BlessingSnowSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 40;
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

    private int getRadius(int spellLevel) {
        return switch (spellLevel) {
            case 2 -> 10;
            case 3 -> 20;
            case 4 -> 30;
            default -> 5;
        };
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            int radius = getRadius(spellLevel);

            RadiusSnowRingEntity ring = new RadiusSnowRingEntity(level);
            ring.setOwner(entity);
            ring.setPos(entity.getX(), entity.getY(), entity.getZ());
            ring.setRadius(radius);
            ring.setCircular();
            level.addFreshEntity(ring);

            if (level instanceof ServerLevel serverLevel) {
                serverLevel.sendParticles(ParticleTypes.SNOWFLAKE, entity.getX(), entity.getY() + 0.5, entity.getZ(), 60, 0.8, 0.5, 0.8, 0.15);
            }

            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundRegistry.ICE_CAST.get(), SoundSource.PLAYERS, 1.0f, 1.0f);

            if (entity instanceof ServerPlayer serverPlayer) {
                serverPlayer.displayClientMessage(Component.literal("กด §b[Shift + คลิ๊กขวา]§r เพื่อเลือก§a§lพันธมิตร§r"), true);
            }

            entity.addEffect(new MobEffectInstance(MobEffectsRegistry.BLESSING_SNOW_MANA.get(), 20, spellLevel - 1, false, false, true));
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
