---
name: bhspells-vfx
description: Create, modify, balance, debug, and review visual effects (VFX) in BHSpells Forge 1.20.1, including Minecraft particles, custom particle types and options, visual entity models (GeckoLib and vanilla), direct and procedural renderers, player and entity render layers, custom RenderTypes, textures, Effekseer and AAAParticles, screen shake, and impact VFX. Use when authoring or troubleshooting any visual presentation or effect system in this repository; do not use for non-visual spell mechanics.
---

# BHSpells VFX & Effect Development

Use this skill as the repository-specific guide for creating, maintaining, and debugging particle VFX, visual entities, renderers, models, textures, and combat impact visuals in BHSpells.

## First Actions

1. Check repository root `AGENTS.md` and inspect `git status --short`. Preserve unrelated or pre-existing modifications.
2. If this VFX work belongs to a spell, read `docs/spells/README.md` and the spell's specification document in `docs/spells/<school>/<spell_id>.md`.
3. Choose the smallest and most appropriate VFX technique for the effect. Do not invent an overly complex entity or custom shader when a simple particle or existing helper is sufficient.
4. Consult [references/exemplar-map.md](references/exemplar-map.md) to locate an established working analogue before creating new assets or classes.

## VFX Family Selection & Routing

Read only the reference file matching the visual technique you are implementing or fixing:

| VFX Requirement | Technique / Architecture | Primary Reference |
|---|---|---|
| Transient sparks, bursts, smoke, leaf drops, magic dust, custom particle types | `SimpleParticleType`, custom `ParticleOptions`, `TextureSheetParticle`, `ParticleRenderType` | [references/particles.md](references/particles.md) |
| Animated 3D models spawned in-world (gates, eagles, bamboo, crystals, slashes, barriers) | GeckoLib (`GeoEntity`), Vanilla `EntityModel` (`ModelLayerLocation`), Direct quad/mesh `EntityRenderer` | [references/visual-entities.md](references/visual-entities.md) |
| Auras, ribbons, charging overlays, custom RenderType, blend modes, fullbright textures | `RenderLayer` on players/living entities, custom `RenderType.CompositeState`, scrolling UVs, textures | [references/render-layers-shaders-textures.md](references/render-layers-shaders-textures.md) |
| 3D Effekseer effects (`.efkefc`), screen shake, ground fractures, coordinated impact feel | `AAALevel` emitter, `ScreenShakePacket`, `EpicFightFractureHelper`, `*Vfx` helper classes | [references/orchestration-and-balance.md](references/orchestration-and-balance.md) |
| Invisible effects, missing textures, blend leaks, dedicated-server crash, FPS drops | Systematic layer-by-layer diagnostics, verification checklists, performance auditing | [references/debugging-and-verification.md](references/debugging-and-verification.md) |

## Core Repository Invariants

- **Gameplay & Spawn Authority:** The logical server drives spell outcomes, spawns synchronized entities, and sends particle packets. The client handles presentation, rendering, interpolation, and local particle physics.
- **Dedicated Server Classloading Safety:** Common and server classes MUST NOT import or directly reference `net.minecraft.client.*`, client-only renderer classes, or client-only compatibility hooks. Restrict all client-only registrations and handlers to `Dist.CLIENT` (e.g., `BHSpellsClient.java`).
- **Resource Consistency:** Java identifiers (`ResourceLocation`), registry keys, JSON definitions, particle sprite names, texture PNG paths, and model JSON paths must match exactly in casing (lowercase snake_case) and directory structure.
- **Visual Lifecycle & Cleanup:** Visual entities must have bounded lifespans (`tickCount >= lifetime -> discard()`), synchronized removal states (e.g., fade out, closing animations), and proper discard on caster death or dimension changes.
- **Performance & Budget:** Avoid excessive particle counts (>50 per tick per entity), un-culled complex meshes, or recreating vertex buffers every frame without caching. Use bounded radii for packet broadcasts.
- **Spell Documentation Requirement:** When modifying or adding VFX to any spell, update the corresponding `docs/spells/<school>/<spell_id>.md` document to accurately describe the visual presentation, particles, models, and sounds.
