# Shocking (ช็อคกิ้ง)

## ข้อมูลหลัก

| รายการ | ค่าปัจจุบัน |
| --- | --- |
| ตัวละคร | ลู่ ฮวา (Lu Hwa / Lu Hua) |
| Registry ID | `bhspells:shocking` |
| Class | `net/offkung/bhspells/spells/ground/ShockingSpell.java` |
| School | Ground (`bhspells:ground`) |
| Rarity | Uncommon |
| Max Level | 1 |
| Cast Type | Instant |
| Cast Time | 0 ticks |
| Cooldown | 15.0 วินาที (ปรับแต่งผ่าน `SpellConfig.Shocking.cooldown`) |
| Mana Cost | Base 50, +5 ต่อเลเวล (ปรับแต่งผ่าน `SpellConfig.Shocking.baseMana` / `manaPerLevel`) |
| Range (Length) | 8.0 บล็อก (ตัดระยะเมื่อชนบล็อกทึบ) |
| Hitbox Volume | กว้าง 1.8 บล็อก (`BEAM_HALF_WIDTH = 0.9F`), สูง 1.8 บล็อก (`BEAM_HALF_HEIGHT = 0.9F`), ยาว 8 บล็อก |
| Targeting Mode | **Piercing Beam (AoE 3D Volume ด้านหน้า)** เจาะทะลวงศัตรูทุกตัวในแนววิถี |
| Impact Damage | Base 8.0, +1.5 ต่อเลเวล (ปรับแต่งผ่าน `SpellConfig.Shocking.baseDamage` / `damagePerLevel`) |
| Debuff | ติดสถานะ **ช็อคกิ้ง (`bhspells:shocking`)** นาน 8.0 วินาที (160 ticks) |
| Debuff Effect | - ลดความเร็วการเคลื่อนที่ 25% คงที่ (`Attributes.MOVEMENT_SPEED` -0.25)<br>- สร้าง Magic Damage ต่อเนื่อง (DoT) ทุกๆ 20 ticks (1.0 วินาที) Base 2.0 (+0.5 ต่อ amplifier) |
| Language Keys | `spell.bhspells.shocking`, `spell.bhspells.shocking.guide`, `effect.bhspells.shocking` |

## พฤติกรรมและกลไกการทำงาน

1. **การปล่อยลำแสง (Hand-origin Raycast):**
   - คำนวณจุดปล่อยพลังจากมือขวาของผู้ร่ายเยื้องจากระดับสายตาเล็กน้อย (Hand-origin offset) เพื่อความสมจริง
   - ตรวจจับบล็อกทึบข้างหน้าด้วย `Utils.raycastForBlock` หากมีสิ่งกีดขวาง ลำแสงและระยะดาเมจจะสิ้นสุดที่บล็อกแรกที่ขวาง
2. **การตรวจจับและโจมตี 3D Volume ด้านหน้า (Width, Height, Length):**
   - ใช้ Broad-phase `AABB` คลุมพื้นที่กว้าง 1.8 บล็อก, สูง 1.8 บล็อก และยาว 8 บล็อกด้านหน้าผู้ร่าย
   - ทำ Narrow-phase ตรวจสอบระยะห่างทรงกระบอก 3D จากแกนกลางลำแสงร่วมกับ Hitbox ของเป้าหมาย
   - กรองเฉพาะเป้าหมายที่เป็น `LivingEntity`, ยังมีชีวิตอยู่, ไม่ใช่ Spectator, ไม่ใช่ผู้ร่าย และเคารพกฎ Friendly Fire ของระบบ Iron's Spells
3. **การสร้างความเสียหายและสถานะผิดปกติ:**
   - ศัตรูทุกตัวที่อยู่ในแนวลำแสงจะได้รับ Initial Impact Damage ทันที
   - เป้าหมายที่โดนจะติดสถานะ `ShockingEffect` นาน 160 ticks (8 วินาที)
   - สถานะจะลดสปีดคงที่ 25% และกระตุกสร้างดาเมจเวทซ้ำทุกๆ 1 วินาที (รวม 8 ครั้ง) พร้อมสะเก็ดประกายไฟฟ้าสีเขียว

## VFX และเสียง

- **Emerald Arc Discharge (`shocking_beam`):**
  - **แนวคิด (Concept):** *"Emerald Arc Discharge / Unstable Magical Electricity"* พลังไฟฟ้าเวทมนตร์สีเขียวมรกตที่ดูรุนแรง อลังการ และไม่เสถียร มีลักษณะการดิ้นและกระตุกในอากาศ ไม่ใช่เลเซอร์เส้นตรง
  - **จุดกำเนิดพลัง (Hand Energy Core):**
    - แกนพลังงาน 3 ชั้น: แกนกลางขาวบริสุทธิ์ (`White Hot Core`), วงขอบสีเขียวอมเหลือง (`Lime Rim`) และเรืองแสงรอบนอกสีเขียวมรกต (`Emerald Outer Glow`)
    - มีสายฟ้าเล็กวิ่งวน (`Swirling Arcs`) และวงแหวนพลังงานโปร่งใส 2-3 ชั้นรอบจุดกำเนิดที่มีรอยแยก การสั่นไหว และแตกเป็นส่วนๆ (`Fragmented / Distorted Shock Rings`) เสมือนสนามแม่เหล็กกำลังเสียเสถียรภาพ
    - เมื่อปล่อยพลังเกิดแสงวาบ (`Muzzle Flash`) สั้นๆ และคลื่นกระแทกวงแหวน (`Muzzle Shock Ring`) ขยายตัวออกอย่างรวดเร็ว
  - **สายฟ้าหลัก (Main Jagged Arc):**
    - วิถีสายฟ้าซิกแซกในพื้นที่ 3 มิติ (3D Jagged Displacement) มีการหนา-บางไม่เท่ากันตามแนววิถี
    - โครงสร้าง 3 Layer Coincident Tubes:
      - *Layer 1 (White Hot Core):* แกนกลางสีขาวอมเขียว บางมากและสว่างที่สุด
      - *Layer 2 (Emerald Arc):* ตัวสายฟ้าหลักสีเขียวมรกต หนาและเด่นชัด
      - *Layer 3 (Outer Electrical Glow):* แสงเรืองรองสีเขียว/Cyan รอบสายฟ้า ให้ความรู้สึกล้นทะลักของพลังงาน
    - ใช้ระบบสลับรูปทรง 4 สถานะไม่ต่อเนื่อง (`State A -> B -> C -> D`) อิงตามช่วงเวลา Tick เพื่อให้เกิดความรู้สึก "สายฟ้ากำลังกระตุกและดิ้นอยู่ในอากาศ" โดยไม่ใช้สัญญาณสุ่มต่อเฟรมเรนเดอร์ (ป้องกันการเกิด Noise รบกวนสายตา)
  - **กิ่งสายฟ้าแตกแขนง (Lightning Branches & Sub-branches):**
    - แตกกิ่งสุ่มออกจากสายฟ้าหลักตามความยาวที่แตกต่างกัน และมีกิ่งย่อย (`Sub-branches`) แตกซ้ำอีกทอด
    - อายุของแต่ละกิ่งถูกกำหนดด้วย Seed เฉพาะตัวให้เกิดเพียง 2-5 ticks สร้างความรู้สึกประจุไฟฟ้าพยายามคายออกสู่อากาศอย่างไม่เสถียร
    - ใช้เฉดสีเขียวมรกต, Cyan-Green และปลายกิ่งสีขาวสว่าง
  - **คลื่นพลังงานนำทาง (Energy Pulse):**
    - มีชีพจรพลังงานความสว่างสูงวิ่งจากมือไปยังปลายสายฟ้าตามฟังก์ชัน Ease-out
    - ส่วนของสายฟ้าที่คลื่นพลังงานวิ่งผ่านจะสว่างวาบขึ้นชั่วขณะ พร้อมกระตุ้นให้กิ่งสายฟ้าบริเวณนั้นระเบิดตัวออกและดีดประกายไฟ (`Sparks`)
  - **ความปั่นป่วนทางไฟฟ้า (Electrical Chaos):**
    - สร้างสะเก็ดไฟ (`Electric Spark`) และละอองไอพลาสมาไฟฟ้า (`Electric Dust`) สีเขียวมรกตและ Cyan ในจังหวะ `tick()` เพื่อรักษาต้นทุนเฟรมเรตให้คงที่
    - ประกายไฟฟ้าถูกแรงผลักดีดออกทางด้านข้างรอบแกนสายฟ้า
  - **จุดกระทบเป้าหมาย (Impact Visuals):**
    - *White-Green Flash:* แสงสว่างจ้าสีขาวอมเขียว ณ จุดปะทะแบบ Crossed Billboards มองเห็นได้ทุกมุมกล้อง
    - *Radial Lightning:* สายฟ้า 3D Spherical Arcs แตกแขนงพุ่งออกรอบทิศทางในทรงกลม 3 มิติอย่างสมบูรณ์
    - *Expanding Shock Ring:* วงแหวนสี Cyan/Emerald ขยายตัวออกอย่างรวดเร็วบนระนาบการปะทะ
    - *Residual Arcs:* สายฟ้าเล็กๆ ดิ้นกระตุกค้างอยู่บริเวณจุดกระทบก่อนจางหายไปในช่วงท้าย (Residual Fade)
- **สถานะไฟฟ้าช็อตเป้าหมาย (Target Shock Effect):**
  - แสดงผลฝั่ง Client เท่านั้นผ่าน `ShockingEffectClientEvents` บน Entity ที่ติดสถานะ `bhspells:shocking`
  - เกิดขึ้นเป็นระลอก (`Periodic Pulses` รอบละ 24 ticks โดยทำงาน 7 ticks และหยุดพัก 17 ticks) เพื่อไม่ให้รกสายตาตลอดเวลา
  - สายฟ้าสั้นๆ ดิ้นและกระโดดเชื่อมโยงระหว่างจุด Anchor ต่างๆ บนร่างกายของเป้าหมาย เช่น Head -> Shoulder, Shoulder -> Chest, Chest -> Arm, Leg -> Ground ตามขนาด Bounding Box จริงของม็อบทุกประเภท โดยใช้ Crossed Double-sided Ribbons จึงไม่ล่องหนเมื่อมองจากด้านข้าง
  - มีสะเก็ดไฟและเสียงกระตุกไฟฟ้าเปรี๊ยะเบาๆ (`Electric Pop`) ในช่วงเริ่มต้นของแต่ละระลอก (ตรวจจับทุก Tick ป้องกันเสียงหลุด)
- **จานสี (Color Palette):**
  - Primary: Emerald Green (`RGB: 0.08, 0.95, 0.35`)
  - Secondary: Lime / Electric Green (`RGB: 0.50, 1.00, 0.18`)
  - Highlight: White / White-Green (`RGB: 0.98, 1.00, 0.96`)
  - Accent: Cyan / Cyan-Green (`RGB: 0.05, 0.95, 0.85`)
- **ประสิทธิภาพและสถาปัตยกรรมเครือข่าย (Performance & Multiplayer):**
  - เซิร์ฟเวอร์ส่งเพียง `ShockingBeamParticleOption` เฉพาะผู้เล่นในระยะสายตา 64 บล็อก พร้อมจุดปลายทาง สเกล และ `castSeed` ครั้งเดียว ไม่มีการส่ง Packet แยกตามข้อต่อสายฟ้า
  - ฝั่ง Client คำนวณความหยึกหยัก กิ่งก้าน และการกระตุกแบบ Deterministic จาก Seed เดียวกัน ผู้เล่นทุกคนจึงเห็นสายฟ้ารูปร่างเดียวกันอย่างแม่นยำ
  - มีระบบ Distance Culling โดยไม่เรนเดอร์ลำสายฟ้าเมื่อกล้องอยู่ห่างเกิน 48 บล็อก และเปิดโหมดลดทอนรายละเอียด (LOD - ลด Segment และกิ่งก้าน) เมื่อเกิน 24 บล็อก
  - ทำความสะอาด Blend Mode และ Depth Mask สมบูรณ์หลังเรนเดอร์ ป้องกันภาพทะลุหรือบั๊กกับวัตถุโปร่งแสงอื่น
  - ฝั่ง Dedicated Server ปลอดภัย 100% ปราศจากการโหลดคลาส Client

การปรับ VFX นี้ไม่เปลี่ยน Damage, Hitbox, Range, Mana, Cooldown, Debuff, Friendly Fire หรือกติกาการหยุดลำแสงเมื่อชนบล็อก

## การตรวจสอบ

- ตรวจสอบการคอมไพล์ผ่าน `.\gradlew.bat compileJava` (สำเร็จ ปราศจาก Error)
- ตรวจสอบการลงทะเบียน ID `bhspells:shocking` ในสายเวท Ground และ Particle `shocking_beam`
- ตรวจสอบความถูกต้องของ Hitbox 3 มิติ (ความกว้าง 1.8, ความสูง 1.8, ความยาว 8 บล็อก)
- ตรวจสอบการส่ง Seed ผ่าน Packet `ShockingBeamParticleOption` เฉพาะผู้เล่นในระยะ 64 บล็อก
- ตรวจสอบ Hand Energy Core, Main Arc 3 เลเยอร์, Branching Arcs (2-5 ticks window), Pulse Travel, Impact Shock Ring/3D Spherical Radial Arcs และ Residual Arcs
- ตรวจสอบ Target Shock Effect บนเอนทิตีที่ติดสถานะ `ShockingEffect` ว่ามีสายฟ้า Crossed Ribbons กระตุกข้ามจุด Anchor, มีเสียง Pop และหายไปเมื่อสถานะหมด
- ตรวจสอบระบบ Distance Culling (>48 บล็อก) และ LOD Reduction (>24 บล็อก)
- ตรวจสอบการไม่แครชบน Dedicated Server และไม่มี Blend/Depth-Mask รั่วไหลสู่การเรนเดอร์น้ำหรือ Particle อื่น
