package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.SayuneEntry;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class SayuneData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(SayuneData.class.getName());
	private final Map<Integer, SayuneEntry> _maps = new HashMap<>();

	protected SayuneData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this.parseDatapackFile("data/SayuneData.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._maps.size() + " maps.");
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
					if ("map".equalsIgnoreCase(d.getNodeName()))
					{
						int id = this.parseInteger(d.getAttributes(), "id");
						SayuneEntry map = new SayuneEntry(id);
						this.parseEntries(map, d);
						this._maps.put(map.getId(), map);
					}
				}
			}
		}
	}

	private void parseEntries(SayuneEntry lastEntry, Node n)
	{
		for (Node d = n.getFirstChild(); d != null; d = d.getNextSibling())
		{
			if ("selector".equals(d.getNodeName()) || "choice".equals(d.getNodeName()) || "loc".equals(d.getNodeName()))
			{
				NamedNodeMap attrs = d.getAttributes();
				int id = this.parseInteger(attrs, "id");
				int x = this.parseInteger(attrs, "x");
				int y = this.parseInteger(attrs, "y");
				int z = this.parseInteger(attrs, "z");
				this.parseEntries(lastEntry.addInnerEntry(new SayuneEntry("selector".equals(d.getNodeName()), id, x, y, z)), d);
			}
		}
	}

	public SayuneEntry getMap(int id)
	{
		return this._maps.get(id);
	}

	public Collection<SayuneEntry> getMaps()
	{
		return this._maps.values();
	}

	public static SayuneData getInstance()
	{
		return SayuneData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final SayuneData INSTANCE = new SayuneData();
	}
}
