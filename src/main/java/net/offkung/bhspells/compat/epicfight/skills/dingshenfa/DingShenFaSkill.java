package net.offkung.bhspells.compat.epicfight.skills.dingshenfa;

import net.offkung.bhspells.BHSpells;
import net.offkung.bhspells.service.DingShenFaService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillCategories;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.entitypatch.player.ServerPlayerPatch;

public class DingShenFaSkill extends Skill {
    public static final ResourceLocation SKILL_ICON = new ResourceLocation(BHSpells.MODID, "textures/gui/skills/ding_shen_fa.png");

    public static SkillBuilder<DingShenFaSkill> create() {
        return new SkillBuilder<DingShenFaSkill>()
                .setCategory(SkillCategories.IDENTITY)
                .setActivateType(ActivateType.ONE_SHOT)
                .setResource(Resource.COOLDOWN);
    }

    public DingShenFaSkill(SkillBuilder<? extends Skill> builder) {
        super(builder);
        this.consumption = 50.0F;
        this.maxStackSize = 1;
    }

    @Override
    public void executeOnServer(SkillContainer container, FriendlyByteBuf args) {
        ServerPlayerPatch playerpatch = container.getServerExecutor();
        if (playerpatch != null) {
            ServerPlayer player = (ServerPlayer) playerpatch.getOriginal();
            if (player.isShiftKeyDown()) {
                boolean released = DingShenFaService.releaseAllByCaster(player);
                if (released) {
                    player.displayClientMessage(Component.translatable("ui.bhspells.ding_shen_fa_cancelled"), true);
                }
                return;
            }
            if (net.offkung.bhspells.compat.epicfight.common.animation.IronSpellAnimations.DING_SHEN_FA != null) {
                playerpatch.playAnimationSynchronized(net.offkung.bhspells.compat.epicfight.common.animation.IronSpellAnimations.DING_SHEN_FA, 0.0F);
            }
        }
        super.executeOnServer(container, args);
    }

    @Override
    public ResourceLocation getSkillTexture() {
        return SKILL_ICON;
    }

    @Override
    public boolean shouldDraw(SkillContainer container) {
        return true;
    }
}
