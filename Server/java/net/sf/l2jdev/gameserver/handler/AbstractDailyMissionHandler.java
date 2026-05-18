package net.sf.l2jdev.gameserver.handler;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.DailyMissionStatus;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionDataHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.DailyMissionPlayerEntry;
import net.sf.l2jdev.gameserver.model.actor.holders.player.MissionLevelPlayerDataHolder;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.events.ListenersContainer;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public abstract class AbstractDailyMissionHandler extends ListenersContainer
{
	public static final int MISSION_LEVEL_POINTS = 97224;
	public static final int CLAN_EXP = 94481;
	protected Logger LOGGER = Logger.getLogger(this.getClass().getName());
	private final Map<Integer, DailyMissionPlayerEntry> _entries = new ConcurrentHashMap<>();
	private final DailyMissionDataHolder _holder;

	protected AbstractDailyMissionHandler(DailyMissionDataHolder holder)
	{
		this._holder = holder;
		this.init();
	}

	public DailyMissionDataHolder getHolder()
	{
		return this._holder;
	}

	public abstract boolean isAvailable(Player var1);

	public abstract void init();

	public int getStatus(Player player)
	{
		DailyMissionPlayerEntry entry = this.getPlayerEntry(player.getObjectId(), false);
		return entry != null ? entry.getStatus().getClientId() : DailyMissionStatus.NOT_AVAILABLE.getClientId();
	}

	public int getProgress(Player player)
	{
		DailyMissionPlayerEntry entry = this.getPlayerEntry(player.getObjectId(), false);
		return entry != null ? entry.getProgress() : 0;
	}

	public boolean isRecentlyCompleted(Player player)
	{
		DailyMissionPlayerEntry entry = this.getPlayerEntry(player.getObjectId(), false);
		return entry != null && entry.isRecentlyCompleted();
	}

	public synchronized void reset()
	{
		if (this._holder.dailyReset())
		{
			try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("DELETE FROM character_daily_rewards WHERE rewardId = ? AND status = ?");)
			{
				ps.setInt(1, this._holder.getId());
				ps.setInt(2, DailyMissionStatus.COMPLETED.getClientId());
				ps.execute();
			}
			catch (SQLException var16)
			{
				this.LOGGER.log(Level.WARNING, "Error while clearing data for: " + this.getClass().getSimpleName(), var16);
			}
			finally
			{
				this._entries.clear();
			}
		}
	}

	public boolean requestReward(Player player)
	{
		if (this.isAvailable(player))
		{
			this.giveRewards(player);
			DailyMissionPlayerEntry entry = this.getPlayerEntry(player.getObjectId(), true);
			entry.setStatus(DailyMissionStatus.COMPLETED);
			entry.setLastCompleted(System.currentTimeMillis());
			entry.setRecentlyCompleted(true);
			this.storePlayerEntry(entry);
			return true;
		}
		return false;
	}

	protected void giveRewards(Player player)
	{
		for (ItemHolder holder : this._holder.getRewards())
		{
			switch (holder.getId())
			{
				case 94481:
					Clan clan = player.getClan();
					if (clan != null)
					{
						int expAmount = (int) holder.getCount();
						clan.addExp(player.getObjectId(), expAmount);
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2).addItemName(97224).addLong(expAmount));
					}
					break;
				case 97224:
					int levelPoints = (int) holder.getCount();
					MissionLevelPlayerDataHolder info = player.getMissionLevelProgress();
					info.calculateEXP(levelPoints);
					info.storeInfoInVariable(player);
					player.sendPacket(new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_X_S2).addItemName(97224).addLong(levelPoints));
					break;
				default:
					player.addItem(ItemProcessType.REWARD, holder, player, true);
			}
		}
	}

	protected void storePlayerEntry(DailyMissionPlayerEntry entry)
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("REPLACE INTO character_daily_rewards (charId, rewardId, status, progress, lastCompleted) VALUES (?, ?, ?, ?, ?)");)
		{
			ps.setInt(1, entry.getObjectId());
			ps.setInt(2, entry.getRewardId());
			ps.setInt(3, entry.getStatus().getClientId());
			ps.setInt(4, entry.getProgress());
			ps.setLong(5, entry.getLastCompleted());
			ps.execute();
			this._entries.computeIfAbsent(entry.getObjectId(), _ -> entry);
		}
		catch (Exception var10)
		{
			this.LOGGER.log(Level.WARNING, "Error while saving reward " + entry.getRewardId() + " for player: " + entry.getObjectId() + " in database: ", var10);
		}
	}

	protected DailyMissionPlayerEntry getPlayerEntry(int objectId, boolean createIfNone)
	{
		DailyMissionPlayerEntry entry = this._entries.get(objectId);
		if (entry != null)
		{
			return entry;
		}
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement ps = con.prepareStatement("SELECT * FROM character_daily_rewards WHERE charId = ? AND rewardId = ?");)
		{
			ps.setInt(1, objectId);
			ps.setInt(2, this._holder.getId());

			try (ResultSet rs = ps.executeQuery())
			{
				if (rs.next())
				{
					entry = new DailyMissionPlayerEntry(rs.getInt("charId"), rs.getInt("rewardId"), rs.getInt("status"), rs.getInt("progress"), rs.getLong("lastCompleted"));
					this._entries.put(objectId, entry);
				}
			}
		}
		catch (Exception var15)
		{
			this.LOGGER.log(Level.WARNING, "Error while loading reward " + this._holder.getId() + " for player: " + objectId + " in database: ", var15);
		}

		if (entry == null && createIfNone)
		{
			entry = new DailyMissionPlayerEntry(objectId, this._holder.getId());
			this._entries.put(objectId, entry);
		}

		return entry;
	}
}
