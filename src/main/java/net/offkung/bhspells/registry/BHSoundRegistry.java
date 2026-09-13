package net.offkung.bhspells.registry;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;
import net.offkung.bhspells.BHSpells;

public class BHSoundRegistry {
    private static final DeferredRegister<SoundEvent> SOUND_EVENTS = DeferredRegister.create(Registries.SOUND_EVENT, BHSpells.MODID);

    public static void register(IEventBus eventBus) {
        SOUND_EVENTS.register(eventBus);
    }

    public static RegistryObject<SoundEvent> WATER_BUBBLE = registerSoundEvent("water_bubble");
    public static RegistryObject<SoundEvent> BREAK_LARGE = registerSoundEvent("break_large");
    public static RegistryObject<SoundEvent> HIT_MEDIUM_2 = registerSoundEvent("hit_medium_2");
    public static RegistryObject<SoundEvent> SMALL_ROCK_HIT = registerSoundEvent("small_rock_hit");
    public static RegistryObject<SoundEvent> RUMBLE_1 = registerSoundEvent("rumble_1");
    public static RegistryObject<SoundEvent> SWORD_IMPACT = registerSoundEvent("sword_impact");
    public static RegistryObject<SoundEvent> IRON_PARRY = registerSoundEvent("iron_parry");
    public static RegistryObject<SoundEvent> FIRE_IMPACT_SPELL = registerSoundEvent("fire_impact_spell");
    public static RegistryObject<SoundEvent> GROUND_BREAKING = registerSoundEvent("ground_breaking");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, name)));
    }
}
