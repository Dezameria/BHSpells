# Hymn of Purification (ทำนองชำระล้าง)

## ข้อมูลหลัก

| รายการ | ค่าปัจจุบัน |
| --- | --- |
| ตัวละคร | เจียง หลิงหยวน (Jiang Lingyuan) |
| Registry ID | `bhspells:hymn_of_purification` |
| Class | `spells/gold/HymnofPurificationSpell.java` |
| School | `bhspells:gold`; fallback เป็น Ender เมื่อหา Gold school ไม่พบ |
| Theme | พลังธาตุหลัก: ทองคำ, พลังธาตุ: เสียง (ควบคุมทองคำให้ก่อรูปเป็นสายบาง ๆ นำพลังผ่านเสียงฉิน) |
| Rarity | Rare |
| Max level | 1 |
| Cast type | Instant (กดคลิกครั้งเดียว ไม่ต้องกดค้าง) |
| Cast time | 0 ticks (ร่ายทันทีแล้วเข้าสู่สถานะบรรเลงกู่ฉิน 23 วินาที) |
| Cooldown | 60 วินาที |
| Mana | 80 mana |
| Spell power | base 1, เพิ่ม 1 ต่อเลเวล |
| Status effect | `bhspells:hymn_of_purification` (23 วินาที / 460 ticks) |
| Radius | 20 blocks (ทรงกลมรอบตัวผู้ร่าย) |
| Healing pulse | 1.0 HP × Spell Power ทุก 20 ticks (วินาทีที่ 1–22 รวม 22 ครั้ง) |
| Cleanse effect | ล้าง harmful debuffs ทั้งหมดทันทีเมื่อบรรเลงครบ 23 วินาที (tick 460) |
| Interruption penalty | ดีบัฟเวียนหัว Nausea (Confusion) 40 ticks (2 วินาที) แก่ทุกคนในวง 20 blocks |
| Language keys | `spell.bhspells.hymn_of_purification`, `.guide`, และ `effect.bhspells.hymn_of_purification` |

## กลไกการทำงานและผลลัพธ์

- ผู้ร่ายเข้าสู่สมาธินั่งบรรเลงกู่ฉินเป็นเวลา 23 วินาที (460 ticks) โดยต้องอยู่กับที่
- **วินาทีที่ 1–22 (ticks 20–440)**: ปล่อยคลื่นเสียงทำนองฟื้นฟูพลังชีวิตเล็กน้อย (1.0 HP × Spell Power) ทุก ๆ 1 วินาที (20 ticks) แก่ผู้ร่ายและพันธมิตรในรัศมี 20 บล็อกรอบตัว
- **วินาทีที่ 23 (tick 460)**: ทำนองช่วงสุดท้ายบรรเลงสมบูรณ์ ปลดปล่อยคลื่นพลังประกายทองคำชำระล้างสถานะผิดปกติ (ลบเฉพาะ Harmful Effects ทั้งหมด โดยคงสถานะบัฟที่เป็นประโยชน์ไว้) แก่ผู้ร่ายและพันธมิตรทุกคนในรัศมี 20 บล็อก

## เงื่อนไขการขัดจังหวะและบทลงโทษ

- หากผู้ร่ายถูกขัดจังหวะก่อนบรรเลงครบ 23 วินาที สกิลจะยุติลงทันที:
  - **การขัดจังหวะเกิดจาก**:
    1. ผู้ร่ายได้รับความเสียหาย (Damage > 0)
    2. ผู้ร่ายเคลื่อนที่ (Displacement เกิน 0.2 บล็อก)
    3. มี Entity อื่นเดินเข้ามาชนกล่องฮิตบ็อกซ์ของผู้ร่าย
    4. ผู้ร่ายหลุดออกจากเกมหรือเปลี่ยนมิติ
  - **ผลของบทลงโทษและการหยุดสกิล**:
    1. สกิลถูกยกเลิกทันที (Interrupt)
    2. วง Area of Effect (`TargetedAreaEntity`) ถูกลบออกทันที
    3. ตัว Effekseer (`bhspells:vfx/Shiba_Commission`) หยุดทำงานและจางหายไปทันที (ทั้งผ่านการ discard entity ที่ผูกไว้ และผ่านการสั่ง stop named emitter ฝั่ง client)
    4. ปลดปล่อยอนุภาคคลื่นควันกระจายตัวออกมาเป็นวงกว้าง (`ParticleTypes.POOF`, `CAMPFIRE_COSY_SMOKE`, `SMOKE`)
    5. เสียงเพลงทำนองชำระล้างหยุดเล่นทันที และเล่นเสียงโน้ตดีดพลาด (Discordant Bass Note / Item Break Snap)
    6. การฟื้นฟูหยุดลงทันที และ**ไม่เกิดผลล้างดีบัฟ**
    7. ผู้ร่ายและพันธมิตรทุกคนในรัศมี 20 บล็อกได้รับดีบัฟเวียนหัว (Nausea / Confusion) เป็นเวลา 2 วินาที (40 ticks) อันเนื่องมาจากลมปราณไม่คงที่

## Audio, VFX และ Animation

- **วง Area of Effect**: สร้างเอนทิตี `TargetedAreaEntity` รัศมี 20 บล็อก สีทองคำ (`0xFFD700`) ติดตามตัวผู้ร่ายเป็นวงขอบเขตบอกระยะสกิล
- **Effekseer VFX**: แสดงผลเอฟเฟกต์ 3D Effekseer `bhspells:vfx/Shiba_Commission` ผ่าน AAA Particles ที่ตำแหน่งของผู้ร่าย ตลอดระยะเวลาการบรรเลง โดยลงทะเบียนเป็น Named Emitter (`hymn_shiba_<entityId>`) หากบรรเลงจนครบ 23 วินาทีตามปกติ ตัวเอฟเฟกต์จะเล่นต่อเนื่องจนจบลูปอย่างสมบูรณ์โดยไม่ถูกตัดจบ และจะถูกสั่งหยุด (`stop`) ให้หายไปทันทีเฉพาะเมื่อเข้าเงื่อนไขการขัดจังหวะ (Interrupted) เท่านั้น
- **อนุภาค (Particles)**:
  - นำอนุภาคเดิมทั้งหมด (NOTE, WAX_OFF, GLOW, HEART, FLASH, END_ROD) ออกทั้งหมด
  - เมื่อถูกขัดจังหวะ (Interrupted): เกิดคลื่นควันกระจายตัวออกรอบทิศทาง (`POOF`, `CAMPFIRE_COSY_SMOKE`, `SMOKE`) จากตำแหน่งผู้ร่าย
- **เสียงเพลงหลัก**: `bhspells:hymnofpurification` มีการหน่วงเวลา 2 วินาที (40 ticks) ก่อนเริ่มเล่นเสียงเพลง เพื่อให้สัมพันธ์กับจังหวะแอนิเมชันตั้งท่า จากนั้นเล่นต่อเนื่องจนจบการร่าย จัดการระดับ client instance ผ่าน `HymnofPurificationClientEvents` และตัดเสียงทันทีที่หลุดจากการร่าย
- **เสียงโน้ตพลาด**: `SoundEvents.NOTE_BLOCK_BASS` (pitch 0.5), `NOTE_BLOCK_DIDGERIDOO`, และ `ITEM_BREAK`
- **เสียงเมื่อบรรเลงสำเร็จ**: `SoundEvents.PLAYER_LEVELUP`, `AMETHYST_BLOCK_CHIME`, และ `BEACON_ACTIVATE`
- **แอนิเมชัน**: `SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED`

## สถาปัตยกรรม Server / Client

- **Server**:
  - `HymnofPurificationSpell`: ร่ายแบบ Instant ใส่เอฟเฟกต์ `HymnofPurificationEffect` นาน 460 ticks (23 วินาที), สร้าง `TargetedAreaEntity` รัศมี 20 บล็อก, เรียกเล่น Effekseer `bhspells:vfx/Shiba_Commission` ผ่าน `AAALevel.addParticle`, มี helper `cleanUpArea` สำหรับลบวง AoE เมื่อหมดเวลาโดยไม่ตัด Effekseer, `stopEffek` ส่ง `StopHymnEffekPacket` ไปยัง Client, `cleanUpVisuals` (เรียกทั้งสองอย่างเมื่อโดนขัด) และ `spawnInterruptionSmoke` สำหรับกระจายคลื่นควันเมื่อโดนขัด
  - `HymnofPurificationEffect`: ควบคุมลูปทุก server tick, ตรวจจับการเคลื่อนที่, entity collision, และความเสียหาย (สามารถหันมุมมองได้โดยไม่ขัดจังหวะสกิล) เมื่อเข้าเงื่อนไขขัดจังหวะจะเรียก `interrupt(...)` เพื่อลบวง AoE / ส่งแพ็กเก็ตดับ Effekseer ทันที, ปล่อยคลื่นควัน, เล่นเสียงโน้ตพลาด และแจกจ่าย Nausea แต่เมื่อบรรเลงครบกำหนดอย่างสมบูรณ์ จะเรียกเฉพาะ `cleanUpArea` เพื่อให้ Effekseer เล่นจนจบลูป
- **Client**:
  - `StopHymnEffekPacket`: รับสัญญาณหยุดจาก Server เมื่อเข้าเงื่อนไขขัดจังหวะ แล้วเรียก `HymnofPurificationClientEvents.stopClientEffek(casterEntityId)` เพื่อสั่ง `ParticleEmitter.stop()` บน Named Emitter ทันที
  - `HymnofPurificationClientEvents`: ติดตามสถานะผ่าน `player.hasEffect(MobEffectsRegistry.HYMN_OF_PURIFICATION.get())` และดีเลย์เสียง 2 วินาที (40 ticks) ก่อนเริ่มเล่นเสียงเพลงทำนอง และไม่ตัดการทำงานของ Effekseer เมื่อหมดเวลา เพื่อให้ Effekseer แสดงผลจนจบลูปได้สมบูรณ์

## การตรวจสอบ (Verification)

- ตรวจสอบการคอมไพล์โค้ด Java ด้วย `./gradlew.bat compileJava`
- ตรวจสอบความถูกต้องของทรัพยากรเสียงใน `sounds.json` และข้อความภาษาใน `en_us.json`
- ตรวจสอบ fallback ของ Magic School ในกรณีที่ไม่มี `bhspells`
