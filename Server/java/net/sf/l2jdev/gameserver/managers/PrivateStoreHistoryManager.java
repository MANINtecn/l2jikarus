package net.sf.l2jdev.gameserver.managers;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class PrivateStoreHistoryManager
{
	protected static final Logger LOGGER = Logger.getLogger(PrivateStoreHistoryManager.class.getName());
	public static final String SELECT = "SELECT * FROM item_transaction_history";
	public static final String INSERT = "INSERT INTO item_transaction_history (created_time,item_id,transaction_type,enchant_level,price,count) VALUES (?,?,?,?,?,?)";
	public static final String TRUNCATE = "TRUNCATE TABLE item_transaction_history";
	private static final ArrayList<PrivateStoreHistoryManager.ItemHistoryTransaction> _items = new ArrayList<>();

	public void registerTransaction(PrivateStoreType transactionType, Item item, long count, long price)
	{
		try
		{
			PrivateStoreHistoryManager.ItemHistoryTransaction historyItem = new PrivateStoreHistoryManager.ItemHistoryTransaction(transactionType, count, price, item);
			_items.add(historyItem);
		}
		catch (Exception var8)
		{
			LOGGER.log(Level.WARNING, "Could not store history for item: " + item, var8);
		}
	}

	public void restore()
	{
		_items.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM item_transaction_history"); ResultSet rs = statement.executeQuery();)
		{
			while (rs.next())
			{
				PrivateStoreHistoryManager.ItemHistoryTransaction item = new PrivateStoreHistoryManager.ItemHistoryTransaction(rs);
				_items.add(item);
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.WARNING, "Could not restore history.", var12);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + _items.size() + " items history.");
	}

	public void reset()
	{
		_items.clear();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("TRUNCATE TABLE item_transaction_history");)
		{
			statement.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.WARNING, "Could not reset history.", var9);
		}

		LOGGER.info(this.getClass().getSimpleName() + ": weekly reset.");
	}

	public List<PrivateStoreHistoryManager.ItemHistoryTransaction> getHistory()
	{
		return this.getHistory(false);
	}

	public List<PrivateStoreHistoryManager.ItemHistoryTransaction> getHistory(boolean full)
	{
		if (!full)
		{
			List<PrivateStoreHistoryManager.ItemHistoryTransaction> tempList = new ArrayList<>(_items);
			Map<Integer, Integer> uniqueItemIds = new HashMap<>();

			for (PrivateStoreHistoryManager.ItemHistoryTransaction transaction : tempList)
			{
				int itemId = transaction.getItemId();
				if (!uniqueItemIds.containsKey(itemId))
				{
					uniqueItemIds.put(itemId, 0);
				}
			}

			tempList.sort(new PrivateStoreHistoryManager.SortByDate());
			List<PrivateStoreHistoryManager.ItemHistoryTransaction> finalList = new ArrayList<>();

			for (PrivateStoreHistoryManager.ItemHistoryTransaction transactionx : tempList)
			{
				int itemId = transactionx.getItemId();
				if (uniqueItemIds.get(itemId) < GeneralConfig.STORE_REVIEW_LIMIT)
				{
					finalList.add(transactionx);
					uniqueItemIds.put(itemId, uniqueItemIds.get(itemId) + 1);
				}
			}

			return finalList;
		}
		return _items;
	}

	public List<PrivateStoreHistoryManager.ItemHistoryTransaction> getTopHighestItem()
	{
		List<PrivateStoreHistoryManager.ItemHistoryTransaction> list = new ArrayList<>(_items);
		list.sort(new PrivateStoreHistoryManager.SortByPrice());
		return list;
	}

	public List<PrivateStoreHistoryManager.ItemHistoryTransaction> getTopMostItem()
	{
		Map<Integer, PrivateStoreHistoryManager.ItemHistoryTransaction> map = new HashMap<>();

		for (PrivateStoreHistoryManager.ItemHistoryTransaction transaction : _items)
		{
			if (map.get(transaction.getItemId()) == null)
			{
				map.put(transaction.getItemId(), new PrivateStoreHistoryManager.ItemHistoryTransaction(transaction.getTransactionType(), transaction.getCount(), transaction.getPrice(), transaction.getItemId(), 0, false));
			}
			else
			{
				map.get(transaction.getItemId()).addCount(transaction.getCount());
			}
		}

		List<PrivateStoreHistoryManager.ItemHistoryTransaction> list = new ArrayList<>();
		map.forEach((_, transactionx) -> list.add(transactionx));
		list.sort(new PrivateStoreHistoryManager.SortByQuantity());
		return list;
	}

	public static PrivateStoreHistoryManager getInstance()
	{
		return PrivateStoreHistoryManager.SingletonHolder.INSTANCE;
	}

	public static class ItemHistoryTransaction
	{
		private final long _transactionDate;
		private final int _itemId;
		private final PrivateStoreType _transactionType;
		private final int _enchantLevel;
		private final long _price;
		private long _count;

		public ItemHistoryTransaction(ResultSet rs) throws SQLException
		{
			this._transactionDate = rs.getLong("created_time");
			this._itemId = rs.getInt("item_id");
			this._transactionType = rs.getInt("transaction_type") == 0 ? PrivateStoreType.SELL : PrivateStoreType.BUY;
			this._enchantLevel = rs.getInt("enchant_level");
			this._price = rs.getLong("price");
			this._count = rs.getLong("count");
		}

		public ItemHistoryTransaction(PrivateStoreType transactionType, long count, long price, Item item)
		{
			this(transactionType, count, price, item.getId(), item.getEnchantLevel(), true);
		}

		public ItemHistoryTransaction(PrivateStoreType transactionType, long count, long price, int itemId, int enchantLevel, boolean saveToDB)
		{
			this._transactionDate = System.currentTimeMillis();
			this._itemId = itemId;
			this._transactionType = transactionType;
			this._enchantLevel = enchantLevel;
			this._price = price;
			this._count = count;
			if (saveToDB)
			{
				this.storeInDB();
			}
		}

		public long getTransactionDate()
		{
			return this._transactionDate;
		}

		public PrivateStoreType getTransactionType()
		{
			return this._transactionType;
		}

		public int getItemId()
		{
			return this._itemId;
		}

		public int getEnchantLevel()
		{
			return this._enchantLevel;
		}

		public long getPrice()
		{
			return this._price;
		}

		public long getCount()
		{
			return this._count;
		}

		public void addCount(long count)
		{
			this._count += count;
		}

		private void storeInDB()
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO item_transaction_history (created_time,item_id,transaction_type,enchant_level,price,count) VALUES (?,?,?,?,?,?)");)
			{
				ps.setLong(1, this._transactionDate);
				ps.setInt(2, this._itemId);
				ps.setInt(3, this._transactionType == PrivateStoreType.SELL ? 0 : 1);
				ps.setInt(4, this._enchantLevel);
				ps.setLong(5, this._price);
				ps.setLong(6, this._count);
				ps.execute();
			}
			catch (Exception var9)
			{
				PrivateStoreHistoryManager.LOGGER.log(Level.SEVERE, "Could not insert history item " + this + " into DB: Reason: " + var9.getMessage(), var9);
			}
		}

		@Override
		public String toString()
		{
			return this._transactionDate + "(" + this._transactionType + ")[" + this._itemId + " +" + this._enchantLevel + " c:" + this._count + " p:" + this._price + " ]";
		}
	}

	private static class SingletonHolder
	{
		protected static final PrivateStoreHistoryManager INSTANCE = new PrivateStoreHistoryManager();
	}

	protected static class SortByDate implements Comparator<PrivateStoreHistoryManager.ItemHistoryTransaction>
	{
		@Override
		public int compare(PrivateStoreHistoryManager.ItemHistoryTransaction a, PrivateStoreHistoryManager.ItemHistoryTransaction b)
		{
			return a.getTransactionDate() > b.getTransactionDate() ? -1 : (a.getTransactionDate() == b.getTransactionDate() ? 0 : 1);
		}
	}

	protected static class SortByPrice implements Comparator<PrivateStoreHistoryManager.ItemHistoryTransaction>
	{
		@Override
		public int compare(PrivateStoreHistoryManager.ItemHistoryTransaction a, PrivateStoreHistoryManager.ItemHistoryTransaction b)
		{
			return a.getPrice() > b.getPrice() ? -1 : (a.getPrice() == b.getPrice() ? 0 : 1);
		}
	}

	protected static class SortByQuantity implements Comparator<PrivateStoreHistoryManager.ItemHistoryTransaction>
	{
		@Override
		public int compare(PrivateStoreHistoryManager.ItemHistoryTransaction a, PrivateStoreHistoryManager.ItemHistoryTransaction b)
		{
			return a.getCount() > b.getCount() ? -1 : (a.getCount() == b.getCount() ? 0 : 1);
		}
	}
}
