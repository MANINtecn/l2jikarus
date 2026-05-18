package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RaidDropAnnounceData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RaidDropAnnounceData.class.getName());
	private final Set<Integer> _itemIds = new HashSet<>();

	protected RaidDropAnnounceData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._itemIds.clear();
		this.parseDatapackFile("data/RaidDropAnnounceData.xml");
		if (!this._itemIds.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._itemIds.size() + " raid drop announce data.");
		}
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
					if ("item".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						StatSet set = new StatSet();

						for (int i = 0; i < attrs.getLength(); i++)
						{
							Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}

						int id = this.parseInteger(attrs, "id");
						ItemTemplate item = ItemData.getInstance().getTemplate(id);
						if (item != null)
						{
							this._itemIds.add(id);
						}
						else
						{
							LOGGER.warning(this.getClass().getSimpleName() + ": Could not find item with id: " + id);
						}
					}
				}
			}
		}
	}

	public boolean isAnnounce(int itemId)
	{
		return this._itemIds.contains(itemId);
	}

	public static RaidDropAnnounceData getInstance()
	{
		return RaidDropAnnounceData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RaidDropAnnounceData INSTANCE = new RaidDropAnnounceData();
	}
}
