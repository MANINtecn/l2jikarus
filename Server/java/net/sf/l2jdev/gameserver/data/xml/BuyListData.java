package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.buylist.Product;
import net.sf.l2jdev.gameserver.model.buylist.ProductList;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;

public class BuyListData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(BuyListData.class.getName());
	private final Map<Integer, ProductList> _buyLists = new ConcurrentHashMap<>();

	protected BuyListData()
	{
		this.load();
	}

	@Override
	public synchronized void load()
	{
		this._buyLists.clear();
		this.parseDatapackDirectory("data/buylists", false);
		if (GeneralConfig.CUSTOM_BUYLIST_LOAD)
		{
			this.parseDatapackDirectory("data/buylists/custom", false);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._buyLists.size() + " buyLists.");

		try (Connection con = DatabaseFactory.getConnection(); Statement statement = con.createStatement(); ResultSet rs = statement.executeQuery("SELECT * FROM `buylists`");)
		{
			while (rs.next())
			{
				int buyListId = rs.getInt("buylist_id");
				int itemId = rs.getInt("item_id");
				long count = rs.getLong("count");
				long nextRestockTime = rs.getLong("next_restock_time");
				ProductList buyList = this.getBuyList(buyListId);
				if (buyList == null)
				{
					LOGGER.warning("BuyList found in database but not loaded from xml! BuyListId: " + buyListId);
				}
				else
				{
					Product product = buyList.getProductByItemId(itemId);
					if (product == null)
					{
						LOGGER.warning("ItemId found in database but not loaded from xml! BuyListId: " + buyListId + " ItemId: " + itemId);
					}
					else if (count < product.getMaxCount())
					{
						product.setCount(count);
						product.restartRestockTask(nextRestockTime);
					}
				}
			}
		}
		catch (Exception var18)
		{
			LOGGER.log(Level.WARNING, "Failed to load buyList data from database.", var18);
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		try
		{
			int buyListId = Integer.parseInt(file.getName().replaceAll(".xml", ""));
			this.forEach(document, "list", list -> {
				int defaultBaseTax = this.parseInteger(list.getAttributes(), "baseTax", 0);
				ProductList buyList = new ProductList(buyListId);
				this.forEach(list, node -> {
					String s0$ = node.getNodeName();
					switch (s0$)
					{
						case "item":
							NamedNodeMap attrs = node.getAttributes();
							int itemId = this.parseInteger(attrs, "id");
							ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
							if (item != null)
							{
								long price = this.parseLong(attrs, "price", -1L);
								long restockDelay = this.parseLong(attrs, "restock_delay", -1L);
								long count = this.parseLong(attrs, "count", -1L);
								int baseTax = this.parseInteger(attrs, "baseTax", defaultBaseTax);
								int sellPrice = item.getReferencePrice() / 2;
								if (GeneralConfig.CORRECT_PRICES && price > -1L && sellPrice > price && buyList.getNpcsAllowed() != null)
								{
									LOGGER.warning("Buy price " + price + " is less than sell price " + sellPrice + " for ItemID:" + itemId + " of buylist " + buyList.getListId() + ".");
									buyList.addProduct(new Product(buyListId, item, sellPrice, restockDelay, count, baseTax));
								}
								else
								{
									buyList.addProduct(new Product(buyListId, item, price, restockDelay, count, baseTax));
								}
							}
							else
							{
								LOGGER.warning("Item not found. BuyList:" + buyListId + " ItemID:" + itemId + " File:" + file);
							}
							break;
						case "npcs":
							this.forEach(node, "npc", npcNode -> buyList.addAllowedNpc(Integer.parseInt(npcNode.getTextContent())));
					}
				});
				this._buyLists.put(buyListId, buyList);
			});
		}
		catch (Exception var4)
		{
			LOGGER.log(Level.WARNING, "Failed to load buyList data from xml File:" + file.getName(), var4);
		}
	}

	@Override
	public boolean isValidXmlFile(File file)
	{
		return file != null && file.isFile() && file.getName().toLowerCase().matches("\\d+\\.xml");
	}

	public ProductList getBuyList(int listId)
	{
		return this._buyLists.get(listId);
	}

	public static BuyListData getInstance()
	{
		return BuyListData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final BuyListData INSTANCE = new BuyListData();
	}
}
