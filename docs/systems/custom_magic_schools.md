# สถาปัตยกรรมสายเวทมนตร์ใหม่ (Custom Magic Schools Architecture: Gold & Ground)

เอกสารนี้อธิบายรายละเอียดทางเทคนิคของสายเวทมนตร์ใหม่ที่ **BHSpells** พัฒนาขึ้นมาเพิ่มเติมจากระบบพื้นฐานของ Iron’s Spells ‘n Spellbooks ซึ่งได้แก่ **สายทองคำ (Gold School)** และ **สายปฐพี (Ground School)**

---

## 1. การลงทะเบียนสายเวท (School Registration)

สายเวทมนตร์ใหม่ถูกลงทะเบียนในคลาส [`BHSchoolRegistry.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/registry/BHSchoolRegistry.java) โดยเชื่อมต่อเข้ากับ `SchoolRegistry.SCHOOL_REGISTRY_KEY` ของ Iron’s Spells:

```java
public class BHSchoolRegistry {
    public static final ResourceLocation GOLD_RESOURCE = BHSpells.id("gold");
    public static final ResourceLocation GROUND_RESOURCE = BHSpells.id("ground");

    public static final RegistryObject<SchoolType> GOLD = registerSchool(new SchoolType(
            GOLD_RESOURCE,
            BHTags.GOLD_FOCUS,
            Component.translatable("school.bhspells.gold").withStyle(ChatFormatting.GOLD),
            AttributeRegistry.GOLD_SPELL_POWER,
            AttributeRegistry.GOLD_MAGIC_RESIST,
            SoundRegistry.ICE_CAST,
            DamageTypesRegistry.GOLD_MAGIC
    ));

    public static final RegistryObject<SchoolType> GROUND = registerSchool(new SchoolType(
            GROUND_RESOURCE,
            BHTags.GROUND_FOCUS,
            Component.translatable("school.bhspells.ground").withStyle(Style.EMPTY.withColor(0xFF563D2D)),
            AttributeRegistry.GROUND_SPELL_POWER,
            AttributeRegistry.GROUND_MAGIC_RESIST,
            SoundRegistry.NATURE_CAST,
            DamageTypesRegistry.GROUND_MAGIC
    ));
}
```

---

## 2. คุณลักษณะและค่าสถานะประจำสาย (Attributes & Modifiers)

ลงทะเบียนค่าสถานะใน [`AttributeRegistry.java`](file:///d:/Minecraft/Dev/ironspell_more/BHSpells/src/main/java/net/offkung/bhspells/registry/AttributeRegistry.java):

| คุณสมบัติ | สายทองคำ (Gold School) | สายปฐพี (Ground School) |
| :--- | :--- | :--- |
| **School ID** | `bhspells:gold` | `bhspells:ground` |
| **สีประจำสาย** | สีทองสว่าง (`ChatFormatting.GOLD`) | สีกากี/ดินทมิฬ (`0xFF563D2D`) |
| **Focus Tag** | `#bhspells:gold_focus` | `#bhspells:ground_focus` |
| **Spell Power Attribute** | `bhspells:gold_spell_power` | `bhspells:ground_spell_power` |
| **Magic Resist Attribute**| `bhspells:gold_magic_resist`| `bhspells:ground_magic_resist`|
| **Cast Sound Effect** | `SoundRegistry.ICE_CAST` (เสียงคริสตัลดังกังวาน) | `SoundRegistry.NATURE_CAST` (เสียงแผ่นดินไหวคำราม) |
| **Damage Type** | `DamageTypesRegistry.GOLD_MAGIC` | `DamageTypesRegistry.GROUND_MAGIC` |

---

## 3. ความเข้ากันได้และการทำ Fallback (Graceful Fallback)

เนื่องจาก BHSpells รองรับการใช้งานทั้งแบบ Standalone และการเชื่อมต่อกับ Modpack:
- หากรันในสภาพแวดล้อมที่โมดูลสายเวทของ BHSpells ถูกปิดใช้งาน ตัวเวทมนตร์สาย Gold จะ Fallback ไปใช้สาย **Ender** หรือ **Holy** อัตโนมัติ
- ตัวเวทมนตร์สาย Ground จะ Fallback ไปใช้สาย **Evocation** หรือ **Nature** โดยไม่ทำให้เซิร์ฟเวอร์แครช
- ไอเทม Focus และชุดเกราะสามารถคราฟต์เพื่อบูสต์ค่า `Gold/Ground Spell Power` ได้ตามปกติ
