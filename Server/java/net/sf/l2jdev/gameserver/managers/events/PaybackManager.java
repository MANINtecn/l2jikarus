package net.sf.l2jdev.gameserver.managers.events;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;

public class PaybackManager
{
	private static final Logger LOGGER = Logger.getLogger(PaybackManager.class.getName());
	private int _coinID;
	private int _minLevel;
	private int _maxLevel;
	private final List<Long> _multisells = new ArrayList<>();
	private long _endTime;
	private final ConcurrentHashMap<Integer, PaybackManager.PaybackManagerHolder> _rewards = new ConcurrentHashMap<>();
	private final Map<String, Map<String, String>> _local = new HashMap<>();
	private final Map<Integer, StatSet> _playerProgress = new HashMap<>();
	public static final String GOLDEN_WHEEL_VAR = "GOLDEN_WHEEL_VAR";

	protected PaybackManager()
	{
	}

	public void init()
	{
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._rewards.size() + " rewards.");
	}

	public int getCoinID()
	{
		return this._coinID;
	}

	public void setCoinID(int coinID)
	{
		this._coinID = coinID;
	}

	public int getMinLevel()
	{
		return this._minLevel;
	}

	public void setMinLevel(int minLevel)
	{
		this._minLevel = minLevel;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public void setMaxLevel(int maxLevel)
	{
		this._maxLevel = maxLevel;
	}

	public ConcurrentHashMap<Integer, PaybackManager.PaybackManagerHolder> getRewards()
	{
		return this._rewards;
	}

	public PaybackManager.PaybackManagerHolder getRewardsById(int id)
	{
		return this._rewards.get(id);
	}

	public void addRewardsToHolder(int id, long count, List<ItemChanceHolder> rewards)
	{
		this._rewards.put(id, new PaybackManager.PaybackManagerHolder(rewards, count));
	}

	public List<Long> getMultisells()
	{
		return this._multisells;
	}

	public void addToMultisells(List<Long> ids)
	{
		this._multisells.addAll(ids);
	}

	public long getEndTime()
	{
		return this._endTime;
	}

	public void setEndTime(long endTime)
	{
		this._endTime = endTime;
	}

	public void resetField()
	{
		this._coinID = 94834;
		this._minLevel = 1;
		this._maxLevel = -1;
		this._multisells.clear();
		this._rewards.clear();
	}

	@SuppressWarnings("unchecked")
	public void addLocalString(String lang, String type, String message)
	{
		this._local.putIfAbsent(lang, null);
		Map<String, String> strings = (Map<String, String>) (this._local.get(lang) == null ? new HashMap<>() : this._local.get(lang));
		strings.put(type, message);
		this._local.replace(lang, strings);
	}

	public Map<String, String> getLocalString(String lang)
	{
		return !this._local.isEmpty() && !this._local.containsKey(lang) ? null : this._local.get(lang);
	}

	public long getPlayerConsumedProgress(int objectID)
	{
		return this._playerProgress.getOrDefault(objectID, null).getInt("CONSUMED_COINS");
	}

	public void changePlayerConsumedProgress(int objectID, long newValue)
	{
		if (this._playerProgress.getOrDefault(objectID, null) != null)
		{
			StatSet set = this._playerProgress.getOrDefault(objectID, null);
			set.remove("CONSUMED_COINS");
			set.set("CONSUMED_COINS", newValue);
			this._playerProgress.replace(objectID, set);
		}
	}

	public List<Integer> getPlayerMissionProgress(int objectID)
	{
		return this._playerProgress.get(objectID).getIntegerList("MISSION_PROGRESS");
	}

	public void changeMissionProgress(int objectID, int missionID, int status)
	{
		if (this._playerProgress.getOrDefault(objectID, null) != null)
		{
			List<Integer> currentProgress = this._playerProgress.get(objectID).getIntegerList("MISSION_PROGRESS");
			currentProgress.set(missionID, status);
			this._playerProgress.get(objectID).setIntegerList("MISSION_PROGRESS", currentProgress);
		}
	}

	public void storePlayerProgress(Player player)
	{
		player.getVariables().set("GOLDEN_WHEEL_VAR", this.getStringVariable(this._playerProgress.get(player.getObjectId())));
	}

	public String getStringVariable(StatSet progress)
	{
		StringBuilder returnString = new StringBuilder();
		returnString.append("MISSION_PROGRESS").append("=").append("[").append(progress.getString("MISSION_PROGRESS")).append("]");
		returnString.append(":");
		returnString.append("CONSUMED_COINS").append("=").append(progress.getLong("CONSUMED_COINS"));
		return returnString.toString();
	}

	public StatSet getStatSetVariable(String variable)
	{
		String[] splitsVariable = variable.split(":");
		List<Integer> missionProgress = new ArrayList<>();

		for (String temp : splitsVariable[0].split("=")[1].split(","))
		{
			missionProgress.add(Integer.parseInt(temp.replace("[", "").replace("]", "").replace(" ", "")));
		}

		Long consumed = Long.parseLong(splitsVariable[1].split("=")[1]);
		StatSet returnSet = new StatSet();
		returnSet.set("CONSUMED_COINS", consumed);
		returnSet.setIntegerList("MISSION_PROGRESS", missionProgress);
		return returnSet;
	}

	public void addPlayerToList(Player player)
	{
		String variable = player.getVariables().getString("GOLDEN_WHEEL_VAR", null);
		StatSet progress;
		if (variable == null)
		{
			progress = new StatSet();
			progress.set("CONSUMED_COINS", 0);
			List<Integer> temp = new ArrayList<>();
			this._rewards.keySet().forEach(_ -> temp.add(0));
			progress.setIntegerList("MISSION_PROGRESS", temp);
		}
		else
		{
			progress = this.getStatSetVariable(variable);
		}

		this._playerProgress.put(player.getObjectId(), progress);
	}

	public void removePlayerFromList(int objectID)
	{
		this._playerProgress.remove(objectID);
	}

	public static PaybackManager getInstance()
	{
		return PaybackManager.SingletonHolder.INSTANCE;
	}

	public static class PaybackManagerHolder
	{
		final List<ItemChanceHolder> _rewards;
		final long _count;

		public PaybackManagerHolder(List<ItemChanceHolder> rewards, long count)
		{
			this._rewards = rewards;
			this._count = count;
		}

		public List<ItemChanceHolder> getRewards()
		{
			return this._rewards;
		}

		public long getCount()
		{
			return this._count;
		}
	}

	private static class SingletonHolder
	{
		protected static final PaybackManager INSTANCE = new PaybackManager();
	}
}
