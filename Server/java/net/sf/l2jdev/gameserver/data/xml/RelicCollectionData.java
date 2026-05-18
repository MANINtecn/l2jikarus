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
import net.sf.l2jdev.gameserver.config.RelicSystemConfig;
import net.sf.l2jdev.gameserver.data.holders.RelicCollectionDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicCollectionInfoHolder;
import net.sf.l2jdev.gameserver.data.holders.RelicDataHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class RelicCollectionData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(RelicCollectionData.class.getName());
	private static final Map<Integer, RelicCollectionDataHolder> RELIC_COLLECTIONS = new HashMap<>();
	private static final Map<Integer, List<RelicCollectionDataHolder>> RELIC_COLLECTION_CATEGORIES = new HashMap<>();

	protected RelicCollectionData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		RELIC_COLLECTIONS.clear();
		if (RelicSystemConfig.RELIC_SYSTEM_ENABLED)
		{
			this.parseDatapackFile("data/RelicCollectionData.xml");
		}

		if (!RELIC_COLLECTIONS.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + RELIC_COLLECTIONS.size() + " relic collections.");
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
					if ("relicCollection".equalsIgnoreCase(d.getNodeName()))
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
						int combatPower = this.parseInteger(attrs, "combatPower", 0);
						List<RelicCollectionInfoHolder> relics = new ArrayList<>();

						for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
						{
							attrs = b.getAttributes();
							if ("relic".equalsIgnoreCase(b.getNodeName()))
							{
								int relicId = this.parseInteger(attrs, "id");
								int enchantLevel = this.parseInteger(attrs, "enchantLevel", 0);
								RelicDataHolder relic = RelicData.getInstance().getRelic(relicId);
								if (relic == null)
								{
									LOGGER.severe(this.getClass().getSimpleName() + ": Relic null for relicId: " + relicId + " relics collection item: " + id);
								}
								else
								{
									relics.add(new RelicCollectionInfoHolder(relicId, enchantLevel));
								}
							}
						}

						RelicCollectionDataHolder template = new RelicCollectionDataHolder(id, optionId, category, completeCount, combatPower, relics);
						RELIC_COLLECTIONS.put(id, template);
						RELIC_COLLECTION_CATEGORIES.computeIfAbsent(template.getCategory(), _ -> new ArrayList<>()).add(template);
					}
				}
			}
		}
	}

	public RelicCollectionDataHolder getRelicCollection(int id)
	{
		return RELIC_COLLECTIONS.get(id);
	}

	public List<RelicCollectionDataHolder> getRelicCategory(int tabId)
	{
		return RELIC_COLLECTION_CATEGORIES.containsKey(tabId) ? RELIC_COLLECTION_CATEGORIES.get(tabId) : Collections.emptyList();
	}

	public Collection<RelicCollectionDataHolder> getRelicCollections()
	{
		return RELIC_COLLECTIONS.values();
	}

	public static RelicCollectionData getInstance()
	{
		return RelicCollectionData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RelicCollectionData INSTANCE = new RelicCollectionData();
	}
}
