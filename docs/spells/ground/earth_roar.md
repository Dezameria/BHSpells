# Earth Roar (ปฐพีคำราม)

## ข้อมูลหลัก

| รายการ | ค่า |
| --- | --- |
| ตัวละคร | เล่อเปิน (Le Ben) |
| Registry ID | `bhspells:earth_roar` |
| Class | `spells/ground/EarthRoarSpell.java` |
| School | `BHSchoolRegistry.GROUND_RESOURCE` (`bhspells:ground`); fallback เป็น Evocation เมื่อไม่มี BHSpells |
| Rarity | Epic |
| Max level | 1 |
| Cast type / time | Instant / 0 ticks |
| Mana cost | 80 |
| Cooldown | 2,400 ticks (120.0 วินาที) เริ่มนับทันทีเมื่อกดใช้ ครอบคลุมรอบ Buff + Debuff ครบลูป |
| Dash distance | 40 บล็อก (แนวนอนขนานพื้น พร้อม Auto Step-up 1.25 บล็อก) |
| Damage | 35.0 (สเกลตามตัวคูณ Spell Power) |
| Knockback | ผลักศัตรูกระเด็นไถลออกด้านข้างขนานพื้นอย่างรุนแรง (Lateral Outward Ground Slide 1.4–2.6) หากเฉียดโดนขอบเส้นทางพุ่ง พร้อมแรงส่งไปข้างหน้า 0.85 (แนบพื้น ไม่ยกลอยหรือลอยเคว้ง) |
| Stun | 100 ticks (5.0 วินาที) กับทุกสิ่งมีชีวิตรวมถึงบอส ผ่าน Universal Stun Service |
| Shield | ทำลายการป้องกันโล่ (Shield Break / Disable) ก่อนสร้างความเสียหาย |
| Buff | Resistance II (amp 1), Haste I (amp 0), Strength II (amp 1) นาน 1,200 ticks (60 วินาที) |
| Debuff | Weakness II (amp 1), Slowness II (amp 1), Nausea I (amp 0), Mining Fatigue I (amp 0) นาน 1,200 ticks (60 วินาที) เฉพาะเมื่อ Buff หมดอายุตามธรรมชาติ |

---

## ข้อกำหนดที่ยืนยันแล้ว

1. **Free-aim Piercing Dash**: ผู้เล่นหันหน้าไปทิศทางใด จะพุ่งตรงไปตามแนวนอนของสายตา ทะลวงผ่านศัตรูทุกตัวในระยะ 40 บล็อก (Piercing line dash) ไม่หยุดที่เป้าหมายแรก
2. **Anti-Multi-Hit Guarantee**: ศัตรูแต่ละตัวถูกบันทึกด้วย UUID และจะถูกโจมตีเพียงครั้งเดียวต่อการพุ่งหนึ่งครั้ง
3. **Auto Step-up**: รองรับการก้าวข้ามสิ่งกีดขวางหรือพื้นต่างระดับความสูงไม่เกิน 1.25 บล็อกได้อย่างราบรื่น หากชนกำแพงสูงทึบจะหยุดพุ่งทันที
4. **Universal 100-Tick Stun & Anti-Float**: ปลดล็อคข้อจำกัดของระบบ Potion ปกติ เพื่อให้บอสทุกตัวและสิ่งมีชีวิตทุกชนิดหยุดการเคลื่อนไหวและ AI อย่างสมบูรณ์ 5 วินาที โดยไม่ทำให้ศัตรูลอยเคว้งในอากาศ และกดให้ติดพื้นเสมอ
5. **Cleanse / Expiry Contract**:
   - หากผู้เล่นได้รับผล Buff ครบ 60 วินาทีตามธรรมชาติ จะได้รับ Debuff ทันที 60 วินาที
   - หากดื่มนม, เสียชีวิต, หรือถูกล้างด้วย Anti-Magic ระหว่างช่วง 60 วินาทีแรก ผล Buff จะหายไปโดย**ไม่ได้รับ Debuff** และคูลดาวน์ 120 วินาทียังคงนับต่อไปตามปกติ
6. **Epic Fight Integration & Fallback**:
   - แบ่งเป็น 3 ลำดับแอนิเมชัน:
     - **ตอนชาร์จ (Charge)**: `epicfight:biped/living/kneel` ผ่าน `AnimationCue.EARTH_ROAR_CHARGE` (หรือ `EARTH_ROAR`)
     - **ตอนพุ่งแรก (Initial Dash)**: `epicfight:biped/living/sneak` ผ่าน `AnimationCue.EARTH_ROAR_DASH`
     - **ท่าเตะ (Kick Strike)**: `wom:biped/skill/kick_3` ผ่าน `AnimationCue.EARTH_ROAR_KICK` (Fallback เป็น `Animations.BIPED_STEP_FORWARD` เมื่อไม่มี WOM)
   - หากไม่มี Epic Fight ตัวเกมฝั่ง Server ยังคงทำงานครบถ้วน 100% (State Machine, Dash, Swept Hit, Damage, Stun, Buff/Debuff)
7. **Ground Fractures & Lateral Knockback**:
   - เกิดรอยแตกพื้นดิน 3D (Epic Fight Ground Fracture & Particle Shockwave) ใน 4 จังหวะ: จุดเริ่มชาร์จ, ตลอดเส้นทางพุ่งทุก 4 บล็อก, จุดเปิดท่าเตะ, และจุดกระทบเป้าหมาย
   - เป้าหมายที่โดนเฉียดด้านข้างเส้นทางพุ่งจะถูกแรงผลักไถลกระเด็นออกด้านข้างอย่างรุนแรงในแนวราบ (Outward Lateral Ground Slide) ไม่ทำให้ลอยตัว

---

## ลำดับการร่าย (36-Tick Timeline)

| ช่วงเวลา / Ticks | เฟส | รายละเอียดการทำงานฝั่ง Server | การแสดงผล Client (VFX / Animation / Sound) |
| --- | --- | --- | --- |
| **0–11 ticks** (0.0s–0.6s) | Phase 1: Kneel Charge (ตอนชาร์จ) | หน่วงความเร็วผู้ร่ายให้อยู่กับที่ชั่วคราว, สลายแรงเฉื่อย | เล่นท่าคุกเข่า `epicfight:biped/living/kneel`, เกิดคลื่นรอยแตกพื้นดิน 3D รัศมี 3.2 บล็อก (`EpicFightCompat.spawnFracture`), ฝุ่นดินพุ่งกระจาย, ละอองพลังงานรอบขา และเสียงกระแทกพื้น |
| **12–19 ticks** (0.6s–1.0s) | Phase 2: Initial Dash (ตอนพุ่งแรก) | พุ่งไปข้างหน้า 2.5 บล็อก/tick (รวม 20 บล็อก), ซิงค์ตำแหน่งผ่าน Teleport Packet และ Forward DeltaMovement, ตรวจจับการชน, สร้างรอยแตกพื้นดินทุก 4 บล็อก | เล่นท่าย่อตัวพุ่ง `epicfight:biped/living/sneak`, รอยแตกดินต่อเนื่อง (รัศมี 2.0 บล็อก), ละอองลมพายุ Sweeping Wind และฝุ่นดินตามเส้นทาง |
| **20–27 ticks** (1.0s–1.4s) | Phase 3: Kick Strike (ท่าเตะ) | พุ่งเตะต่ออีก 2.5 บล็อก/tick (รวมครบ 40 บล็อก), ตรวจจับ Swept AABB, สลายโล่, ดาเมจ 35, สตั้น 100 ticks, ผลักไถลออกข้างแนวราบ, รอยแตกพื้นดินใต้เป้าหมาย | เล่นท่ากระโดดเตะ `wom:biped/skill/kick_3`, ระเบิดรอยแตกพื้นขนาดใหญ่ รัศมี 3.5 บล็อก ณ จุดเริ่มเตะ และรัศมี 2.2 บล็อก ณ จุดกระทบเป้าหมาย, เสียงกระแทก Sonic Boom/Crit, ประกายระเบิด |
| **28–35 ticks** (1.4s–1.8s) | Phase 4: Recovery | ชะลอความเร็วผู้ร่ายสู่ระดับปกติ, สิ้นสุด State Machine | ท่าลงสู่พื้นและเตรียมพร้อมรบ |
| **1,200 ticks** (60.0s) | Phase 5: Expiry Transition | ตรวจสอบ Natural Expiration ของ `EarthRoarEmpowermentEffect` เพื่อแจก 4 Debuffs นาน 60s | เสียง Elder Guardian Curse และข้อความแจ้งเตือนความเหนื่อยล้า |

---

## การเคลื่อนที่และการตรวจชน (Movement & Collision)

- **Authoritative Movement & Client Sync**: Server ควบคุมทิศทางและตำแหน่งผู้เล่นด้วยการย่อยก้าวเดิน (5 Sub-steps ต่อ tick รวมเป็นก้าวละ 0.5 บล็อก) และซิงค์ตำแหน่งไปยัง Client ผ่าน `player.connection.teleport(...)` ควบคู่กับ `setDeltaMovement(...)` และ `hurtMarked = true` เพื่อให้ตัวละครพุ่งไปข้างหน้าได้อย่างต่อเนื่อง ไม่ค้างอยู่ที่เดิม
- **Step-up Mechanism**:
  - เมื่อพบการชนข้างหน้า จะตรวจสอบความสูงตั้งแต่ 0.25 ถึง 1.25 บล็อก
  - หากมีที่ว่างให้ศีรษะและลำตัว จะยกตำแหน่งขึ้นบันไดหรือบล็อกชัน
  - หากเป็นกำแพงสูงทึบเกิน 1.25 บล็อก จะหยุดการพุ่งทันทีพร้อมเสียงกระแทก
- **Slope-down Handling**: เมื่อวิ่งบนพื้นราบและเจอบล็อกลาดลง ระบบจะตรวจสอบพื้นด้านล่างเพื่อรักษาสถานะติดพื้นอย่างต่อเนื่อง
- **Swept Box**: คำนวณ AABB ครอบคลุมระหว่างตำแหน่งเดิมและตำแหน่งใหม่ ขยายด้านข้างกว้าง 2.2 บล็อก เพื่อครอบคลุมทั้งเป้าหมายที่อยู่ตรงหน้าและเป้าหมายที่อยู่เฉียดด้านข้าง (Grazing Side Targets)

---

## ดาเมจ, การสลายโล่, การสตั้น และแรงผลักกระเด็นออกข้าง

- **Shield Destruction**: หากเป้าหมายกำลังยกโล่ป้องกัน ระบบจะบังคับให้หยุดใช้โล่, ติดคูลดาวน์โล่ 100 ticks (สำหรับผู้เล่น) และสร้างความเสียหายต่อความทนทานของโล่โดยตรง
- **Damage**: `35.0 × Spell Power Multiplier` ด้วย DamageSource ประเภท Magic/Physical ของ Iron's Spells 'n Spellbooks
- **Universal Stun (5 วินาที) & Anti-Float System**:
  - ควบคุมผ่าน `EarthRoarStunService` และบันทึกลงใน Map ฝั่ง Server
  - **ระบบป้องกันการลอย (Anti-Float Guarantee)**:
    - ลบผล Levitation ของเป้าหมายทันที
    - เวกเตอร์ Knockback เป็นแนวราบขนานพื้น 100% ไม่มีแรงยกในแกน Y (`kb.y = 0`)
    - หากม็อบอยู่ในอากาศ (เช่น โดนเตะขณะกระโดดหรือตกขอบบล็อก) ระบบจะดึงดูดลงสู่พื้นด้วยแรงโน้มถ่วงทันที โดยไม่เปิด NoAI ค้างไว้กลางอากาศ
  - **Ground Slide Window (10 ticks แรก)**: เปิดให้ศัตรูไถลกระเด็นไปตามแรงผลักแนวราบขนานพื้นได้อย่างเป็นธรรมชาติ พร้อมส่ง `ClientboundSetEntityMotionPacket` ไปยัง Client ผู้เล่น
  - **Grounded Lock**: เมื่อศัตรูอยู่บนพื้น ระบบจะล็อคตำแหน่ง `setDeltaMovement(0, 0, 0)` และปิด AI ม็อบ (`mob.setNoAi(true)`) สมบูรณ์ตลอดระยะเวลาที่เหลือจนครบ 100 ticks (5 วินาที)
  - **No Effect Particles**: ปิดการแสดงผลละออง Potion Swirl และ Crit Particles ของสถานะ Stun โดยสิ้นเชิง (`visible: false, showIcon: true`) แสดงเฉพาะไอคอนบน HUD/UI เพื่อความสะอาดของหน้าจอ
  - มีผลกับบอสทุกตัวอย่างแน่นอน แม้บอสจะมีภูมิคุ้มกันต่อ Potion Effect ปกติ
- **Effect Particle Suppression (Buff & Debuff)**:
  - บัฟทั้ง 4 ตัว (Resistance, Haste, Strength, Empowerment) และดีบัฟ 4 ตัว (Weakness, Slowness, Nausea, Mining Fatigue) ถูกปิดอนุภาคละอองยา Potion Swirl ทั้งหมด (`visible: false, showIcon: true`) เพื่อไม่ให้บดบังการมองเห็นและทัศนวิสัยของผู้เล่น
- **Lateral Outward Ground Slide (แรงผลักไถลออกด้านข้างขนานพื้น)**:
  - คำนวณเวกเตอร์ตั้งฉากกับการพุ่ง (Left Normal Vector: `(-dir.z, 0, dir.x)`) และหาค่าเยื้องศูนย์ (Lateral Offset)
  - ศัตรูที่อยู่ฝั่งซ้ายจะถูกผลักไถลไปทางซ้าย ศัตรูที่อยู่ฝั่งขวาจะถูกผลักไถลไปทางขวา
  - แรงผลักด้านข้างจะสเกลตามระยะห่างจากแกนกลาง (`sideScale = clamp(1.2 + lateralDist * 0.9, 1.4, 2.6)`) ยิ่งโดนเฉียดขอบยิ่งถูกดีดไถลออกด้านข้างอย่างรุนแรง
  - แรงส่งไปข้างหน้า 0.85 ในแนวราบขนานพื้น ศัตรูจะไถลเปิดทางออกข้างโดยไม่ลอยเคว้ง
- **Ground Fractures (รอยแตกพื้นดิน)**:
  - **จุดเริ่มคุกเข่า (Tick 0)**: รอยแตกพื้น 3D รัศมี 3.2 บล็อก
  - **ตลอดทางพุ่ง**: รอยแตกพื้นต่อเนื่องทุกๆ 4 บล็อก (รัศมี 2.0 บล็อก)
  - **จุดเริ่มเตะ (Tick 20)**: รอยแตกกระแทกขนาดใหญ่ รัศมี 3.5 บล็อก
  - **จุดปะทะเป้าหมาย**: รอยแตกใต้เท้าเป้าหมาย รัศมี 2.2 บล็อก พร้อมสะเก็ดดินและระเบิด

---

## Epic Fight Architecture & Fallback

- **Facade Design**: สกิลหลักใน `net.offkung.bhspells.spells.ground.EarthRoarSpell` อ้างอิงเฉพาะ `AnimationCue.EARTH_ROAR` ผ่าน `EpicFightCompat.playAnimation(...)` โดยไม่มีการ Import คลาสของ Epic Fight เข้ามาในแพ็กเกจหลัก
- **Optional Isolation**: โค้ดของ Epic Fight ทั้งหมดถูกแยกอยู่ภายใต้ `net.offkung.bhspells.compat.epicfight.skills.earth_roar.*`
- **Graceful Fallback**: หากไม่ได้ติดตั้ง Epic Fight ระบบ Server จะรันแอนิเมชันสำรองของ Vanilla และประมวลผลการพุ่ง พาร์ติเคิล ดาเมจ และการสตั้นอย่างครบถ้วน 100%

---

## เอกสารและการตรวจสอบ

- **Registration Check**:
  - `SpellRegistry.EARTH_ROAR_SPELL` ลงทะเบียนสำเร็จภายใต้ `// GROUND`
  - `MobEffectsRegistry.EARTH_ROAR_EMPOWERMENT` และ `EARTH_ROAR_STUN` ลงทะเบียนสำเร็จ
  - คอนฟิก `SpellConfig.EarthRoar` พร้อมค่าดีฟอลต์ (Damage: 35, Mana: 80, Cooldown: 120s)
- **Formatting Rule Compliance**:
  - โครงสร้างของคลาส `EarthRoarSpell` เป็นไปตามมาตรฐาน:
    `spell identity -> constants/static fields -> optional unit info -> default config field -> constructor -> standard getters/overrides -> spell logic/helpers`
