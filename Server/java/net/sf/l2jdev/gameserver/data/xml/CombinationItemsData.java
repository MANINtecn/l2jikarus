package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItem;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItemReward;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItemType;
import org.w3c.dom.Document;

public class CombinationItemsData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(CombinationItemsData.class.getName());
	private final List<CombinationItem> _items = new ArrayList<>();

	protected CombinationItemsData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._items.clear();
		this.parseDatapackFile("data/CombinationItems.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._items.size() + " combinations.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "item", itemNode -> {
			CombinationItem item = new CombinationItem(new StatSet(this.parseAttributes(itemNode)));
			this.forEach(itemNode, "reward", rewardNode -> {
				int id = this.parseInteger(rewardNode.getAttributes(), "id");
				int count = this.parseInteger(rewardNode.getAttributes(), "count", 1);
				int enchant = this.parseInteger(rewardNode.getAttributes(), "enchant", 0);
				CombinationItemType type = this.parseEnum(rewardNode.getAttributes(), CombinationItemType.class, "type");
				item.addReward(new CombinationItemReward(id, count, type, enchant));
				if (id > 0 && ItemData.getInstance().getTemplate(id) == null)
				{
					LOGGER.info(this.getClass().getSimpleName() + ": Could not find item with id " + id);
				}
			});
			this._items.add(item);
		}));
	}

	public int getLoadedElementsCount()
	{
		return this._items.size();
	}

	public List<CombinationItem> getItems()
	{
		return this._items;
	}

	public CombinationItem getItemsBySlots(int firstSlot, int enchantOne, int secondSlot, int enchantTwo)
	{
		for (CombinationItem item : this._items)
		{
			if (item.getItemOne() == firstSlot && item.getItemTwo() == secondSlot && item.getEnchantOne() == enchantOne && item.getEnchantTwo() == enchantTwo)
			{
				return item;
			}
		}

		return null;
	}

	public List<CombinationItem> getItemsByFirstSlot(int id, int enchantOne)
	{
		List<CombinationItem> result = new ArrayList<>();

		for (CombinationItem item : this._items)
		{
			if (item.getItemOne() == id && item.getEnchantOne() == enchantOne)
			{
				result.add(item);
			}
		}

		return result;
	}

	public static final CombinationItemsData getInstance()
	{
		return CombinationItemsData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CombinationItemsData INSTANCE = new CombinationItemsData();
	}
}
