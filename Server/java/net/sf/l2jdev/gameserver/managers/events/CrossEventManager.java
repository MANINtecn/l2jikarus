package net.sf.l2jdev.gameserver.managers.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventAdvancedRewardHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventHolder;
import net.sf.l2jdev.gameserver.model.actor.holders.player.CrossEventRegularRewardHolder;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventInfo;

public class CrossEventManager
{
	private final List<CrossEventRegularRewardHolder> _regularRewards = new ArrayList<>();
	private final List<CrossEventAdvancedRewardHolder> _advancedRewards = new ArrayList<>();
	private final Map<String, List<Integer>> _rewardsAvailable = new ConcurrentHashMap<>();
	private int _ticketId = 72458;
	private int _displayId = 1;
	private int _endTime;
	private int _dailyReset;
	private int _resetCostAmount;

	public void setTicketId(int ticketId)
	{
		this._ticketId = ticketId;
	}

	public int getTicketId()
	{
		return this._ticketId;
	}

	public void setDisplayId(int displayId)
	{
		this._displayId = displayId;
	}

	public int getDisplayId()
	{
		return this._displayId;
	}

	public void setEndTime(int endTime)
	{
		this._endTime = endTime;
	}

	public int getEndTime()
	{
		return this._endTime;
	}

	public List<CrossEventRegularRewardHolder> getRegularRewardsList()
	{
		return this._regularRewards;
	}

	public List<CrossEventAdvancedRewardHolder> getAdvancedRewardList()
	{
		return this._advancedRewards;
	}

	public void setDailyResets(int dailyReset)
	{
		this._dailyReset = dailyReset;
	}

	public int getDailyResets()
	{
		return this._dailyReset;
	}

	public void setResetCostAmount(int resetCostAmount)
	{
		this._resetCostAmount = resetCostAmount;
	}

	public int getResetCostAmount()
	{
		return this._resetCostAmount;
	}

	public int getGameTickets(Player player)
	{
		Item item = player.getInventory().getItemByItemId(this._ticketId);
		return item != null ? (int) item.getCount() : 0;
	}

	public List<Integer> getPlayerRewardsAvailable(Player player)
	{
		String accountName = player.getAccountName();
		return !this._rewardsAvailable.containsKey(accountName) ? Collections.emptyList() : this._rewardsAvailable.get(accountName);
	}

	public void addRewardsAvailable(Player player, int reward)
	{
		String accountName = player.getAccountName();
		List<Integer> rewards;
		if (this._rewardsAvailable.containsKey(accountName))
		{
			rewards = this._rewardsAvailable.get(accountName);
		}
		else
		{
			rewards = new ArrayList<>();
			this._rewardsAvailable.put(player.getAccountName(), rewards);
		}

		rewards.add(reward);
	}

	public void checkAdvancedRewardAvailable(Player player)
	{
		List<CrossEventHolder> cells = player.getCrossEventCells();
		if (!this.rewardExist(player, 1) && cells.stream().filter(c -> c.getHorizontal() == 0).count() == 4L)
		{
			this.addRewardsAvailable(player, 1);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 2) && cells.stream().filter(c -> c.getHorizontal() == 1).count() == 4L)
		{
			this.addRewardsAvailable(player, 2);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 3) && cells.stream().filter(c -> c.getHorizontal() == 2).count() == 4L)
		{
			this.addRewardsAvailable(player, 3);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 4) && cells.stream().filter(c -> c.getHorizontal() == 3).count() == 4L)
		{
			this.addRewardsAvailable(player, 4);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 5) && cells.stream().filter(c -> c.getVertical() == 0).count() == 4L)
		{
			this.addRewardsAvailable(player, 5);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 6) && cells.stream().filter(c -> c.getVertical() == 1).count() == 4L)
		{
			this.addRewardsAvailable(player, 6);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 7) && cells.stream().filter(c -> c.getVertical() == 2).count() == 4L)
		{
			this.addRewardsAvailable(player, 7);
			player.setCrossAdvancedRewardCount(1);
		}

		if (!this.rewardExist(player, 8) && cells.stream().filter(c -> c.getVertical() == 3).count() == 4L)
		{
			this.addRewardsAvailable(player, 8);
			player.setCrossAdvancedRewardCount(1);
		}

		player.sendPacket(new ExCrossEventInfo(player));
	}

	private boolean rewardExist(Player player, int reward)
	{
		String accountName = player.getAccountName();
		if (!this._rewardsAvailable.containsKey(accountName))
		{
			return false;
		}
		List<Integer> list = this._rewardsAvailable.get(accountName);
		return list.contains(reward);
	}

	public void resetAdvancedRewards(Player player)
	{
		this._rewardsAvailable.remove(player.getAccountName());
	}

	public static CrossEventManager getInstance()
	{
		return CrossEventManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final CrossEventManager INSTANCE = new CrossEventManager();
	}
}
