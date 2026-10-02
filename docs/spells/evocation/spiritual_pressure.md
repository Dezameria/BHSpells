# Spiritual Pressure (灵压 / Reiatsu Field)

| Item | Value |
| --- | --- |
| Spell id | `bhspells:spiritual_pressure` |
| School / rarity / max level | Evocation / Legendary / 5 |
| Spell cast type | Instant (Entity-follow field activation with Recast-to-Close toggle) |
| Mana / cooldown | 75 (+15/level) / 35 seconds |
| Duration | 300 ticks (15.0 seconds) |
| Base Radius | 12 blocks (+2 blocks/level, max 20 blocks) |
| Architecture | Reusable `ServerPressureManager` + `ClientPressureManager` Framework with `PressureReactionDispatcher` |
| Visual Concept | Procedural vertical Reiatsu streaks pulling downward + Single-pass screen distortion/vignette |

## System Overview & Architecture

The Spiritual Pressure system is built on the strict architectural principle:
- **Server = Gameplay Truth**: The server tracks field identity, owner, center (following caster), radius, duration, and ticks every 5 ticks to evaluate spatial AoE. It applies server-authoritative debuffs (`SpiritualPressureEffect`, slowness, root/jump restriction) and synchronizes minimal lifecycle events.
- **Recast-to-Close Toggle**: Casting the spell while a field is already active will cancel the active domain immediately and initiate cooldown without additional mana cost.
- **Decoupled Event Architecture**: Reactions are dispatched via `PressureReactionDispatcher` observer registry, completely decoupling server mechanics from external mod compatibility (Epic Fight).
- **Client = Visual Complexity**: The client reconstructs all procedural streak geometry deterministically using a field seed and game time. All fields share a single batched renderer (`PressureFieldRenderer`) with a hard-capped global streak budget (configurable, default: 256 streaks) and zero raycasts per frame (O(1) heightmap lookup cached per cycle in `PressureStreak`).
- **Screen Aggregation**: Multiple overlapping fields (e.g. Pink + Green) are evaluated by `ScreenPressureAggregator` and drawn in a unified single composite post-processing pass (`ScreenPressurePostProcessor`), combining primary color push, secondary chromatic interference, vignette, noise, and subtle camera instability (`ViewportEvent.ComputeCameraAngles`).

## Targeting & Reaction States

### Targeting
- Affects hostile mobs, PvP targets, and non-allied living entities within the field radius.
- The caster and allied players/entities are immune to hostile debuffs and receive mild aura feedback.
- Overlap policy: **Strongest-Field-Wins** per target for server debuffs to avoid exponential attribute multiplication, while client visual effects aggregate both sources.

### Reaction States & Thresholds
Calculated from normalized pressure intensity `[0.0 - 1.0]`:
1. **NONE / Mild (0.00 - 0.30)**: Slight visual tint.
2. **STAGGER (0.30 - 0.45)**: `SpiritualPressureEffect` (amplifier 0), stagger reaction animation.
3. **CROUCH (0.45 - 0.60)**: `SpiritualPressureEffect` (amplifier 0), crouch reaction animation.
4. **KNEEL (0.60 - 0.85)**: `SpiritualPressureEffect` (amplifier 1), Slowness II, server-enforced jump lockout (`TAG_ROOTED`), kneel reaction animation.
5. **KNOCKDOWN (0.85 - 1.00)**: `SpiritualPressureEffect` (amplifier 2), Slowness IV, server-enforced jump lockout (`TAG_ROOTED`), knockdown reaction animation.

## Epic Fight Compatibility

- Isolated in `compat/epicfight/pressure/`. The core mod functions completely without Epic Fight.
- Reaction transitions (`NONE -> STAGGER -> CROUCH/KNEEL -> KNOCKDOWN`) map to Epic Fight animations (`BIPED_HIT_SHORT`, `BIPED_KNEEL`, `BIPED_KNOCKDOWN`).
- Exiting pressure fields (`PressureReaction.NONE`) resets affected entities back to `BIPED_IDLE` if they are currently executing a pressure reaction.
- **Animation Priority Guard**:
  ```text
  DEATH > DODGING / SKILL CAST > EXISTING KNOCKDOWN > KNEEL > STAGGER > NORMAL LOCOMOTION
  ```
  Pressure reactions never override death, active dodges, or cast skills. Packets are dispatched only on state transition.

## World Rendering & Performance Budget

- **Streaks**: Vertical crossed quads with a neutral white texture tinted by vertex color.
- **Heightmap Caching**: Zero raycasts per frame. Ground Y is cached per streak cycle using `level.getHeight(Heightmap.Types.MOTION_BLOCKING, x, z)`.
- **Global Budget**: Configurable via `SpellConfig.SpiritualPressure.global_streak_budget` (default: 256 active streaks across all visible fields).
- **Distance LOD**:
  - `0 - 24 blocks`: 100% streak density (~200–250 streaks).
  - `24 - 40 blocks`: 50% streak density (~100–120 streaks).
  - `40 - 64 blocks`: 25% streak density (~50–60 streaks).
  - `> 64 blocks`: Culled.

## Configuration (`SpellConfig.SpiritualPressure`)

- `base_mana`: Base mana cost (default: 75).
- `mana_per_level`: Mana increase per level (default: 15).
- `cooldown_seconds`: Cooldown duration in seconds (default: 35.0).
- `base_radius`: Base radius in blocks (default: 12.0).
- `radius_per_level`: Radius increase per level (default: 2.0).
- `duration_ticks`: Duration in ticks (default: 300).
- `global_streak_budget`: Client max streaks rendered across all fields (default: 256).
