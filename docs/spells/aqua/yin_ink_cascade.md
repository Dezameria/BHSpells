# Yin Ink Cascade (วารีหมึกหยินหยาง)

## ข้อมูลพื้นฐาน (Basic Information)
- **ตัวละคร:** สุ่ว เยว่ชิง (Shui Yueqing)
- **Display Name:** Yin Ink Cascade
- **Registry ID:** `bhspells:yin_ink_cascade`
- **School:** Aqua (`TravelopticsSchools.AQUA_RESOURCE`)
- **Rarity:** EPIC
- **Max Level:** 5 (ปรับแต่งได้ผ่าน `bhspells-server.toml`)
- **Cast Type:** LONG
- **Cast Time:** 100 ticks (5.0 วินาที)
- **Base Mana Cost:** 80 (+10 ต่อเลเวล)
- **Cooldown:** 60.0 วินาที
- **Animation:** `SpellAnimations.ANIMATION_CONTINUOUS_CAST_ONE_HANDED`
- **Sounds:**
  - Cast Start: `SoundEvents.EVOKER_PREPARE_ATTACK` (เสียงบริกรรมคาถาชั่วคราว)
  - Cast Finish / Surge: `SoundEvents.GENERIC_SPLASH` (เสียงกระแสน้ำผุด)
  - Finale Explosion: `SoundEvents.GENERIC_EXPLODE` ผสาน `SoundEvents.GENERIC_SPLASH`
- **VFX / Effekseer:** `assets/bhspells/effeks/vfx/Mrquestion_commission_02.efkefc` (`vfx/Mrquestion_commission_02`)

## กลไกการทำงาน (Spell Mechanics)
1. **ขั้นตอนการร่ายเวท (Cast Phase - 5.0 วินาที / 100 Ticks):**
   - ทันทีที่เริ่มร่ายเวท (`onServerPreCast`), เรียกเล่นเอฟเฟกต์ Effekseer `vfx/Mrquestion_commission_02` ณ ตำแหน่งด้านหน้าผู้ร่าย 2.0 บล็อก (`FORWARD_OFFSET = 2.0D`)
   - ตลอดระยะเวลาการร่าย 5.0 วินาที ตัวเอฟเฟกต์ Effekseer จะหันทิศทางและเคลื่อนที่ตามมุมมองการมอง (Yaw / Facing) และตำแหน่งด้านหน้า 2.0 บล็อกของผู้ร่ายแบบเรียลไทม์ผ่าน `YinInkCascadeClientEvents`
   - **การยกเลิกระหว่างร่าย:** หากการร่ายถูกขัดจังหวะ ยกเลิก หรือไม่เสร็จสมบูรณ์ก่อนครบ 5.0 วินาที (`cancelled = true`), เซิร์ฟเวอร์จะส่งแพ็กเก็ต `StopYinInkEffekPacket` เพื่อสั่งหยุดและสลายตัว Effekseer บนไคลเอนต์ทันที
   - ในระหว่าง 5 วินาทีนี้ ผู้ร่ายจะอยู่ในท่วงท่าการร่ายแบบต่อเนื่องมือเดียว (`ANIMATION_CONTINUOUS_CAST_ONE_HANDED`)
2. **การปลดปล่อยกระแสน้ำหมึกหยิน (Surge Phase - เมื่อร่ายเสร็จสิ้น / 100 Ticks - onCast):**
   - เมื่อร่ายครบ 5.0 วินาทีสำเร็จ (Cast Complete) เอฟเฟกต์ Effekseer จะหยุดหมุนตามผู้ร่ายและปักหลักอยู่นิ่งๆ ณ ตำแหน่งด้านหน้า 2.0 บล็อกที่ปล่อยเวท (และเล่นต่อเนื่องจนจบแอนิเมชัน ไม่หายไป)
   - ก่อกำเนิดเอนทิตีอาณาเขตกระแสน้ำหมึกสีเทา (`YinInkCascadeAreaEntity`, รัศมี 30 บล็อก, ความคงอยู่ 100 ticks) ณ ตำแหน่งด้านหน้า 2.0 บล็อกให้ตรงกับจุดศูนย์กลางของ Effekseer พอดี
   - **Visual Pure Effekseer:** นำวงแหวนพาร์ติเคิล (`TargetedAreaEntity`) และละอองน้ำวนวานิลลาออกทั้งหมด เพื่อคงเหลือเฉพาะความงดงามคมชัดของ Effekseer เพียงอย่างเดียว
   - ศัตรูทุกตัวที่อยู่ภายในระยะ 30 บล็อกจะติดผล `Slowness II` (Amplifier 1) เป็นเวลา 25 ticks อย่างต่อเนื่อง ตราบใดที่ยังไม่สามารถหนีพ้นเขตอาณาเขต 30 บล็อก
3. **การระเบิดของวารีหมึกบทสรุป (Finale Explosion - Tick 100):**
   - เมื่อเวลาผ่านไปครบ 5 วินาที (Tick 100) กระแสน้ำหมึกจะระเบิดออกอย่างรุนแรงรอบทิศทาง
   - ศัตรูทุกตัวในระยะรัศมี 30 บล็อกจะได้รับความเสียหายเวทวารีอย่างมหาศาล (25 Base Damage สเกลตาม Spell Power)
   - ศัตรูที่โดนแรงระเบิดจะติดสถานะเน่าเปื่อย `Wither I` นาน 14 วินาที (280 ticks)
   - เล่นเสียงระเบิดผสานกระแสน้ำอย่างดังกึกก้อง โดยไม่มีการสแปมพาร์ติเคิลวานิลลาบดบังทัศนวิสัย
4. **ความปลอดภัยของตนเองและพันธมิตร (Self & Ally Safety):**
   - ผู้ร่ายจะไม่ได้รับความเสียหายหรือสถานะผิดปกติใดๆ จากสกิลของตนเอง (`target == owner`)
   - พันธมิตรของผู้ร่ายจะได้รับการยกเว้นความเสียหายและดีบัฟตามระบบ Friendly Fire
5. **ระบบความปลอดภัยและการยกเลิก (Safety & Anti-Magic):**
   - หากผู้ร่ายเสียชีวิต หลุดออกจากเซิร์ฟเวอร์ หรือเปลี่ยนมิติก่อนการระเบิด ตัวอาณาเขตจะถูกลบล้างทิ้งทันทีโดยไม่ระเบิด
   - รองรับระบบ `AntiMagicSusceptible` หากถูกเวทลบล้างเวทมนตร์จะสลายตัวทันที

## ค่าตัวเลขและการคำนวณ (Formulas & Scaling)
- **Burst Damage:** 25.0 Base Damage (+5.0 ต่อเลเวลเวทมนตร์) สเกลตาม Aqua Spell Power ของผู้ร่าย (`getEntityPowerMultiplier`)
- **Area Radius:** 30.0 บล็อก (พื้นที่ทรงกลมรอบจุดศูนย์กลางด้านหน้า)
- **Forward Offset:** 2.0 บล็อก (เยื้องไปข้างหน้าตามทิศทางที่ผู้ร่ายหันหน้า)
- **Surge Debuff:** `Slowness II` (Amplifier 1) รีเฟรชต่อเนื่องทุก 5 ticks ครั้งละ 25 ticks
- **Explosion Debuff:** `Wither I` นาน 14.0 วินาที (280 ticks)
- **Self Damage:** ปลอดภัย 100% ไม่โดนดีบัฟและดาเมจใดๆ
- **Mana Cost:** 80 Base Mana (+10 ต่อเลเวล)
- **Cooldown:** 60.0 วินาที

## การทำงานฝั่ง Server / Client
- **Server:**
  - เริ่มเรียกเอฟเฟกต์ Effekseer ในช่วง Pre-Cast พร้อมตั้งค่า Rotation เริ่มต้นตาม Yaw ของผู้ร่าย
  - ควบคุมการตรวจจับการยกเลิกเวทมนตร์ผ่าน `onServerCastComplete(..., cancelled)` หากยกเลิกก่อนครบ 5 วิ จะส่ง `StopYinInkEffekPacket` ไปยังไคลเอนต์เพื่อยกเลิก Effekseer ทันที
  - คำนวณเป้าหมายศัตรู (เป็นมิตร/พันธมิตรจะได้รับการยกเว้นผ่าน `DamageSources.isFriendlyFireBetween`)
  - อัปเดตสถานะ Slowness II ต่อเนื่องในระยะ 30 บล็อก
  - ตรวจจับเงื่อนไขเวลาที่ Tick 100 เพื่อสร้างความเสียหายเวท, ใส่ Wither แก่ศัตรู และเล่นเสียงระเบิด (ผู้ร่ายและพันธมิตรปลอดภัย 100%)
  - บันทึกและโหลดสถานะ NBT สำหรับดาเมจและการระเบิด
- **Client:**
  - แสดงผลเอฟเฟกต์พาร์ติเคิล Effekseer `vfx/Mrquestion_commission_02` เพียงลำพัง (ไม่มีอนุภาควานิลลาบดบัง)
  - `YinInkCascadeClientEvents` หันทิศทาง Effekseer ตามสายตาผู้ร่ายตลอด 5.0 วินาทีแรก และปล่อยให้อยู่นิ่งเมื่อร่ายเสร็จสิ้น
  - รับแพ็กเก็ต `StopYinInkEffekPacket` เพื่อสั่งหยุด Effekseer ทันทีเมื่อผู้เล่นหยุดร่ายกลางคัน
  - แสดงผลอนิเมชั่นการร่ายเวทต่อเนื่อง
