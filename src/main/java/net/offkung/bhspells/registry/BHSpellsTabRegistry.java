package net.offkung.bhspells.registry;

import com.gametechbc.traveloptics.init.TravelopticsItems;
import com.gametechbc.traveloptics.init.TravelopticsTabs;
import io.redspace.ironsspellbooks.api.registry.SpellRegistry;
import io.redspace.ironsspellbooks.api.spells.ISpellContainer;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;

@Mod.EventBusSubscriber(modid = BHSpells.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class BHSpellsTabRegistry {
    public static final DeferredRegister<CreativeModeTab> REGISTRY = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, BHSpells.MODID);

    public static final RegistryObject<CreativeModeTab> BHSPELLS_SCROLLS = REGISTRY.register("bhspells_scrolls", () -> CreativeModeTab.builder().title(Component.translatable("item_group.bhspells.bhspells_scrolls")).icon(() -> new ItemStack(ItemRegistry.GOLD_SCROLL_DUMMY.get())).withTabsAfter(new ResourceKey[]{TravelopticsTabs.TRAVELOPTICS_SCROLLS.getKey()}).build());

    @SubscribeEvent
    public static void fillCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        if (event.getTab() == CreativeModeTabs.searchTab() || event.getTab() == BHSPELLS_SCROLLS.get()) {
            SpellRegistry.getEnabledSpells().stream().filter((spellType) -> spellType != SpellRegistry.none()).filter((spell) -> spell.getSpellResource().getNamespace().equals("bhspells")).forEach((spell) -> {
                for(int i = spell.getMinLevel(); i <= spell.getMaxLevel(); ++i) {
                    ItemStack itemstack = new ItemStack(io.redspace.ironsspellbooks.registries.ItemRegistry.SCROLL.get());
                    ISpellContainer.createScrollContainer(spell, i, itemstack);
                    event.accept(itemstack);
                }
            });
        }
    }

    public static void register(IEventBus modEventBus) {
        REGISTRY.register(modEventBus);
    }
}
