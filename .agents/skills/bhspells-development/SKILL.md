---
name: bhspells-development
description: Navigate and implement changes in the BHSpells Forge 1.20.1 project, including spells, configuration, registries, entities, effects, services, networking, client rendering, assets, compatibility, documentation, and verification. Use when inspecting, fixing, extending, balancing, or reviewing this repository; do not use for unrelated generic Minecraft modding.
---

# BHSpells Development

Use this skill as the repository-specific navigation and correctness layer for BHSpells. Preserve the user's scope; do not treat this skill as authorization to refactor neighboring systems.

## Start with the smallest useful context

1. Read the repository-root `AGENTS.md` and inspect `git status --short`. Preserve unrelated or pre-existing edits.
2. Classify the request before opening broad parts of the repository:
   - For any spell behavior, balance, implementation, refactor, addition, or removal, read `docs/spells/README.md`, then every affected `docs/spells/<school>/<spell_id>.md`, then read [references/spell-workflow.md](references/spell-workflow.md).
   - For registries, effects, entities, networking, rendering, assets, mixins, compatibility, pressure, or another shared subsystem, read [references/system-workflow.md](references/system-workflow.md).
   - When the relevant area or entry point is unclear, read [references/project-map.md](references/project-map.md).
3. Inspect a nearby working implementation before inventing a new pattern. Prefer an analogue with the same cast type, entity lifecycle, packet direction, renderer type, or compatibility boundary.
4. Treat implementation and registries as the current runtime truth, spell documents as required specifications, and overview documents as navigation. If they disagree, resolve the discrepancy according to the requested behavior and synchronize every affected artifact.

Do not load every reference for a narrow task. Cross-cutting changes may require both workflow references.

## Find related files in one command

Run the read-only context locator from the repository root:

```powershell
powershell -NoProfile -ExecutionPolicy Bypass -File .agents/skills/bhspells-development/scripts/find-context.ps1 -Query rapturous_bloom -Kind Spell
powershell -NoProfile -ExecutionPolicy Bypass -File .agents/skills/bhspells-development/scripts/find-context.ps1 -Query screen_shake -Kind System
```

Accepted kinds are `All`, `Spell`, `System`, `Docs`, and `Assets`. Use the output as a candidate set, then inspect only files that can affect the request. The locator searches names and textual references; it does not prove runtime ownership or dependency direction.

## Project invariants

- Keep gameplay authoritative on the logical server. The client renders synchronized state and handles presentation only.
- Keep dedicated-server classloading safe: common/server code must not import or eagerly link `net.minecraft.client.*` or client-only compatibility classes.
- Route optional-mod behavior through `compat/` boundaries and verify both loaded and absent behavior when the dependency is optional.
- For entity, effect, recast, or manager state, define cleanup for death, disconnect, dimension change, unload, cancellation, expiry, and normal completion as applicable.
- For packets, verify direction, registration order/protocol, payload bounds, thread enqueueing, sender validation, tracking scope, and server-side revalidation of client requests.
- Keep registry IDs, config fields, language keys, assets, data files, render/provider registration, and documentation consistent.
- Do not trust hard-coded spell totals in overview prose. Derive the current set from `BHSpellRegistry.java` and compare it with the per-spell document tree.
- Follow the spell class member order in `AGENTS.md` for new classes and structural refactors. Preserve stable local structure for smaller legacy edits.
- Preserve existing code: do not modify existing classes unless required for registration (`*Registry.java`, `PacketHandler.java`, etc.) or explicitly requested. Keep code clean and well-ordered.
- Multi-line block and brace standard: never place opening and closing braces on the same line (e.g. `public static void onUse() {}` is forbidden). Always place the closing brace `}` on a separate line so conditions and block scopes are visually distinct.
- Single mixin config: never create new `*.mixins.json` files. Consolidate all mixins into `src/main/resources/bhspells.mixins.json`.

## Finish with evidence

Review the final diff and verify only the affected surface first. Use the repository wrapper:

```powershell
.\gradlew.bat compileJava
.\gradlew.bat build
```

Use `build` for completed implementation unless the task is documentation/skill-only or a narrower check is explicitly justified. Compilation does not validate visuals, animation timing, multiplayer synchronization, optional-mod absence, or in-game balance; report those as manual checks. For spell work, perform the final code-to-document comparison required by `AGENTS.md`.
