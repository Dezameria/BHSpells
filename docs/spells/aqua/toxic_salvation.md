# Toxic Salvation (ระเหยพิษให้ข้าจงรอด)

## ข้อมูลหลัก (Basic Information)

| รายการ | ค่าปัจจุบัน |
| --- | --- |
| ตัวละคร | ซงหลิน (Song Lin) |
| บทบาท/สาย | ดาเมจ (Damage) / สนับสนุนตนเอง |
| Registry ID | `bhspells:toxic_salvation` |
| `getSpellResource()` | `bhspells:toxic_salvation` |
| Class | `spells/aqua/ToxicSalvationSpell.java` |
| School | Aqua (`TravelopticsSchools.AQUA_RESOURCE`) |
| Rarity | Rare |
| Max Level | 1 (ทุก scroll มี 1 เลเวล; scale ต่อได้ด้วยคำสั่ง /cast และปรับแต่งได้ผ่าน bhspells-server.toml) |
| Cast Type | Long |
| Cast Time | 25 ticks (1.25 วินาที) |
| Cooldown | 22.0 วินาที |
| Base Mana Cost | 40 (+5 ต่อเลเวล) |
| Spawned Entity | `bhspells:toxic_salvation_aoe` (`entity/spells/toxic_salvation/ToxicSalvationAoe.java`) |
| Language keys | `spell.bhspells.toxic_salvation` และ `.guide` |

---

## 1. คำอธิบายและพฤติกรรมการทำงาน (Skill Overview & Mechanics)

แปลงพิษในตัวซงหลิน ให้ระเหยกลายเป็นหมอกพิษแผ่ขยายปกคลุมพื้นที่เป้าหมาย พร้อมขับเคลื่อนไปข้างหน้าและหมุนวนดั่งพายุหมอกพิษ (นำระบบการเคลื่อนที่และพายุหมุนวนมาจาก BlizzardSpell / BlizzardAoe)

1. **การล้างพิษและการฟื้นฟูเลือดผู้ร่าย (Caster Cleansing & Instant Heal):**
   - เมื่อร่ายเสร็จสิ้น จะล้างสถานะ Poison และ Azure Venomous ออกจากตัวผู้ร่ายทันที
   - ผู้ร่ายได้รับการฟื้นฟูเลือดทันที (`getHealAmount`: base 2.0, +0.5 ต่อเลเวล สเกลตาม Spell Power)
2. **การปล่อยและเคลื่อนที่ของหมอกพิษ (AoE Deployment & Drift):**
   - เล็งเป้าหมายด้วย Target Helper หรือ Raycast ระยะสูงสุด 32 บล็อก และปรับระดับลงสู่ผิวดิน (`Utils.moveToRelativeGroundLevel`)
   - สปอว์น `ToxicSalvationAoe` ที่ตำแหน่งเป้าหมาย โดยได้รับแรงขับเคลื่อนลอยไปข้างหน้าอย่างช้าๆ ตามทิศทางที่ผู้ร่ายหันหน้า (`entity.getForward().multiply(1, 0, 1).normalize().scale(0.05f)`)
   - มีระบบแนบไปกับภูมิประเทศ (Terrain Snap แบบ BlizzardAoe) เพื่อให้หมอกลอยตามทางลาดและหน้าผาได้อย่างราบรื่น
3. **แรงดูดและผลกระทบต่อศัตรูในหมอก (Vortex Suction & Poison Damage):**
   - มีแรงหมุนวนดูดศัตรูเข้าสู่ใจกลางม่านหมอกพิษ (Tangential vortex velocity แบบ BlizzardAoe ด้วย strength 0.03)
   - มีการตรวจสอบ Friendly Fire (`DamageSources.isFriendlyFireBetween`) ไม่ทำร้ายพวกเดียวกัน
   - ศัตรูในระยะหมอกจะได้รับเอฟเฟกต์ **Poison I** นาน 5 วินาที (100 ticks) และดาเมจเวทมนตร์ (`BHSpellRegistry.TOXIC_SALVATION_SPELL`) ต่อเนื่องทุก 1 วินาที (20 ticks)
4. **การฟื้นฟูเลือดต่อเนื่องในหมอก (Lingering Mist Heal):**
   - ผู้ร่ายหากยืนอยู่ภายในระยะม่านหมอกพิษ จะได้รับการฟื้นฟูเลือดต่อเนื่อง (50% ของ instant heal) ทุก 1 วินาที
5. **การสลายตัวด้วยเวทต่อต้าน (Anti-Magic):**
   - `ToxicSalvationAoe` สืบทอด `AntiMagicSusceptible` สามารถถูกลบล้างได้ด้วยผลของ Anti-magic

---

## 2. พารามิเตอร์และสเตตัส (Spell Parameters)

- **ความเสียหายหมอกพิษ (Damage):** base 4.0 (+1.0 ต่อเลเวล) สเกลตาม Aqua Spell Power
- **การฟื้นฟูเลือด (Heal):** base 2.0 (+0.5 ต่อเลเวล) สเกลตาม Aqua Spell Power
- **รัศมีหมอกพิษ (Radius):** base 5.0 บล็อก สเกลตาม Aqua Spell Power
- **ระยะเวลาคงอยู่ (Duration):** base 10.0 วินาที (+1.5 วินาทีต่อเลเวล)
- **สถานะพิษแก่ศัตรู:** Poison I นาน 5 วินาที (รีเฟรชทุก 1 วินาทีในหมอก)

---

## 3. เสียงและเอฟเฟกต์ (Audio & Visuals)

- **Cast Start Sound:** `SoundRegistry.POISON_BREATH_LOOP` ควบคู่แอนิเมชันร่ายยาว (`SpellAnimations.ANIMATION_LONG_CAST`)
- **Cast Finish Sound:** `SoundRegistry.POISON_CAST`
- **Release Impacts:** เสียงระเหยของหมอก `SoundEvents.BREWING_STAND_BREW` ที่ตัวผู้ร่าย และ `SoundRegistry.POISON_SPLASH_BEGIN` ที่จุดปล่อยหมอก
- **Ambient Vortex Sound:** เล่นเสียงลมหายใจพิษหมุนวน `SoundRegistry.POISON_BREATH_LOOP` ทุก 10 ticks ควบคู่เสียงหม้อต้มยา `SoundEvents.BREWING_STAND_BREW` ทุก 20 ticks
- **Swirling Storm Particles (ระบบพายุหมุนวนแบบ 3 มิติสีขาวดั่งหิมะ Blizzard):**
   - ใช้ `SwirlingParticleOptions` หมุนวนละอองสีขาวรอบแกนพายุ 3 มิติแบบเดียวกับ BlizzardSpell / BlizzardAoe:
     - `ParticleHelper.SNOWFLAKE` (เกล็ดหิมะหมุนวน)
     - `ParticleHelper.SNOW_DUST` (ละอองหิมะหมุนวน)
   - ปิดอนุภาคหมอกพิษสีเขียว (`POISON_CLOUD`, `ACID_BUBBLE`, `FOG`) เพื่อให้แสดงผลเป็นพายุหมอกสีขาวโปร่งสวยงาม

---

## 4. สถานะความสอดคล้อง (Synchronization Status)

- [x] `@AutoSpellConfig` เชื่อมต่อ config `bhspells-server.toml` อย่างสมบูรณ์
- [x] นำระบบ Swirling Particle สีขาว (SNOWFLAKE, SNOW_DUST) และ Inward Vortex Suction แบบ BlizzardAoe มาใช้กับ ToxicSalvationAoe
- [x] แนบไปกับความชันของภูมิประเทศอย่างราบรื่น (Terrain Snap)
- [x] ป้องกัน Friendly Fire ต่อพันธมิตรและสัตว์เลี้ยง
- [x] รองรับการถูกลบล้างด้วย Anti-Magic (`AntiMagicSusceptible`)
- [x] เสียงและเอฟเฟกต์สอดคล้องกับการใช้งานจริงที่ผู้ใช้กำหนด

