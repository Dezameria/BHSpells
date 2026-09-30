package net.offkung.bhspells.compat.epicfight.skills.dingshenfa;

import net.offkung.bhspells.BHSpells;
import yesman.epicfight.api.forgeevent.SkillBuildEvent;
import yesman.epicfight.skill.Skill;

public final class DingShenFaSkills {
    public static Skill DING_SHEN_FA;

    private DingShenFaSkills() {
    }

    public static void buildSkills(SkillBuildEvent event) {
        SkillBuildEvent.ModRegistryWorker worker = event.createRegistryWorker(BHSpells.MODID);
        DING_SHEN_FA = worker.build("ding_shen_fa", DingShenFaSkill::new, DingShenFaSkill.create());
    }
}
