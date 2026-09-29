package net.offkung.bhspells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;

public class ItemRegistry {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BHSpells.MODID);

    public static final RegistryObject<Item> GOLD_SCROLL_DUMMY = ITEMS.register("gold_scroll_dummy", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));

    public static void register(IEventBus modEventBus) {
        ITEMS.register(modEventBus);
    }
}
