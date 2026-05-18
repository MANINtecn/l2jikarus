package net.sf.l2jdev.gameserver.managers.events;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class LetterCollectorManager
{
	protected static final Logger LOGGER = Logger.getLogger(LetterCollectorManager.class.getName());
	private final Map<Integer, LetterCollectorManager.LetterCollectorRewardHolder> _rewards = new HashMap<>();
	private final Map<Integer, List<ItemHolder>> _words = new HashMap<>();
	private final Map<String, Integer> _letter = new HashMap<>();
	private final Map<Integer, Boolean> _needToSumAllChance = new HashMap<>();
	private int _minLevel = 1;
	private int _maxLevel = PlayerConfig.PLAYER_MAXIMUM_LEVEL;

	protected LetterCollectorManager()
	{
	}

	public void init()
	{
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._rewards.size() + " words.");
		LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._letter.size() + " letters.");
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
		if (maxLevel < 1)
		{
			this._maxLevel = PlayerConfig.PLAYER_MAXIMUM_LEVEL;
		}
		else
		{
			this._maxLevel = maxLevel;
		}
	}

	public LetterCollectorManager.LetterCollectorRewardHolder getRewards(int id)
	{
		return this._rewards.get(id);
	}

	public List<ItemHolder> getWord(int id)
	{
		return this._words.get(id);
	}

	public void setRewards(Map<Integer, LetterCollectorManager.LetterCollectorRewardHolder> rewards)
	{
		this._rewards.putAll(rewards);
	}

	public void setWords(Map<Integer, List<ItemHolder>> words)
	{
		this._words.putAll(words);
	}

	public void addRewards(int id, LetterCollectorManager.LetterCollectorRewardHolder rewards)
	{
		this._rewards.put(id, rewards);
	}

	public void addWords(int id, List<ItemHolder> words)
	{
		this._words.put(id, words);
	}

	public void resetField()
	{
		this._minLevel = 1;
		this._rewards.clear();
		this._words.clear();
		this._needToSumAllChance.clear();
	}

	public void setLetters(Map<String, Integer> letters)
	{
		this._letter.putAll(letters);
	}

	public Map<String, Integer> getLetters()
	{
		return this._letter;
	}

	public void setNeedToSumAllChance(int id, boolean needToSumAllChance)
	{
		this._needToSumAllChance.put(id, needToSumAllChance);
	}

	public boolean getNeedToSumAllChance(int id)
	{
		return this._needToSumAllChance.get(id);
	}

	public static LetterCollectorManager getInstance()
	{
		return LetterCollectorManager.SingletonHolder.INSTANCE;
	}

	public static class LetterCollectorRewardHolder
	{
		final List<ItemChanceHolder> _rewards;
		final double _chance;

		public LetterCollectorRewardHolder(List<ItemChanceHolder> rewards, double chance)
		{
			this._rewards = rewards;
			this._chance = chance;
		}

		public List<ItemChanceHolder> getRewards()
		{
			return this._rewards;
		}

		public double getChance()
		{
			return this._chance;
		}
	}

	private static class SingletonHolder
	{
		protected static final LetterCollectorManager INSTANCE = new LetterCollectorManager();
	}
}
