package net.offkung.bhspells.spells.gold;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.golden_cloud.GoldenCloudEntity;
import net.offkung.bhspells.event.ShakenMonkeyDashManager;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;

import java.util.List;

public class ShakenMonkeySpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "shaken_monkey");
    private static final int COOLDOWN_SECONDS = 20;

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.irons_spellbooks.damage", getDamage(spellLevel, caster)),
                Component.translatable("ui.irons_spellbooks.cooldown", COOLDOWN_SECONDS)
        );
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.COMMON)
            .setSchoolResource(BHSchoolRegistry.GOLD_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(0)
            .build();

    public ShakenMonkeySpell() {
        this.manaCostPerLevel = 20;
        this.baseSpellPower = 120;
        this.spellPowerPerLevel = 2;
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
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (entity.getVehicle() instanceof GoldenCloudEntity cloud) {
                // Phase 2: When cast second time during flight
                float damage = getDamage(spellLevel, entity);
                boolean onGround = entity.onGround() || cloud.onGround() || !level.noCollision(entity, entity.getBoundingBox().move(0, -0.6, 0));
                entity.stopRiding();
                cloud.discard();

                if (entity instanceof ServerPlayer serverPlayer) {
                    ShakenMonkeyDashManager.applyCloudBless(serverPlayer);
                    if (onGround) {
                        ShakenMonkeyDashManager.performSlam(serverPlayer, serverPlayer.position(), damage);
                    } else {
                        ShakenMonkeyDashManager.startDash(serverPlayer, damage);
                    }
                }

                // Apply full cooldown after dash / slam
                playerMagicData.getPlayerCooldowns().addCooldown(this, COOLDOWN_SECONDS * 20);
            } else {
                // Phase 1: Summon golden cloud and mount
                Vec3 forward = entity.getForward().normalize().scale(1.5f);
                Vec3 spawn = entity.position().add(forward.x, 0.25f, forward.z);

                GoldenCloudEntity cloudEntity = new GoldenCloudEntity(level, entity);
                cloudEntity.setPos(spawn);
                level.addFreshEntity(cloudEntity);

                if (entity instanceof Player player) {
                    cloudEntity.doPlayerRide(player);
                } else {
                    entity.startRiding(cloudEntity);
                }

                // Short 10-tick (0.5s) buffer so player doesn't accidentally double-click
                playerMagicData.getPlayerCooldowns().addCooldown(this, 10);
            }
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private float getDamage(int spellLevel, LivingEntity entity) {
        return getSpellPower(spellLevel, entity);
    }
}
