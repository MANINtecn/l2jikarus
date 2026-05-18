package net.sf.l2jdev.gameserver.data.xml;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.IXmlReader;
import net.sf.l2jdev.gameserver.config.HuntPassConfig;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import org.w3c.dom.Document;

public class HuntPassData implements IXmlReader
{
	private static final Logger LOGGER = Logger.getLogger(HuntPassData.class.getName());
	private final List<ItemHolder> _rewards = new ArrayList<>();
	private final List<ItemHolder> _premiumRewards = new ArrayList<>();
	private int _rewardCount = 0;
	private int _premiumRewardCount = 0;

	protected HuntPassData()
	{
		this.load();
	}

	@Override
	public void load()
	{
		if (HuntPassConfig.ENABLE_HUNT_PASS)
		{
			this._rewards.clear();
			this.parseDatapackFile("data/HuntPass.xml");
			this._rewardCount = this._rewards.size();
			this._premiumRewardCount = this._premiumRewards.size();
			LOGGER.info(this.getClass().getSimpleName() + ": Loaded " + this._rewardCount + " HuntPass rewards.");
		}
		else
		{
			LOGGER.info(this.getClass().getSimpleName() + ": Disabled.");
		}
	}

	@Override
	public void parseDocument(Document document, File file)
	{
		this.forEach(document, "list", listNode -> this.forEach(listNode, "item", rewardNode -> {
			StatSet set = new StatSet(this.parseAttributes(rewardNode));
			int itemId = set.getInt("id");
			int itemCount = set.getInt("count");
			int premiumItemId = set.getInt("premiumId");
			int premiumItemCount = set.getInt("premiumCount");
			if (ItemData.getInstance().getTemplate(itemId) == null)
			{
				LOGGER.info(this.getClass().getSimpleName() + ": Item with id " + itemId + " does not exist.");
			}
			else
			{
				this._rewards.add(new ItemHolder(itemId, itemCount));
				this._premiumRewards.add(new ItemHolder(premiumItemId, premiumItemCount));
			}
		}));
	}

	public List<ItemHolder> getRewards()
	{
		return this._rewards;
	}

	public int getRewardsCount()
	{
		return this._rewardCount;
	}

	public List<ItemHolder> getPremiumRewards()
	{
		return this._premiumRewards;
	}

	public int getPremiumRewardsCount()
	{
		return this._premiumRewardCount;
	}

	public static HuntPassData getInstance()
	{
		return HuntPassData.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final HuntPassData INSTANCE = new HuntPassData();
	}
}
