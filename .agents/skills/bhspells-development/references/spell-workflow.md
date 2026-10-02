# Spell change workflow

Read this reference for every task that creates, changes, fixes, balances, refactors, or removes a spell.

## 1. Lock the spell surface

For each affected spell independently:

1. Read `docs/spells/README.md` and `docs/spells/<school>/<spell_id>.md` before editing code.
2. Run the context locator with `-Kind Spell` and inspect the spell class plus actual references returned for registry, config, entities, effects, events/services, packets, client code, assets, language, and docs.
3. Read `docs/spell_mechanics.md` when cast type, recast state, entity lifecycle, targeting, or the server/client boundary is involved.
4. Inspect one local analogue with the closest lifecycle. Do not copy a visually similar spell whose authority or lifecycle is different.
5. State the requested behavior in concrete terms: target rules, timing, range/shape, formulas, scaling, mana/cooldown, effects, spawned state, audiovisual behavior, cancellation, and cleanup.

Treat multiple spells as separate specifications even when they share infrastructure.

## 2. Trace the implementation chain

Not every spell uses every layer. Check the applicable chain:

```text
BHSpellRegistry
  -> spell class and DefaultConfig
  -> optional SpellConfig server values
  -> target/pre-cast validation
  -> server cast/recast/channel callback
  -> entity/effect/event/service state
  -> synchronized entity data or packet
  -> client renderer/particle/sound/animation
  -> lang/icon/model/texture/data assets
  -> per-spell documentation
```

Confirm values at their real owner. A tooltip, document, or `DefaultConfig` may not be the live source when `SpellConfig` overrides it.

## 3. Implement or change safely

### Spell class

- Preserve the stable local structure for a small edit. For a new class or structural refactor, follow the member order in repository `AGENTS.md`.
- Keep identity and registry path stable unless renaming is explicitly requested; a rename is a migration across code, docs, lang, icons, config, commands, and saved references.
- Put denial logic in `checkPreCastConditions` when failure must avoid mana/cooldown consumption and give player feedback when useful.
- Use Iron's Spells friendly-fire helpers consistently. Check whether the operation targets allies, enemies, self, or all living entities.
- Keep damage, effects, teleport/motion, entity creation, mana/cooldown, and stage transitions server-authoritative.

### Persistent or multi-stage state

- Decide who owns state: `MagicData`, effect/NBT, entity synced data, server manager, or packet-fed client cache.
- Use UUIDs for identity that must survive entity-ID timing; never let the client decide combat outcomes from display state.
- Define termination for success, timeout, cancellation/anti-magic, death, disconnect, dimension change, unload, and owner/target disappearance as applicable.
- Keep integrated-server sides separate. Add server-side guards before placing logical entities in static collections.
- Bound active instances, search radius, packet recipients, and per-tick work.

### Entity and presentation layers

- Register new entity types in `EntityRegistry` and client renderers in `BHSpellsClient`.
- Sync only state needed by remote clients. Interpolate presentation without moving gameplay authority to the renderer.
- Register new particles in `ParticleRegistry`, their providers in `BHSpellsClient`, and both particle JSON and textures.
- Register sounds in `BHSoundRegistry` and `assets/bhspells/sounds.json`; add the `.ogg` resource.
- Keep client imports out of the spell/entity common path unless the class is client-only and cannot load on a dedicated server.

### Configuration and assets

- If adding configurable values, follow the nearest `SpellConfig` nested-class pattern and wire the values into the actual runtime calculation.
- Keep `DefaultConfig`, exposed server config, tooltips, and docs semantically aligned.
- Check `assets/bhspells/lang/en_us.json` for spell name, guide, unique info, denial/action-bar messages, effects, and entities as applicable.
- Check the spell icon at `assets/bhspells/textures/gui/spell_icons/<spell_id>.png` and any model/geometry/animation/particle assets referenced by ID.

## 4. New or removed spell integration

For a new spell, create its per-spell document in the same change. Also update every applicable artifact:

- `BHSpellRegistry.java`
- spell class under the correct school package
- `SpellConfig.java` if configurable
- entities/effects/events/services and their registries
- packet channel and client handler if synchronization beyond entity data is needed
- renderer/particle/provider registrations
- language, icon, sounds, models, geometry, animation, data, and tags
- `docs/spells/<school>/<spell_id>.md`
- `docs/spells/README.md` and `docs/all_spells.md`

For removal, find all references before deleting and preserve save/data compatibility when the request requires it. Do not use overview spell counts as proof; derive the registry set and document set.

## 5. Documentation synchronization

Update the per-spell document whenever implementation details change, including:

- identity, school, rarity, maximum level, cast type/time, animation, and sounds
- mana, cooldown, range, radius/shape, target rules, friendly fire, and validation
- damage/healing formulas, spell-power scaling, effects, amplifiers, durations, and intervals
- entities, phases, recast windows, cancellation, anti-magic, and cleanup
- packets, synced data, server/client responsibility, particles, renderer, models, textures, and other assets
- verification results and runtime behavior still requiring in-game checks

If code and document were out of sync before the task, report what was synchronized rather than silently choosing one.

## 6. Verification

1. Re-run the locator and inspect the final candidate set for missed IDs or references.
2. Compare the final implementation to the spell document line by line for the affected behavior.
3. Validate JSON syntax and resource paths when assets/lang/data changed.
4. Run `.\gradlew.bat compileJava`, then `.\gradlew.bat build` for completed implementation.
5. Exercise the relevant runtime matrix when feasible: single-player/integrated server, dedicated server, multiple players, owner/target death, disconnect, dimension change, chunk unload, cancellation/anti-magic, and optional mod loaded/absent.
6. Report manual checks that remain, especially VFX geometry, animation timing, sound, camera behavior, latency, and balance.
