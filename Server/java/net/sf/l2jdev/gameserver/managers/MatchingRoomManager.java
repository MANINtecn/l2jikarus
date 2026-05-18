package net.sf.l2jdev.gameserver.managers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomType;
import net.sf.l2jdev.gameserver.model.groups.matching.PartyMatchingRoomLevelType;

public class MatchingRoomManager
{
	private Set<Player> _waitingList;
	private static final Map<MatchingRoomType, Map<Integer, MatchingRoom>> _rooms = new ConcurrentHashMap<>(2);
	private final AtomicInteger _id = new AtomicInteger(0);

	public void addToWaitingList(Player player)
	{
		if (this._waitingList == null)
		{
			synchronized (this)
			{
				if (this._waitingList == null)
				{
					this._waitingList = ConcurrentHashMap.newKeySet(1);
				}
			}
		}

		this._waitingList.add(player);
	}

	public void removeFromWaitingList(Player player)
	{
		this.getPlayerInWaitingList().remove(player);
	}

	public Set<Player> getPlayerInWaitingList()
	{
		return this._waitingList == null ? Collections.emptySet() : this._waitingList;
	}

	public List<Player> getPlayerInWaitingList(int minLevel, int maxLevel, List<PlayerClass> classIds, String query)
	{
		if (this._waitingList == null)
		{
			return Collections.emptyList();
		}
		List<Player> players = new ArrayList<>();

		for (Player player : this._waitingList)
		{
			if (player != null && player.getLevel() >= minLevel && player.getLevel() <= maxLevel && (classIds == null || classIds.contains(player.getPlayerClass())) && (query == null || query.isEmpty() || player.getName().toLowerCase().contains(query)))
			{
				players.add(player);
			}
		}

		return players;
	}

	public int addMatchingRoom(MatchingRoom room)
	{
		int roomId = this._id.incrementAndGet();
		_rooms.computeIfAbsent(room.getRoomType(), _ -> new ConcurrentHashMap<>()).put(roomId, room);
		return roomId;
	}

	public void removeMatchingRoom(MatchingRoom room)
	{
		_rooms.getOrDefault(room.getRoomType(), Collections.emptyMap()).remove(room.getId());
	}

	public Map<Integer, MatchingRoom> getPartyMathchingRooms()
	{
		return _rooms.get(MatchingRoomType.PARTY);
	}

	public List<MatchingRoom> getPartyMathchingRooms(int location, PartyMatchingRoomLevelType type, int requestorLevel)
	{
		List<MatchingRoom> result = new ArrayList<>();
		if (_rooms.containsKey(MatchingRoomType.PARTY))
		{
			for (MatchingRoom room : _rooms.get(MatchingRoomType.PARTY).values())
			{
				if ((location < 0 || room.getLocation() == location) && (type == PartyMatchingRoomLevelType.ALL || room.getMinLevel() >= requestorLevel && room.getMaxLevel() <= requestorLevel))
				{
					result.add(room);
				}
			}
		}

		return result;
	}

	public Map<Integer, MatchingRoom> getCCMathchingRooms()
	{
		return _rooms.get(MatchingRoomType.COMMAND_CHANNEL);
	}

	public List<MatchingRoom> getCCMathchingRooms(int location, int level)
	{
		List<MatchingRoom> result = new ArrayList<>();
		if (_rooms.containsKey(MatchingRoomType.COMMAND_CHANNEL))
		{
			for (MatchingRoom room : _rooms.get(MatchingRoomType.COMMAND_CHANNEL).values())
			{
				if (room.getLocation() == location && room.getMinLevel() <= level && room.getMaxLevel() >= level)
				{
					result.add(room);
				}
			}
		}

		return result;
	}

	public MatchingRoom getCCMatchingRoom(int roomId)
	{
		return _rooms.getOrDefault(MatchingRoomType.COMMAND_CHANNEL, Collections.emptyMap()).get(roomId);
	}

	public MatchingRoom getPartyMathchingRoom(int location, int level)
	{
		if (_rooms.containsKey(MatchingRoomType.PARTY))
		{
			for (MatchingRoom room : _rooms.get(MatchingRoomType.PARTY).values())
			{
				if (room.getLocation() == location && room.getMinLevel() <= level && room.getMaxLevel() >= level)
				{
					return room;
				}
			}
		}

		return null;
	}

	public MatchingRoom getPartyMathchingRoom(int roomId)
	{
		return _rooms.getOrDefault(MatchingRoomType.PARTY, Collections.emptyMap()).get(roomId);
	}

	public static MatchingRoomManager getInstance()
	{
		return MatchingRoomManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final MatchingRoomManager INSTANCE = new MatchingRoomManager();
	}
}
