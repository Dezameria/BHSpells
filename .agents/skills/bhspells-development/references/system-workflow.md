# Shared system workflow

Use this reference for non-spell systems and for cross-cutting parts of a spell change.

## Route by change type

| Change | Required entry points | Consistency checks |
| --- | --- | --- |
| Registry/bootstrap | `BHSpells.java`, matching `registry/*Registry.java` | registration bus, namespace/path, supplier timing, duplicate IDs |
| Entity | `EntityRegistry.java`, entity class, `BHSpellsClient.java` | dimensions/tracking/update interval, spawn data, synced data, renderer, cleanup |
| Mob effect | `MobEffectsRegistry.java`, `effect/*`, callers | icon, lang, curative/visibility flags, server tick behavior |
| Particle | `ParticleRegistry.java`, `client/particle/*`, `BHSpellsClient.java` | provider registration, particle JSON, texture frames, render type |
| Sound | `BHSoundRegistry.java`, `sounds.json`, `.ogg` | event ID, subtitle/lang if used, server/client playback ownership |
| General network | `network/PacketHandler.java`, packet class | direction, sequential discriminator, encode/decode symmetry, enqueue, validation, recipient scope |
| Pressure network | `pressure/network/PressureNetwork.java` | do not also register the packet in the general channel |
| Feature-owned network | owning `*Network.java` such as Savage Bite | ownership, protocol/version, lifecycle cleanup |
| Client event/HUD/input | `client/event/*` | `Dist.CLIENT`, disconnect/world reset, request validation on server |
| Compatibility | `compat/CompatBootstrap.java`, `CompatMods.java`, loaded bridge | no eager optional classloading, fallback result, loaded/absent tests |
| Epic Fight animation | `compat/epicfight/*`, animation assets | cue ID, builder/player wiring, client/common boundary, resource path |
| Mixin | mixin class, `bhspells.mixins.json` | common vs client list, target signature, optional target safety, failure mode |
| Config | `config/SpellConfig.java`, consumer | define once, valid range, actual runtime use, docs/default alignment |
| Damage type/tag/data | damage registries, `data/bhspells/*` | resource ID, JSON, tag membership, source construction |
| Pressure field | `pressure/server/*`, `pressure/network/*`, `pressure/client/*` | lifecycle, tracking recipients, visual budget, world/disconnect cleanup |

## Authority and side safety

- Common/server classes may reference common Minecraft/Forge APIs only. A runtime `level.isClientSide` check does not prevent classloading failure from a client-only import.
- Client-to-server packets express intent. Revalidate sender, permission/state, distance, target, cooldown/mana, and payload bounds on the server.
- Server-to-client packets should target only tracking/affected players and carry enough stable identity for client timing.
- Schedule packet work onto the correct game thread. Keep encode/decode field order identical.
- Do not use the renderer or a client cache as persistent gameplay state.

## Lifecycle and multiplayer audit

For every long-lived map, set, cache, entity link, scheduled action, or state machine, answer:

1. Is it per server, level, dimension, entity, player, or client session?
2. Can two casters or two worlds collide in the key space?
3. What clears it on death, logout, dimension transfer, unload, server stop, client disconnect, and normal expiry?
4. Can an entity ID be unresolved or reused? Is UUID or synchronized data required?
5. Is work bounded per tick and scoped spatially?
6. Does integrated server execution mix client and server instances in shared static state?

Prefer motion plus synchronization over server teleport every tick. Prefer tracking or radius-targeted recipients over broadcasting inside nested loops.

## Optional dependency audit

- Keep direct external types inside a bridge loaded only after the mod-presence check.
- Beware method signatures, static fields, annotations, mixin targets, and class literals: each can trigger eager linkage before a guard runs.
- Define explicit fallback behavior for absent integrations.
- Consult `docs/compat_architecture.md` for current animation/VFX layout and `docs/multiplayer_and_crash_analysis.md` for historical failure patterns, then verify against current code.

## Resource and registration audit

When adding a runtime object, search for a complete local analogue and compare all layers:

```text
registry -> bootstrap -> implementation -> client provider/renderer
         -> lang -> JSON definition -> binary texture/model/sound
         -> data/tag -> documentation
```

Use lowercase namespace paths and keep Java `ResourceLocation`, JSON references, and filesystem case identical. Validate JSON after edits.

## Verification by risk

- Pure docs/skill/reference: validate links, scripts, and changed text; a Gradle build is optional if production inputs did not change.
- Java logic or registry/config: `compileJava`, then `build`.
- Client resources/rendering: build plus `runClient` manual verification.
- Server/common/network/compat: build plus dedicated-server and multiplayer checks when feasible.
- Data generation changes: `runData`, review generated diff, then build.
- Mixins: build and launch the affected environment; compilation alone does not validate target application.

Always distinguish automated evidence from manual runtime checks.
