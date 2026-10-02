# Tempest Reiatsu (แรงดันอัสนีบาตสีชมพู / 绯雷威压)

| Item | Value |
| --- | --- |
| Spell id | `bhspells:tempest_reiatsu` |
| School / rarity / max level | Lightning / Legendary / 5 |
| Spell cast type | Instant (Stance Toggle Domain Activation) |
| Mana Cost | 50 (+30/level) on activation, 0 upkeep |
| Cooldown | 60.0 seconds (triggered only upon closing the domain) |
| Base Radius | 64.0 blocks (+14.0 blocks/level, reaching 120.0 blocks at Level 5) |
| Strike Base Damage | 10.0 (+2.5/level) |
| Strike Cadence | Random strike every 10 to 30 ticks against candidate targets |
| Architecture | Reusable `ServerPressureManager` + `ClientPressureManager` Torrential Curtain Framework with `PressureReactionDispatcher` |
| Visual Concept | Camera-Centric 360° Hot Pink Torrential Deluge (`0xFF1493`) + Periodic Pink Thunderbolts |

## System Overview & Architecture

Tempest Reiatsu unleashes a catastrophic pink lightning storm domain across 64 to 120 blocks:
- **Stance Toggle Mode**: Casting once manifests the domain around the caster. It remains active indefinitely until recast, at which point the domain closes and triggers a 60-second cooldown.
- **Server Gameplay Truth & Periodic Strikes**: Evaluated every 5 ticks. In addition to standard movement debuffs and jump lockout, the server runs a seeded periodic strike scheduler every 10-30 ticks, selecting a random hostile/non-allied target inside the domain. The target is struck by a thunderbolt dealing lightning damage, suffering Slowness and a stagger/stun reaction.
- **Decoupled Event Architecture**: Reactions are dispatched via `PressureReactionDispatcher` observer registry, completely decoupling server mechanics from external mod compatibility (Epic Fight).
- **Client Torrential Curtain (Full 3D Heaven-to-Earth / ทั่วฟ้าดิน)**: Employs 3-tier camera-centric procedural streak generation across a 48-block cylindrical canopy around the player. Wherever the player travels inside the 120-block domain, they are enveloped in an unceasing 3D deluge of violent pink lightning energy pouring down from 48 blocks in the heavens, surging through mid-air, and driving into the earth with high-speed texture streaming.
- **Screen & Audio Presentation**: Top and bottom streaming energy bands in hot pink, heavy vignette, chromatic fringe, and rhythmic continuous rumble via `TOFollowingScreenShakeEntity`.

## Targeting & Reaction States

### Targeting
- Targets hostile mobs, PvP enemies, and non-allied entities within the 64-120 block radius.
- The caster and allies are excluded from strikes and hostile debuffs.

### Reaction States & Thresholds
Normalized intensity `[0.0 - 1.0]`:
1. **NONE / Mild (0.00 - 0.30)**: Slight pink electrical aura.
2. **STAGGER (0.30 - 0.45)**: `SpiritualPressureEffect` I, Slowness I, stagger reaction animation.
3. **CROUCH (0.45 - 0.60)**: `SpiritualPressureEffect` I, Slowness I, crouch reaction animation.
4. **KNEEL (0.60 - 0.85)**: `SpiritualPressureEffect` II, Slowness II, server jump lockout (`TAG_ROOTED`), kneel reaction animation.
5. **KNOCKDOWN (0.85 - 1.00)**: `SpiritualPressureEffect` III, Slowness IV, server jump lockout (`TAG_ROOTED`), knockdown reaction animation.
- Direct lightning strikes additionally inflict a brief stun/stagger reaction.

## Configuration (`SpellConfig.TempestReiatsu`)

- `base_mana`: 50
- `mana_per_level`: 30
- `cooldown_seconds`: 60.0
- `base_radius`: 64.0
- `radius_per_level`: 14.0
- `base_damage`: 10.0
- `damage_per_level`: 2.5
- `strike_interval_min`: 10
- `strike_interval_max`: 30
- `global_streak_budget`: 480

## Visual Profile (`PressureVisualProfile.TEMPEST_LIGHTNING`)
- `curtain_radius`: 48.0 (visual profile constant for torrential camera curtain)
- `streak_count`: 480
- `color`: `0xFF1493` (Deep pink lightning storm)
