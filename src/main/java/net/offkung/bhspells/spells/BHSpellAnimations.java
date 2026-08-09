package net.offkung.bhspells.spells;

import io.redspace.ironsspellbooks.api.util.AnimationHolder;
import net.minecraft.resources.ResourceLocation;
import net.offkung.bhspells.BHSpells;

public class BHSpellAnimations {
    public static ResourceLocation ANIMATION_RESOURCE = ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "animation");
    public static final AnimationHolder PURIFICATION_PILLAR_CHARGE = new AnimationHolder("bhspells:purification_pillar_charge", true, false);
    public static final AnimationHolder PURIFICATION_PILLAR_CAST = new AnimationHolder("bhspells:purification_pillar_cast", true, false);
}