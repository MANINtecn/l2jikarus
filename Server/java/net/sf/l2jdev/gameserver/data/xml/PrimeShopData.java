package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.primeshop.PrimeShopGroup;
import net.sf.l2jdev.gameserver.model.primeshop.PrimeShopItem;
import net.sf.l2jdev.gameserver.network.serverpackets.primeshop.ExBRProductInfo;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class PrimeShopData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(PrimeShopData.class.getName());
	private final Map<Integer, PrimeShopGroup> _primeItems = new LinkedHashMap<>();

	protected PrimeShopData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._primeItems.clear();
		this.parseDatapackFile("data/PrimeShop.xml");
		if (!this._primeItems.isEmpty())
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._primeItems.size() + " items.");
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
				NamedNodeMap at = n.getAttributes();
				Node attribute = at.getNamedItem("enabled");
				if (attribute != null && Boolean.parseBoolean(attribute.getNodeValue()))
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

							List<PrimeShopItem> items = new ArrayList<>();

							for (Node b = d.getFirstChild(); b != null; b = b.getNextSibling())
							{
								if ("item".equalsIgnoreCase(b.getNodeName()))
								{
									attrs = b.getAttributes();
									int itemId = this.parseInteger(attrs, "itemId");
									int count = this.parseInteger(attrs, "count");
									ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
									if (item == null)
									{
										LOGGER.severe(this.getClass().getSimpleName() + ": Item template null for itemId: " + itemId + " brId: " + set.getInt("id"));
										return;
									}

									items.add(new PrimeShopItem(itemId, count, item.getWeight(), item.isTradeable() ? 1 : 0));
								}
							}

							this._primeItems.put(set.getInt("id"), new PrimeShopGroup(set, items));
						}
					}
				}
			}
		}
	}

	public void showProductInfo(Player player, int productId)
	{
		PrimeShopGroup item = this._primeItems.get(productId);
		if (player != null && item != null)
		{
			player.sendPacket(new ExBRProductInfo(item, player));
		}
	}

	public PrimeShopGroup getItem(int productId)
	{
		return this._primeItems.get(productId);
	}

	public Map<Integer, PrimeShopGroup> getPrimeItems()
	{
		return this._primeItems;
	}

	public static PrimeShopData getInstance()
	{
		return PrimeShopData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final PrimeShopData INSTANCE = new PrimeShopData();
	}
}
