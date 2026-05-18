package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.model.MissionLevelHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;

public class MissionLevel implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(MissionLevel.class.getName());
	private final Map<Integer, MissionLevelHolder> _template = new HashMap<>();
	private int _currentSeason;

	protected MissionLevel()
	{
		this.load();
	}

	@Override
	public void load()
	{
		this._template.clear();
		this.parseDatapackFile("data/MissionLevel.xml");
		if (this._currentSeason > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._template.size() + " seasons.");
		}
		else
		{
			this._template.clear();
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> {
			this.forEach(listNode, "current", current -> this._currentSeason = this.parseInteger(current.getAttributes(), "season"));
			this.forEach(listNode, "missionLevel", missionNode -> {
				StatSet missionSet = new StatSet(this.parseAttributes(missionNode));
				AtomicInteger season = new AtomicInteger(missionSet.getInt("season"));
				AtomicInteger maxLevel = new AtomicInteger(missionSet.getInt("maxLevel"));
				AtomicBoolean bonusRewardIsAvailable = new AtomicBoolean(missionSet.getBoolean("bonusRewardIsAvailable"));
				AtomicBoolean bonusRewardByLevelUp = new AtomicBoolean(missionSet.getBoolean("bonusRewardByLevelUP"));
				AtomicReference<Map<Integer, ItemHolder>> keyReward = new AtomicReference<>(new HashMap<>());
				AtomicReference<Map<Integer, ItemHolder>> normalReward = new AtomicReference<>(new HashMap<>());
				AtomicReference<Map<Integer, Integer>> xpForLevel = new AtomicReference<>(new HashMap<>());
				AtomicReference<ItemHolder> specialReward = new AtomicReference<>();
				AtomicReference<ItemHolder> bonusReward = new AtomicReference<>();
				this.forEach(missionNode, "expTable", expListNode -> this.forEach(expListNode, "exp", expNode -> {
					StatSet expSet = new StatSet(this.parseAttributes(expNode));
					xpForLevel.get().put(expSet.getInt("level"), expSet.getInt("amount"));
				}));
				this.forEach(missionNode, "baseRewards", baseRewardsNode -> this.forEach(baseRewardsNode, "baseReward", rewards -> {
					StatSet rewardsSet = new StatSet(this.parseAttributes(rewards));
					normalReward.get().put(rewardsSet.getInt("level"), new ItemHolder(rewardsSet.getInt("itemId"), rewardsSet.getLong("itemCount")));
				}));
				this.forEach(missionNode, "keyRewards", keyRewardsNode -> this.forEach(keyRewardsNode, "keyReward", rewards -> {
					StatSet rewardsSet = new StatSet(this.parseAttributes(rewards));
					keyReward.get().put(rewardsSet.getInt("level"), new ItemHolder(rewardsSet.getInt("itemId"), rewardsSet.getLong("itemCount")));
				}));
				this.forEach(missionNode, "specialReward", specialRewardNode -> {
					StatSet specialRewardSet = new StatSet(this.parseAttributes(specialRewardNode));
					specialReward.set(new ItemHolder(specialRewardSet.getInt("itemId"), specialRewardSet.getLong("itemCount")));
				});
				this.forEach(missionNode, "bonusReward", bonusRewardNode -> {
					StatSet bonusRewardSet = new StatSet(this.parseAttributes(bonusRewardNode));
					bonusReward.set(new ItemHolder(bonusRewardSet.getInt("itemId"), bonusRewardSet.getLong("itemCount")));
				});
				int bonusLevel = normalReward.get().keySet().stream().max(Integer::compare).orElse(maxLevel.get());
				if (bonusLevel == maxLevel.get())
				{
					bonusLevel--;
				}

				this._template.put(season.get(), new MissionLevelHolder(maxLevel.get(), bonusLevel + 1, xpForLevel.get(), normalReward.get(), keyReward.get(), specialReward.get(), bonusReward.get(), bonusRewardByLevelUp.get(), bonusRewardIsAvailable.get()));
			});
		});
	}

	public int getCurrentSeason()
	{
		return this._currentSeason;
	}

	public MissionLevelHolder getMissionBySeason(int season)
	{
		return this._template.getOrDefault(season, null);
	}

	public static MissionLevel getInstance()
	{
		return MissionLevel.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final MissionLevel INSTANCE = new MissionLevel();
	}
}
