package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.data.holders.RandomCraftExtractDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RandomCraftRewardDataHolder;
import net.sf.l2jdev.gameserver.data.holders.RandomCraftRewardItemHolder;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import org.w3c.dom.Document;

public class RandomCraftData implements IXmlReader
{
	private static final Map<Integer, RandomCraftExtractDataHolder> EXTRACT_DATA = new HashMap<>();
	private static final Map<Integer, RandomCraftRewardDataHolder> REWARD_DATA = new HashMap<>();
	private List<RandomCraftRewardDataHolder> _randomRewards = null;
	private int _randomRewardIndex = 0;

	protected RandomCraftData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		EXTRACT_DATA.clear();
		this.parseDatapackFile("data/RandomCraftExtractData.xml");
		int extractCount = EXTRACT_DATA.size();
		if (extractCount > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + extractCount + " extraction data.");
		}

		REWARD_DATA.clear();
		this.parseDatapackFile("data/RandomCraftRewardData.xml");
		int rewardCount = REWARD_DATA.size();
		if (rewardCount > 4)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + rewardCount + " rewards.");
		}
		else if (rewardCount > 0)
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Random craft rewards should be more than " + rewardCount + ".");
			REWARD_DATA.clear();
		}

		this.randomizeRewards();
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "extract", extractNode -> this.forEach(extractNode, "item", itemNode -> {
			StatSet stats = new StatSet(this.parseAttributes(itemNode));
			int itemId = stats.getInt("id");
			long points = stats.getLong("points");
			long fee = stats.getLong("fee");
			EXTRACT_DATA.put(itemId, new RandomCraftExtractDataHolder(points, fee));
		})));
		this.forEach(document, "list", listNode -> this.forEach(listNode, "rewards", rewardNode -> this.forEach(rewardNode, "item", itemNode -> {
			StatSet stats = new StatSet(this.parseAttributes(itemNode));
			int itemId = stats.getInt("id");
			ItemTemplate item = ItemData.getInstance().getTemplate(itemId);
			if (item == null)
			{
				LOGGER.warning(this.getClass().getSimpleName() + " unexisting item reward: " + itemId);
			}
			else
			{
				REWARD_DATA.put(itemId, new RandomCraftRewardDataHolder(stats.getInt("id"), stats.getLong("count", 1L), Math.min(100.0, Math.max(1.0E-14, stats.getDouble("chance", 100.0))), stats.getBoolean("announce", false)));
			}
		})));
	}

	public double getRewardChance(int itemId)
	{
		RandomCraftRewardDataHolder holder = REWARD_DATA.get(itemId);
		return holder == null ? 0.0 : holder.getChance();
	}

	public boolean isEmpty()
	{
		return REWARD_DATA.isEmpty();
	}

	public synchronized RandomCraftRewardItemHolder getNewReward()
	{
		RandomCraftRewardDataHolder reward = null;
		double random = Rnd.get(100.0);

		while (!REWARD_DATA.isEmpty())
		{
			if (this._randomRewardIndex == REWARD_DATA.size() - 1)
			{
				this.randomizeRewards();
			}

			this._randomRewardIndex++;
			reward = this._randomRewards.get(this._randomRewardIndex);
			if (random < reward.getChance())
			{
				return new RandomCraftRewardItemHolder(reward.getItemId(), reward.getCount(), false, 20);
			}
		}

		return null;
	}

	private void randomizeRewards()
	{
		this._randomRewardIndex = -1;
		this._randomRewards = new ArrayList<>(REWARD_DATA.values());
		Collections.shuffle(this._randomRewards);
	}

	public boolean isAnnounce(int id)
	{
		RandomCraftRewardDataHolder holder = REWARD_DATA.get(id);
		return holder == null ? false : holder.isAnnounce();
	}

	public long getPoints(int id)
	{
		RandomCraftExtractDataHolder holder = EXTRACT_DATA.get(id);
		return holder == null ? 0L : holder.getPoints();
	}

	public long getFee(int id)
	{
		RandomCraftExtractDataHolder holder = EXTRACT_DATA.get(id);
		return holder == null ? 0L : holder.getFee();
	}

	public static RandomCraftData getInstance()
	{
		return RandomCraftData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final RandomCraftData INSTANCE = new RandomCraftData();
	}
}
