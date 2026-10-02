# BHSpells project map

Use this reference when the affected area or dependency chain is not yet clear. Paths are relative to the repository root.

## Platform and authority

- Minecraft Forge 1.20.1, Forge 47.4.0, Java 17.
- Mod ID and Java root: `bhspells` / `net.offkung.bhspells`.
- Iron's Spells 'n Spellbooks supplies the spell API and lifecycle. GeckoLib, Epic Fight-related modules, and AAA Particles participate in selected presentation and combat paths.
- Runtime behavior is determined by implementation and registries. Per-spell documents are mandatory specifications that must be synchronized with changes. Overview docs are navigation and may contain stale totals or historical status.

## Primary entry points

| Concern | Start here | Typical next files |
| --- | --- | --- |
| Mod bootstrap and common registration | `src/main/java/net/offkung/bhspells/BHSpells.java` | `registry/*`, `network/PacketHandler.java`, `compat/CompatBootstrap.java` |
| Client provider/renderer registration | `src/main/java/net/offkung/bhspells/client/BHSpellsClient.java` | `client/particle/*`, `client/renderer/*`, entity renderers |
| Spells | `registry/BHSpellRegistry.java` | `spells/<school>/*Spell.java`, `config/SpellConfig.java`, spell document |
| Entities | `registry/EntityRegistry.java` | `entity/spells/<feature>/*`, `BHSpellsClient.java` |
| Effects | `registry/MobEffectsRegistry.java` | `effect/*`, `event/*`, effect textures and lang |
| Particles | `registry/ParticleRegistry.java` | `client/particle/*`, `BHSpellsClient.java`, particle JSON and textures |
| Sounds | `registry/BHSoundRegistry.java` | `assets/bhspells/sounds.json`, `assets/bhspells/sounds/*` |
| Schools and attributes | `registry/BHSchoolRegistry.java`, `registry/AttributeRegistry.java` | `docs/systems/custom_magic_schools.md`, tags/data |
| Damage types | `registry/DamageTypesRegistry.java`, `registry/DamageSourcesRegistry.java` | `data/bhspells/damage_type/*`, tags |
| General packets | `network/PacketHandler.java` | `network/client/*`, `network/server/*` |
| Pressure packets | `pressure/network/PressureNetwork.java` | `pressure/server/*`, `pressure/client/*` |
| Compatibility | `compat/CompatBootstrap.java`, `compat/CompatMods.java` | provider-specific bridge packages |
| Mixins | `src/main/resources/bhspells.mixins.json` | `mixin/*`, `mixin/client/*`, `mixin/epicfight/*` |

All Java paths in the table are under `src/main/java/net/offkung/bhspells/` unless shown otherwise.

## Package responsibilities

- `spells/<school>/`: spell metadata, cast validation, lifecycle callbacks, and orchestration of spell-specific results.
- `entity/spells/`: projectiles, persistent AoEs, control entities, synced state, models, and entity-local renderers.
- `effect/`: `MobEffect` implementations and state expressed through effects.
- `event/`: Forge lifecycle/combat hooks and longer-lived managers.
- `service/`: shared server gameplay services such as stun or skill state.
- `client/`: client events, particles, render helpers, HUD/input, and provider registration.
- `network/`: the general `SimpleChannel`; some large subsystems intentionally own separate channels.
- `pressure/`: self-contained spiritual-pressure data, server manager, restrictions, network, and client presentation.
- `compat/`: optional or external API boundaries. Keep direct external class references inside safe loaded bridges.
- `registry/`: Forge/Iron's Spells registration roots.
- `config/SpellConfig.java`: server-configurable tuning for the spells that expose configuration.
- `util/`: shared tags, particle helpers, and general utilities.

## Resource map

| Artifact | Location |
| --- | --- |
| English localization | `src/main/resources/assets/bhspells/lang/en_us.json` |
| Spell icons | `assets/bhspells/textures/gui/spell_icons/` |
| Effect icons | `assets/bhspells/textures/mob_effect/` |
| Particle definitions/textures | `assets/bhspells/particles/`, `assets/bhspells/textures/particle/` |
| GeckoLib geometry/animations | `assets/bhspells/geo/`, `animations/`, `animmodels/` |
| Effekseer assets | `assets/bhspells/effeks/` |
| Sound definitions/files | `assets/bhspells/sounds.json`, `assets/bhspells/sounds/` |
| Data pack content | `src/main/resources/data/bhspells/` |
| Mod/mixin metadata | `src/main/resources/META-INF/mods.toml`, `src/main/resources/bhspells.mixins.json` |

Resource paths in the table that begin with `assets/` are under `src/main/resources/`.

## Documentation router

- `docs/README.md`: document map and source-of-truth order.
- `docs/spell_mechanics.md`: shared cast types, server/client split, entity-backed patterns, recast and lifecycle examples.
- `docs/spells/README.md`: per-spell document rules and index.
- `docs/spells/<school>/<spell_id>.md`: required detailed spell specification.
- `docs/system_architecture.md`: package/registry/subsystem overview.
- `docs/compat_architecture.md`: Epic Fight/Avalon/AAA integration and animation resources.
- `docs/multiplayer_and_crash_analysis.md`: known/historical crash, packet, sync, and multiplayer risk patterns. Re-check current code before treating status as current.
- `docs/spiritual_pressure_architecture_and_roadmap.md`: pressure field design and roadmap.
- `docs/systems/*`: focused documents for custom schools, screen shake, chains, and Earth Roar stun.
- `docs/integration_plan_bhspells.md`: historical integration plan; use for lineage and expected artifacts, not as current implementation truth.

## High-risk seams

- Client/common classloading and optional dependencies can fail before runtime guards execute.
- Entity IDs may not resolve on a client when a spawn packet arrives; stable UUID/state synchronization and retry behavior may be needed.
- Static state can mix logical sides in an integrated server or leak across disconnects/world changes.
- Per-tick teleports and broad per-player packet loops can cause rubber-banding or packet amplification.
- Shared target-owned tags can let multiple casters overwrite each other's state; key state by caster when ownership is per caster.
- Friendly-fire predicates are easy to invert. Confirm semantics at every heal, buff, debuff, and damage call site.
- The general, pressure, and feature-specific network channels are distinct. Extend the owning channel instead of duplicating a protocol.

## Build and runtime commands

- Compile: `.\gradlew.bat compileJava`
- Full verification/package: `.\gradlew.bat build`
- Client runtime: `.\gradlew.bat runClient`
- Dedicated server runtime: `.\gradlew.bat runServer`
- GameTest runtime: `.\gradlew.bat runGameTestServer`
- Data generation: `.\gradlew.bat runData`

The repository currently relies heavily on compile/build plus manual in-game verification; inspect `src/test` before claiming automated test coverage.
