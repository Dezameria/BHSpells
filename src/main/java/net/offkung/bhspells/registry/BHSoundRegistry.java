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

        public static final RegistryObject<SoundEvent> XULI_DING_SOU = registerSoundEvent("xuli_ding_sou");
    public static final RegistryObject<SoundEvent> FASHU_DING1 = registerSoundEvent("fashu_ding1");
    public static final RegistryObject<SoundEvent> FASHU_DING2 = registerSoundEvent("fashu_ding2");

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
    public static RegistryObject<SoundEvent> BIRD_HITS = registerSoundEvent("entity.red_beryl_bird.bird_hits");
    public static RegistryObject<SoundEvent> UNLEASH_BIRDS = registerSoundEvent("entity.red_beryl_bird.bird_unleash");
    public static RegistryObject<SoundEvent> AUTUMN_WAVE = registerSoundEvent("spell.generic.jade_autumn_wave");
    public static RegistryObject<SoundEvent> SPRING_WAVE = registerSoundEvent("spell.generic.jade_spring_wave");
    public static RegistryObject<SoundEvent> WINTER_WAVE = registerSoundEvent("spell.generic.jade_winter_wave");
    public static RegistryObject<SoundEvent> SUMMER_WAVE = registerSoundEvent("spell.generic.jade_summer_wave");
    public static RegistryObject<SoundEvent> RAINY_WAVE = registerSoundEvent("spell.generic.jade_rainy_wave");
    public static RegistryObject<SoundEvent> JADE_WAVE_HIT = registerSoundEvent("entity.generic.jade_wave_hit");
    public static RegistryObject<SoundEvent> EAGLE_SCREAM = registerSoundEvent("entity.eagle.eagle_scream");
    public static RegistryObject<SoundEvent> SNAKE_EMBLEM = registerSoundEvent("snake_emblem");
    public static RegistryObject<SoundEvent> VENOMOUS_BLOSSOMFALL_CHARGE_1 = registerSoundEvent("venomous_blossomfall_charge_1");
    public static RegistryObject<SoundEvent> VENOMOUS_BLOSSOMFALL_CHARGE_2 = registerSoundEvent("venomous_blossomfall_charge_2");
    public static RegistryObject<SoundEvent> HYMN_OF_PURIFICATION = registerSoundEvent("hymnofpurification");

    private static RegistryObject<SoundEvent> registerSoundEvent(String name) {
        return SOUND_EVENTS.register(name, () -> SoundEvent.createVariableRangeEvent(ResourceLocation.fromNamespaceAndPath(BHSpells.MODID, name)));
    }
}
