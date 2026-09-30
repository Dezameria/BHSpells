# Ding Shen Fa (定身法)

| Item | Value |
| --- | --- |
| Spell id / Epic Fight skill id | `bhspells:ding_shen_fa` |
| School / rarity / max level | Evocation / Epic / 1 |
| Spell cast type | Instant |
| Mana / cooldown | 50 / 50 seconds (1000 ticks) |
| Ding duration | 192 ticks (9.6 seconds) |
| Targeting range | 50 blocks |
| Multi-target AoE radius | Configurable via `multi_target_radius` (default: 0.0 = single target) |
| Reference | Wukong `biped/fashu/fashu_magicarts_dsf_start`, `BattleUnit.ding`, and `FashuDingshenfaSkill` |

## Animation and targeting

- `assets/bhspells/animmodels/animations/biped/spells/ding_shen_fa.json` is byte-identical to Wukong's `fashu_magicarts_dsf_start` animation asset.
- Uses `SpecialActionAnimation` extending Epic Fight's `ActionAnimation` with `shouldMove = true` (Root Motion enabled, allowing the caster to step forward naturally with the animation), `transitionTime = 0.14s`, and `2.0x` playback speed modifier (`PLAY_SPEED_MODIFIER = 2.0F`) matching Wukong exactly.
- Its server action executes Ding at `0.0s` (`ON_BEGIN_EVENTS`) and triggers the charge sound `xuli_ding_sou` at `0.0s`. The registration is fully parameterized to support alternate transition times, speeds, Root Motion toggles, and custom timestamp events if animations are swapped in the future.
- Target selection prioritizes:
  1. Epic Fight battle target (if locked on).
  2. Crosshair raycast target via `Utils.raycastForEntity(caster, 50.0, true, 0.6F)`.
  3. Closest living entity within 50 blocks as a fallback.
- **Multi-target AoE**: If `multi_target_radius` in config is set greater than 0, Ding Shen Fa targets all valid living entities within that radius around the aimed impact point (target entity position, looked-at block, or line of sight).
- **Dodge evasion**: Targets actively executing an Epic Fight `DodgeAnimation` evade the spell (matching Wukong's `BattleUnit.ding` evasion mechanic).

## Immobilize behaviour

- On the server, Ding saves the target movement-speed base value strictly in `entity.getPersistentData()` and sets that base value to `0.0`, adds the `ding` entity tag, and applies `bhspells:ding` plus `minecraft:glowing` and `minecraft:slowness` (amplifier 4 = Slowness V) for 192 ticks.
- Immediately stops any active item use (`target.stopUsingItem()`) and cancels any active Iron's Spells spell casting (`CancelCastPacket.cancelCast(serverPlayer, false)`).
- `TigershadeNetwork.spawnDingAfterImage` sends packets to `TRACKING_ENTITY_AND_SELF` to render the iconic after-image glyph over the target's head.
- Wukong applies NoAI to Monsters and Ender Dragons. This integration preserves each Mob's prior AI/aggression state, disables it during Ding, and restores that exact state at release.
- Complete immobilization matches Wukong's natural physics: entity horizontal drift is zeroed while natural vertical gravity is preserved, allowing targets in mid-air to land smoothly on the ground without rubberbanding or position snapping.
- **Knockback Immunity**: `DingEvents.onLivingKnockBack` cancels knockback so attacks do not push or jitter the target during Ding.
- **Movement & Jump Lockout**:
  - Client-side: `DingClientEvents.onMovementInput` (`MovementInputUpdateEvent`) zeroes out `forwardImpulse` and `leftImpulse`, and sets `up`, `down`, `left`, `right`, `jumping`, and `shiftKeyDown` to `false`.
  - Server-side: `DingEvents.onLivingJump` nullifies upward jump velocity (`Math.min(0.0D, motion.y)`).
  - Continuous enforcement: `DingEvents.onLivingTick` (`LivingTickEvent`) continuously zeroes horizontal velocity while preserving natural vertical gravity.
- **Attack & Action Lockout**:
  - Client-side: `DingClientEvents.onInteractionKeyMapping` cancels attack (`event.isAttack()`), use-item, and pick-block triggers and prevents hand swing.
  - Epic Fight: `DingLocalPlayerPatchMixin` prevents `LocalPlayerPatch.canPlayAttackAnimation()`, while `DingPlayerPatchMixin` blocks `PlayerPatch.attack(...)`.
  - Common events: `AttackEntityEvent`, `LeftClickBlock`, `LeftClickEmpty`, and `LivingAttackEvent` (damage dealt by immobilized entities) are canceled.
- **Right-Click, Item Use & Spell Casting Lockout**:
  - Common events: `RightClickItem`, `RightClickBlock`, `EntityInteract`, `EntityInteractSpecific`, `LivingEntityUseItemEvent.Start`, and `LivingEntityUseItemEvent.Tick` are canceled.
  - Spells: `SpellPreCastEvent` is canceled for any immobilized entity, and active casts are interrupted.
  - Epic Fight Skills: `DingSkillMixin` injects into `Skill.canExecute(...)`, disabling Dodge, Guard, Weapon Skills, and Innate Skills while Dinged.
- **Animation Freeze**:
  - `DingAnimationPlayerMixin` injects at `HEAD` of `AnimationPlayer.tick(...)`, locking `prevElapsedTime = elapsedTime` and cancelling tick advancement. This guarantees that frame interpolation (`lerp(prevElapsedTime, elapsedTime, partialTicks)`) renders a 100% frozen pose without oscillation or jitter.
  - `DingStaticAnimationMixin`, `DingMovementAnimationMixin`, `DingAttackAnimationMixin`, `DingLinkAnimationMixin`, and `DingDynamicAnimationMixin` force `getPlaySpeed(...)` to `0.0F`.
  - `DingServerAnimatorMixin` (server) and `DingClientAnimatorMixin` (client) block new animation requests (`playAnimation`, `playAnimationInstantly`, `reserveAnimation`, and client `playAnimationAt`) from replacing the frozen pose.
- **Camera, Character Body & Head Stillness (กล้องหันได้อิสระ แต่โมเดลตัวละครและหัวอยู่นิ่งสนิท ไม่หันตามกล้อง)**:
  - Free Camera Rotation: Player's camera and screen can rotate and look around freely, preserving full compatibility with Freecam, standard camera panning, and third-person view.
  - Character Body Stillness:
    - Client input: `DingClientEvents.onMovementInput` suppresses WASD movement and jumping, anchoring the character body to its position.
    - Epic Fight Model Lock: `DingPlayerPatchMixin` injects into `PlayerPatch.disableModelYRot` to prevent disabling model rotation lock while Dinged, and injects into `tick(...)` at both `HEAD` and `TAIL` to enforce `setModelYRot(getYRot(), false)`, locking the 3D model's body angle in place so the character does not rotate its torso/body when the player pans the camera.
    - Mob Rotation: Non-player entities (mobs) have their rotation angles saved in `DATA_ROTATION_Y/X/HEAD_Y/BODY_Y` in persistent data and enforced in `enforceFreeze`, keeping mob bodies and heads completely fixed.
  - Head Lock to Latest Direction (ล็อคหัวตามทิศทางล่าสุดที่หัน ไม่สะบัดกลับตรงและไม่หันตามกล้อง):
    - `DingShenFaService.FrozenHeadPose` captures and freezes the entity's exact head look angles (`yawOffset = modelY - headY`, `pitch = xRot`, `netHeadYaw = headY - bodyY`) at the instant of immobilization.
    - `DingAbstractClientPlayerPatchMixin` injects at `HEAD` of `AbstractClientPlayerPatch.poseTick(...)`. When immobilized, it retrieves `FrozenHeadPose` and applies the frozen yaw and pitch offsets to the Armature's `Head` joint via `OpenMatrix4f` rotation transforms, then cancels live camera tracking. The player's 3D head model remains locked in the exact direction the player was looking when Dinged, without snapping straight or tracking mouse look.
    - `DingLivingEntityPatchMixin` performs the same frozen orientation application on `LivingEntityPatch.poseTick(...)` for immobilized mobs and living entities.
    - `DingPatchedLivingEntityRendererMixin` redirects `model.setupAnim(...)` in `PatchedLivingEntityRenderer.prepareVanillaModel(...)` to pass `frozen.netHeadYaw` and `frozen.pitch`, keeping vanilla layers (helmets, curios, custom heads) aligned to the latest frozen look direction.
    - `DingLivingEntityRendererMixin` redirects `model.setupAnim(...)` in vanilla `LivingEntityRenderer.render(...)` to pass `frozen.netHeadYaw` and `frozen.pitch`, keeping vanilla entity models fixed at the latest look direction.
    - On release or expiry, `DingShenFaService.clearFrozenHeadPose(entity)` clears the snapshot, smoothly restoring normal look tracking.

- **Unbreakable by damage**: Damage does not shatter or end Ding; the freeze persists for its full duration unless expired or explicitly cancelled.
- **Shift + Cast Cancellation (Lift Ding)**: Sneak-casting (Shift + Cast) releases and lifts the immobilize on all targets previously bound by the caster:
  - Consumes no mana.
  - Does not trigger the full 50-second cooldown (applies a brief 1.5-second / 30-tick safety cooldown to prevent spamming).
  - Emits the two 12-particle `minecraft:glow` and `minecraft:wax_off` end bursts and displays release feedback.

## Particles

- Only `bhspells:ding` is registered, using `SimpleParticleType(true)`.
- The spell emits exactly one `ding` glyph above the target head (`Y + bounding-box height + 1`). Its entity id is carried through the x-speed field, so it removes itself when the entity is unavailable or dead.
- `ding` uses Wukong's 100-tick entity-after-image behaviour: `2.5 x 2.5`, quad size multiplied by `2.85`, full-bright (`15728880`), non-culling, no gravity/physics, and the lit particle sheet.
- While Ding is active, the effect emits 3 `minecraft:wax_off` particles every fifth tick at half target height with Wukong's `0.15 / 0.25 / 0.15` spread and `0.02` speed.
- On expiry or release, end bursts of 12 `minecraft:glow` and 12 `minecraft:wax_off` particles emit at `Y + 1.0`.

## Configuration (`SpellConfig.DingShenFa`)

- `base_mana`: Base mana cost (default: 50).
- `mana_per_level`: Mana increase per level (default: 0).
- `cooldown_seconds`: Cooldown duration in seconds (default: 50.0).
- `multi_target_radius`: Multi-target AoE radius around aimed point (default: 0.0 = single-target mode, >0.0 = AoE freeze).

## Runtime ownership

- Common gameplay, targeting, evasion, tracking, and cancellation: `service/DingShenFaService.java`.
- Common gameplay event listeners: `event/DingEvents.java`.
- Client input and interaction cancellation: `client/event/DingClientEvents.java`.
- Custom root motion action animation: `compat/epicfight/skills/dingshenfa/SpecialActionAnimation.java`.
- Epic Fight animation registration & event binding: `compat/epicfight/skills/dingshenfa/DingShenFaAnimations.java`.
- Epic Fight companion skill: `compat/epicfight/skills/dingshenfa/DingShenFaSkill.java`.
- Epic Fight evasion check bridge: `compat/epicfight/EpicFightCompat.java` & `EpicFightLoadedBridge.java`.
- Epic Fight Ding pose, model & head freeze:
  - `mixin/epicfight/DingAnimationPlayerMixin.java`
  - `mixin/epicfight/DingServerAnimatorMixin.java`
  - `mixin/epicfight/DingClientAnimatorMixin.java`
  - `mixin/epicfight/DingAttackAnimationMixin.java`
  - `mixin/epicfight/DingDynamicAnimationMixin.java`
  - `mixin/epicfight/DingLinkAnimationMixin.java`
  - `mixin/epicfight/DingLivingEntityPatchMixin.java`
  - `mixin/epicfight/DingLocalPlayerPatchMixin.java`
  - `mixin/epicfight/DingMovementAnimationMixin.java`
  - `mixin/epicfight/DingPlayerPatchMixin.java`
  - `mixin/epicfight/DingSkillMixin.java`
  - `mixin/epicfight/DingStaticAnimationMixin.java`
  - `mixin/epicfight/DingAbstractClientPlayerPatchMixin.java`
  - `mixin/epicfight/DingPatchedLivingEntityRendererMixin.java`
  - `mixin/DingLivingEntityRendererMixin.java`
- Client particle and packet: `client/particle/DingEntityAfterImageParticle.java` and `network/DingAfterImageParticlePacket.java`.
- Particle registrations: `registry/ParticleRegistry.java`.

