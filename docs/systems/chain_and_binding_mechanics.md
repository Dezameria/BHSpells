# ระบบโซ่พันธนาการและเชื่อมโยงพลัง (Chain & Binding Mechanics Architecture)

เอกสารนี้อธิบายสถาปัตยกรรมและการทำงานของระบบโซ่พันธนาการ (Chains) และการควบคุมศัตรู/เชื่อมโยงพันธมิตรในม็อด BHSpells ครอบคลุม **Intrusion Chain** (โซ่จิตวิญญาณแห่งปฐพี) และ **Gold Chain / Arcane Shackle** (โซ่ทองคำพันธนาการแห่งความกลัว)

---

## 1. ภาพรวมของระบบ (System Overview)

ในม็อด BHSpells มีกลไกโซ่เชื่อมโยง 2 รูปแบบหลัก:

1. **Intrusion Chain (`IntrusionChainEntity` & `IntrusionChainManager`):**
   - โซ่เชื่อมต่อระหว่างผู้ร่ายกับเป้าหมายในระยะ 10–30 บล็อก
   - มี 2 รูปแบบการทำงาน: **BUFF** (เชื่อมพันธมิตรมอบบัฟแบ่งปันพลัง) และ **DEBUFF** (เชื่อมศัตรูเพื่อดูดกลืนพลังและลดทอนความสามารถ)
   - ควบคุมการเชื่อมต่อแบบเรียลไทม์ผ่าน [`IntrusionChainManager.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/event/IntrusionChainManager.java)
2. **Gold Chain (`GoldChain.java`):**
   - โซ่ทองคำพันธนาการเป้าหมายไว้กับผิวดินหรือสิ่งกีดขวาง
   - มีความทนทานของโซ่ (`Chain Health`), แรงต้านทานการดึง (`Restraint Strength`), และการจำกัดระยะการเคลื่อนที่ไม่ให้หลบหนี
   - ใช้งานร่วมกับ [`ShackleofFearSpell.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/spells/gold/ShackleofFearSpell.java)

---

## 2. Intrusion Chain Mechanics

### 2.1 ข้อกำหนดและเงื่อนไขการเชื่อมต่อ
- **ระยะการค้นหา (Select Range):** 10.0 บล็อกรอบตัวผู้ร่าย
- **ระยะขาดการเชื่อมต่อ (Break Distance):** 30.0 บล็อก (หากเดินห่างเกิน 30 บล็อกโซ่จะขาดทันที)
- **จำนวนเป้าหมายสูงสุด (Max Targets):** สูงสุด 2 เป้าหมายต่อผู้ร่ายหนึ่งคน (`MAX_ALLIES = 2`)
- **การรีเฟรชเอฟเฟกต์ (Refresh Cadence):** ตรวจสอบและให้บัฟ/ดีบัฟทุกๆ 30 Ticks (1.5 วินาที)

### 2.2 โหมดการทำงาน (Modes)
- **โหมด BUFF (`IntrusionChainBuffSpell`):**
  - เชื่อมโยงกับเพื่อนร่วมทีม มอบสถานะต้านทานความเสียหาย (`Resistance`), เพิ่มความเร็ว (`Speed`), และส่งผ่านพลังเวทมนตร์
  - เมื่อเพื่อนร่วมทีมที่ถูกเชื่อมต่อได้รับความเสียหาย ผู้ร่ายจะช่วยดูดซับและแบ่งเบาความเสียหายบางส่วน
- **โหมด DEBUFF (`IntrusionChainDebuffSpell`):**
  - ตรึงสายใยใส่ศัตรู ส่งสถานะ `Slowness`, `Weakness` และค่อยๆ ถ่ายโอนพลังชีวิต/มานาของศัตรูกลับมายังผู้ร่าย

---

## 3. Gold Chain Restraint Mechanics

- **Restraint Strength:** 0.015 แรงดึงกลับสู่จุดยึดเกาะทุก Tick
- **Multi-Part Collision:** แต่ละข้อต่อโซ่คำนวณผ่าน [`GoldChainPart.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/entity/spells/gold_chain/GoldChainPart.java) รองรับการเลี้ยวโค้งตามการเคลื่อนไหว
- **Health Pool:** โซ่แต่ละเส้นมีหลอดเลือดอิสระ ผู้เล่นที่ถูกล่ามสามารถโจมตีทำลายโซ่เพื่อปลดปล่อยตนเองได้
- **Slowness Lockdown:** ระหว่างที่โซ่ยังไม่ถูกทำลาย เป้าหมายจะติดสถานะ Slowness ระดับสูง

---

## 4. สถานะความสอดคล้อง (Synchronization Status)

- [x] ซิงโครไนซ์ตำแหน่งและจำนวนข้อต่อโซ่ระหว่าง Server/Client ผ่าน `IntrusionChainEntityRenderer` และ `GoldChainRenderer`
- [x] ตรวจสอบ Friendly Fire ป้องกันการใส่ Debuff ให้เพื่อนร่วมทีมโดยไม่ตั้งใจ
- [x] มีระบบกำจัด State ตกค้างเมื่อผู้ร่ายหรือเป้าหมายตาย หรือตัดการเชื่อมต่อจากเซิร์ฟเวอร์
