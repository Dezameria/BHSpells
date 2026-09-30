# รายการเวททั้งหมด

รายการนี้ซิงก์กับ `src/main/java/net/offkung/bhspells/registry/BHSpellRegistry.java` ปัจจุบันมีเอกสารข้อกำหนดครบทั้ง **63 เวทใน 7 สายเวท** รายละเอียดค่าพลัง ระยะเวลา damage, cooldown, entity, asset และ edge case ให้ยึดเอกสารรายสกิลที่ลิงก์ไว้

> **หมายเหตุระบบ Scroll และ Config:**
> - สกิลทุกสกิลในม็อดถูกกำหนดให้มี Scroll เพียงระดับเดียวคือ **Level 1** (`maxLevel = 1`) ทำให้ในเมนูสร้างสรรค์ (Creative Tab), JEI, ดรอปจากมอนสเตอร์ หรือกล่องสมบัติ จะพบเพียงคัมภีร์ระดับ 1 เท่านั้น
> - ตัวคูณความสามารถตามเลเวล (Level Scaling) ยังคงทำงานได้เต็มรูปแบบผ่านคำสั่งร่ายเวท: `/cast <ผู้เล่น> <ชื่อเวท> <เลเวล>`
> - ค่าสเตตัสทั้งหมด (Base Damage, Damage Per Level, Base Mana, Mana Per Level, Cooldown) สามารถปรับแต่งได้อย่างสะดวกรวดเร็วผ่านไฟล์คอนฟิกเซิร์ฟเวอร์ `config/bhspells-server.toml` หรือผ่านค่าคงที่ `SPELL TUNING CONSTANTS` ด้านบนสุดของแต่ละคลาสเวท

## Fire (10 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Blazing Chakra | จาง เจียอี้ (Zhang Jiayi) | `bhspells:blazing_chakra` | กระแทกพื้นและปล่อยคลื่นเพลิงหลายชั้นพร้อม damage falloff | [เปิดเอกสาร](spells/fire/blazing_chakra.md) |
| Crimson Thornbind | ซานฉา กุยจง (Shancha Guizhong) | `bhspells:crimson_thornbind` | ปล่อยเถาวัลย์แนวนอนจากมือ จับเป้าหมายแรกเพียงตัวเดียวด้วยรากแนวตั้ง และเสริม Rend Damage | [เปิดเอกสาร](spells/fire/crimson_thornbind.md) |
| Fiery Dance | จาง เจียอี้ (Zhang Jiayi) | `bhspells:fiery_dance` | ร่ายระบำเพลิงปล่อยลูกแก้วทองคำระเบิดดาเมจไฟและผลักศัตรู | [เปิดเอกสาร](spells/fire/fiery_dance.md) |
| Firebird | - | `bhspells:firebird` | อัญเชิญวิหคเพลิงพุ่งทะลวงเป้าหมายระเบิดเพลิงเผาไหม้ | [เปิดเอกสาร](spells/fire/firebird.md) |
| Gale Drive | อู่ฉ่าย กวนหลง (Wucai Guanlong) | `bhspells:gale_drive` | พุ่งทะลวงและสร้าง vortex เมื่อชนเป้าหมาย | [เปิดเอกสาร](spells/fire/gale_drive.md) |
| Heaven Lion | - | `bhspells:heaven_lion` | คำรามสิงโตสวรรค์ระเบิดคลื่นเพลิงทำลายล้างรอบทิศทาง | [เปิดเอกสาร](spells/fire/heaven_lion.md) |
| Pure White Flame Burst | ฮุ่นตุ้น ฮ่าวเหยียน (Hundun Haoyan) | `bhspells:pure_white_flame_burst` | จับเป้าหมายระยะประชิด ทุ่ม และสร้างระเบิด/คลื่นเพลิงขาว | [เปิดเอกสาร](spells/fire/pure_white_flame_burst.md) |
| Resonant Knell | โม่ ซินซิน (Mo Xinxin) | `bhspells:resonant_knell` | Recast 6 ครั้งเพื่อสลับโดมป้องกันกับ shockwave รัศมี 15/20/30 | [เปิดเอกสาร](spells/fire/resonant_knell.md) |
| Smiles of Fire | - | `bhspells:smiles_of_fire` | ยิ้มเย้ยเพลิงผลาญ บัฟตนเองเพิ่มพลังโจมตีเพลิงและสร้างความเสียหายตอบโต้ | [เปิดเอกสาร](spells/fire/smiles_of_fire.md) |
| Spin Strike | - | `bhspells:spin_strike` | พุ่งหมุนไปข้างหน้าและโจมตีเป้าหมายตามเส้นทาง | [เปิดเอกสาร](spells/fire/spin_strike.md) |

## Lightning (5 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Divine Thunder | - | `bhspells:divine_thunder` | ฟาดอัสนีบาตศักดิ์สิทธิ์ทำลายล้างเป้าหมายจากฟากฟ้า | [เปิดเอกสาร](spells/lightning/divine_thunder.md) |
| Lightning Strike | - | `bhspells:lightning_strike` | พุ่ง/วาร์ปตาม raycast และโจมตีเป้าหมายด้วยสายฟ้า | [เปิดเอกสาร](spells/lightning/lightning_strike.md) |
| Tempest Reiatsu | แรงดันอัสนีบาตสีชมพู / 绯雷威压 | `bhspells:tempest_reiatsu` | ปลดปล่อยอาณาเขตพายุสายฟ้าชมพู 64-120 บล็อก ม่านฝนแรงดันรอบตัว 360° สุ่มสายฟ้าชมพูฟาดเป้าหมายพร้อมดาเมจและสตั๊น (โหมดเปิด/ปิด) | [เปิดเอกสาร](spells/lightning/tempest_reiatsu.md) |
| Thunder Step | - | `bhspells:thunder_step` | Teleport ไปข้างหน้าและทำ damage ตลอดเส้นทาง | [เปิดเอกสาร](spells/lightning/thunder_step.md) |
| Ultrashock | - | `bhspells:ultrashock` | กระแสไฟฟ้าแรงสูงช็อตศัตรูต่อเนื่องพร้อมสตั๊นและสโลว์ | [เปิดเอกสาร](spells/lightning/ultrashock.md) |

## Nature (12 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Art of Healing | - | `bhspells:art_of_healing` | ศาสตร์แห่งการรักษา ปลดปล่อยละอองพฤกษาฟื้นฟูเลือดผู้ร่ายและพันธมิตร | [เปิดเอกสาร](spells/nature/art_of_healing.md) |
| Art of Truth | - | `bhspells:art_of_truth` | ศาสตร์แห่งสัจธรรม ขจัดม่านลวงตาและบัฟการรับรู้รอบตัว | [เปิดเอกสาร](spells/nature/art_of_truth.md) |
| Eternal Purification | - | `bhspells:eternal_purification` | การชำระล้างนิรันดร์ ลบล้างดีบัฟและฟื้นฟูพลังชีวิตอย่างต่อเนื่อง | [เปิดเอกสาร](spells/nature/eternal_purification.md) |
| Explosive Lily | - | `bhspells:explosive_lily` | วางดอกลิลลี่ระเบิดพฤกษา ทำดาเมจกระจายเมื่อศัตรูเหยียบย่ำ | [เปิดเอกสาร](spells/nature/explosive_lily.md) |
| Gale Piercer | ลันลัน (Lanlan) | `bhspells:gale_piercer` | ชาร์จลูกศรลมแบบ homing; full charge ทะลุกำแพงและติดสถานะ | [เปิดเอกสาร](spells/nature/gale_piercer.md) |
| Healing Lily | - | `bhspells:healing_lily` | วางดอกลิลลี่ฟื้นฟูรักษา มอบบัฟฟื้นฟูเลือดแก่พันธมิตรที่เข้าใกล้ | [เปิดเอกสาร](spells/nature/healing_lily.md) |
| Rapturous Bloom | ฮวา เหมยเซียง (Hua Meixiang) | `bhspells:rapturous_bloom` | สร้างดอกไม้พิษใต้เป้าหมาย ใส่สถานะเป็นระยะ และระเบิดกลีบดอกเมื่อครบเวลา | [เปิดเอกสาร](spells/nature/rapturous_bloom.md) |
| Six Petal Waltz | - | `bhspells:six_petal_waltz` | ระบำกลีบดอกไม้ 6 กลีบ หมุนวนรอบตัวสร้างความเสียหายและผลักศัตรู | [เปิดเอกสาร](spells/nature/six_petal_waltz.md) |
| Supporting Bamboo | - | `bhspells:supporting_bamboo` | ไผ่ค้ำจุน มอบเกราะป้องกันและต้านทานการกระเด็น | [เปิดเอกสาร](spells/nature/supporting_bamboo.md) |
| Venomous Blossomfall | หูเหยียน เฟิงเซียว (Huyan Fengxiao) | `bhspells:venomous_blossomfall` | ชาร์จเข็มพิษสามระดับและยิงไปยัง crosshair | [เปิดเอกสาร](spells/nature/venomous_blossomfall.md) |
| Wing of Wind | - | `bhspells:wing_of_wind` | ปีกวายุ เพิ่มความเร็วการเคลื่อนที่และกระโดดสูง | [เปิดเอกสาร](spells/nature/wing_of_wind.md) |
| Wings of Tempest | จี่ จื่อเกอ (Ji Zige) | `bhspells:wings_of_tempest` | AoE ติดตามผู้ร่าย หมุนศัตรูรอบพายุและใส่ debuff | [เปิดเอกสาร](spells/nature/wings_of_tempest.md) |

## Aqua (9 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Aqua Flower | - | `bhspells:aqua_flower` | ดอกไม้วารีเบ่งบาน ปลดปล่อยละอองน้ำฟื้นฟูเลือดและผลักศัตรู | [เปิดเอกสาร](spells/aqua/aqua_flower.md) |
| Blessing Snow | - | `bhspells:blessing_snow` | หิมะอวยพร มอบความเร็วและเกราะน้ำแข็งแก่ผู้ร่ายและพันธมิตร | [เปิดเอกสาร](spells/aqua/blessing_snow.md) |
| Crimson Rain Bathes Moon | หาน หลิงเหวิน (Han Lingwen) | `bhspells:crimson_rain_bathes_moon` | Channel พายุสายฟ้าครามและฝนหอกสีชาด | [เปิดเอกสาร](spells/aqua/crimson_rain_bathes_moon.md) |
| Crystal Hydro Dome | - | `bhspells:crystal_hydro_dome` | กางโดมคริสตัลวารีป้องกันการโจมตีจากภายนอกและกักขังศัตรู | [เปิดเอกสาร](spells/aqua/crystal_hydro_dome.md) |
| Glacial Firmament | ปิงเยว่ (Bingyue) | `bhspells:glacial_firmament` | สร้าง ice domain และวงหนาม/สุสานน้ำแข็งหลายชั้น | [เปิดเอกสาร](spells/aqua/glacial_firmament.md) |
| Glacial Veil | หยิง ซีหยาง (Ying Xiyang) | `bhspells:glacial_veil` | คลื่นน้ำแข็งและหนามน้ำแข็งแปดทิศรอบผู้ร่าย | [เปิดเอกสาร](spells/aqua/glacial_veil.md) |
| Hazard Area | - | `bhspells:hazard_area` | สร้างพื้นที่อันตรายจากน้ำท่วมขัง ลดความเร็วและสร้างดาเมจน้ำต่อเนื่อง | [เปิดเอกสาร](spells/aqua/hazard_area.md) |
| Star Ice | - | `bhspells:star_ice` | ผลึกน้ำแข็งดวงดาว ยิงสะเก็ดน้ำแข็งแหลมคมแทงทะลุเป้าหมาย | [เปิดเอกสาร](spells/aqua/star_ice.md) |
| Toxic Salvation | ซงหลิน (Song Lin) | `bhspells:toxic_salvation` | แปลงพิษในตัวซงหลิน ให้ระเหยกลายเป็นหมอกพิษ ใส่ Poison 1 แก่ศัตรู และฟื้นฟูเลือด | [เปิดเอกสาร](spells/aqua/toxic_salvation.md) |

## Gold (15 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Amethyst Decree | - | `bhspells:amethyst_decree` | โองการอเมทิสต์ ปลดปล่อยคลื่นมนตรากักขังและสร้างความเสียหายทองคำ | [เปิดเอกสาร](spells/gold/amethyst_decree.md) |
| Gilded Hare | เว่ยลู่เยี่ยน (Wei Luyan) | `bhspells:gilded_hare` | บัฟตนเอง 2 นาที เตะสะสมคอมโบติดริบบิ้นสโลว์ และเตะครบ 5 ครั้งตรึงดักแด้สตั๊นพร้อมหูกระต่าย | [เปิดเอกสาร](spells/gold/gilded_hare.md) |
| Golden Gate | - | `bhspells:golden_gate` | เปิดประตูปฐมทองคำ ปลดปล่อยลำแสงสังหารทำลายล้าง | [เปิดเอกสาร](spells/gold/golden_gate.md) |
| Golden Hand | - | `bhspells:golden_hand` | หัตถ์ทองคำคว้าจับและฟาดเป้าหมายลงกับพื้น | [เปิดเอกสาร](spells/gold/golden_hand.md) |
| Hymn of Purification | เจียง หลิงหยวน (Jiang Lingyuan) | `bhspells:hymn_of_purification` | บรรเลงเพลงทองคำ 20 วินาที ฟื้นฟูเลือดและล้างสถานะผิดปกติทั้งหมด | [เปิดเอกสาร](spells/gold/hymn_of_purification.md) |
| Jade Cluster | - | `bhspells:jade_cluster` | กลุ่มผลึกหยก ระเบิดสะเก็ดหยกแหลมคมรอบตัว | [เปิดเอกสาร](spells/gold/jade_cluster.md) |
| Jade Wave | - | `bhspells:jade_wave` | คลื่นหยกซัดสาด ผลักและลดความเร็วศัตรูในแนวรบ | [เปิดเอกสาร](spells/gold/jade_wave.md) |
| Savage Bite | - | `bhspells:savage_bite` | พุ่งเกาะข้อเท้าเป้าหมายในระยะ 7 บล็อก อนิเมชั่นว่ายน้ำขบกัด ดูดมานาและใส่ Slowness II | [เปิดเอกสาร](spells/gold/savage_bite.md) |
| Shackle of Fear | อ็อตโต (Otto) | `bhspells:shackle_of_fear` | ยิง projectile เพื่อสร้างโซ่หลายเส้นตรึงเป้าหมาย | [เปิดเอกสาร](spells/gold/shackle_of_fear.md) |
| Shaken Monkey | - | `bhspells:shaken_monkey` | วานรสั่นคลอน พุ่งกระแทกศัตรูอย่างคล่องแคล่วและหลบหลีก | [เปิดเอกสาร](spells/gold/shaken_monkey.md) |
| Shining Radiant | - | `bhspells:shining_radiant` | ประกายแสงเจิดจรัส ตาบอดศัตรูและสร้างความเสียหายทองคำ | [เปิดเอกสาร](spells/gold/shining_radiant.md) |
| Sky Eater | - | `bhspells:sky_eater` | กลืนกินนภา อัญเชิญพลังอำนาจทำลายล้างเป้าหมายเบื้องหน้า | [เปิดเอกสาร](spells/gold/sky_eater.md) |
| Stone Crumble | - | `bhspells:stone_crumble` | ศิลากลืนสลาย ทลายการป้องกันและบดขยี้เกราะของศัตรู | [เปิดเอกสาร](spells/gold/stone_crumble.md) |
| Thousand Arrows | - | `bhspells:thousand_arrows` | ฝนธนูทองคำพันดอก ตกกระหน่ำใส่พื้นที่เป้าหมาย | [เปิดเอกสาร](spells/gold/thousand_arrows.md) |
| Wheel of Karma | - | `bhspells:wheel_of_karma` | กงล้อแห่งกรรม วงล้อทองคำหมุนวนสะท้อนดาเมจและลงทัณฑ์ศัตรู | [เปิดเอกสาร](spells/gold/wheel_of_karma.md) |

## Ground (9 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Demonic Spin | - | `bhspells:demonic_spin` | หมุนวนปฐพีปิศาจ ฟาดฟันศัตรูรอบตัวด้วยพลังดิน | [เปิดเอกสาร](spells/ground/demonic_spin.md) |
| Earth Roar | เล่อเปิน (Le Ben) | `bhspells:earth_roar` | Free-aim dash 40 blocks, shield break, 35 damage, lateral outward slide และ universal stun 100 ticks | [เปิดเอกสาร](spells/ground/earth_roar.md) |
| Embracing Bosom | - | `bhspells:embracing_bosom` | อ้อมกอดแห่งธรณี ตรึงเป้าหมายแนบพื้นดินและป้องกันการหลบหนี | [เปิดเอกสาร](spells/ground/embracing_bosom.md) |
| Feet Stomp | - | `bhspells:feet_stomp` | กระทืบเท้าสะเทือนปฐพี ปล่อยคลื่นกระแทกทำลายสมดุลศัตรูรอบตัว | [เปิดเอกสาร](spells/ground/feet_stomp.md) |
| Intrusion Chain Buff | - | `bhspells:intrusion_chain_buff` | โซ่ปฐพีเชื่อมโยงพันธมิตร แบ่งเบาความเสียหายและมอบบัฟป้องกัน | [เปิดเอกสาร](spells/ground/intrusion_chain_buff.md) |
| Intrusion Chain Debuff | - | `bhspells:intrusion_chain_debuff` | โซ่ปฐพีล่ามศัตรู ดูดกลืนพลังและลดทอนความเร็ว | [เปิดเอกสาร](spells/ground/intrusion_chain_debuff.md) |
| Jade Aura | มู่หรง เฟิงอี้ (Murong Fengyi) | `bhspells:jade_aura` | ออร่าหยกบัฟตนเอง/เพื่อนร่วมทีม เพิ่มความเร็ว โจมตีเร็ว และฟื้นฟูเลือด | [เปิดเอกสาร](spells/ground/jade_aura.md) |
| Shocking | ลู่ ฮวา (Lu Hwa) | `bhspells:shocking` | ปล่อยลำแสงสายฟ้ามรกตเจาะทะลวง 8 บล็อก พร้อมดีบัฟสโลว์และ DoT | [เปิดเอกสาร](spells/ground/shocking.md) |
| Tigershade Terrabreak | มู่ หลิงเยว่ (Mu Lingyue) | `bhspells:tigershade_terrabreak` | Mark เป้าหมาย รับบัฟ stance และ recast เพื่อพุ่งทุบทำ damage ในระยะ 5 บล็อก | [เปิดเอกสาร](spells/ground/tigershade_terrabreak.md) |

## Evocation (3 Spells)

| เวท | ตัวละคร | Registry ID | บทบาทหลัก | Specification |
| --- | --- | --- | --- | --- |
| Ding Shen Fa | ซุนหงอคง (Sun Wukong) / 定身法 | `bhspells:ding_shen_fa` | สะกดตรึงร่างและห้ามกระทำการของศัตรูในระยะสายตา | [เปิดเอกสาร](spells/evocation/ding_shen_fa.md) |
| Spiritual Pressure | แรงดันวิญญาณ / 灵压 | `bhspells:spiritual_pressure` | ปลดปล่อยสนามแรงดันวิญญาณ 16-24 บล็อก ตรึงร่าง ห้ามกระโดด และบีบอัดหน้าจอ | [เปิดเอกสาร](spells/evocation/spiritual_pressure.md) |
| Vengeful Pressure | แรงดันวิญญาณอาฆาต / 怨灵威压 | `bhspells:vengeful_pressure` | ปลดปล่อยอาณาเขตแรงดันวิญญาณสีเขียวมรกต 64-120 บล็อก ฉีกเกราะ ลดพลังโจมตี ห้ามกระโดด | [เปิดเอกสาร](spells/evocation/vengeful_pressure.md) |

