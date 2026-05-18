package net.sf.l2jdev.gameserver.model.groups.matching;

import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.data.sql.PartyMatchingHistoryTable;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.enums.UserInfoType;

public abstract class MatchingRoom
{
	private final int _id = MatchingRoomManager.getInstance().addMatchingRoom(this);
	private String _title;
	private int _loot;
	private int _minLevel;
	private int _maxLevel;
	private int _maxCount;
	private Set<Player> _members;
	private Player _leader;

	public MatchingRoom(String title, int loot, int minLevel, int maxLevel, int maxmem, Player leader)
	{
		this._title = title;
		this._loot = loot;
		this._minLevel = minLevel;
		this._maxLevel = maxLevel;
		this._maxCount = maxmem;
		this._leader = leader;
		this.addMember(leader);
		this.onRoomCreation(leader);
		PartyMatchingHistoryTable.getInstance().addRoomHistory(title, leader.getName());
	}

	public Set<Player> getMembers()
	{
		if (this._members == null)
		{
			synchronized (this)
			{
				if (this._members == null)
				{
					this._members = ConcurrentHashMap.newKeySet(1);
				}
			}
		}

		return this._members;
	}

	public void addMember(Player player)
	{
		if (player.getLevel() >= this._minLevel && player.getLevel() <= this._maxLevel && (this._members == null || this._members.size() < this._maxCount))
		{
			this.getMembers().add(player);
			MatchingRoomManager.getInstance().removeFromWaitingList(player);
			this.notifyNewMember(player);
			player.setMatchingRoom(this);
			player.broadcastUserInfo(UserInfoType.CLAN);
		}
		else
		{
			this.notifyInvalidCondition(player);
		}
	}

	public void deleteMember(Player player, boolean kicked)
	{
		boolean leaderChanged = false;
		if (player == this._leader)
		{
			if (this.getMembers().isEmpty())
			{
				MatchingRoomManager.getInstance().removeMatchingRoom(this);
			}
			else
			{
				Iterator<Player> iter = this.getMembers().iterator();
				if (iter.hasNext())
				{
					this._leader = iter.next();
					iter.remove();
					leaderChanged = true;
				}
			}
		}
		else
		{
			this.getMembers().remove(player);
		}

		player.setMatchingRoom(null);
		player.broadcastUserInfo(UserInfoType.CLAN);
		MatchingRoomManager.getInstance().addToWaitingList(player);
		this.notifyRemovedMember(player, kicked, leaderChanged);
	}

	public int getId()
	{
		return this._id;
	}

	public int getLootType()
	{
		return this._loot;
	}

	public int getMinLevel()
	{
		return this._minLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public int getLocation()
	{
		return MapRegionManager.getInstance().getBBs(this._leader.getLocation());
	}

	public int getMembersCount()
	{
		return this._members == null ? 0 : this._members.size();
	}

	public int getMaxMembers()
	{
		return this._maxCount;
	}

	public String getTitle()
	{
		return this._title;
	}

	public Player getLeader()
	{
		return this._leader;
	}

	public boolean isLeader(Player player)
	{
		return player == this._leader;
	}

	public void setMinLevel(int minLevel)
	{
		this._minLevel = minLevel;
	}

	public void setMaxLevel(int maxLevel)
	{
		this._maxLevel = maxLevel;
	}

	public void setLootType(int loot)
	{
		this._loot = loot;
	}

	public void setMaxMembers(int maxCount)
	{
		this._maxCount = maxCount;
	}

	public void setTitle(String title)
	{
		this._title = title;
	}

	protected abstract void onRoomCreation(Player var1);

	protected abstract void notifyInvalidCondition(Player var1);

	protected abstract void notifyNewMember(Player var1);

	protected abstract void notifyRemovedMember(Player var1, boolean var2, boolean var3);

	public abstract void disbandRoom();

	public abstract MatchingRoomType getRoomType();

	public abstract MatchingMemberType getMemberType(Player var1);
}
