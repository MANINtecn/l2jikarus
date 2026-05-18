package net.sf.l2jdev.gameserver.managers;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.WorldExchangeConfig;
import net.sf.l2jdev.gameserver.config.custom.MultilingualSupportConfig;
import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.VariationInstance;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemLocation;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemStatusType;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemSubType;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeSortType;
import net.sf.l2jdev.gameserver.model.item.holders.WorldExchangeHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.InventoryUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeBuyItem;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeRegisterItem;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeSellCompleteAlarm;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeSettleList;
import net.sf.l2jdev.gameserver.network.serverpackets.worldexchange.WorldExchangeSettleRecvResult;
import org.w3c.dom.Document;

public class WorldExchangeManager implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(WorldExchangeManager.class.getName());
	public static final String SELECT_ALL_ITEMS = "SELECT * FROM `items` WHERE `loc`=?";
	public static final String RESTORE_INFO = "SELECT * FROM world_exchange_items";
	public static final String INSERT_WORLD_EXCHANGE = "REPLACE INTO world_exchange_items (`world_exchange_id`, `item_object_id`, `item_status`, `category_id`, `price`, `old_owner_id`, `start_time`, `end_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
	private final Map<Long, WorldExchangeHolder> _itemBids = new ConcurrentHashMap<>();
	private final Map<Integer, WorldExchangeItemSubType> _itemCategories = new ConcurrentHashMap<>();
	private final Map<String, Map<Integer, String>> _localItemNames = new HashMap<>(new HashMap<>());
	private long _lastWorldExchangeId = 0L;
	private ScheduledFuture<?> _checkStatus = null;

	public WorldExchangeManager()
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			this.firstLoad();
			if (this._checkStatus == null)
			{
				this._checkStatus = ThreadPool.scheduleAtFixedRate(this::checkBidStatus, WorldExchangeConfig.WORLD_EXCHANGE_SAVE_INTERVAL, WorldExchangeConfig.WORLD_EXCHANGE_SAVE_INTERVAL);
			}
		}
	}

	@Override
	public void load()
	{
		if (MultilingualSupportConfig.MULTILANG_ENABLE)
		{
			this._localItemNames.clear();

			for (String lang : MultilingualSupportConfig.MULTILANG_ALLOWED)
			{
				File file = new File("data/lang/" + lang + "/ItemNameLocalisation.xml");
				if (file.isFile())
				{
					this.parseDatapackFile("data/lang/" + lang + "/ItemNameLocalisation.xml");
					int size = this._localItemNames.get(lang).size();
					if (size == 0)
					{
						this._localItemNames.remove(lang);
					}
					else
					{
						LOGGER.log(Level.INFO, this.getClass().getSimpleName() + ": Loaded ItemName localisations for [" + lang + "].");
					}
				}
			}
		}

		if (!MultilingualSupportConfig.MULTILANG_DEFAULT.equals(WorldExchangeConfig.WORLD_EXCHANGE_DEFAULT_LANG) && !this._localItemNames.containsKey(WorldExchangeConfig.WORLD_EXCHANGE_DEFAULT_LANG))
		{
			this.parseDatapackFile("data/lang/" + WorldExchangeConfig.WORLD_EXCHANGE_DEFAULT_LANG + "/ItemNameLocalisation.xml");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		ConcurrentHashMap<Integer, String> local = new ConcurrentHashMap<>();
		this.forEach(document, "list", listNode -> {
			this.forEach(listNode, "blessed", itemNode -> {
				StatSet itemSet = new StatSet(this.parseAttributes(itemNode));
				local.put(-1, itemSet.getString("name"));
			});
			this.forEach(listNode, "item", itemNode -> {
				StatSet itemSet = new StatSet(this.parseAttributes(itemNode));
				local.put(itemSet.getInt("id"), itemSet.getString("name"));
			});
		});
		this._localItemNames.put(document.getDocumentURI().split("data/lang/")[1].split("/")[0], local);
	}

	public Map<Integer, String> getItemLocalByLang(String lang)
	{
		return this._localItemNames.get(lang);
	}

	synchronized void firstLoad()
	{
		Map<Integer, Item> itemInstances = this.loadItemInstances();
		this.loadItemBids(itemInstances);
		this.load();
	}

	private void checkBidStatus()
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			for (Entry<Long, WorldExchangeHolder> entry : this._itemBids.entrySet())
			{
				WorldExchangeHolder holder = entry.getValue();
				long currentTime = System.currentTimeMillis();
				long endTime = holder.getEndTime();
				if (endTime <= currentTime)
				{
					switch (holder.getStoreType())
					{
						case WORLD_EXCHANGE_NONE:
							this._itemBids.remove(entry.getKey());
							break;
						case WORLD_EXCHANGE_REGISTERED:
							holder.setEndTime(this.calculateDate(WorldExchangeConfig.WORLD_EXCHANGE_ITEM_BACK_PERIOD));
							holder.setStoreType(WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME);
							this._itemBids.replace(entry.getKey(), holder);
							this.insert(entry.getKey(), false);
							break;
						case WORLD_EXCHANGE_SOLD:
						case WORLD_EXCHANGE_OUT_TIME:
							holder.setStoreType(WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE);
							this.insert(entry.getKey(), true);
							Item item = holder.getItemInstance();
							item.setItemLocation(ItemLocation.VOID);
							item.updateDatabase(!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE);
					}
				}
			}
		}
	}

	synchronized Map<Integer, Item> loadItemInstances()
	{
		if (!WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			return Collections.emptyMap();
		}
		Map<Integer, Item> itemInstances = new HashMap<>();

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM `items` WHERE `loc`=?");)
		{
			ps.setString(1, ItemLocation.EXCHANGE.name());

			try (ResultSet rs = ps.executeQuery())
			{
				while (rs.next())
				{
					Item itemInstance = new Item(rs);
					itemInstances.put(itemInstance.getObjectId(), itemInstance);
				}
			}
		}
		catch (SQLException var13)
		{
			LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading items instances.", var13);
		}

		return itemInstances;
	}

	private synchronized void loadItemBids(Map<Integer, Item> itemInstances)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM world_exchange_items"); ResultSet rs = ps.executeQuery();)
			{
				while (rs.next())
				{
					boolean needChange = false;
					long worldExchangeId = rs.getLong("world_exchange_id");
					this._lastWorldExchangeId = Math.max(worldExchangeId, this._lastWorldExchangeId);
					Item itemInstance = itemInstances.get(rs.getInt("item_object_id"));
					WorldExchangeItemStatusType storeType = WorldExchangeItemStatusType.getWorldExchangeItemStatusType(rs.getInt("item_status"));
					if (storeType != WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE)
					{
						if (itemInstance == null)
						{
							LOGGER.warning(this.getClass().getSimpleName() + ": Failed loading commission item with world exchange id " + worldExchangeId + " because item instance does not exist or failed to load.");
						}
						else
						{
							WorldExchangeItemSubType categoryId = WorldExchangeItemSubType.getWorldExchangeItemSubType(rs.getInt("category_id"));
							long price = rs.getLong("price");
							int bidPlayerObjectId = rs.getInt("old_owner_id");
							long startTime = rs.getLong("start_time");
							long endTime = rs.getLong("end_time");
							if (endTime < System.currentTimeMillis())
							{
								if (storeType == WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME || storeType == WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD)
								{
									itemInstance.setItemLocation(ItemLocation.VOID);
									itemInstance.updateDatabase(true);
									continue;
								}

								endTime = this.calculateDate(WorldExchangeConfig.WORLD_EXCHANGE_ITEM_BACK_PERIOD);
								storeType = WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME;
								needChange = true;
							}

							this._itemBids.put(worldExchangeId, new WorldExchangeHolder(worldExchangeId, itemInstance, new ItemInfo(itemInstance), price, bidPlayerObjectId, storeType, categoryId, startTime, endTime, needChange));
						}
					}
				}
			}
			catch (Exception var24)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed loading bid items.", var24);
			}
		}
	}

	public long calculateFeeForRegister(Player player, int objectId, long amount, long priceForEach)
	{
		return Math.round(priceForEach * WorldExchangeConfig.WORLD_EXCHANGE_ADENA_FEE);
	}

	public synchronized void registerItemBid(Player player, int itemObjectId, long amount, long priceForEach)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			Map<WorldExchangeItemStatusType, List<WorldExchangeHolder>> playerBids = this.getPlayerBids(player.getObjectId());
			if (playerBids.size() >= 10)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_SLOTS_AVAILABLE));
				player.sendPacket(WorldExchangeRegisterItem.FAIL);
			}
			else if (player.getInventory().getItemByObjectId(itemObjectId) == null)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeRegisterItem.FAIL);
			}
			else if (amount >= 1L && priceForEach >= 1L && amount * priceForEach >= 1L)
			{
				Item item = player.getInventory().getItemByObjectId(itemObjectId);
				long feePrice = this.calculateFeeForRegister(player, itemObjectId, amount, priceForEach);
				if (WorldExchangeConfig.WORLD_EXCHANGE_MAX_ADENA_FEE != -1L && feePrice > WorldExchangeConfig.WORLD_EXCHANGE_MAX_ADENA_FEE)
				{
					feePrice = WorldExchangeConfig.WORLD_EXCHANGE_MAX_ADENA_FEE;
				}

				if (feePrice > player.getAdena())
				{
					player.sendPacket(new SystemMessage(SystemMessageId.NOT_ENOUGH_ADENA));
					player.sendPacket(WorldExchangeRegisterItem.FAIL);
				}
				else if (feePrice < 1L)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ITEM_COUNT_2));
					player.sendPacket(WorldExchangeRegisterItem.FAIL);
				}
				else
				{
					long freeId = this.getNextId();
					InventoryUpdate iu = new InventoryUpdate();
					if (item.isStackable() && player.getInventory().getInventoryItemCount(item.getId(), -1) > amount)
					{
						iu.addModifiedItem(item);
					}
					else
					{
						iu.addRemovedItem(item);
					}

					Item itemInstance = player.getInventory().detachItem(ItemProcessType.TRANSFER, item, amount, ItemLocation.EXCHANGE, player, null);
					if (itemInstance == null)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
						player.sendPacket(WorldExchangeRegisterItem.FAIL);
					}
					else
					{
						WorldExchangeItemSubType category = this._itemCategories.get(itemInstance.getId());
						if (category == null)
						{
							player.sendPacket(new SystemMessage(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD));
							player.sendPacket(WorldExchangeRegisterItem.FAIL);
						}
						else
						{
							player.sendInventoryUpdate(iu);
							player.getInventory().reduceAdena(ItemProcessType.FEE, feePrice, player, null);
							long endTime = this.calculateDate(WorldExchangeConfig.WORLD_EXCHANGE_ITEM_SELL_PERIOD);
							this._itemBids.put(freeId, new WorldExchangeHolder(freeId, itemInstance, new ItemInfo(itemInstance), priceForEach, player.getObjectId(), WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED, category, System.currentTimeMillis(), endTime, true));
							player.sendPacket(new WorldExchangeRegisterItem(itemObjectId, amount, (byte) 1));
							if (!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
							{
								this.insert(freeId, false);
							}
						}
					}
				}
			}
			else
			{
				player.sendPacket(new SystemMessage(SystemMessageId.INCORRECT_ITEM_COUNT_2));
				player.sendPacket(WorldExchangeRegisterItem.FAIL);
			}
		}
	}

	private synchronized long getNextId()
	{
		return this._lastWorldExchangeId++;
	}

	public long calculateDate(int days)
	{
		Calendar calendar = Calendar.getInstance();
		calendar.add(5, days);
		return calendar.getTimeInMillis();
	}

	public void getItemStatusAndMakeAction(Player player, long worldExchangeIndex)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			WorldExchangeHolder worldExchangeItem = this._itemBids.get(worldExchangeIndex);
			if (worldExchangeItem == null)
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else
			{
				WorldExchangeItemStatusType storeType = worldExchangeItem.getStoreType();
				switch (storeType)
				{
					case WORLD_EXCHANGE_REGISTERED:
						this.cancelBid(player, worldExchangeItem);
						break;
					case WORLD_EXCHANGE_SOLD:
						this.takeBidMoney(player, worldExchangeItem);
						break;
					case WORLD_EXCHANGE_OUT_TIME:
						this.returnItem(player, worldExchangeItem);
				}
			}
		}
	}

	private void cancelBid(Player player, WorldExchangeHolder worldExchangeItem)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			if (worldExchangeItem.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (!this._itemBids.containsKey(worldExchangeItem.getWorldExchangeId()))
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (this._itemBids.get(worldExchangeItem.getWorldExchangeId()) != worldExchangeItem)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (player.getObjectId() != worldExchangeItem.getOldOwnerId())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ITEM_OUT_OF_STOCK));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (worldExchangeItem.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else
			{
				player.sendPacket(new WorldExchangeSettleRecvResult(worldExchangeItem.getItemInstance().getObjectId(), worldExchangeItem.getItemInstance().getCount(), (byte) 1));
				player.getInventory().addItem(ItemProcessType.TRANSFER, worldExchangeItem.getItemInstance(), player, player);
				worldExchangeItem.setStoreType(WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE);
				worldExchangeItem.setHasChanges(true);
				this._itemBids.replace(worldExchangeItem.getWorldExchangeId(), worldExchangeItem);
				if (!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
				{
					this.insert(worldExchangeItem.getWorldExchangeId(), true);
				}
			}
		}
	}

	private void takeBidMoney(Player player, WorldExchangeHolder worldExchangeItem)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			if (worldExchangeItem.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (!this._itemBids.containsKey(worldExchangeItem.getWorldExchangeId()))
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (this._itemBids.get(worldExchangeItem.getWorldExchangeId()) != worldExchangeItem)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (player.getObjectId() != worldExchangeItem.getOldOwnerId())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (worldExchangeItem.getStoreType() != WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_ITEM_YOU_REGISTERED_HAS_BEEN_SOLD));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (worldExchangeItem.getEndTime() < System.currentTimeMillis())
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_REGISTRATION_PERIOD_FOR_THE_ITEM_YOU_REGISTERED_HAS_EXPIRED));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else
			{
				player.sendPacket(new WorldExchangeSettleRecvResult(worldExchangeItem.getItemInstance().getObjectId(), worldExchangeItem.getItemInstance().getCount(), (byte) 1));
				long fee = Math.max(1L, Math.min(20000L, Math.round(worldExchangeItem.getPrice() * WorldExchangeConfig.WORLD_EXCHANGE_LCOIN_TAX * 100.0 / 100.0)));
				long returnPrice = worldExchangeItem.getPrice() - Math.min(fee, WorldExchangeConfig.WORLD_EXCHANGE_MAX_LCOIN_TAX != -1L ? WorldExchangeConfig.WORLD_EXCHANGE_MAX_LCOIN_TAX : Long.MAX_VALUE);
				player.getInventory().addItem(ItemProcessType.FEE, 91663, returnPrice, player, null);
				worldExchangeItem.setStoreType(WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE);
				Item item = worldExchangeItem.getItemInstance();
				item.setItemLocation(ItemLocation.VOID);
				item.updateDatabase(!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE);
				worldExchangeItem.setHasChanges(true);
				this._itemBids.replace(worldExchangeItem.getWorldExchangeId(), worldExchangeItem);
				if (!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
				{
					this.insert(worldExchangeItem.getWorldExchangeId(), true);
				}
			}
		}
	}

	private void returnItem(Player player, WorldExchangeHolder worldExchangeItem)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			if (worldExchangeItem.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (!this._itemBids.containsKey(worldExchangeItem.getWorldExchangeId()))
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (this._itemBids.get(worldExchangeItem.getWorldExchangeId()) != worldExchangeItem)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.ITEM_OUT_OF_STOCK));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (player.getObjectId() != worldExchangeItem.getOldOwnerId())
			{
				player.sendPacket(new SystemMessage(SystemMessageId.ITEM_TO_BE_TRADED_DOES_NOT_EXIST));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (worldExchangeItem.getStoreType() != WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME)
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.ITEM_OUT_OF_STOCK));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else if (worldExchangeItem.getEndTime() < System.currentTimeMillis())
			{
				player.sendPacket(new WorldExchangeSettleList(player));
				player.sendPacket(new SystemMessage(SystemMessageId.THE_REGISTRATION_PERIOD_FOR_THE_ITEM_YOU_REGISTERED_HAS_EXPIRED));
				player.sendPacket(WorldExchangeSettleRecvResult.FAIL);
			}
			else
			{
				player.sendPacket(new WorldExchangeSettleRecvResult(worldExchangeItem.getItemInstance().getObjectId(), worldExchangeItem.getItemInstance().getCount(), (byte) 1));
				player.getInventory().addItem(ItemProcessType.TRANSFER, worldExchangeItem.getItemInstance(), player, null);
				worldExchangeItem.setStoreType(WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE);
				worldExchangeItem.setHasChanges(true);
				this._itemBids.replace(worldExchangeItem.getWorldExchangeId(), worldExchangeItem);
				if (!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
				{
					this.insert(worldExchangeItem.getWorldExchangeId(), true);
				}
			}
		}
	}

	public void buyItem(Player player, long worldExchangeId)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			if (!this._itemBids.containsKey(worldExchangeId))
			{
				player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
				player.sendPacket(WorldExchangeBuyItem.FAIL);
			}
			else
			{
				WorldExchangeHolder worldExchangeItem = this._itemBids.get(worldExchangeId);
				if (worldExchangeItem.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.NO_ITEM_FOUND));
					player.sendPacket(WorldExchangeBuyItem.FAIL);
				}
				else if (worldExchangeItem.getStoreType() != WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED)
				{
					player.sendPacket(new SystemMessage(SystemMessageId.ITEM_OUT_OF_STOCK));
					player.sendPacket(WorldExchangeBuyItem.FAIL);
				}
				else
				{
					Item lcoin = player.getInventory().getItemByItemId(91663);
					if (lcoin != null && lcoin.getCount() >= worldExchangeItem.getPrice())
					{
						player.getInventory().destroyItem(ItemProcessType.BUY, lcoin, worldExchangeItem.getPrice(), player, null);
						Item newItem = this.createItem(worldExchangeItem.getItemInstance(), player);
						long destroyTime = this.calculateDate(WorldExchangeConfig.WORLD_EXCHANGE_PAYMENT_TAKE_PERIOD);
						WorldExchangeHolder newHolder = new WorldExchangeHolder(worldExchangeId, newItem, new ItemInfo(newItem), worldExchangeItem.getPrice(), worldExchangeItem.getOldOwnerId(), WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD, worldExchangeItem.getCategory(), worldExchangeItem.getStartTime(), destroyTime, true);
						this._itemBids.replace(worldExchangeId, worldExchangeItem, newHolder);
						if (!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
						{
							this.insert(worldExchangeItem.getWorldExchangeId(), false);
						}

						Item receivedItem = player.getInventory().addItem(ItemProcessType.BUY, worldExchangeItem.getItemInstance(), player, null);
						player.sendPacket(new WorldExchangeBuyItem(receivedItem.getObjectId(), receivedItem.getCount(), (byte) 1));
						SystemMessage sm;
						if (receivedItem.getEnchantLevel() > 0)
						{
							if (receivedItem.getCount() < 2L)
							{
								sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2_6);
								sm.addByte(receivedItem.getEnchantLevel());
								sm.addItemName(receivedItem);
							}
							else
							{
								sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_S2_X_S3);
								sm.addItemName(receivedItem);
								sm.addLong(receivedItem.getCount());
								sm.addByte(receivedItem.getEnchantLevel());
							}
						}
						else
						{
							sm = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2);
							sm.addItemName(receivedItem);
							sm.addLong(receivedItem.getCount());
						}

						player.sendPacket(sm);

						for (Player oldOwner : World.getInstance().getPlayers())
						{
							if (oldOwner.getObjectId() == newHolder.getOldOwnerId())
							{
								oldOwner.sendPacket(new WorldExchangeSellCompleteAlarm(newItem.getId(), newItem.getCount()));
								break;
							}
						}
					}
					else
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_L2_COINS_ADD_MORE_L2_COINS_AND_TRY_AGAIN));
						player.sendPacket(WorldExchangeBuyItem.FAIL);
					}
				}
			}
		}
	}

	public Item createItem(Item oldItem, Player requestor)
	{
		Item newItem = new Item(oldItem.getId());
		newItem.setOwnerId(requestor.getObjectId());
		newItem.setEnchantLevel(oldItem.getEnchantLevel() < 1 ? 0 : oldItem.getEnchantLevel());
		newItem.setItemLocation(ItemLocation.EXCHANGE);
		newItem.setCount(oldItem.getCount());
		newItem.setVisualId(oldItem.getVisualId(), false);
		newItem.setBlessed(oldItem.isBlessed());
		newItem.setOwnerId(oldItem.getOwnerId());
		newItem.updateDatabase(true);
		VariationInstance vi = oldItem.getAugmentation();
		if (vi != null)
		{
			newItem.setAugmentation(vi, true);
		}

		InventoryUpdate iu = new InventoryUpdate();
		iu.addRemovedItem(newItem);
		requestor.sendInventoryUpdate(iu);
		return newItem;
	}

	public List<WorldExchangeHolder> getItemBids(int ownerId, WorldExchangeItemSubType type, WorldExchangeSortType sortType, String lang)
	{
		if (!WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			return Collections.emptyList();
		}
		List<WorldExchangeHolder> returnList = new ArrayList<>();

		for (WorldExchangeHolder holder : this._itemBids.values())
		{
			if (holder.getStoreType() != WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE && holder.getOldOwnerId() != ownerId && holder.getCategory() == type && holder.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED)
			{
				returnList.add(holder);
			}
		}

		return this.sortList(returnList, sortType, lang);
	}

	public List<WorldExchangeHolder> getItemBids(List<Integer> ids, WorldExchangeSortType sortType, String lang)
	{
		if (!WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			return Collections.emptyList();
		}
		List<WorldExchangeHolder> returnList = new ArrayList<>();

		for (WorldExchangeHolder holder : this._itemBids.values())
		{
			if (holder.getStoreType() != WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE && ids.contains(holder.getItemInstance().getId()) && holder.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED)
			{
				returnList.add(holder);
			}
		}

		return this.sortList(returnList, sortType, lang);
	}

	private List<WorldExchangeHolder> sortList(List<WorldExchangeHolder> unsortedList, WorldExchangeSortType sortType, String lang)
	{
		List<WorldExchangeHolder> sortedList = new ArrayList<>(unsortedList);
		switch (sortType)
		{
			case PRICE_ASCE:
				Collections.sort(sortedList, Comparator.comparing(WorldExchangeHolder::getPrice));
				break;
			case PRICE_DESC:
				Collections.sort(sortedList, Comparator.comparing(WorldExchangeHolder::getPrice));
				Collections.reverse(sortedList);
				break;
			case ITEM_NAME_ASCE:
				if (lang != null && (lang.equals("en") || !this._localItemNames.containsKey(lang)))
				{
					Collections.sort(sortedList, Comparator.comparing(o -> (o.getItemInstance().isBlessed() ? "Blessed " : "") + o.getItemInstance().getItemName()));
				}
				else
				{
					Collections.sort(sortedList, Comparator.comparing(o -> this.getItemName(lang, o.getItemInstance().getId(), o.getItemInstance().isBlessed())));
				}
				break;
			case ITEM_NAME_DESC:
				if (lang != null && (lang.equals("en") || !this._localItemNames.containsKey(lang)))
				{
					Collections.sort(sortedList, Comparator.comparing(o -> (o.getItemInstance().isBlessed() ? "Blessed " : "") + o.getItemInstance().getItemName()));
				}
				else
				{
					Collections.sort(sortedList, Comparator.comparing(o -> this.getItemName(lang, o.getItemInstance().getId(), o.getItemInstance().isBlessed())));
				}

				Collections.reverse(sortedList);
				break;
			case PRICE_PER_PIECE_ASCE:
				Collections.sort(sortedList, Comparator.comparingLong(WorldExchangeHolder::getPrice));
				break;
			case PRICE_PER_PIECE_DESC:
				Collections.sort(sortedList, Comparator.comparingLong(WorldExchangeHolder::getPrice).reversed());
		}

		return sortedList.size() > 399 ? sortedList.subList(0, 399) : sortedList;
	}

	private String getItemName(String lang, int id, boolean isBlessed)
	{
		if (!this._localItemNames.containsKey(lang))
		{
			return "";
		}
		Map<Integer, String> names = this._localItemNames.get(lang);
		String name = names.get(id);
		if (name == null)
		{
			return "";
		}
		return isBlessed ? names.get(-1) + " " + name : name;
	}

	public Map<WorldExchangeItemStatusType, List<WorldExchangeHolder>> getPlayerBids(int ownerId)
	{
		if (!WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			return Collections.emptyMap();
		}
		List<WorldExchangeHolder> registered = new ArrayList<>();
		List<WorldExchangeHolder> sold = new ArrayList<>();
		List<WorldExchangeHolder> outTime = new ArrayList<>();

		for (WorldExchangeHolder holder : this._itemBids.values())
		{
			if (holder.getStoreType() != WorldExchangeItemStatusType.WORLD_EXCHANGE_NONE && holder.getOldOwnerId() == ownerId)
			{
				switch (holder.getStoreType())
				{
					case WORLD_EXCHANGE_REGISTERED:
						registered.add(holder);
						break;
					case WORLD_EXCHANGE_SOLD:
						sold.add(holder);
						break;
					case WORLD_EXCHANGE_OUT_TIME:
						outTime.add(holder);
				}
			}
		}

		EnumMap<WorldExchangeItemStatusType, List<WorldExchangeHolder>> returnMap = new EnumMap<>(WorldExchangeItemStatusType.class);
		returnMap.put(WorldExchangeItemStatusType.WORLD_EXCHANGE_REGISTERED, registered);
		returnMap.put(WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD, sold);
		returnMap.put(WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME, outTime);
		return returnMap;
	}

	public void addCategoryType(List<Integer> itemIds, int category)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			for (int itemId : itemIds)
			{
				WorldExchangeItemSubType type = WorldExchangeItemSubType.getWorldExchangeItemSubType(category);
				if (type == null)
				{
					LOGGER.warning(this.getClass().getSimpleName() + ": Non existent category type [" + category + "] for item id " + itemId + "!");
				}
				else
				{
					this._itemCategories.putIfAbsent(itemId, type);
				}
			}
		}
	}

	public void checkPlayerSellAlarm(Player player)
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE)
		{
			for (WorldExchangeHolder holder : this._itemBids.values())
			{
				if (holder.getOldOwnerId() == player.getObjectId() && (holder.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_SOLD || holder.getStoreType() == WorldExchangeItemStatusType.WORLD_EXCHANGE_OUT_TIME))
				{
					player.sendPacket(new WorldExchangeSellCompleteAlarm(holder.getItemInstance().getId(), holder.getItemInstance().getCount()));
					break;
				}
			}
		}
	}

	public void storeMe()
	{
		if (WorldExchangeConfig.ENABLE_WORLD_EXCHANGE && WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement("REPLACE INTO world_exchange_items (`world_exchange_id`, `item_object_id`, `item_status`, `category_id`, `price`, `old_owner_id`, `start_time`, `end_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");)
			{
				for (WorldExchangeHolder holder : this._itemBids.values())
				{
					if (holder.hasChanges())
					{
						statement.setLong(1, holder.getWorldExchangeId());
						statement.setLong(2, holder.getItemInstance().getObjectId());
						statement.setInt(3, holder.getStoreType().getId());
						statement.setInt(4, holder.getCategory().getId());
						statement.setLong(5, holder.getPrice());
						statement.setInt(6, holder.getOldOwnerId());
						statement.setLong(7, holder.getStartTime());
						statement.setLong(8, holder.getEndTime());
						statement.addBatch();
					}
				}

				statement.executeBatch();
				statement.closeOnCompletion();
			}
			catch (SQLException var9)
			{
				LOGGER.log(Level.SEVERE, "Error while saving World Exchange item bids:\n", var9);
			}
		}
	}

	public void insert(long worldExchangeId, boolean remove)
	{
		if (!WorldExchangeConfig.WORLD_EXCHANGE_LAZY_UPDATE)
		{
			try (Connection con = DatabaseFactory.getConnection();
				PreparedStatement statement = con.prepareStatement("REPLACE INTO world_exchange_items (`world_exchange_id`, `item_object_id`, `item_status`, `category_id`, `price`, `old_owner_id`, `start_time`, `end_time`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)");)
			{
				WorldExchangeHolder holder = this._itemBids.get(worldExchangeId);
				statement.setLong(1, holder.getWorldExchangeId());
				statement.setLong(2, holder.getItemInstance().getObjectId());
				statement.setInt(3, holder.getStoreType().getId());
				statement.setInt(4, holder.getCategory().getId());
				statement.setLong(5, holder.getPrice());
				statement.setInt(6, holder.getOldOwnerId());
				statement.setString(7, String.valueOf(holder.getStartTime()));
				statement.setString(8, String.valueOf(holder.getEndTime()));
				statement.execute();
				if (remove)
				{
					this._itemBids.remove(worldExchangeId);
				}
			}
			catch (SQLException var12)
			{
				LOGGER.log(Level.SEVERE, "Error while saving World Exchange item bid " + worldExchangeId + "\n", var12);
			}
		}
	}

	public long getAveragePriceOfItem(int itemId)
	{
		long totalPrice = 0L;
		long totalItemCount = 0L;

		for (WorldExchangeHolder holder : this._itemBids.values())
		{
			if (holder.getItemInstance().getTemplate().getId() == itemId)
			{
				totalItemCount++;
				totalPrice += holder.getPrice();
			}
		}

		return totalItemCount == 0L ? 0L : totalPrice / totalItemCount;
	}

	public static WorldExchangeManager getInstance()
	{
		return WorldExchangeManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final WorldExchangeManager INSTANCE = new WorldExchangeManager();
	}
}
