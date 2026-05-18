package net.sf.l2jdev.gameserver.managers.events;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.logging.Logger;
import java.util.stream.Stream;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.data.xml.ItemData;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.enums.BlackCouponRestoreCategory;
import net.sf.l2jdev.gameserver.model.item.enums.BodyPart;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemRestoreHolder;

public class BlackCouponManager
{
	private static final Logger LOGGER = Logger.getLogger(BlackCouponManager.class.getName());
	private ItemHolder _coupon;
	private int _multisellId;
	private final Map<Integer, EnumMap<BlackCouponRestoreCategory, List<ItemRestoreHolder>>> _playerRecords = new HashMap<>();
	private final Map<Integer, HashMap<BlackCouponRestoreCategory, Integer>> _xmlRestoreList = new HashMap<>(new HashMap<>());
	private final List<ItemRestoreHolder> _holdersToStore = new ArrayList<>();
	private final List<ItemRestoreHolder> _holdersToDelete = new ArrayList<>();
	private boolean _restoreIdFromXml;
	private boolean _eventPeriod;
	private final long[] _dateRange = new long[]
	{
		1651134344000L,
		2524608000000L
	};

	protected BlackCouponManager()
	{
	}

	public void setCouponItem(ItemHolder coupon)
	{
		this._coupon = coupon;
	}

	public int getBlackCouponId()
	{
		return this._coupon.getId();
	}

	public long getBlackCouponCount()
	{
		return this._coupon.getCount();
	}

	public void setMultisellId(int id)
	{
		this._multisellId = id;
	}

	public int getMultisellId()
	{
		return this._multisellId;
	}

	public void setRestoreIdFromXml(boolean restoreIdByXml)
	{
		this._restoreIdFromXml = restoreIdByXml;
	}

	public void setXmlRestoreList(Map<Integer, HashMap<BlackCouponRestoreCategory, Integer>> list)
	{
		this._xmlRestoreList.putAll(list);
	}

	public void setEventStatus(boolean status)
	{
		this._eventPeriod = status;
	}

	public boolean getEventStatus()
	{
		return this._eventPeriod;
	}

	public void setDateRange(long first, long second)
	{
		this._dateRange[0] = first;
		this._dateRange[1] = second;
	}

	public boolean isInEventRange(long addTime)
	{
		return this._dateRange[0] < addTime && this._dateRange[1] > addTime;
	}

	public BlackCouponRestoreCategory getCategoryByItemId(int itemId)
	{
		ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
		if (this._restoreIdFromXml)
		{
			return this._xmlRestoreList.containsKey(itemId) ? this._xmlRestoreList.get(item.getId()).keySet().stream().findFirst().orElse(null) : null;
		}
		else if (item.isWeapon() || item.isMagicWeapon())
		{
			return BlackCouponRestoreCategory.WEAPON;
		}
		else if (item.isArmor())
		{
			return BlackCouponRestoreCategory.ARMOR;
		}
		else
		{
			return item.getBodyPart() != BodyPart.L_EAR && item.getBodyPart() != BodyPart.R_EAR && item.getBodyPart() != BodyPart.LR_EAR && item.getBodyPart() != BodyPart.L_FINGER && item.getBodyPart() != BodyPart.R_FINGER && item.getBodyPart() != BodyPart.LR_FINGER && item.getBodyPart() != BodyPart.NECK && item.getBodyPart() != BodyPart.ALLDRESS ? BlackCouponRestoreCategory.MISC : BlackCouponRestoreCategory.BOSS_ACCESSORIES;
		}
	}

	public List<ItemRestoreHolder> getRestoreItems(int ownerId, BlackCouponRestoreCategory category)
	{
		EnumMap<BlackCouponRestoreCategory, List<ItemRestoreHolder>> ownerMap = this._playerRecords.get(ownerId);
		return ownerMap == null ? Collections.emptyList() : ownerMap.getOrDefault(category, Collections.emptyList());
	}

	public synchronized void removePlayerRecords(int ownerId)
	{
		List<ItemRestoreHolder> toDelete = this._holdersToDelete.stream().filter(holderx -> holderx.getOwnerId() == ownerId).toList();
		if (!toDelete.isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM `black_coupon` WHERE owner_id = ? AND item_id = ? AND enchant_level = ? AND add_time = ?");)
			{
				for (ItemRestoreHolder holder : toDelete)
				{
					ps.setInt(1, holder.getOwnerId());
					ps.setInt(2, holder.getDestroyedItemId());
					ps.setShort(3, holder.getEnchantLevel());
					ps.setLong(4, holder.getDestroyDate());
					ps.addBatch();
				}

				ps.executeBatch();
			}
			catch (Exception var17)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Problem deleting player records before removal. " + var17.getMessage());
			}

			this._holdersToDelete.removeAll(toDelete);
		}

		List<ItemRestoreHolder> toStore = this._holdersToStore.stream().filter(holderx -> holderx.getOwnerId() == ownerId).toList();
		if (!toStore.isEmpty())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO `black_coupon` (`owner_id`, `item_id`, `enchant_level`, `add_time`) values (?, ?, ?, ?)");)
			{
				for (ItemRestoreHolder holder : toStore)
				{
					if (holder.needToStore())
					{
						ps.setInt(1, holder.getOwnerId());
						ps.setInt(2, holder.getDestroyedItemId());
						ps.setShort(3, holder.getEnchantLevel());
						ps.setLong(4, holder.getDestroyDate());
						ps.addBatch();
						holder.setNeedToStore(false);
					}
				}

				ps.executeBatch();
			}
			catch (Exception var14)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Problem storing player records before removal. " + var14.getMessage());
			}

			this._holdersToStore.removeAll(toStore);
		}

		this._playerRecords.remove(ownerId);
	}

	public synchronized void restorePlayerRecords(int ownerId)
	{
		if (this._eventPeriod)
		{
			EnumMap<BlackCouponRestoreCategory, List<ItemRestoreHolder>> formedMap = new EnumMap<>(BlackCouponRestoreCategory.class);
			formedMap.put(BlackCouponRestoreCategory.WEAPON, new ArrayList<>());
			formedMap.put(BlackCouponRestoreCategory.ARMOR, new ArrayList<>());
			formedMap.put(BlackCouponRestoreCategory.BOSS_ACCESSORIES, new ArrayList<>());
			formedMap.put(BlackCouponRestoreCategory.MISC, new ArrayList<>());

			try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("SELECT * FROM `black_coupon` WHERE `owner_id` = ?");)
			{
				ps.setInt(1, ownerId);
				ResultSet rs = ps.executeQuery();

				while (rs.next())
				{
					int owner_id = rs.getInt("owner_id");
					int item_id = rs.getInt("item_id");
					short enchant_level = rs.getShort("enchant_level");
					long destroyTime = rs.getLong("add_time");
					BlackCouponRestoreCategory category = this.getCategoryByItemId(item_id);
					if (category != null)
					{
						ItemRestoreHolder holder = new ItemRestoreHolder(owner_id, item_id, enchant_level, destroyTime, false);
						Optional<Integer> any = this._xmlRestoreList.get(item_id).values().stream().findAny();
						holder.setRepairItemId(any.orElseGet(holder::getDestroyedItemId));
						if (!this._holdersToDelete.isEmpty())
						{
							Stream<ItemRestoreHolder> check = this._holdersToDelete.stream().filter(h -> h.getOwnerId() == owner_id).filter(h -> h.getDestroyDate() == destroyTime).filter(h -> h.getDestroyedItemId() == item_id).filter(h -> h.getEnchantLevel() == enchant_level);
							if (check.findFirst().isPresent())
							{
								continue;
							}
						}

						formedMap.get(category).add(holder);
					}
				}

				ps.closeOnCompletion();
			}
			catch (Exception var19)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Problem loading event player records. " + var19.getMessage());
			}

			this._playerRecords.put(ownerId, formedMap);
		}
	}

	public synchronized void createNewRecord(int objectId, int itemId, short enchantLevel)
	{
		ItemRestoreHolder holder = new ItemRestoreHolder(objectId, itemId, enchantLevel, System.currentTimeMillis(), true);
		this._holdersToStore.add(holder);
		if (this._eventPeriod)
		{
			BlackCouponRestoreCategory category = this.getCategoryByItemId(itemId);
			if (category != null)
			{
				Optional<Integer> any = this._xmlRestoreList.getOrDefault(itemId, new HashMap<>()).values().stream().findAny();
				holder.setRepairItemId(any.orElseGet(holder::getDestroyedItemId));
				EnumMap<BlackCouponRestoreCategory, List<ItemRestoreHolder>> playerRecords = this._playerRecords.computeIfAbsent(objectId, _ -> new EnumMap<>(BlackCouponRestoreCategory.class));
				playerRecords.computeIfAbsent(category, _ -> new ArrayList<>()).add(holder);
			}
		}
	}

	public synchronized void addToDelete(BlackCouponRestoreCategory category, ItemRestoreHolder holder)
	{
		if (holder.needToStore())
		{
			holder.setNeedToStore(false);
			this._holdersToStore.remove(holder);
		}

		this._holdersToDelete.add(holder);
		this._playerRecords.get(holder.getOwnerId()).get(category).remove(holder);
	}

	public void storeMe()
	{
		this.deleteRestoreHolders();
		this.saveRestoreHolders();
	}

	private synchronized void deleteRestoreHolders()
	{
		if (!this._holdersToDelete.isEmpty())
		{
			try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("DELETE FROM `black_coupon` WHERE owner_id = ? AND item_id = ? AND enchant_level = ? AND add_time = ?");)
			{
				for (ItemRestoreHolder holder : this._holdersToDelete)
				{
					ps.setInt(1, holder.getOwnerId());
					ps.setInt(2, holder.getDestroyedItemId());
					ps.setShort(3, holder.getEnchantLevel());
					ps.setLong(4, holder.getDestroyDate());
					ps.addBatch();
				}

				ps.executeBatch();
				ps.closeOnCompletion();
			}
			catch (Exception var9)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Problem deleting event player records. " + var9.getMessage());
			}

			this._holdersToDelete.clear();
		}
	}

	private synchronized void saveRestoreHolders()
	{
		if (!this._holdersToStore.isEmpty())
		{
			try (Connection conn = DatabaseFactory.getConnection(); PreparedStatement ps = conn.prepareStatement("REPLACE INTO `black_coupon` (`owner_id`, `item_id`, `enchant_level`, `add_time`) values (?, ?, ?, ?)");)
			{
				for (ItemRestoreHolder holder : this._holdersToStore)
				{
					if (holder.needToStore())
					{
						ps.setInt(1, holder.getOwnerId());
						ps.setInt(2, holder.getDestroyedItemId());
						ps.setShort(3, holder.getEnchantLevel());
						ps.setLong(4, holder.getDestroyDate());
						ps.addBatch();
						holder.setNeedToStore(false);
					}
				}

				ps.executeBatch();
				ps.closeOnCompletion();
			}
			catch (Exception var9)
			{
				LOGGER.warning(this.getClass().getSimpleName() + ": Problem storing event player records. " + var9.getMessage());
			}

			this._holdersToStore.clear();
		}
	}

	public static BlackCouponManager getInstance()
	{
		return BlackCouponManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final BlackCouponManager INSTANCE = new BlackCouponManager();
	}
}
