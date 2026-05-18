package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.data.holders.CollectionDataHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class CollectionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(CollectionData.class.getName());
	private final Map<Integer, CollectionDataHolder> _collections = new HashMap<>();
	private final Map<Integer, List<CollectionDataHolder>> _collectionsByTabId = new HashMap<>();

	protected CollectionData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._collections.clear();
		this.parseDatapackFile("data/CollectionData.xml");
		if (!this._collections.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._collections.size() + " collections.");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": System is disabled.");
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
					if ("collection".equalsIgnoreCase(d.getNodeName()))
					{
						NamedNodeMap attrs = d.getAttributes();
						StatSet set = new StatSet();

						for (int i = 0; i < attrs.getLength(); i++)
						{
							Node att = attrs.item(i);
							set.set(att.getNodeName(), att.getNodeValue());
						}

						int id = this.parseInteger(attrs, "id");
						int optionId = this.parseInteger(attrs, "optionId");
						int category = this.parseInteger(attrs, "category");
						int completeCount = this.parseInteger(attrs, "completeCount");
						List<ItemEnchantHolder> items = new ArrayList<>();

						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							attrs = b.getAttributes();
							if ("item".equalsIgnoreCase(b.getNodeName()))
							{
								int itemId = this.parseInteger(attrs, "id");
								long itemCount = this.parseLong(attrs, "count", 1L);
								int itemEnchantLevel = this.parseInteger(attrs, "enchantLevel", 0);
								ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
								if (item == null)
								{
									LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + itemId + " collection item: " + id);
								}
								else
								{
									items.add(new ItemEnchantHolder(itemId, itemCount, itemEnchantLevel));
								}
							}
						}

						CollectionDataHolder template = new CollectionDataHolder(id, optionId, category, completeCount, items);
						this._collections.put(id, template);
						this._collectionsByTabId.computeIfAbsent(template.getCategory(), _ -> new ArrayList<>()).add(template);
					}
				}
			}
		}
	}

	public CollectionDataHolder getCollection(int id)
	{
		return this._collections.get(id);
	}

	public List<CollectionDataHolder> getCollectionsByTabId(int tabId)
	{
		return this._collectionsByTabId.containsKey(tabId) ? this._collectionsByTabId.get(tabId) : Collections.emptyList();
	}

	public Collection<CollectionDataHolder> getCollections()
	{
		return this._collections.values();
	}

	public static CollectionData getInstance()
	{
		return CollectionData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CollectionData INSTANCE = new CollectionData();
	}
}
