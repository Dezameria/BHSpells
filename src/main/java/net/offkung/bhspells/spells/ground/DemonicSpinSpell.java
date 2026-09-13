package net.offkung.bhspells.spells.ground;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.spells.AbstractSpell;
import io.redspace.ironsspellbooks.api.spells.CastSource;
import io.redspace.ironsspellbooks.api.spells.CastType;
import io.redspace.ironsspellbooks.api.spells.SpellRarity;
import io.redspace.ironsspellbooks.capabilities.magic.RecastInstance;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.event.DemonicSpinEvents;
import net.offkung.bhspells.registry.BHSchoolRegistry;
import net.offkung.bhspells.registry.MobEffectsRegistry;
import net.offkung.bhweapons.registry.AnimationRegistry;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;
import yesman.epicfight.world.capabilities.entitypatch.player.PlayerPatch;
import yesman.epicfight.world.effect.EpicFightMobEffects;

import java.util.List;

public class DemonicSpinSpell extends AbstractSpell {
    private static final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "demonic_spin");
    private static final int DURATION_TICKS = 400; // 10 seconds

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.EPIC)
            .setSchoolResource(BHSchoolRegistry.GROUND_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(60)
            .build();

    public DemonicSpinSpell() {
        this.baseManaCost = 60;
        this.manaCostPerLevel = 0;
        this.baseSpellPower = 1;
        this.spellPowerPerLevel = 0;
        this.castTime = 0;
    }

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(
                Component.translatable("ui.bhspells.demonic_spin.damage_reduction", 40),
                Component.translatable("ui.bhspells.demonic_spin.projectile_discard_chance", 40),
                Component.translatable("ui.bhspells.demonic_spin.radius", 5),
                Component.translatable("ui.bhspells.demonic_spin.duration", DURATION_TICKS / 20)
        );
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
        return CastType.INSTANT;
    }

    @Override
    public int getRecastCount(int spellLevel, @Nullable LivingEntity entity) {
        return 2;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        if (!level.isClientSide) {
            if (playerMagicData.getPlayerRecasts().hasRecastForSpell(this)) {
                entity.removeEffect(MobEffectsRegistry.DEMONIC_SPIN.get());
                entity.removeEffect(EpicFightMobEffects.STUN_IMMUNITY.get());
                DemonicSpinEvents.stopSpinAnimation(entity);
            } else {
                entity.addEffect(new MobEffectInstance(MobEffectsRegistry.DEMONIC_SPIN.get(), DURATION_TICKS, 0, false, false, true));
                entity.addEffect(new MobEffectInstance(EpicFightMobEffects.STUN_IMMUNITY.get(), DURATION_TICKS, 4, false, false, true));
                LivingEntityPatch<?> entityPatch = EpicFightCapabilities.getEntityPatch(entity, LivingEntityPatch.class);
                if (entityPatch instanceof PlayerPatch<?> playerPatch) {
                    playerPatch.toEpicFightMode(true);
                }
                if (entityPatch != null) {
                    entityPatch.playAnimationSynchronized(AnimationRegistry.STAFF_SPIN_TWOHAND_LOOP_FAST, 0.0f);
                }
                playerMagicData.getPlayerRecasts().addRecast(new RecastInstance(getSpellId(), spellLevel, 2, DURATION_TICKS, castSource, null), playerMagicData);
            }
        }
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }
}
