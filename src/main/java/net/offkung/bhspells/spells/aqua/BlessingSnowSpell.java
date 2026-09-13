package net.offkung.bhspells.spells.aqua;

import com.gametechbc.traveloptics.api.init.TravelopticsSchools;
import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.ICastDataSerializable;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import io.redspace.ironsspellbooks.capabilities.magic.RecastResult;
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
import net.minecraftforge.network.PacketDistributor;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.blessing_snow.RadiusSnowRingEntity;
import net.offkung.bhspells.network.PacketHandler;
import net.offkung.bhspells.network.server.BlessingSnowSelectSyncPacket;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import org.jetbrains.annotations.Nullable;

public class BlessingSnowSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "blessing_snow");
    private static final int RECAST_DURATION_TICKS = 600; // 30 seconds to choose radius and cast

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(TravelopticsSchools.AQUA_RESOURCE)
            .setMaxLevel(1)
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

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            boolean isSelecting = entity.getPersistentData().getBoolean("BlessingSnowSelecting");
            boolean hasRecast = playerMagicData.getPlayerRecasts().hasRecastForSpell(getSpellId());

            if (isSelecting || hasRecast) {
                // Second cast: Summon RadiusSnowRingEntity with chosen radius
                int radius = entity.getPersistentData().getInt("BlessingSnowRadius");
                if (radius != 5 && radius != 10 && radius != 20 && radius != 30) {
                    radius = 5;
                }

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
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new BlessingSnowSelectSyncPacket(false, radius));
                    serverPlayer.displayClientMessage(Component.literal("กด §b[Shift + คลิ๊กขวา]§r เพื่อเลือก§a§lพันธมิตร§r"), true);
                }

                int manaAmplifier = switch (radius) {
                    case 10 -> 1;
                    case 20 -> 2;
                    case 30 -> 3;
                    default -> 0;
                };
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.BLESSING_SNOW_MANA.get(), 20, manaAmplifier, false, false, true));
                entity.getPersistentData().remove("BlessingSnowSelecting");
                entity.getPersistentData().remove("BlessingSnowRadius");
            } else {
                // First cast: Enter radius selection mode
                int defaultRadius = 5;
                entity.getPersistentData().putBoolean("BlessingSnowSelecting", true);
                entity.getPersistentData().putInt("BlessingSnowRadius", defaultRadius);

                // Clear cooldown so player can cast the recast immediately
                playerMagicData.getPlayerCooldowns().removeCooldown(getSpellId());

                // Add recast with totalRecasts = 2 (remainingRecasts = 1)
                playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 2, RECAST_DURATION_TICKS, castSource, null), playerMagicData);

                if (entity instanceof ServerPlayer serverPlayer) {
                    PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new BlessingSnowSelectSyncPacket(true, defaultRadius));
                }
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    @Override
    public void onRecastFinished(ServerPlayer serverPlayer, RecastInstance recastInstance, RecastResult recastResult, ICastDataSerializable castDataSerializable) {
        super.onRecastFinished(serverPlayer, recastInstance, recastResult, castDataSerializable);
        serverPlayer.getPersistentData().remove("BlessingSnowSelecting");
        serverPlayer.getPersistentData().remove("BlessingSnowRadius");
        PacketHandler.INSTANCE.send(PacketDistributor.PLAYER.with(() -> serverPlayer), new BlessingSnowSelectSyncPacket(false, 5));
    }
}
