package net.offkung.bhspells.spells.nature;

import io.redspace.ironsspellbooks.api.config.DefaultConfig;
import io.redspace.ironsspellbooks.api.magic.MagicData;
import io.redspace.ironsspellbooks.api.registry.SchoolRegistry;
import io.redspace.ironsspellbooks.api.spells.*;
import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import io.redspace.ironsspellbooks.api.util.Utils;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.entity.spells.six_petal_waltz.PetalWaltzSword;
import net.offkung.bhspells.event.SwordDashManager;

import java.util.List;

public class SixPetalWaltzSpell extends AbstractSpell {
    private final ResourceLocation spellId = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "six_petal_waltz");

    @Override
    public List<MutableComponent> getUniqueInfo(int spellLevel, LivingEntity caster) {
        return List.of(Component.translatable("ui.irons_spellbooks.damage", Utils.stringTruncation(this.getSpellPower(spellLevel, caster), 1)), Component.translatable("ui.irons_spellbooks.projectile_count", Utils.stringTruncation((double)this.getSwordCount(spellLevel, caster), 1)));
    }

    private final DefaultConfig defaultConfig = new DefaultConfig()
            .setMinRarity(SpellRarity.UNCOMMON)
            .setSchoolResource(SchoolRegistry.NATURE_RESOURCE)
            .setMaxLevel(1)
            .setCooldownSeconds(16)
            .build();

    public SixPetalWaltzSpell() {
        this.manaCostPerLevel = 10;
        this.baseSpellPower = 20;
        this.spellPowerPerLevel = 2;
        this.castTime = 10;
        this.baseManaCost = 40;
    }

    @Override
    public CastType getCastType() {
        return CastType.INSTANT;
    }

    @Override
    public DefaultConfig getDefaultConfig() {
        return this.defaultConfig;
    }

    @Override
    public ResourceLocation getSpellResource() {
        return this.spellId;
    }

    @Override
    public void onCast(Level level, int spellLevel, LivingEntity entity, CastSource castSource, MagicData playerMagicData) {
        int swordCount = this.getSwordCount(spellLevel, entity);
        int offset = 360 / swordCount;
        Vec3 center = entity.getEyePosition().add(0.0F, -0.75F, 0.0F);

        if (!level.isClientSide) {
            SwordDashManager.registerSwordCount(entity.getUUID(), swordCount);
        }

        for(int i = 0; i < swordCount; ++i) {
            Vec3 motion = new Vec3(0.0F, 0.0F, 1.0F);
            motion = motion.xRot(((float)Math.PI / 180F));
            motion = motion.yRot((float)(offset * i) * ((float)Math.PI / 180F));
            PetalWaltzSword petalWaltzSword = new PetalWaltzSword(level, entity);
            if (i % 3 == 0) {
                petalWaltzSword.setSilent(true);
            }

            petalWaltzSword.setWaitTimer(440);
            petalWaltzSword.setDeltaMovement(motion);

            petalWaltzSword.moveTo(center.x, center.y, center.z);
            level.addFreshEntity(petalWaltzSword);
        }

        super.onCast(level, spellLevel, entity, castSource, playerMagicData);
    }

    private int getSwordCount(int spellLevel, LivingEntity entity) {
        return spellLevel + 5;
    }

    public AnimationHolder getCastStartAnimation() {
        return SpellAnimations.SELF_CAST_ANIMATION;
    }
}
