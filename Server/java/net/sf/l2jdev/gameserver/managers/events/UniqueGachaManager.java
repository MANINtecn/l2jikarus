package net.sf.l2jdev.gameserver.managers.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.Containers;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogin;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerLogout;
import net.sf.l2jdev.gameserver.model.events.listeners.ConsumerEventListener;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.UniqueGachaRank;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemTimeStampHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.itemcontainer.GachaWarehouse;
import net.sf.l2jdev.gameserver.model.itemcontainer.PlayerInventory;
import net.sf.l2jdev.gameserver.model.script.Quest;
import net.sf.l2jdev.gameserver.network.serverpackets.gacha.UniqueGachaSidebarInfo;

public class UniqueGachaManager
{
	public static final int MINIMUM_CHANCE = 1000000;
	public static final int MINIMUM_CHANCE_AFTER_DOT = 6;
	public static final String GACHA_PLAYER_VARIABLE = "GACHA_ROLL_COUNT";
	public static final String GACHA_LOCK_PLAYER_VARIABLE = "UniqueGachaRoll";
	private static final LinkedList<GachaItemTimeStampHolder> EMPTY_LINKED_LIST = new LinkedList<>();
	private final Set<GachaItemHolder> _visibleItems = new HashSet<>();
	private final Map<UniqueGachaRank, Set<GachaItemHolder>> _rewardItems = new HashMap<>();
	private final Map<UniqueGachaRank, Integer> _rewardChance = new HashMap<>();
	private final Map<Integer, Long> _gameCosts = new HashMap<>();
	private final Map<Player, GachaWarehouse> _temporaryWarehouse = new HashMap<>();
	private final Map<Integer, LinkedList<GachaItemTimeStampHolder>> _gachaHistory = new HashMap<>();
	private boolean _isActive;
	private long _activeUntilPeriod;
	private int _guaranteeRoll;
	private boolean _showProbability;
	private int _currencyItemId;
	private int _totalRewardCount = 0;
	private int _totalChanceSumma = 0;

	public UniqueGachaManager()
	{
		restoreGachaHistory(this._gachaHistory);
		ThreadPool.scheduleAtFixedRate(() -> storeGachaHistory(this._gachaHistory), TimeUnit.MINUTES.toMillis(5L), TimeUnit.MINUTES.toMillis(5L));
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOGIN, event -> this.onPlayerLogin((OnPlayerLogin) event), this));
		Containers.Global().addListener(new ConsumerEventListener(Containers.Global(), EventType.ON_PLAYER_LOGOUT, event -> this.onPlayerLogout((OnPlayerLogout) event), this));
	}

	public void reload()
	{
		Quest quest = ScriptManager.getInstance().getScript("UniqueGacha");
		if (quest != null)
		{
			this._totalRewardCount = 0;
			this._totalChanceSumma = 0;
			this._visibleItems.clear();
			this._rewardItems.clear();
			this._rewardChance.clear();
			this._gameCosts.clear();
			quest.notifyEvent("RELOAD", null, null);
		}
	}

	public void setParameters(StatSet params)
	{
		this._isActive = params.getBoolean("isActive", false);
		this._activeUntilPeriod = params.getLong("activeUntilPeriod", 0L);
		this._guaranteeRoll = params.getInt("guaranteeRoll", 200);
		this._showProbability = params.getBoolean("showProbability", false);
		this._currencyItemId = params.getInt("currencyItemId", 57);
	}

	public void addReward(UniqueGachaRank rank, int itemId, long itemCount, int itemChance, int enchantLevel)
	{
		this._totalRewardCount++;
		this._totalChanceSumma += itemChance;
		this._rewardItems.putIfAbsent(rank, new HashSet<>());
		this._rewardItems.get(rank).add(new GachaItemHolder(itemId, itemCount, itemChance, enchantLevel, rank));
	}

	public void addGameCost(int rollCount, long itemCount)
	{
		this._gameCosts.put(rollCount, itemCount);
	}

	public void recalculateChances()
	{
		for (Entry<UniqueGachaRank, Set<GachaItemHolder>> entry : this._rewardItems.entrySet())
		{
			int totalChance = 0;

			for (GachaItemHolder item : entry.getValue())
			{
				totalChance += item.getItemChance();
			}

			this._rewardChance.put(entry.getKey(), totalChance);
		}

		this.recalculateVisibleItems();
	}

	private void recalculateVisibleItems()
	{
		List<GachaItemHolder> rewards = new ArrayList<>(this._rewardItems.getOrDefault(UniqueGachaRank.RANK_UR, Set.of()));
		rewards.sort(Comparator.comparingInt(GachaItemHolder::getItemChance));
		if (!rewards.isEmpty())
		{
			this._visibleItems.clear();
			this._visibleItems.addAll(rewards.subList(0, Math.min(5, rewards.size())));
			rewards.clear();
		}
	}

	private void onPlayerLogin(OnPlayerLogin event)
	{
		Player player = event == null ? null : event.getPlayer();
		if (player != null)
		{
			if (this.isActive())
			{
				GachaWarehouse warehouse = this._temporaryWarehouse.getOrDefault(player, null);
				if (warehouse == null)
				{
					warehouse = new GachaWarehouse(player);
					warehouse.restore();
					this._temporaryWarehouse.put(player, warehouse);
				}
			}

			player.sendPacket(this.isActive() ? UniqueGachaSidebarInfo.GACHA_ON : UniqueGachaSidebarInfo.GACHA_OFF);
		}
	}

	private void onPlayerLogout(OnPlayerLogout event)
	{
		Player player = event == null ? null : event.getPlayer();
		if (player != null)
		{
			GachaWarehouse warehouse = this._temporaryWarehouse.getOrDefault(player, null);
			if (warehouse != null)
			{
				warehouse.deleteMe();
				this._temporaryWarehouse.remove(player);
			}
		}
	}

	public Set<GachaItemHolder> getVisibleItems()
	{
		return this._visibleItems;
	}

	public Map<UniqueGachaRank, Set<GachaItemHolder>> getRewardItems()
	{
		return this._rewardItems;
	}

	public Map<Integer, Long> getGameCosts()
	{
		return this._gameCosts;
	}

	public boolean isActive()
	{
		return this._isActive;
	}

	public long getActiveUntilPeriod()
	{
		return this._activeUntilPeriod;
	}

	public int getGuaranteeRoll()
	{
		return this._guaranteeRoll;
	}

	public boolean isShowProbability()
	{
		return this._showProbability;
	}

	public int getCurrencyItemId()
	{
		return this._currencyItemId;
	}

	public long getCurrencyCount(Player player)
	{
		long count = player.getInventory().getInventoryItemCount(this.getCurrencyItemId(), -1);
		return Math.min(Long.MAX_VALUE, count);
	}

	private void removeCurrency(Player player, long count)
	{
		PlayerInventory inv = player.getInventory();
		if (inv != null && inv.canManipulateWithItemId(this._currencyItemId))
		{
			Item item = inv.getItemByItemId(this._currencyItemId);
			if (item != null)
			{
				player.destroyItemByItemId(ItemProcessType.FEE, item.getId(), count, player, true);
			}
		}
	}

	public int getStepsToGuaranteedReward(Player player)
	{
		return this._guaranteeRoll - player.getVariables().getInt("GACHA_ROLL_COUNT", 0);
	}

	public Entry<List<GachaItemHolder>, Boolean> tryToRoll(Player player, int rollCount)
	{
		boolean rare = false;
		List<GachaItemHolder> rewards = new ArrayList<>();
		if (!this.checkRequirements(player, rollCount))
		{
			return returnEmptyList();
		}
		try
		{
			player.getVariables().set("UniqueGachaRoll", true);
			this.removeCurrency(player, this._gameCosts.get(rollCount));
			int playerRollProgress = player.getVariables().getInt("GACHA_ROLL_COUNT", 0);

			for (int roll = 0; roll < rollCount; roll++)
			{
				playerRollProgress++;
				boolean isGuaranteed = playerRollProgress >= this._guaranteeRoll;
				playerRollProgress = isGuaranteed ? 0 : playerRollProgress;
				UniqueGachaRank rank = this.randomRank(isGuaranteed);
				GachaItemHolder item = this.getRandomReward(rank);
				rare = rare || item.getRank().equals(UniqueGachaRank.RANK_SR) || item.getRank().equals(UniqueGachaRank.RANK_UR);
				this.addItemToTemporaryWarehouse(player, item);
				rewards.add(item);
				this.addToHistory(player, item, roll);
			}

			player.getVariables().set("GACHA_ROLL_COUNT", playerRollProgress);
		}
		finally
		{
			player.getVariables().remove("UniqueGachaRoll");
		}

		return new SimpleEntry<>(rewards, rare);
	}

	private boolean checkRequirements(Player player, int rollCount)
	{
		if (player.getVariables().getBoolean("UniqueGachaRoll", false))
		{
			return false;
		}
		long currencyCount = this._gameCosts.getOrDefault(rollCount, -1L);
		if (currencyCount == -1L)
		{
			return false;
		}
		PlayerInventory inv = player.getInventory();
		if (inv == null)
		{
			return false;
		}
		else if (!inv.canManipulateWithItemId(this._currencyItemId))
		{
			return false;
		}
		else if (this.getCurrencyCount(player) < currencyCount)
		{
			return false;
		}
		else
		{
			GachaWarehouse warehouse = this._temporaryWarehouse.getOrDefault(player, null);
			return warehouse == null ? false : warehouse.getSize() < 1100;
		}
	}

	private void addToHistory(Player player, GachaItemHolder item, int roll)
	{
		String count = String.valueOf(roll);
		String timeStamp = System.currentTimeMillis() / 1000L + "0".repeat(3 - count.length()) + count;
		this._gachaHistory.computeIfAbsent(player.getObjectId(), _ -> new LinkedList<>()).addLast(new GachaItemTimeStampHolder(item.getId(), item.getCount(), item.getEnchantLevel(), item.getRank(), Long.parseLong(timeStamp), false));
	}

	private boolean addItemToTemporaryWarehouse(Player player, GachaItemHolder reward)
	{
		GachaWarehouse warehouse = this._temporaryWarehouse.getOrDefault(player, null);
		if (warehouse == null)
		{
			return false;
		}
		ItemTemplate template = ItemData.getInstance().getTemplate(reward.getId());
		if (template == null)
		{
			return false;
		}
		boolean isSuccess = false;
		if (!template.isStackable())
		{
			for (long index = 0L; index < reward.getCount(); index++)
			{
				Item item = warehouse.addItem(ItemProcessType.REWARD, reward.getId(), reward.getCount(), player, null);
				if (item == null)
				{
					isSuccess = false;
					break;
				}

				isSuccess = true;
			}
		}
		else
		{
			isSuccess = warehouse.addItem(ItemProcessType.REWARD, reward.getId(), reward.getCount(), player, null) != null;
		}

		return isSuccess;
	}

	public boolean receiveItemsFromTemporaryWarehouse(Player player, List<ItemHolder> requestedItems)
	{
		GachaWarehouse warehouse = this._temporaryWarehouse.getOrDefault(player, null);
		PlayerInventory inventory = player.getInventory();
		if (warehouse != null && inventory != null)
		{
			for (ItemHolder requestedItem : requestedItems)
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(requestedItem.getId());
				List<Item> item = template == null ? null : (template.isStackable() ? List.of(warehouse.getItemByItemId(requestedItem.getId())) : new ArrayList<>(warehouse.getAllItemsByItemId(requestedItem.getId())));
				if (item != null && !item.isEmpty())
				{
					if (template != null && template.isStackable() ? item.get(0).getCount() >= requestedItem.getCount() : item.size() >= requestedItem.getCount())
					{
						continue;
					}

					return false;
				}

				return false;
			}

			for (ItemHolder requestedItem : requestedItems)
			{
				ItemTemplate template = ItemData.getInstance().getTemplate(requestedItem.getId());

				for (Item wareHouseItem : template.isStackable() ? List.of(warehouse.getItemByItemId(requestedItem.getId())) : new ArrayList<>(warehouse.getAllItemsByItemId(requestedItem.getId())))
				{
					warehouse.transferItem(ItemProcessType.TRANSFER, wareHouseItem.getObjectId(), template.isStackable() ? requestedItem.getCount() : 1L, inventory, player, null);
				}
			}

			player.sendItemList();
			return true;
		}
		return false;
	}

	public Collection<Item> getTemporaryWarehouse(Player player)
	{
		GachaWarehouse warehouse = this._temporaryWarehouse.getOrDefault(player, null);
		return warehouse == null ? Collections.emptyList() : (Collection<Item>) warehouse.getItems();
	}

	private UniqueGachaRank randomRank(boolean isGuaranteed)
	{
		int rollRank = Rnd.get(0, this._totalChanceSumma);
		int sumChance = 0;
		UniqueGachaRank rank;
		if ((!isGuaranteed || Rnd.get(0, 100) > 10) && rollRank > (sumChance = sumChance + this._rewardChance.getOrDefault(UniqueGachaRank.RANK_UR, 0)))
		{
			if (!isGuaranteed && rollRank > sumChance + this._rewardChance.getOrDefault(UniqueGachaRank.RANK_SR, 0))
			{
				rank = UniqueGachaRank.RANK_R;
			}
			else
			{
				rank = UniqueGachaRank.RANK_SR;
			}
		}
		else
		{
			rank = UniqueGachaRank.RANK_UR;
		}

		return rank;
	}

	private GachaItemHolder getRandomReward(UniqueGachaRank rank)
	{
		Set<GachaItemHolder> rollRewards = this._rewardItems.getOrDefault(rank, Collections.emptySet());
		int totalItems = rollRewards.size();
		if (totalItems == 0)
		{
			return null;
		}
		int rollChance = 0;
		GachaItemHolder rewardItem = null;

		for (GachaItemHolder item : rollRewards)
		{
			rollChance += item.getItemChance();
		}

		int randomIndex = Rnd.get(0, rollChance);
		int cumulativeChance = 0;

		for (GachaItemHolder item : rollRewards)
		{
			cumulativeChance += item.getItemChance();
			if (randomIndex < cumulativeChance)
			{
				rewardItem = item;
				break;
			}
		}

		return rewardItem;
	}

	public int getTotalRewardCount()
	{
		return this._totalRewardCount;
	}

	public List<GachaItemTimeStampHolder> getGachaCharacterHistory(Player player)
	{
		return this._gachaHistory.getOrDefault(player.getObjectId(), EMPTY_LINKED_LIST);
	}

	private static void restoreGachaHistory(Map<Integer, LinkedList<GachaItemTimeStampHolder>> history)
	{
		try (Connection con = DatabaseFactory.getConnection(); Statement st = con.createStatement(); ResultSet rset = st.executeQuery("SELECT * FROM character_gacha_history ORDER BY receive_time ASC LIMIT 110");)
		{
			while (rset.next())
			{
				int characterId = rset.getInt("char_id");
				int itemId = rset.getInt("item_id");
				long itemCount = rset.getLong("item_count");
				int enchantLevel = rset.getInt("item_enchant");
				int itemRank = rset.getInt("item_rank");
				long receiveTime = rset.getLong("receive_time");
				history.computeIfAbsent(characterId, _ -> new LinkedList<>()).addLast(new GachaItemTimeStampHolder(itemId, itemCount, enchantLevel, UniqueGachaRank.getRankByClientId(itemRank), receiveTime, true));
			}
		}
		catch (Exception var18)
		{
			var18.printStackTrace();
		}
	}

	private static void storeGachaHistory(Map<Integer, LinkedList<GachaItemTimeStampHolder>> history)
	{
		Map<Integer, LinkedList<GachaItemTimeStampHolder>> map = new HashMap<>(history);

		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("INSERT INTO `character_gacha_history`(`char_id`, `item_id`, `item_count`, `item_enchant`, `item_rank`, `receive_time`) VALUES(?, ?, ?, ?, ?, ?)");)
		{
			boolean containsUpdate = false;

			for (Entry<Integer, LinkedList<GachaItemTimeStampHolder>> entry : map.entrySet())
			{
				int charId = entry.getKey();

				for (GachaItemTimeStampHolder item : new LinkedList<>(entry.getValue()))
				{
					if (!item.getStoredStatus())
					{
						statement.setInt(1, charId);
						statement.setInt(2, item.getId());
						statement.setLong(3, item.getCount());
						statement.setInt(4, item.getEnchantLevel());
						statement.setInt(5, item.getRank().getClientId());
						statement.setLong(6, item.getTimeStamp());
						statement.addBatch();
						containsUpdate = true;
					}
				}
			}

			if (containsUpdate)
			{
				statement.executeBatch();
			}
		}
		catch (SQLException var15)
		{
			var15.printStackTrace();
		}
	}

	private static Entry<List<GachaItemHolder>, Boolean> returnEmptyList()
	{
		return new SimpleEntry<>(Collections.emptyList(), false);
	}

	public static UniqueGachaManager getInstance()
	{
		return UniqueGachaManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final UniqueGachaManager INSTANCE = new UniqueGachaManager();
	}
}
