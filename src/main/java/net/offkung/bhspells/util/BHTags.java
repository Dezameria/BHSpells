package net.offkung.bhspells.util;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.offkung.bhspells.BHSpells;

public class BHTags {
    public static final TagKey<Item> GOLD_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "gold_focus"));
    public static final TagKey<Item> GROUND_FOCUS = ItemTags.create(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, "ground_focus"));
}
