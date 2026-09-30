# Savage Bite (กัดขย้ำ)

## ข้อมูลหลัก

| รายการ | ค่าปัจจุบัน |
| --- | --- |
| Registry ID | `bhspells:savage_bite` |
| Class | `net.offkung.bhspells.spells.gold.SavageBiteSpell` |
| School | `bhspells:gold`; fallback เป็น Ender เมื่อหา Gold school ไม่พบ |
| Rarity | Rare |
| Max level | 1 |
| Cast type | Instant |
| Cast time | 0 ticks |
| Cooldown | 12 วินาที (คำนวณและเริ่มนับหลังจากหลุดออกจากการกัด / Deferred Cooldown) |
| Mana | เริ่มต้น 20 mana, ดูดต่อเนื่อง 10 mana/วินาที (ทุกๆ 20 ticks) |
| Base damage | 2.25 damage/วินาที (ทุกๆ 20 ticks) |
| Target range | 7 blocks (Raycast เล็งเป้าหมายในระยะสายตา) |
| Status effect | Slowness II บนเป้าหมายตลอดช่วงเวลาที่โดนกัด |
| Animation | Epic Fight `epicfight:biped/living/swim` (ว่ายน้ำจำลองการหมอบกัดข้อเท้า) |
| Language keys | `spell.bhspells.savage_bite` และ `spell.bhspells.savage_bite.guide` |

## เป้าหมายและการเริ่มต้น (Targeting & Initiation)

- เล็งเป้าหมาย LivingEntity ในระยะ 7 บล็อกในแนวเล็ง (Raycast / Aim-cone line-of-sight)
- ตรวจสอบความปลอดภัย: ยกเว้นพันธมิตร (Team/Allies), ผู้เล่น Spectator, สิ่งมีชีวิตประเภท Multipart Boss (เช่น Ender Dragon) หรือสิ่งมีชีวิตในแท็ก `#bhspells:savage_bite_immune`
- เมื่อร่ายผ่าน:
  1. หักมานาเริ่มต้น 20 หน่วย
  2. เข้าสู่สถานะ `LUNGING`: พุ่งตัวเข้าหาเท้า/ข้อเท้าของเป้าหมายอย่างรวดเร็ว (Collision-checked)
  3. เมื่อถึงตัวเป้าหมาย จะเปลี่ยนเป็นสถานะ `LATCHED` และเริ่มการกัด

## กลไกการเกาะและลาก (Attachment & Tethering Mechanics)

- ผู้เล่นจะถูกผูกติด (Tethered) กับตำแหน่งข้อเท้าของเป้าหมาย โดยตำแหน่งจะอัปเดตและถูกดึงลากตามการเคลื่อนที่ของเป้าหมาย (Drag)
- ผู้เล่นยังสามารถกด WASD เพื่อส่ง Input Intent ขืน/ควบคุมทิศทางการลากได้เล็กน้อย
- เป้าหมายที่โดนกัด **ยังคงสามารถหันมาโจมตีผู้เล่นที่เกาะขาอยู่ได้** (ผู้เล่นมีความเสี่ยงที่จะโดนโจมตีสวนกลับจนเลือดหมด)
- ทุกๆ 20 ticks (1 วินาที):
  - สร้างความเสียหาย 2.25 magic/physical damage แก่เป้าหมาย
  - หักมานาของผู้ร่าย 10 หน่วย (SyncManaPacket)
  - รีเฟรชสถานะ Slowness II บนเป้าหมาย
  - แสดงเอฟเฟกต์ประกายละอองทอง (Gold Sparkles) ที่ข้อเท้า และเล่นเสียงขบฟันกัด (Snap/Bite sound)

## เงื่อนไขการสิ้นสุดและการยกเลิก (Termination & Cancellation)

สกิลจะหยุดทำงานและปลดปล่อยทันทีเมื่อเข้าเงื่อนไขข้อใดข้อหนึ่งต่อไปนี้:
1. **การยกเลิกโดยผู้เล่น (Manual Cancel)**: ผู้เล่นกด **Spacebar (กระโดด)**
2. **เลือดผู้เล่นหมด**: ผู้เล่นเสียชีวิต (Health <= 0)
3. **มานาผู้เล่นหมด**: มานาของผู้เล่นเหลือน้อยกว่า 10 หรือหมดลง
4. **เป้าหมายเสียชีวิตหรือหายไป**: เป้าหมายตาย (Dead), หายไป (Despawn), หรือเทเลพอร์ตข้ามมิติ/ระยะไกล
5. **หลุดระยะ/หลุดโลก**: เกิดความผิดปกติของพิกัดหรือการเชื่อมต่อ
- เมื่อสิ้นสุดการกัด:
  - รีเซ็ตและคืนค่าอนิเมชั่นของ Epic Fight กลับสู่ปกติ (Synchronized release)
  - ล้างสถานะการยึดติดบน Server และแจ้ง Client
  - เริ่มนับ Cooldown 12 วินาทีทันที

## เครือข่ายและการซิงโครไนซ์ (Networking & Client/Server Boundaries)

- **Server-Authoritative**: Server ควบคุมตำแหน่งจริง ความเสียหาย การหักมานา เอฟเฟกต์ และการตรวจเช็คการยกเลิกทั้งหมด
- **Client Input Capture**: Client ดักจับปุ่ม WASD และ Spacebar (ส่งเป็น Packet สั่ง Cancel โดยระงับ Jump Impulse ปกติ)
- **Packets (`SavageBiteNetwork`)**:
  - `SavageBiteStatePacket` (S2C): ซิงก์สถานะ LUNGING, LATCHED, และ END แก่ Client
  - `SavageBiteInputPacket` (C2S): ส่งทิศทาง WASD และการกด Spacebar ยกเลิก

## การทดสอบและการตรวจสอบ (Verification)

1. คอมไพล์ผ่าน `./gradlew.bat compileJava`
2. ทดสอบการเล็งเป้าหมาย: ทั้งระยะใกล้-ไกล, ข้ามสิ่งกีดขวาง, สัตว์เลี้ยง/เพื่อนร่วมทีม, และบอสขนาดใหญ่
3. ทดสอบการลากตัว: เป้าหมายเดิน วิ่ง กระโดด และตรวจว่าผู้เล่นถูกลากตามไปอย่างราบรื่น
4. ทดสอบการกด Spacebar ยกเลิก และการกด WASD ต้านแรงลาก
5. ทดสอบการหมดมานา, การโดนโจมตีจนตาย, และเป้าหมายตายระหว่างกัด
6. ตรวจสอบการนับคูลดาวน์หลังจากการกัดสิ้นสุดลง
