package net.offkung.bhspells.spells.ground;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.intrusion_chain.IntrusionChainEntity;
import net.offkung.bhspells.event.IntrusionChainManager;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.BHSoundRegistry;

public class IntrusionChainBuffSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "intrusion_chain_buff");
    private static final ParticleEmitterInfo SNAKE_EMBLEM = new ParticleEmitterInfo(BHSpells.id("snake_emblem"));

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(BHSchoolRegistry.GROUND_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(20)
            .build();

    public IntrusionChainBuffSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 0;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
        this.baseManaCost = 30;
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
    public void onCast(Level world, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (IntrusionChainManager.onCast(entity, IntrusionChainEntity.Type.BUFF)) {
            float lookX = entity.getXRot() + 90.0F;
            float lookY = entity.getYRot() + 180.0F;
            float rollZ = 0.0F;

            lookX = lookX / 180.0F * 3.14F - 1.57F;
            lookY = lookY / 360.0F * 6.28F;
            rollZ = (rollZ + 180.0F) / 360.0F * 6.28F + 3.14F;
            AAALevel.addParticle(world, 64.0, SNAKE_EMBLEM.clone().position(entity.getX(), entity.getY() + 0.5, entity.getZ()).rotation(-lookX, -lookY, -rollZ));
            world.playSound(null, entity.getX(), entity.getY(), entity.getZ(), BHSoundRegistry.SNAKE_EMBLEM.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        }
        super.onCast(world, spellLevel, entity, castSource, playerMagicData);
    }
}
