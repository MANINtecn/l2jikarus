package net.sf.l2jdev.gameserver.model.stats;

import java.io.File;
import java.util.NoSuchElementException;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public enum BaseStat
{
	STR(Stat.STAT_STR),
	INT(Stat.STAT_INT),
	DEX(Stat.STAT_DEX),
	WIT(Stat.STAT_WIT),
	CON(Stat.STAT_CON),
	MEN(Stat.STAT_MEN);

	private static final BaseStat[] VALUES = values();
	public static final int MAX_STAT_VALUE = 201;
	private final double[] _bonus = new double[201];
	private final Stat _stat;

	private BaseStat(Stat stat)
	{
		this._stat = stat;
	}

	public Stat getStat()
	{
		return this._stat;
	}

	public int calcValue(Creature creature)
	{
		return creature != null && this._stat != null ? (int) creature.getStat().getValue(this._stat) : 0;
	}

	public double calcBonus(Creature creature)
	{
		if (creature != null)
		{
			int value = this.calcValue(creature);
			return value < 1 ? 1.0 : this._bonus[value];
		}
		return 1.0;
	}

	void setValue(int index, double value)
	{
		this._bonus[index] = value;
	}

	public double getValue(int index)
	{
		return this._bonus[index];
	}

	public static BaseStat valueOf(Stat stat)
	{
		for (BaseStat baseStat : VALUES)
		{
			if (baseStat.getStat() == stat)
			{
				return baseStat;
			}
		}

		throw new NoSuchElementException("Unknown base stat '" + stat + "' for enum BaseStats");
	}

	static
	{
		(new IXmlReader()
		{
			final Logger LOGGER = Logger.getLogger(BaseStat.class.getName());

			@Override
			public void load()
			{
				this.parseDatapackFile("data/stats/statBonus.xml");
			}

			@Override
			public void parseDocument(Document document, File file)
			{
				this.forEach(document, "list", listNode -> this.forEach(listNode, IXmlReader::isNode, statNode -> {
					BaseStat baseStat;
					try
					{
						baseStat = BaseStat.valueOf(statNode.getNodeName());
					}
					catch (Exception var4)
					{
						this.LOGGER.severe("Invalid base stats type: " + statNode.getNodeValue() + ", skipping");
						return;
					}

					this.forEach(statNode, "stat", statValue -> {
						NamedNodeMap attrs = statValue.getAttributes();
						int val = this.parseInteger(attrs, "value");
						double bonus = this.parseDouble(attrs, "bonus");
						baseStat.setValue(val, bonus);
					});
				}));
			}
		}).load();
	}
}
