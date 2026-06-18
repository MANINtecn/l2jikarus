"""
Gera o HuntPass.xml do L2 Ikarus (estreia).
AS DUAS trilhas ajudam:
 - FREE (todos): Random Craft + buff scrolls + potions + Sayha Points + bau bom no fim.
 - PREMIUM (passe): consumiveis melhores + buff scrolls + Random Craft + skins
   (transformacao) + ajuda de skills basicas. Final = Master Skill Book 3rd Class.
SEM grade-A, joias boss ou lifestones.
"""

PAGES = 28
STEPS = 10
SAYHA = 72286
CRAFT_MATS = [92908, 92910, 92911, 92913, 92914]  # Wood, Rags, Leather, Metal, Sack

# ---------- FREE (passos 1-9). None = material de craft (rotaciona). ----------
ROTATION_FREE = [
    (90907, 10),   # 1 Soulshot Ticket
    (49674, 1),    # 2 XP Scroll
    (None, 5),     # 3 Random Craft material
    (91690, 5),    # 4 Special HP Recovery Potion
    (SAYHA, 40),   # 5 Sayha Points
    (94269, 1),    # 6 Boost Attack Scroll
    (3031, 1000),  # 7 Spirit Ore
    (None, 5),     # 8 Random Craft material (outro)
    (94271, 1),    # 9 Boost Defense Scroll
]
# FREE marco (passo 10): por padrao bundle de craft; paginas 7/14/21 dao Spellbook
# Chest 1 Star (ajuda skill); pagina 28 da um bau bom (Master Skill Book 2nd Class).
FREE_SKILL_PAGES = {7, 14, 21}
FREE_FINAL = (49699, 1, "BAU FREE: Master's Skill Book - 2nd Class")

# ---------- PREMIUM (passos 1-9). None = material de craft (rotaciona). ----------
ROTATION_PREM = [
    (90907, 30),   # 1 Soulshot Ticket
    (49674, 3),    # 2 XP Scroll
    (94269, 3),    # 3 Boost Attack Scroll
    (91690, 10),   # 4 Special HP Recovery Potion
    (3031, 3000),  # 5 Spirit Ore
    (94271, 3),    # 6 Boost Defense Scroll
    (96729, 2),    # 7 Einhasad Protection
    (91641, 3),    # 8 Sayha Potion
    (None, 10),    # 9 Random Craft material
]
# PREMIUM marco (passo 10) por pagina.
PREM_MILESTONES = {
    1:  (71023, 1,  "Skin: Teddy Bear"),
    2:  (92908, 30, "Random Craft: Lump of Wood (bundle)"),
    3:  (100979, 1, "Spellbook Chest - 1 Star"),
    4:  (71025, 1,  "Skin: Cat"),
    5:  (100678, 1, "+1 Skill Enchant Coupon"),
    6:  (92910, 30, "Random Craft: Rags (bundle)"),
    7:  (71024, 1,  "Skin: Panda"),
    8:  (100981, 1, "Spellbook Chest - 2 Stars"),
    9:  (91076, 5,  "Venir Talisman"),
    10: (70353, 1,  "Skin: Pirate"),
    11: (92911, 30, "Random Craft: Leather Scraps (bundle)"),
    12: (100679, 1, "+2 Skill Enchant Coupon"),
    13: (70354, 1,  "Skin: Dark Assassin"),
    14: (49699, 1,  "Master's Skill Book - 2nd Class"),
    15: (100983, 1, "Spellbook Chest - 3 Stars"),
    16: (92913, 30, "Random Craft: Unknown Metal (bundle)"),
    17: (70355, 1,  "Skin: White Assassin"),
    18: (100680, 1, "+3 Skill Enchant Coupon"),
    19: (94732, 4,  "Agathion Coupon"),
    20: (72485, 1,  "Skin: Formal Wear"),
    21: (92914, 30, "Random Craft: Sack Shreds (bundle)"),
    22: (100983, 1, "Spellbook Chest - 3 Stars"),
    23: (72456, 1,  "Skin: Rudolph"),
    24: (100680, 1, "+3 Skill Enchant Coupon"),
    25: (92911, 50, "Random Craft: Leather Scraps (bundle grande)"),
    26: (72388, 1,  "Skin: Swimsuit"),
    27: (100983, 1, "Spellbook Chest - 3 Stars"),
    28: (90866, 1,  "FINAL PREMIUM: Master's Skill Book - 3rd Class (skill master aleatoria)"),
}

NAMES = {
    90907: "Soulshot Ticket", 49674: "XP Scroll", 94269: "Boost Attack Scroll",
    91690: "Special HP Recovery", 3031: "Spirit Ore", 94271: "Boost Defense Scroll",
    96729: "Einhasad Protection", 91641: "Sayha Potion", SAYHA: "Sayha Points",
    92908: "Mat: Wood", 92910: "Mat: Rags", 92911: "Mat: Leather",
    92913: "Mat: Metal", 92914: "Mat: Sack", 100979: "Spellbook Chest 1 Star",
    49699: "Master Skill Book 2nd",
}

def craft_mat(page, slot):
    return CRAFT_MATS[(page - 1 + slot) % len(CRAFT_MATS)]

def free_step(page, step):
    if step == STEPS:
        if page == PAGES:
            return FREE_FINAL
        if page in FREE_SKILL_PAGES:
            return (100979, 1, "Spellbook Chest - 1 Star (free)")
        mat = craft_mat(page, 4)
        return (mat, 15, f"Random Craft bundle ({NAMES.get(mat,'mat')})")
    item, count = ROTATION_FREE[step - 1]
    if item is None:
        item = craft_mat(page, step)
    return (item, count, NAMES.get(item, ""))

def prem_step(page, step):
    if step == STEPS:
        return PREM_MILESTONES[page]
    item, count = ROTATION_PREM[step - 1]
    if item is None:
        item = craft_mat(page, step)
    return (item, count, NAMES.get(item, ""))

lines = ['<?xml version="1.0" encoding="UTF-8"?>',
         '<list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="xsd/HuntPass.xsd">']

for p in range(1, PAGES + 1):
    lines.append(f"\t<!-- Pagina {p} -->")
    for s in range(1, STEPS + 1):
        fid, fcount, fname = free_step(p, s)
        pid, pcount, pname = prem_step(p, s)
        lines.append(
            f'\t<item id="{fid}" count="{fcount}" premiumId="{pid}" premiumCount="{pcount}" /> '
            f'<!-- FREE {fname} / PREM {pname} -->'
        )

lines.append("</list>")
with open("HuntPass.xml", "w", encoding="utf-8") as f:
    f.write("\n".join(lines) + "\n")
print(f"HuntPass.xml gerado: {PAGES}x{STEPS} = {PAGES*STEPS} passos (free + premium uteis).")
