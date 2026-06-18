"""
Gera o HuntPass.xml do L2 Ikarus (estreia).
Free = Sayha Points (72286). Premium = consumiveis + buff scrolls + materiais de
Random Craft + skins (transformacao) + ajuda de skills basicas. Marco por pagina.
Final = Master's Skill Book 3rd Class (skill master aleatoria). SEM grade-A, joias
boss ou lifestones.
"""

PAGES = 28
STEPS = 10

SAYHA = 72286  # free reward (Sayha Points)

# Rotacao premium dos passos 1-9 (item, count). O passo 9 (craft) e' sobrescrito por pagina.
ROTATION = [
    (90907, 30),   # 1 Soulshot Ticket
    (49674, 3),    # 2 XP Scroll
    (94269, 3),    # 3 Boost Attack Scroll
    (91690, 10),   # 4 Special HP Recovery Potion
    (3031, 3000),  # 5 Spirit Ore
    (94271, 3),    # 6 Boost Defense Scroll
    (96729, 2),    # 7 Einhasad Protection
    (91641, 3),    # 8 Sayha Potion
    (92908, 10),   # 9 Random Craft material (sobrescrito por pagina)
]

# Material de Random Craft que rotaciona no passo 9, por pagina.
CRAFT_MATS = [92908, 92910, 92911, 92913, 92914]  # Wood, Rags, Leather, Metal, Sack

# Marco (passo 10) por pagina: (itemId, count, comentario)
MILESTONES = {
    1:  (71023, 1,  "Skin: Transformation Teddy Bear"),
    2:  (92908, 30, "Random Craft: Lump of Wood (bundle)"),
    3:  (100979, 1, "Spellbook Chest - 1 Star (skills basicas)"),
    4:  (71025, 1,  "Skin: Transformation Cat"),
    5:  (100678, 1, "Guaranteed +1 Skill Enchant Coupon"),
    6:  (92910, 30, "Random Craft: Rags (bundle)"),
    7:  (71024, 1,  "Skin: Transformation Panda"),
    8:  (100981, 1, "Spellbook Chest - 2 Stars"),
    9:  (91076, 5,  "Venir Talisman"),
    10: (70353, 1,  "Skin: Transformation Pirate"),
    11: (92911, 30, "Random Craft: Leather Scraps (bundle)"),
    12: (100679, 1, "Guaranteed +2 Skill Enchant Coupon"),
    13: (70354, 1,  "Skin: Transformation Dark Assassin"),
    14: (49699, 1,  "Master's Skill Book - 2nd Class (marco)"),
    15: (100983, 1, "Spellbook Chest - 3 Stars"),
    16: (92913, 30, "Random Craft: Unknown Metal (bundle)"),
    17: (70355, 1,  "Skin: Transformation White Assassin"),
    18: (100680, 1, "Guaranteed +3 Skill Enchant Coupon"),
    19: (94732, 4,  "Agathion Coupon"),
    20: (72485, 1,  "Skin: Transformation Formal Wear"),
    21: (92914, 30, "Random Craft: Sack Shreds (bundle)"),
    22: (100983, 1, "Spellbook Chest - 3 Stars"),
    23: (72456, 1,  "Skin: Transformation Rudolph"),
    24: (100680, 1, "Guaranteed +3 Skill Enchant Coupon"),
    25: (92911, 50, "Random Craft: Leather Scraps (bundle grande)"),
    26: (72388, 1,  "Skin: Transformation Swimsuit"),
    27: (100983, 1, "Spellbook Chest - 3 Stars"),
    28: (90866, 1,  "FINAL: Master's Skill Book - 3rd Class (skill master aleatoria)"),
}

# Nomes pra comentario dos passos regulares.
NAMES = {
    90907: "Soulshot Ticket", 49674: "XP Scroll", 94269: "Boost Attack Scroll",
    91690: "Special HP Recovery Potion", 3031: "Spirit Ore", 94271: "Boost Defense Scroll",
    96729: "Einhasad Protection", 91641: "Sayha Potion",
    92908: "Mat: Lump of Wood", 92910: "Mat: Rags", 92911: "Mat: Leather Scraps",
    92913: "Mat: Unknown Metal", 92914: "Mat: Sack Shreds",
}

lines = ['<?xml version="1.0" encoding="UTF-8"?>',
         '<list xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:noNamespaceSchemaLocation="xsd/HuntPass.xsd">']

for p in range(1, PAGES + 1):
    lines.append(f"\t<!-- Pagina {p} -->")
    for s in range(1, STEPS + 1):
        # Free reward (Sayha Points)
        if p == 1 and s == 1:
            free_count = 200
        elif p == PAGES and s == STEPS:
            free_count = 100
        elif s == STEPS:
            free_count = 60
        else:
            free_count = 30

        if s == STEPS:
            pid, pcount, comment = MILESTONES[p]
        elif s == 9:
            pid = CRAFT_MATS[(p - 1) % len(CRAFT_MATS)]
            pcount = 10
            comment = NAMES.get(pid, "Random Craft material")
        else:
            pid, pcount = ROTATION[s - 1]
            comment = NAMES.get(pid, "")

        lines.append(
            f'\t<item id="{SAYHA}" count="{free_count}" premiumId="{pid}" premiumCount="{pcount}" /> '
            f'<!-- Sayha Points / {comment} -->'
        )

lines.append("</list>")

with open("HuntPass.xml", "w", encoding="utf-8") as f:
    f.write("\n".join(lines) + "\n")

print(f"HuntPass.xml gerado: {PAGES} paginas x {STEPS} passos = {PAGES*STEPS} passos.")
