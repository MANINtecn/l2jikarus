package net.sf.l2jdev.gameserver.model.itemcontainer;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.RandomCraftConfig;
import net.sf.l2jdev.gameserver.data.holders.RandomCraftRewardItemHolder;
import net.sf.l2jdev.gameserver.data.xml.RandomCraftData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.RandomCraftRequest;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExItemAnnounce;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftRandomInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftRandomMake;
import net.sf.l2jdev.gameserver.network.serverpackets.randomcraft.ExCraftRandomRefresh;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class PlayerRandomCraft
{
	private static final Logger LOGGER = Logger.getLogger(PlayerRandomCraft.class.getName());
	public static final int MAX_FULL_CRAFT_POINTS = 99;
	public static final int MAX_CRAFT_POINTS = 20000000;
	private final Player _player;
	private final List<RandomCraftRewardItemHolder> _rewardList = new ArrayList<>(5);
	private int _fullCraftPoints = 0;
	private int _craftPoints = 0;
	private boolean _isSayhaRoll = false;

	public PlayerRandomCraft(Player player)
	{
		this._player = player;
	}

	public void restore()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM character_random_craft WHERE charId=?");)
		{
			ps.setInt(1, this._player.getObjectId());

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					try
					{
						this._fullCraftPoints = rs.getInt("random_craft_full_points");
						this._craftPoints = rs.getInt("random_craft_points");
						this._isSayhaRoll = rs.getBoolean("sayha_roll");

						for (int i = 1; i <= 5; i++)
						{
							int itemId = rs.getInt("item_" + i + "_id");
							long itemCount = rs.getLong("item_" + i + "_count");
							boolean itemLocked = rs.getBoolean("item_" + i + "_locked");
							int itemLockLeft = rs.getInt("item_" + i + "_lock_left");
							RandomCraftRewardItemHolder holder = new RandomCraftRewardItemHolder(itemId, itemCount, itemLocked, itemLockLeft);
							this._rewardList.add(i - 1, holder);
						}
					}
					catch (Exception var14)
					{
						LOGGER.warning("Could not restore random craft for " + this._player);
					}
				}
				else
				{
					this.storeNew();
				}
			}
		}
		catch (Exception var18)
		{
			LOGGER.log(Level.WARNING, "Could not restore random craft for " + this._player, var18);
		}
	}

	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement ps = con.prepareStatement("UPDATE character_random_craft SET random_craft_full_points=?,random_craft_points=?,sayha_roll=?,item_1_id=?,item_1_count=?,item_1_locked=?,item_1_lock_left=?,item_2_id=?,item_2_count=?,item_2_locked=?,item_2_lock_left=?,item_3_id=?,item_3_count=?,item_3_locked=?,item_3_lock_left=?,item_4_id=?,item_4_count=?,item_4_locked=?,item_4_lock_left=?,item_5_id=?,item_5_count=?,item_5_locked=?,item_5_lock_left=? WHERE charId=?");)
		{
			ps.setInt(1, this._fullCraftPoints);
			ps.setInt(2, this._craftPoints);
			ps.setBoolean(3, this._isSayhaRoll);

			for (int i = 0; i < 5; i++)
			{
				if (this._rewardList.size() >= i + 1)
				{
					RandomCraftRewardItemHolder holder = this._rewardList.get(i);
					ps.setInt(4 + i * 4, holder == null ? 0 : holder.getItemId());
					ps.setLong(5 + i * 4, holder == null ? 0L : holder.getItemCount());
					ps.setBoolean(6 + i * 4, holder != null && holder.isLocked());
					ps.setInt(7 + i * 4, holder == null ? 20 : holder.getLockLeft());
				}
				else
				{
					ps.setInt(4 + i * 4, 0);
					ps.setLong(5 + i * 4, 0L);
					ps.setBoolean(6 + i * 4, false);
					ps.setInt(7 + i * 4, 20);
				}
			}

			ps.setInt(24, this._player.getObjectId());
			ps.execute();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not store RandomCraft for: " + this._player, var9);
		}
	}

	public void storeNew()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("INSERT INTO character_random_craft VALUES(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");)
		{
			ps.setInt(1, this._player.getObjectId());
			ps.setInt(2, this._fullCraftPoints);
			ps.setInt(3, this._craftPoints);
			ps.setBoolean(4, this._isSayhaRoll);

			for (int i = 0; i < 5; i++)
			{
				ps.setInt(5 + i * 4, 0);
				ps.setLong(6 + i * 4, 0L);
				ps.setBoolean(7 + i * 4, false);
				ps.setInt(8 + i * 4, 0);
			}

			ps.executeUpdate();
		}
		catch (Exception var9)
		{
			LOGGER.log(Level.SEVERE, "Could not store new RandomCraft for: " + this._player, var9.getMessage());
		}
	}

	public void refresh()
	{
		if (!this._player.hasItemRequest() && !this._player.hasRequest(RandomCraftRequest.class))
		{
			this._player.addRequest(new RandomCraftRequest(this._player));
			if (this._fullCraftPoints > 0 && this._player.reduceAdena(ItemProcessType.FEE, RandomCraftConfig.RANDOM_CRAFT_REFRESH_FEE, this._player, true))
			{
				this._player.sendPacket(new ExCraftInfo(this._player));
				this._player.sendPacket(new ExCraftRandomRefresh());
				this._fullCraftPoints--;
				if (this._isSayhaRoll)
				{
					this._player.addItem(ItemProcessType.REWARD, 91641, 2L, this._player, true);
					this._isSayhaRoll = false;
				}

				this._player.sendPacket(new ExCraftInfo(this._player));

				for (int i = 0; i < 5; i++)
				{
					RandomCraftRewardItemHolder holder;
					if (i > this._rewardList.size() - 1)
					{
						holder = null;
					}
					else
					{
						holder = this._rewardList.get(i);
					}

					if (holder == null)
					{
						this._rewardList.add(i, this.getNewReward());
					}
					else if (!holder.isLocked())
					{
						this._rewardList.set(i, this.getNewReward());
					}
					else
					{
						holder.decLock();
					}
				}

				this._player.sendPacket(new ExCraftRandomInfo(this._player));
			}

			this._player.removeRequest(RandomCraftRequest.class);
		}
	}

	private RandomCraftRewardItemHolder getNewReward()
	{
		if (RandomCraftData.getInstance().isEmpty())
		{
			return null;
		}
		RandomCraftRewardItemHolder result = null;

		while (result == null)
		{
			result = RandomCraftData.getInstance().getNewReward();

			for (RandomCraftRewardItemHolder reward : this._rewardList)
			{
				if (reward.getItemId() == result.getItemId())
				{
					result = null;
					break;
				}
			}
		}

		return result;
	}

	public void make()
	{
		if (!this._player.hasItemRequest() && !this._player.hasRequest(RandomCraftRequest.class))
		{
			this._player.addRequest(new RandomCraftRequest(this._player));
			if (this._player.reduceAdena(ItemProcessType.FEE, RandomCraftConfig.RANDOM_CRAFT_CREATE_FEE, this._player, true))
			{
				int madeId = Rnd.get(0, 4);
				RandomCraftRewardItemHolder holder = this._rewardList.get(madeId);
				this._rewardList.clear();
				int itemId = holder.getItemId();
				long itemCount = holder.getItemCount();
				Item item = this._player.addItem(ItemProcessType.CRAFT, itemId, itemCount, this._player, true);
				if (RandomCraftData.getInstance().isAnnounce(itemId))
				{
					Broadcast.toAllOnlinePlayers(new ExItemAnnounce(this._player, item, 2));
				}

				this._player.sendPacket(new ExCraftRandomMake(itemId, itemCount));
				this._player.sendPacket(new ExCraftRandomInfo(this._player));
			}

			this._player.removeRequest(RandomCraftRequest.class);
		}
	}

	public List<RandomCraftRewardItemHolder> getRewards()
	{
		return this._rewardList;
	}

	public int getFullCraftPoints()
	{
		return this._fullCraftPoints;
	}

	public void addFullCraftPoints(int value)
	{
		this.addFullCraftPoints(value, false);
	}

	public void addFullCraftPoints(int value, boolean broadcast)
	{
		this._fullCraftPoints = Math.min(this._fullCraftPoints + value, 99);
		if (this._craftPoints >= 20000000)
		{
			this._craftPoints = 0;
		}

		if (value > 0)
		{
			this._isSayhaRoll = true;
		}

		if (broadcast)
		{
			this._player.sendPacket(new ExCraftInfo(this._player));
		}
	}

	public void removeFullCraftPoints(int value)
	{
		this._fullCraftPoints -= value;
		this._player.sendPacket(new ExCraftInfo(this._player));
	}

	public void addCraftPoints(int value)
	{
		if (this._craftPoints - 1 < 20000000)
		{
			this._craftPoints += value;
		}

		int fullPointsToAdd = this._craftPoints / 20000000;
		int pointsToRemove = 20000000 * fullPointsToAdd;
		this._craftPoints -= pointsToRemove;
		this.addFullCraftPoints(fullPointsToAdd);
		if (this._fullCraftPoints >= 99)
		{
			this._craftPoints = 20000000;
		}

		SystemMessage sm = new SystemMessage(SystemMessageId.CRAFT_POINTS_S1);
		sm.addLong(value);
		this._player.sendPacket(sm);
		this._player.sendPacket(new ExCraftInfo(this._player));
	}

	public int getCraftPoints()
	{
		return this._craftPoints;
	}

	public void setIsSayhaRoll(boolean value)
	{
		this._isSayhaRoll = value;
	}

	public boolean isSayhaRoll()
	{
		return this._isSayhaRoll;
	}

	public int getLockedSlotCount()
	{
		int count = 0;

		for (RandomCraftRewardItemHolder holder : this._rewardList)
		{
			if (holder.isLocked())
			{
				count++;
			}
		}

		return count;
	}
}
