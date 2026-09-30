# Project Agent Rules

## Spell documentation is mandatory

These rules apply to every task that creates, changes, fixes, balances, refactors, or removes a spell.

1. Before changing spell code, read `docs/spells/README.md` and the spell-specific document at `docs/spells/<school>/<spell_id>.md`.
2. Treat each spell separately. Every spell must have its own document placed in its corresponding magic school directory and named with its registry path in lowercase snake case, for example `docs/spells/nature/wings_of_tempest.md`.
3. If a new spell has no document, create its spell-specific document as part of the same change. Do not implement a new spell without documenting it.
4. When implementation details change, update the corresponding spell document in the same change. This includes balance values, behavior, targeting, range, radius, duration, damage, effects, cooldown, mana cost, cast type/time, scaling, entities, particles, sounds, animations, assets, and server/client behavior.
5. If a task affects multiple spells, read and update each affected spell document independently. Do not combine their detailed specifications into one shared spell file.
6. Keep code, registry IDs, language entries, assets, and spell documentation consistent. If the existing document and code disagree, resolve the discrepancy according to the requested behavior and explicitly report the synchronization.
7. Before finishing a spell task, compare the final implementation against its document and run the relevant build or tests. Report any remaining mismatch or unverified runtime behavior.
8. `docs/all_spells.md` may remain a summary or index, but it does not replace the required per-spell document in `docs/spells/<school>/`.

## Spell class member formatting standard

For new spell classes, and existing spell classes undergoing a structural refactor, follow the BHSpells-preferred member order:
`spell identity -> constants/static fields -> optional unit info -> default config field -> constructor -> standard getters/overrides -> spell logic/helpers`

1. **Spell identity**: Put the spell `ResourceLocation` first. Use the existing local form (`spellId` or `SPELL_ID`); add `SPELL_ID_STR` only when another system needs the string form for networking, tracking, or registration.
2. **Constants and static fields**: Put tuning values and class-wide state after the identity. Use the narrowest visibility that consumers require; constants are not required to be `public`.
3. **Optional unit info**: If the spell supplies custom tooltip rows, place `getUniqueInfo(int spellLevel, LivingEntity caster)` after the constants. Do not add an empty override when the inherited tooltip is sufficient.
4. **Default config field**: Initialize `DefaultConfig defaultConfig` before the constructor. Its `getDefaultConfig()` override belongs with the other standard overrides after the constructor.
5. **Constructor**: Initialize inherited spell properties after all fields.
6. **Standard getters/overrides**: Group metadata and engine overrides such as `getDefaultConfig`, `getSpellResource`, `getCastType`, mana/cooldown methods, animations, and sounds after the constructor.
7. **Spell logic and helpers**: Put casting eligibility and pre-cast checks before `onCast`. Spell-specific calculations may sit next to the lifecycle method they support; private implementation helpers normally follow the public/override methods.

Do not mechanically reorder an otherwise untouched legacy BHSpells class only to satisfy this convention. When changing an existing class without a formatting refactor, preserve its stable local structure and place new members in the nearest matching section.
