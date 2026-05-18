package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.itemauction.ItemAuctionInstance;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

public class ItemAuctionManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(ItemAuctionManager.class.getName());
	private final Map<Integer, ItemAuctionInstance> _managerInstances = new HashMap<>();
	private final AtomicInteger _auctionIds = new AtomicInteger(1);

	protected ItemAuctionManager()
	{
		if (!GeneralConfig.ALT_ITEM_AUCTION_ENABLED)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Disabled.");
		}
		else
		{
			try (Connection con = DatabaseFactory.getConnection(); Statement statement = con.createStatement(); ResultSet rset = statement.executeQuery("SELECT auctionId FROM item_auction ORDER BY auctionId DESC LIMIT 0, 1");)
			{
				if (rset.next())
				{
					this._auctionIds.set(rset.getInt(1) + 1);
				}
			}
			catch (SQLException var12)
			{
				LOGGER.log(Level.SEVERE, "Failed loading auctions.", var12);
			}

			this.load();
		}
	}

	@Override
	public void load()
	{
		this._managerInstances.clear();
		this.parseDatapackFile("data/ItemAuctions.xml");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._managerInstances.size() + " instances.");
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		try
		{
			for (Node na = document.getFirstChild(); na != null; na = na.getNextSibling())
			{
				if ("list".equalsIgnoreCase(na.getNodeName()))
				{
					for (Node nb = na.getFirstChild(); nb != null; nb = nb.getNextSibling())
					{
						if ("instance".equalsIgnoreCase(nb.getNodeName()))
						{
							NamedNodeMap nab = nb.getAttributes();
							int instanceId = Integer.parseInt(nab.getNamedItem("id").getNodeValue());
							if (this._managerInstances.containsKey(instanceId))
							{
								throw new Exception("Dublicated instanceId " + instanceId);
							}

							ItemAuctionInstance instance = new ItemAuctionInstance(instanceId, this._auctionIds, nb);
							this._managerInstances.put(instanceId, instance);
						}
					}
				}
			}
		}
		catch (Exception var8)
		{
			LOGGER.log(Level.SEVERE, this.getClass().getSimpleName() + ": Failed loading auctions from xml.", var8);
		}
	}

	public void shutdown()
	{
		for (ItemAuctionInstance instance : this._managerInstances.values())
		{
			instance.shutdown();
		}
	}

	public ItemAuctionInstance getManagerInstance(int instanceId)
	{
		return this._managerInstances.get(instanceId);
	}

	public int getNextAuctionId()
	{
		return this._auctionIds.getAndIncrement();
	}

	public static void deleteAuction(int auctionId)
	{
		try (Connection con = DatabaseFactory.getConnection())
		{
			try (PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction WHERE auctionId=?"))
			{
				statement.setInt(1, auctionId);
				statement.execute();
			}

			try (PreparedStatement statement = con.prepareStatement("DELETE FROM item_auction_bid WHERE auctionId=?"))
			{
				statement.setInt(1, auctionId);
				statement.execute();
			}
		}
		catch (SQLException var11)
		{
			LOGGER.log(Level.SEVERE, "ItemAuctionManagerInstance: Failed deleting auction: " + auctionId, var11);
		}
	}

	public static ItemAuctionManager getInstance()
	{
		return ItemAuctionManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ItemAuctionManager INSTANCE = new ItemAuctionManager();
	}
}
