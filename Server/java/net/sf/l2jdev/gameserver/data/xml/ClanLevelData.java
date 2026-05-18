package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ClanLevelData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ClanLevelData.class.getName());
	public static final int EXPECTED_CLAN_LEVEL_DATA = 12;
	private int[] CLAN_EXP;
	private int MAX_CLAN_LEVEL = 0;
	private int MAX_CLAN_EXP = 0;

	protected ClanLevelData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this.CLAN_EXP = new int[12];
		this.MAX_CLAN_LEVEL = 0;
		this.MAX_CLAN_EXP = 0;
		this.parseDatapackFile("data/ClanLevelData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded 11 clan level data.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		for (Node n = document.getFirstChild(); n != null; n = n.getNextSibling())
		{
			if ("list".equals(n.getNodeName()))
			{
				for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
				{
					if ("clan".equals(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						int level = this.parseInteger(attrs, "level");
						int exp = this.parseInteger(attrs, "exp");
						if (this.MAX_CLAN_LEVEL < level)
						{
							this.MAX_CLAN_LEVEL = level;
						}

						if (this.MAX_CLAN_EXP < exp)
						{
							this.MAX_CLAN_EXP = exp;
						}

						this.CLAN_EXP[level] = exp;
					}
				}
			}
		}
	}

	public int getLevelExp(int clanLevel)
	{
		return this.CLAN_EXP[clanLevel];
	}

	public int getMaxLevel()
	{
		return this.MAX_CLAN_LEVEL;
	}

	public int getMaxExp()
	{
		return this.MAX_CLAN_EXP;
	}

	public static ClanLevelData getInstance()
	{
		return ClanLevelData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ClanLevelData INSTANCE = new ClanLevelData();
	}
}
