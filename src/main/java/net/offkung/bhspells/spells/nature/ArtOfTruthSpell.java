package net.offkung.bhspells.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
import io.redspace.ironsspellbooks.capabilities.magic.TargetEntityCastData;
import io.redspace.ironsspellbooks.registries.SoundRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.network.PacketDistributor;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.swirling_blossom.SwirlingBlossomAoe;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.server.ArtOfTruthTargetGlowSyncPacket;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ArtOfTruthSpell extends AbstractSpell {
    private static final Map<UUID, UUID> ACTIVE_TRUTHS = new ConcurrentHashMap<>();
    private static final Map<UUID, Integer> ACTIVE_TARGET_IDS = new ConcurrentHashMap<>();

    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "art_of_truth");

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public ArtOfTruthSpell() {
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 10;
        this.baseManaCost = 20;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.bhspells.art_of_truth.duration_infinite"),
                Component.translatable("ui.irons_spellbooks.radius", Utils.stringTruncation(this.getRadius(), 2))
        );
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
        return Optional.of(SoundRegistry.ENDER_CAST.get());
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
    public boolean checkPreCastConditions(Level level, int spellLevel, LivingEntity caster, MagicData playerMagicData) {
        if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
            return true;
        }
        return Utils.preCastTargetHelper(level, caster, playerMagicData, this, 32, 0.25f);
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
                discardActiveTruth(level, entity);
            } else {
                discardActiveTruth(level, entity);
                LivingEntity target = null;
                if (playerMagicData.getAdditionalCastData() instanceof TargetEntityCastData targetData) {
                    Entity targetEntity = targetData.getTarget((ServerLevel) level);
                    if (targetEntity instanceof LivingEntity livingTarget) {
                        target = livingTarget;
                    }
                }
                if (target == null) {
                    super.onCast(level, spellLevel, entity, castSource, playerMagicData);
                    return;
                }

                // 1. Target glows purple (0.43, 0.33, 0.67), only seen by caster
                if (entity instanceof ServerPlayer casterPlayer) {
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> casterPlayer), new ArtOfTruthTargetGlowSyncPacket(target.getId(), true));
                }
                ACTIVE_TARGET_IDS.put(entity.getUUID(), target.getId());

                // 2. Title and subtitle to target once: "You are Forced!", "Now telling the truth!"
                if (target instanceof ServerPlayer targetPlayer) {
                    targetPlayer.connection.send(new ClientboundSetTitlesAnimationPacket(10, 120, 20));
                    targetPlayer.connection.send(new ClientboundSetTitleTextPacket(Component.literal("จงสูดดมกลิ่นดอกไม้...").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD)));
                    targetPlayer.connection.send(new ClientboundSetSubtitleTextPacket(Component.literal("เจ้าจำเป็นต้องเอ่ยความจริงทั้งหมดออกมา!").withStyle(ChatFormatting.RED)));
                    targetPlayer.playNotifySound(SoundEvents.WITHER_DEATH, SoundSource.PLAYERS, 0.8f, 0.0f);
                }

                // 3. Spawn SwirlingBlossomAoe at caster position with purple particles and no effect
                SwirlingBlossomAoe aoe = new SwirlingBlossomAoe(level);
                aoe.setOwner(entity);
                aoe.setPos(entity.position());
                aoe.setRadius(this.getRadius());
                aoe.setDuration(this.getDuration());
                aoe.setPurpleVariant(true);
                aoe.setTruthTarget(target);
                level.addFreshEntity(aoe);
                ACTIVE_TRUTHS.put(entity.getUUID(), aoe.getUUID());

                // 4. Add recast instance for 1 minute (1200 ticks)
                playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 2, this.getDuration(), castSource, null), playerMagicData);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        discardActiveTruth(serverPlayer.level(), serverPlayer);
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
    }

    private void discardActiveTruth(Level level, LivingEntity entity) {
        Integer targetId = ACTIVE_TARGET_IDS.remove(entity.getUUID());
        if (targetId != null && entity instanceof ServerPlayer serverPlayer) {
            PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new ArtOfTruthTargetGlowSyncPacket(targetId, false));
        }
        UUID aoeUuid = ACTIVE_TRUTHS.remove(entity.getUUID());
        boolean discarded = false;
        if (aoeUuid != null && level instanceof ServerLevel serverLevel) {
            Entity existing = serverLevel.getEntity(aoeUuid);
            if (existing instanceof SwirlingBlossomAoe aoe && aoe.isAlive()) {
                aoe.discard();
                discarded = true;
            }
        }
        if (!discarded) {
            for (SwirlingBlossomAoe aoe : level.getEntitiesOfClass(SwirlingBlossomAoe.class, entity.getBoundingBox().inflate(128.0), a -> a.getOwner() == entity && a.isPurpleVariant())) {
                aoe.discard();
            }
        }
    }

    private float getRadius() {
        return 20.0F;
    }

    public int getDuration() {
        return Integer.MAX_VALUE;
    }
}
