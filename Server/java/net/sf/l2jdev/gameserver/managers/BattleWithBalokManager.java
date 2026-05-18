package net.sf.l2jdev.gameserver.managers;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import net.sf.l2jdev.gameserver.config.GrandBossConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class BattleWithBalokManager
{
	private final Map<Integer, Integer> _playerPoints = new ConcurrentHashMap<>();
	private boolean _inBattle = false;
	private int _reward = 0;
	private int _globalPoints = 0;
	private int _globalStage = 0;
	private int _globalStatus = 0;

	public void addPointsForPlayer(Player player, boolean isScorpion)
	{
		int pointsToAdd = isScorpion ? GrandBossConfig.BALOK_POINTS_PER_MONSTER * 10 : GrandBossConfig.BALOK_POINTS_PER_MONSTER;
		int currentPoints = this._playerPoints.computeIfAbsent(player.getObjectId(), _ -> 0);
		int sum = pointsToAdd + currentPoints;
		this._playerPoints.put(player.getObjectId(), sum);
	}

	public Map<Integer, Integer> getTopPlayers(int count)
	{
		return this._playerPoints.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).limit(count).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
	}

	public int getPlayerRank(Player player)
	{
		if (!this._playerPoints.containsKey(player.getObjectId()))
		{
			return 0;
		}
		Map<Integer, Integer> sorted = this._playerPoints.entrySet().stream().sorted(Entry.comparingByValue(Comparator.reverseOrder())).collect(Collectors.toMap(Entry::getKey, Entry::getValue, (e1, _) -> e1, LinkedHashMap::new));
		return sorted.keySet().stream().toList().indexOf(player.getObjectId()) + 1;
	}

	public int getMonsterPoints(Player player)
	{
		return this._playerPoints.computeIfAbsent(player.getObjectId(), _ -> 0);
	}

	public int getReward()
	{
		return this._reward;
	}

	public void setReward(int value)
	{
		this._reward = value;
	}

	public boolean getInBattle()
	{
		return this._inBattle;
	}

	public void setInBattle(boolean value)
	{
		this._inBattle = value;
	}

	public int getGlobalPoints()
	{
		return this._globalPoints;
	}

	public void setGlobalPoints(int value)
	{
		this._globalPoints = value;
	}

	public int getGlobalStage()
	{
		return this._globalStage;
	}

	public void setGlobalStage(int value)
	{
		this._globalStage = value;
	}

	public int getGlobalStatus()
	{
		return this._globalStatus;
	}

	public void setGlobalStatus(int value)
	{
		this._globalStatus = value;
	}

	public static BattleWithBalokManager getInstance()
	{
		return BattleWithBalokManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final BattleWithBalokManager INSTANCE = new BattleWithBalokManager();
	}
}
