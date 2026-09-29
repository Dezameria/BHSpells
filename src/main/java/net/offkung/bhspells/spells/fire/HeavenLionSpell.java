package net.offkung.bhspells.spells.fire;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.capabilities.magic.MagicManager;
import io.redspace.ironsspellbooks.registries.ParticleRegistry;
import mod.chloeprime.aaaparticles.api.common.AAALevel;
import mod.chloeprime.aaaparticles.api.common.ParticleEmitterInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingEquipmentChangeEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.fiery_dance.GoldenMarbleEntity;
import net.offkung.bhspells.entity.spells.heaven_lion.HeavenLionProjectile;
import net.offkung.bhspells.event.GoldenMarbleManager;
import net.offkung.bhspells.registry.BHSoundRegistry;
import net.offkung.bhweapons.registry.ItemRegistry;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class HeavenLionSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "heaven_lion");

    private static final ParticleEmitterInfo BOOM_STORM = new ParticleEmitterInfo(BHSpells.id("boom_storm"));

    public static final String FAN_ACTIVE_TAG = "heaven_lion_fan_active";

    private static final int ABSORPTION_DURATION = 400;
    private static final int FAN_BURN_SECONDS = 3;
    private static final int FAN_WEAKNESS_DURATION = 60;

    private static final String PRIVILEGED_TAG = "chen_yinyue_tag";
    private static final int ABSORPTION_AMOUNT_DEFAULT = 12;

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.RARE)
            .setSchoolResource(SchoolRegistry.FIRE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(30)
            .build();

    public HeavenLionSpell() {
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
    public Optional<SoundEvent> getCastStartSound() {
        return Optional.of(SoundEvents.ARMOR_EQUIP_LEATHER);
    }

    @Override
    public Optional<SoundEvent> getCastFinishSound() {
        return Optional.of(BHSoundRegistry.FIRE_IMPACT_SPELL.get());
    }

    @Override
    public boolean canBeInterrupted(Player player) {
        return false;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
        if (!level.isClientSide) {
            HeavenLionProjectile projectile = new HeavenLionProjectile(level, entity);
            level.addFreshEntity(projectile);

            applyBlessing(entity);

            entity.getPersistentData().putBoolean(FAN_ACTIVE_TAG, true);

            float lookX = entity.getXRot() + 90.0F;
            float lookY = entity.getYRot() + 180.0F;
            float rollZ = 0.0F;

            lookX = lookX / 180.0F * 3.14F - 1.57F;
            lookY = lookY / 360.0F * 6.28F;
            rollZ = (rollZ + 180.0F) / 360.0F * 6.28F + 3.14F;
            AAALevel.addParticle(level, 64.0, BOOM_STORM.clone().position(entity.getX(), entity.getY(), entity.getZ()).rotation(-lookX, -lookY, -rollZ));
        }
    }

    public static void applyBlessing(LivingEntity target) {
        int absorptionAmount = getAbsorptionAmountFor(target);

        target.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, ABSORPTION_DURATION, absorptionAmount, false, false, true));

        clearHarmfulAndNeutralEffects(target);
    }

    private static int getAbsorptionAmountFor(LivingEntity target) {
        if (target.getTags().contains(PRIVILEGED_TAG)) {
            return ABSORPTION_AMOUNT_DEFAULT * 2;
        }
        return ABSORPTION_AMOUNT_DEFAULT;
    }

    private static void clearHarmfulAndNeutralEffects(LivingEntity target) {
        List<MobEffectInstance> toRemove = new ArrayList<>();
        for (MobEffectInstance effectInstance : target.getActiveEffects()) {
            if (effectInstance.getEffect().getCategory() != MobEffectCategory.BENEFICIAL) {
                toRemove.add(effectInstance);
            }
        }
        toRemove.forEach(instance -> target.removeEffect(instance.getEffect()));
    }

    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.ONE_HANDED_HORIZONTAL_SWING_ANIMATION;
    }

    public AnimationHolder getCastFinishAnimation() {
        return AnimationHolder.pass();
    }

    @Mod.EventBusSubscriber(modid = BHSpells.MODID)
    public static class OpenYangFanHandler {

        @SubscribeEvent
        public static void onLivingHurt(LivingHurtEvent event) {
            if (!(event.getSource().getEntity() instanceof LivingEntity attacker)) {
                return;
            }
            if (attacker.level().isClientSide) {
                return;
            }
            if (!attacker.getPersistentData().getBoolean(FAN_ACTIVE_TAG)) {
                return;
            }
            ItemStack mainHand = attacker.getMainHandItem();
            if (!mainHand.is(ItemRegistry.OPEN_YANG_FAN.get())) {
                return;
            }

            LivingEntity target = event.getEntity();
            target.setSecondsOnFire(FAN_BURN_SECONDS);
            target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, FAN_WEAKNESS_DURATION, 0));
        }

        @SubscribeEvent
        public static void onEquipmentChange(LivingEquipmentChangeEvent event) {
            if (event.getSlot() != EquipmentSlot.MAINHAND) {
                return;
            }
            LivingEntity entity = event.getEntity();
            if (entity.level().isClientSide) {
                return;
            }
            if (!entity.getPersistentData().getBoolean(FAN_ACTIVE_TAG)) {
                return;
            }
            if (!event.getTo().is(ItemRegistry.OPEN_YANG_FAN.get())) {
                entity.getPersistentData().putBoolean(FAN_ACTIVE_TAG, false);
            }
        }
    }
}
