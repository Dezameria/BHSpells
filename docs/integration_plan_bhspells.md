# แผนการ Integrate ระบบจาก ironspell_more เข้าสู่ BHSpells (Master Integration Plan)

> **สถานะการวางแผน**: วิเคราะห์สถาปัตยกรรมและตรวจสอบความเข้ากันได้เชิงลึก (Codex Discovery & Antigravity Architectural Plan)  
> **Repository หลัก (Target Base)**: `D:\Minecraft\Dev\ironspell_more\BHSpells` (Branch: `feature/integrate-deizy`)  
> **Repository ต้นทาง (Source Features)**: `D:\Minecraft\Dev\ironspell_more\ironspell_more` (Branch: `ironspell_more-2.0`)  
> **Namespace เป้าหมาย**: `net.offkung.bhspells` | **Mod ID**: `bhspells`

---

## 1. วัตถุประสงค์และข้อกำหนดหลัก (Core Invariants & Constraints)

1. **BHSpells เป็นแกนหลัก 100% (Base Integrity)**:
   - ห้ามแก้ไขหรือเปลี่ยนโครงสร้างเดิมของโปรเจกต์ `BHSpells` (โครงสร้าง package, ระบบ registry, mixin config, build script)
   - สไตล์การเขียนโค้ด การตั้งชื่อ และลำดับของ class members ต้องเป็นไปตามมาตรฐานเดิมของ `BHSpells` และกฎ `AGENTS.md`
2. **เงื่อนไข Duplicate Handling**:
   - Spell, Entity, Effect, Event, Utility, Particle หรือ Asset ใดที่มีอยู่ใน `BHSpells` อยู่แล้ว **ไม่ต้องแตะต้องและไม่ต้องเขียนทับ** ให้คงของ `BHSpells` ไว้ทั้งหมด
3. **การคัดเลือกเฉพาะส่วนใหม่ (Selective Addition Only)**:
   - นำเข้าเฉพาะฟีเจอร์, Spell, Subsystem, Compat และ Asset จาก `ironspell_more` ที่ `BHSpells` ยังไม่มี
4. **มาตรฐานการประกาศสมาชิก Spell (Class Member Ordering Rule ตาม `AGENTS.md`)**:
   `spell identity -> constants/static fields -> optional unit info -> default config field -> constructor -> standard getters/overrides -> spell logic/helpers`
5. **การจัดทำเอกสาร Spell (Documentation Rule ตาม `AGENTS.md`)**:
   - ทุก Spell ที่เพิ่มเข้าไปต้องมีเอกสาร markdown ประจำตัวภายใต้ `docs/spells/<school>/<spell_id>.md`

---

## 2. ผลการตรวจสอบและค้นพบข้อเท็จจริง (Critical Discovery Findings)

จากการตรวจสอบ Source Code ทั้ง 2 โครงการ พบข้อเท็จจริงสำคัญทางเทคนิคที่ต้องกำหนดเป็น Gate ในการ Integrate ดังนี้:

### 2.1 รายการ Spell ที่พร้อม Integrate vs Spell ที่ยังไม่สมบูรณ์
| Spell | สายเวท (School) | สถานะใน ironspell_more | แผนการ Integrate เข้า BHSpells |
| :--- | :--- | :--- | :--- |
| **DingShenFaSpell** | Evocation | สมบูรณ์ (5.6 KB) + Service + Mixins | นำเข้าสู่ `net.offkung.bhspells.spells.evocation` |
| **EarthRoarSpell** | Ground (`BHSchoolRegistry.GROUND`) | สมบูรณ์ (6.9 KB) + Service + Effect | นำเข้าสู่ `net.offkung.bhspells.spells.ground` |
| **SavageBiteSpell** | Gold (`BHSchoolRegistry.GOLD`) | สมบูรณ์ (7.4 KB) + Manager + Network | นำเข้าสู่ `net.offkung.bhspells.spells.gold` |
| **ShockingSpell** | Ground (`BHSchoolRegistry.GROUND`) | สมบูรณ์ (12.3 KB) + Client Beam VFX | นำเข้าสู่ `net.offkung.bhspells.spells.ground` |
| **SpiritualPressureSpell** | Evocation | สมบูรณ์ (6.7 KB) + Pressure Subsystem | นำเข้าสู่ `net.offkung.bhspells.spells.evocation` |
| **VengefulPressureSpell** | Evocation | สมบูรณ์ (6.8 KB) + Pressure Subsystem | นำเข้าสู่ `net.offkung.bhspells.spells.evocation` |
| **TempestReiatsuSpell** | Lightning (`SchoolRegistry.LIGHTNING`) | สมบูรณ์ (8.3 KB) + Lightning Strike | นำเข้าสู่ `net.offkung.bhspells.spells.lightning` |
| **PhantomDodgeSpell** | Evocation | ⚠️ **ไฟล์ว่าง 0 ไบต์ (Unimplemented)** | **ข้ามการนำเข้าโค้ดเวทนี้** จนกว่าจะมี Logic สมบูรณ์ (นำเข้าเฉพาะ Animation asset ไว้ล่วงหน้าได้) |

> ⚠️ **หมายเหตุสำคัญ**: พบว่า `PhantomDodgeSpell.java`, `PhantomDodgeEvents.java`, `PhantomDodgeEffect.java` และเอกสาร `phantom_dodge.md` ใน `ironspell_more` เป็นไฟล์ว่างเปล่า (0 Bytes) จึงต้องยกเว้นเวทนี้จากการลงทะเบียนใน `BHSpells` เพื่อป้องกันปัญหา Compile Error

### 2.2 การจัดการระบบ ScreenShake (Dependency Boundary Fix)
- ใน `ironspell_more` ตัวคลาส `PressureScreenShakeHelper` มีการ import คลาส `TOFollowingScreenShakeEntity` จาก mod ภายนอก (`traveloptics`)
- ในขณะที่ `BHSpells` **มีระบบ ScreenShake ภายในตัวเองอยู่แล้วอย่างสมบูรณ์** (`ScreenShakePacket`, `ScreenShakeEffect`, `ClientScreenShakeEvent`)
- **การปรับปรุงสถาปัตยกรรม (Architecture Decision)**: ปรับแก้ `PressureScreenShakeHelper` ให้เรียกใช้ `ScreenShakePacket` ของ `BHSpells` โดยตรง ไม่ต้องพึ่งพา dependency ภายนอกที่ไม่จำเป็น

### 2.3 การจัดการ Duplicate Effects & Sounds
- `effect/CooldownEffect.java` ใน `ironspell_more`: พบว่าใน `BHSpells` มี `MobEffectsRegistry.COOLDOWN` อยู่แล้ว **ไม่ต้องสร้างไฟล์เพิ่ม**
- `BHSoundRegistry`: เพิ่มเฉพาะเสียงของ Ding Shen Fa (`xuli_ding_sou`, `fashu_ding1`, `fashu_ding2`) โดยเสียง `venomous_blossomfall_charge` และ `hymnofpurification` มีอยู่ใน `BHSpells` แล้ว

---

## 3. ผังการแปลง Package และโครงสร้างไฟล์ (Mapping Matrix)

```
[ironspell_more]                                     [BHSpells]
io.redspace.ironspell_more.spells.evocation.*   --> net.offkung.bhspells.spells.evocation.* (สร้าง package ใหม่)
io.redspace.ironspell_more.spells.gold.*        --> net.offkung.bhspells.spells.gold.*
io.redspace.ironspell_more.spells.ground.*      --> net.offkung.bhspells.spells.ground.*
io.redspace.ironspell_more.spells.lightning.*   --> net.offkung.bhspells.spells.lightning.*
io.redspace.ironspell_more.pressure.*           --> net.offkung.bhspells.pressure.*
io.redspace.ironspell_more.service.*            --> net.offkung.bhspells.service.*
io.redspace.ironspell_more.effect.*             --> net.offkung.bhspells.effect.*
io.redspace.ironspell_more.client.particle.*    --> net.offkung.bhspells.client.particle.*
io.redspace.ironspell_more.compat.epicfight.*   --> net.offkung.bhspells.compat.epicfight.*
io.redspace.ironspell_more.mixin.*              --> net.offkung.bhspells.mixin.*
```

---

## 4. แผนงานรายภารกิจอย่างละเอียด (Task Decomposition)

### Phase 1: Core Effects & Registries (รากฐานสถานะและรีจิสทรี)
- **Task 1.1: เพิ่ม Mob Effects ใหม่ลงใน `MobEffectsRegistry.java`**
  - นำเข้าไฟล์คลาส:
    - `DingEffect.java`
    - `EarthRoarEmpowermentEffect.java`
    - `EarthRoarStunEffect.java`
    - `ShockingEffect.java`
    - `SpiritualPressureEffect.java`
  - ลงทะเบียนใน `net.offkung.bhspells.registry.MobEffectsRegistry`:
    - `DING`
    - `EARTH_ROAR_EMPOWERMENT`
    - `EARTH_ROAR_STUN`
    - `SHOCKING`
    - `SPIRITUAL_PRESSURE`
  - ย้าย texture ไอคอนเอฟเฟกต์ไปยัง `assets/bhspells/textures/mob_effect/`

- **Task 1.2: เพิ่ม Sounds ใน `BHSoundRegistry.java` และ `sounds.json`**
  - เพิ่มเสียง: `xuli_ding_sou`, `fashu_ding1`, `fashu_ding2`
  - นำเข้าไฟล์เสียง `.ogg` ไปยัง `assets/bhspells/sounds/`
  - อัปเดต `assets/bhspells/sounds.json`

- **Task 1.3: เพิ่ม Particle Types ใน `ParticleRegistry.java`**
  - นำเข้า particle คลาส:
    - `DingEntityAfterImageParticle.java`
    - `ShockingBeamParticle.java`
    - `ShockingBeamParticleOption.java`
    - `ShockingLightningGeometry.java`
  - ลงทะเบียนใน `net.offkung.bhspells.registry.ParticleRegistry`:
    - `DING`
    - `SHOCKING_BEAM`
  - นำเข้าไฟล์นิยาม JSON ไปยัง `assets/bhspells/particles/`

---

### Phase 2: Subsystems, Managers & Network (ระบบสนับสนุนและเครือข่าย)
- **Task 2.1: ย้ายและปรับแต่ง Pressure Subsystem**
  - คัดลอกโฟลเดอร์ `pressure/` ทั้งหมดไปยัง `net.offkung.bhspells.pressure`:
    - Model & Data: `PressureAnchor`, `PressureFieldData`, `PressureReaction`, `PressureToggleHelper`, `PressureVisualProfile`
    - Server: `ServerPressureField`, `ServerPressureManager`, `PressureRestrictionEvents`
    - Refactor `PressureScreenShakeHelper` ให้ส่ง `ScreenShakePacket` ของ `BHSpells`
    - Client: `ClientPressureField`, `ClientPressureManager`, `PressureClientEvents`, `PressureFieldRenderer`, `PressureStreak`, `ScreenPressureAggregator`, `ScreenPressurePostProcessor`, `ScreenPressureState`
    - Network: `PressureNetwork` (หรือรวม Packet เข้า `PacketHandler`)

- **Task 2.2: ย้ายระบบ Service & Event ของแต่ละเวท**
  - **Ding Shen Fa**: `DingShenFaService.java`, `DingEvents.java`, `DingClientEvents.java`, `DingAfterImageParticlePacket.java`
  - **Earth Roar**: `EarthRoarDashManager.java`, `EarthRoarStunService.java`, `EarthRoarLifecycleEvents.java`
  - **Savage Bite**: `SavageBiteManager.java`, `SavageBiteLifecycleEvents.java`, `SavageBiteClientEvents.java`, `SavageBiteInputPacket.java`, `SavageBiteStatePacket.java`
  - **Shocking**: `ShockingEffectClientEvents.java`

- **Task 2.3: ปรับแต่ง Network Packets และ Event Registration**
  - ลงทะเบียน Packet ที่เกี่ยวข้องใน `net.offkung.bhspells.network.PacketHandler`
  - ตรวจสอบให้ EventBusSubscriber ทั้งหมดใช้ `bus = Mod.EventBusSubscriber.Bus.FORGE` หรือ `MOD` ตามมาตรฐาน `BHSpells`

---

### Phase 3: Epic Fight Integration & Mixins (ระบบต่อสู้และอนิเมชัน)
- **Task 3.1: Epic Fight Skills & Animation Handlers**
  - นำเข้าโฟลเดอร์:
    - `compat/epicfight/skills/dingshenfa/*` (`DingShenFaAnimations`, `DingShenFaSkill`, `DingShenFaSkills`, `SpecialActionAnimation`)
    - `compat/epicfight/skills/earth_roar/*` (`EarthRoarAnimations`)
    - `compat/epicfight/pressure/*` (`PressureAnimationController`, `PressureEpicFightCompat`)
  - คัดลอกอนิเมชัน JSON ไปยัง `assets/bhspells/animmodels/animations/biped/spells/`
  - ผูกการลงทะเบียนเข้ากับ `CompatBootstrap.java` และ `EpicFightCompat.java` ใน `BHSpells`

- **Task 3.2: นำเข้า Mixins และแก้ไข `bhspells.mixins.json`**
  - นำเข้าคลาส Mixin:
    - `DingLivingEntityRendererMixin`
    - `epicfight.DingAbstractClientPlayerPatchMixin`
    - `epicfight.DingAnimationPlayerMixin`
    - `epicfight.DingServerAnimatorMixin`
    - `epicfight.DingClientAnimatorMixin`
    - `epicfight.DingAttackAnimationMixin`
    - `epicfight.DingDynamicAnimationMixin`
    - `epicfight.DingLinkAnimationMixin`
    - `epicfight.DingLivingEntityPatchMixin`
    - `epicfight.DingLocalPlayerPatchMixin`
    - `epicfight.DingMovementAnimationMixin`
    - `epicfight.DingPatchedLivingEntityRendererMixin`
    - `epicfight.DingPlayerPatchMixin`
    - `epicfight.DingSkillMixin`
    - `epicfight.DingStaticAnimationMixin`
  - เพิ่มรายชื่อ Mixin ลงใน `bhspells.mixins.json` (แยกหมวดหมู่ `mixins` และ `client`)

---

### Phase 4: Spell Implementations & Registration (การนำเข้าและลงทะเบียนเวทมนตร์)
- **Task 4.1: ปรับปรุงโค้ด 7 Spell Classes ให้ตรงตามข้อกำหนด `AGENTS.md`**
  1. `DingShenFaSpell.java` -> Package `net.offkung.bhspells.spells.evocation`
  2. `EarthRoarSpell.java` -> Package `net.offkung.bhspells.spells.ground` (ผูกกับ `BHSchoolRegistry.GROUND_RESOURCE`)
  3. `SavageBiteSpell.java` -> Package `net.offkung.bhspells.spells.gold` (ผูกกับ `BHSchoolRegistry.GOLD_RESOURCE`)
  4. `ShockingSpell.java` -> Package `net.offkung.bhspells.spells.ground` (ผูกกับ `BHSchoolRegistry.GROUND_RESOURCE`)
  5. `SpiritualPressureSpell.java` -> Package `net.offkung.bhspells.spells.evocation`
  6. `VengefulPressureSpell.java` -> Package `net.offkung.bhspells.spells.evocation`
  7. `TempestReiatsuSpell.java` -> Package `net.offkung.bhspells.spells.lightning`
  - ตรวจสอบลำดับสมาชิกภายในคลาส: `spell identity -> constants/static fields -> optional unit info -> default config field -> constructor -> standard getters/overrides -> spell logic/helpers`
  - เปลี่ยน ResourceLocation ทั้งหมดให้เป็น `bhspells:<spell_name>`

- **Task 4.2: ลงทะเบียนใน `BHSpellRegistry.java` และ `SpellConfig.java`**
  - นำเข้าเวทมนตร์ทั้ง 7 ตัวลงในหมวดหมู่ที่ถูกต้องใน `BHSpellRegistry`:
    - EVOCATION: `DING_SHEN_FA`, `SPIRITUAL_PRESSURE`, `VENGEFUL_PRESSURE`
    - GROUND: `EARTH_ROAR`, `SHOCKING`
    - GOLD: `SAVAGE_BITE`
    - LIGHTNING: `TEMPEST_REIATSU`
  - เพิ่มค่า Config เริ่มต้นลงใน `SpellConfig.java` สำหรับเวทใหม่

---

### Phase 5: Assets, Data & Documentation (ทรัพยากรและเอกสาร)
- **Task 5.1: นำเข้า Assets & Data Tags**
  - Textures: ไอคอนเวทมนตร์ 7 รูปใน `assets/bhspells/textures/gui/spell_icons/`
  - Tags: `data/bhspells/tags/entity_types/savage_bite_immune.json`
  - Lang: รวมข้อความ Localization ของเวท/เอฟเฟกต์ใหม่เข้ากับ `assets/bhspells/lang/en_us.json`

- **Task 5.2: สร้างเอกสารสำหรับแต่ละ Spell ใน `docs/spells/` ตามกฎ `AGENTS.md`**
  - `docs/spells/evocation/ding_shen_fa.md`
  - `docs/spells/evocation/spiritual_pressure.md`
  - `docs/spells/evocation/vengeful_pressure.md`
  - `docs/spells/gold/savage_bite.md`
  - `docs/spells/ground/earth_roar.md`
  - `docs/spells/ground/shocking.md`
  - `docs/spells/lightning/tempest_reiatsu.md`
  - `docs/spiritual_pressure_architecture_and_roadmap.md`

---

## 5. การประเมินความเสี่ยงและมาตรการป้องกัน (Risks & Mitigation)

1. **ความเสี่ยง Mixin ขัดแย้งกับ Epic Fight**:
   - *คำอธิบาย*: `Ding*` mixins ดักจับการเล่นอนิเมชันของ Epic Fight เพื่อสั่ง Freeze
   - *มาตรการ*: ตั้งค่า `required: false` ใน `bhspells.mixins.json` เพื่อไม่ให้ตัวเกม Crash หากมี Mod อนิเมชันอื่นหรือเมื่อรันในสภาพแวดล้อมที่ไม่มี Epic Fight
2. **ความเสี่ยงเรื่อง Class Loading เมื่อไม่มี Mod เสริม**:
   - *คำอธิบาย*: โค้ดเรียกใช้ Epic Fight API โดยตรงอาจทำให้ Server เกิด `NoClassDefFoundError` หากรันแบบ Standalone
   - *มาตรการ*: ครอบ Logic ด้วย `CompatMods.isEpicFightLoaded()` และแยก Bridge ให้ปลอดภัยเช่นเดียวกับที่ `BHSpells` ทำใน `EpicFightLoadedBridge.java`
3. **ความเสี่ยงเรื่อง Packet ID ซ้ำซ้อน**:
   - *คำอธิบาย*: การเพิ่ม packet ลงใน `PacketHandler.java` อาจทำให้ลำดับ index ไม่ตรงกัน
   - *มาตรการ*: ใช้ `index++` ต่อท้ายจาก packet ล่าสุดของ `BHSpells` หรือคงแยก Channel เฉพาะของ Pressure Subsystem ไว้

---

## 6. แผนการตรวจสอบและทดสอบ (Verification & Build Steps)

1. **Gradle Compilation Verification**:
   ```powershell
   ./gradlew compileJava --warning-mode all
   ```
   *เกณฑ์ยอมรับ*: โค้ดคอมไพล์ผ่าน 100% ไม่มี error เรื่อง missing classes, unresolved symbols หรือ annotation processor errors
2. **Process Resources & Data Gen Verification**:
   ```powershell
   ./gradlew processResources
   ```
   *เกณฑ์ยอมรับ*: JSON assets, lang, textures, tags ถูก expand และ pack ถูกต้องตาม namespace `bhspells`
3. **Runtime & In-Game Verification**:
   - รัน Client ทดสอบการร่ายเวททั้ง 7 ตัวในเกม
   - ตรวจสอบการ Freeze ของ `Ding Shen Fa` ทั้งบน vanilla mob และ epicfight player
   - ตรวจสอบ Screen Shake และ Pressure Field visual effects
   - ตรวจสอบเสียงและ Particle ของ Shocking beam
