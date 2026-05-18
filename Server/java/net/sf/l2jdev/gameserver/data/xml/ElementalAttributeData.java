package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.AttributeSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.ElementalItemHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.enums.creature.AttributeType;
import net.sf.l2jdev.gameserver.model.item.enums.ElementalItemType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import org.w3c.dom.Document;

public class ElementalAttributeData implements IXmlReader
{
	private static final Map<Integer, ElementalItemHolder> ELEMENTAL_ITEMS = new HashMap<>();
	public static final int FIRST_WEAPON_BONUS = 20;
	public static final int NEXT_WEAPON_BONUS = 5;
	public static final int ARMOR_BONUS = 6;
	public static final int[] WEAPON_VALUES = new int[]
	{
		0,
		25,
		75,
		150,
		175,
		225,
		300,
		325,
		375,
		450,
		475,
		525,
		600,
		Integer.MAX_VALUE
	};
	public static final int[] ARMOR_VALUES = new int[]
	{
		0,
		12,
		30,
		60,
		72,
		90,
		120,
		132,
		150,
		180,
		192,
		210,
		240,
		Integer.MAX_VALUE
	};
	private static final int[][] CHANCE_TABLE = new int[][]
	{
		{
			AttributeSystemConfig.S_WEAPON_STONE,
			AttributeSystemConfig.S_ARMOR_STONE,
			AttributeSystemConfig.S_WEAPON_CRYSTAL,
			AttributeSystemConfig.S_ARMOR_CRYSTAL,
			AttributeSystemConfig.S_WEAPON_STONE_SUPER,
			AttributeSystemConfig.S_ARMOR_STONE_SUPER,
			AttributeSystemConfig.S_WEAPON_CRYSTAL_SUPER,
			AttributeSystemConfig.S_ARMOR_CRYSTAL_SUPER,
			AttributeSystemConfig.S_WEAPON_JEWEL,
			AttributeSystemConfig.S_ARMOR_JEWEL
		},
		{
			AttributeSystemConfig.S80_WEAPON_STONE,
			AttributeSystemConfig.S80_ARMOR_STONE,
			AttributeSystemConfig.S80_WEAPON_CRYSTAL,
			AttributeSystemConfig.S80_ARMOR_CRYSTAL,
			AttributeSystemConfig.S80_WEAPON_STONE_SUPER,
			AttributeSystemConfig.S80_ARMOR_STONE_SUPER,
			AttributeSystemConfig.S80_WEAPON_CRYSTAL_SUPER,
			AttributeSystemConfig.S80_ARMOR_CRYSTAL_SUPER,
			AttributeSystemConfig.S80_WEAPON_JEWEL,
			AttributeSystemConfig.S80_ARMOR_JEWEL
		},
		{
			AttributeSystemConfig.S84_WEAPON_STONE,
			AttributeSystemConfig.S84_ARMOR_STONE,
			AttributeSystemConfig.S84_WEAPON_CRYSTAL,
			AttributeSystemConfig.S84_ARMOR_CRYSTAL,
			AttributeSystemConfig.S84_WEAPON_STONE_SUPER,
			AttributeSystemConfig.S84_ARMOR_STONE_SUPER,
			AttributeSystemConfig.S84_WEAPON_CRYSTAL_SUPER,
			AttributeSystemConfig.S84_ARMOR_CRYSTAL_SUPER,
			AttributeSystemConfig.S84_WEAPON_JEWEL,
			AttributeSystemConfig.S84_ARMOR_JEWEL
		},
		{
			AttributeSystemConfig.R_WEAPON_STONE,
			AttributeSystemConfig.R_ARMOR_STONE,
			AttributeSystemConfig.R_WEAPON_CRYSTAL,
			AttributeSystemConfig.R_ARMOR_CRYSTAL,
			AttributeSystemConfig.R_WEAPON_STONE_SUPER,
			AttributeSystemConfig.R_ARMOR_STONE_SUPER,
			AttributeSystemConfig.R_WEAPON_CRYSTAL_SUPER,
			AttributeSystemConfig.R_ARMOR_CRYSTAL_SUPER,
			AttributeSystemConfig.R_WEAPON_JEWEL,
			AttributeSystemConfig.R_ARMOR_JEWEL
		},
		{
			AttributeSystemConfig.R95_WEAPON_STONE,
			AttributeSystemConfig.R95_ARMOR_STONE,
			AttributeSystemConfig.R95_WEAPON_CRYSTAL,
			AttributeSystemConfig.R95_ARMOR_CRYSTAL,
			AttributeSystemConfig.R95_WEAPON_STONE_SUPER,
			AttributeSystemConfig.R95_ARMOR_STONE_SUPER,
			AttributeSystemConfig.R95_WEAPON_CRYSTAL_SUPER,
			AttributeSystemConfig.R95_ARMOR_CRYSTAL_SUPER,
			AttributeSystemConfig.R95_WEAPON_JEWEL,
			AttributeSystemConfig.R95_ARMOR_JEWEL
		},
		{
			AttributeSystemConfig.R99_WEAPON_STONE,
			AttributeSystemConfig.R99_ARMOR_STONE,
			AttributeSystemConfig.R99_WEAPON_CRYSTAL,
			AttributeSystemConfig.R99_ARMOR_CRYSTAL,
			AttributeSystemConfig.R99_WEAPON_STONE_SUPER,
			AttributeSystemConfig.R99_ARMOR_STONE_SUPER,
			AttributeSystemConfig.R99_WEAPON_CRYSTAL_SUPER,
			AttributeSystemConfig.R99_ARMOR_CRYSTAL_SUPER,
			AttributeSystemConfig.R99_WEAPON_JEWEL,
			AttributeSystemConfig.R99_ARMOR_JEWEL
		}
	};

	protected ElementalAttributeData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		ELEMENTAL_ITEMS.clear();
		this.parseDatapackFile("data/ElementalAttributeData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + ELEMENTAL_ITEMS.size() + " elemental attribute items.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "item", itemNode -> {
			StatSet set = new StatSet(this.parseAttributes(itemNode));
			int id = set.getInt("id");
			if (ItemData.getInstance().getTemplate(id) == null)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Could not find item with id " + id + ".");
			}
			else
			{
				ELEMENTAL_ITEMS.put(id, new ElementalItemHolder(id, set.getEnum("elemental", AttributeType.class), set.getEnum("type", ElementalItemType.class), set.getInt("power", 0)));
			}
		}));
	}

	public AttributeType getItemElement(int itemId)
	{
		ElementalItemHolder item = ELEMENTAL_ITEMS.get(itemId);
		return item != null ? item.getElement() : AttributeType.NONE;
	}

	public ElementalItemHolder getItemElemental(int itemId)
	{
		return ELEMENTAL_ITEMS.get(itemId);
	}

	public int getMaxElementLevel(int itemId)
	{
		ElementalItemHolder item = ELEMENTAL_ITEMS.get(itemId);
		return item != null ? item.getType().getMaxLevel() : -1;
	}

	public boolean isSuccess(Item item, int stoneId)
	{
		int row = -1;
		int column = -1;
		switch (item.getTemplate().getCrystalType())
		{
			case S:
				row = 0;
				break;
			case S80:
				row = 1;
				break;
			case S84:
				row = 2;
				break;
			case R:
				row = 3;
				break;
			case R95:
				row = 4;
				break;
			case R99:
				row = 5;
		}

		switch (ELEMENTAL_ITEMS.get(stoneId).getType())
		{
			case STONE:
				column = item.isWeapon() ? 0 : 1;
				break;
			case CRYSTAL:
				column = item.isWeapon() ? 2 : 3;
				break;
			case STONE_SUPER:
				column = item.isWeapon() ? 4 : 5;
				break;
			case CRYSTAL_SUPER:
				column = item.isWeapon() ? 6 : 7;
				break;
			case JEWEL:
				column = item.isWeapon() ? 8 : 9;
		}

		return row != -1 && column != -1 ? Rnd.get(100) < CHANCE_TABLE[row][column] : true;
	}

	public static ElementalAttributeData getInstance()
	{
		return ElementalAttributeData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ElementalAttributeData INSTANCE = new ElementalAttributeData();
	}
}
