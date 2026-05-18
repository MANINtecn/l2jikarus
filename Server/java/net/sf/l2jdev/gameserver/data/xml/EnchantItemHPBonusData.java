package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.CrystalType;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

public class EnchantItemHPBonusData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(EnchantItemHPBonusData.class.getName());
	public static final float FULL_ARMOR_MODIFIER = 1.5F;
	private final Map<CrystalType, List<Integer>> _armorHPBonuses = new EnumMap<>(CrystalType.class);

	protected EnchantItemHPBonusData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._armorHPBonuses.clear();
		this.parseDatapackFile("data/stats/enchantHPBonus.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._armorHPBonuses.size() + " enchant HP bonuses.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equalsIgnoreCase(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("enchantHP".equalsIgnoreCase(d.getNodeName()))
					{
						List<Integer> bonuses = new ArrayList<>(12);

						for (Node e = d.getFirstChild(); e != null; e = e.getNextSibling())
						{
							if ("bonus".equalsIgnoreCase(e.getNodeName()))
							{
								bonuses.add(Integer.parseInt(e.getTextContent()));
							}
						}

						this._armorHPBonuses.put(this.parseEnum(d.getAttributes(), CrystalType.class, "grade"), bonuses);
					}
				}
			}
		}
	}

	public int getHPBonus(Item item)
	{
		List<Integer> values = this._armorHPBonuses.get(item.getTemplate().getCrystalTypePlus());
		if (values != null && !values.isEmpty() && item.getOlyEnchantLevel() > 0)
		{
			int bonus = values.get(Math.min(item.getOlyEnchantLevel(), values.size()) - 1);
			return item.getTemplate().getBodyPart() == BodyPart.FULL_ARMOR ? (int) (bonus * 1.5F) : bonus;
		}
		return 0;
	}

	public static EnchantItemHPBonusData getInstance()
	{
		return EnchantItemHPBonusData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final EnchantItemHPBonusData INSTANCE = new EnchantItemHPBonusData();
	}
}
