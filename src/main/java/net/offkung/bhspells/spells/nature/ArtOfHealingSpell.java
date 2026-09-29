package net.offkung.bhspells.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.swirling_blossom.SwirlingBlossomAoe;
import net.offkung.bhspells.spells.BHSpellAnimations;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArtOfHealingSpell extends AbstractSpell {
    private static final Map<UUID, UUID> ACTIVE_BLOSSOMS = new ConcurrentHashMap<>();
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "art_of_healing");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.duration", Utils.timeFromTicks((float)this.getDuration(), 2)), Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(this.getRadius(), 2)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public ArtOfHealingSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 40;
        this.baseManaCost = 20;
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
        return Optional.of(SoundRegistry.FIREFLY_SPELL_PREPARE.get());
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(SoundRegistry.NATURE_CAST.get());
    }

    @Override
    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.CHARGE_ANIMATION;
    }

    @Override
    public AnimationHolder getCastFinishAnimation() {
        return SpellAnimations.SLASH_ANIMATION;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public int getEffectiveCastTime(int spellLevel, @Nullable LivingEntity entity) {
        if (entity != null) {
            MagicData magicData = MagicData.getPlayerMagicData(entity);
            if (magicData.getPlayerRecasts().hasRecastForSpell(this)) {
                return 0;
            }
        }
        return super.getEffectiveCastTime(spellLevel, entity);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
                discardActiveBlossom(level, entity);
            } else {
                discardActiveBlossom(level, entity);
                SwirlingBlossomAoe aoe = new SwirlingBlossomAoe(level);
                aoe.setOwner(entity);
                aoe.setPos(entity.position());
                aoe.setRadius(this.getRadius());
                aoe.setDuration(this.getDuration());
                level.addFreshEntity(aoe);
                ACTIVE_BLOSSOMS.put(entity.getUUID(), aoe.getUUID());
                playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 2, this.getDuration(), castSource, null), playerMagicData);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        ACTIVE_BLOSSOMS.remove(serverPlayer.getUUID());
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    private void discardActiveBlossom(Level level, LivingEntity entity) {
        UUID aoeUuid = ACTIVE_BLOSSOMS.remove(entity.getUUID());
        boolean discarded = false;
        if (aoeUuid != null && level instanceof ServerLevel serverLevel) {
            Entity existing = serverLevel.getEntity(aoeUuid);
            if (existing instanceof SwirlingBlossomAoe aoe && aoe.isAlive()) {
                aoe.discard();
                discarded = true;
            }
        }
        if (!discarded) {
            for (SwirlingBlossomAoe aoe : level.getEntitiesOfClass(SwirlingBlossomAoe.class, entity.getBoundingBox().inflate(128.0), a -> a.getOwner() == entity)) {
                aoe.discard();
            }
        }
    }

    private float getRadius() {
        return 20.0F;
    }

    public int getDuration() {
        return 1200;
    }
}
