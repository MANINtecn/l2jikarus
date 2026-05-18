package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.MagicLampDataHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class MagicLampData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MagicLampData.class.getName());
	private static final List<MagicLampDataHolder> LAMPS = new ArrayList<>();

	protected MagicLampData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		LAMPS.clear();
		this.parseDatapackFile("data/MagicLampData.xml");
		LOGGER.info("MagicLampData: Loaded " + LAMPS.size() + " magic lamps exp types.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		NodeList list = document.getFirstChild().getChildNodes();

		for (int i = 0; i < list.getLength(); i++)
		{
			Node n = list.item(i);
			if ("levelRange".equalsIgnoreCase(n.getNodeName()))
			{
				int minLevel = this.parseInteger(n.getAttributes(), "fromLevel");
				int maxLevel = this.parseInteger(n.getAttributes(), "toLevel");
				NodeList lamps = n.getChildNodes();

				for (int j = 0; j < lamps.getLength(); j++)
				{
					Node d = lamps.item(j);
					if ("lamp".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						StatSet set = new StatSet();
						set.set("type", this.parseString(attrs, "type"));
						set.set("exp", this.parseInteger(attrs, "exp"));
						set.set("sp", this.parseInteger(attrs, "sp"));
						set.set("chance", this.parseInteger(attrs, "chance"));
						set.set("minLevel", minLevel);
						set.set("maxLevel", maxLevel);
						LAMPS.add(new MagicLampDataHolder(set));
					}
				}
			}
		}
	}

	public List<MagicLampDataHolder> getLamps()
	{
		return LAMPS;
	}

	public static MagicLampData getInstance()
	{
		return MagicLampData.Singleton.INSTANCE;
	}

	private static class Singleton
	{
		protected static final MagicLampData INSTANCE = new MagicLampData();
	}
}
