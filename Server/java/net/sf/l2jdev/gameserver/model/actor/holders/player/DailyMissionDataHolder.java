package net.sf.l2jdev.gameserver.model.actor.holders.player;

import java.util.Calendar;
import java.util.List;
import java.util.function.Function;

import net.sf.l2jdev.gameserver.handler.AbstractDailyMissionHandler;
import net.sf.l2jdev.gameserver.handler.DailyMissionHandler;
import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.DailyMissionStatus;
import net.sf.l2jdev.gameserver.model.actor.enums.player.MissionResetType;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PlayerClass;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class DailyMissionDataHolder
{
	private final int _id;
	private final List<ItemHolder> _rewardsItems;
	private final List<PlayerClass> _classRestriction;
	private final int _requiredCompletions;
	private final StatSet _params;
	private final boolean _dailyReset;
	private final boolean _isOneTime;
	private final boolean _isMainClassOnly;
	private final boolean _isDualClassOnly;
	private final boolean _isDisplayedWhenNotAvailable;
	private final AbstractDailyMissionHandler _handler;
	private final MissionResetType _missionResetSlot;

	public DailyMissionDataHolder(StatSet set)
	{
		Function<DailyMissionDataHolder, AbstractDailyMissionHandler> handler = DailyMissionHandler.getInstance().getHandler(set.getString("handler"));
		this._id = set.getInt("id");
		this._requiredCompletions = set.getInt("requiredCompletion", 0);
		this._rewardsItems = set.getList("items", ItemHolder.class);
		this._classRestriction = set.getList("classRestriction", PlayerClass.class);
		this._params = set.getObject("params", StatSet.class);
		this._dailyReset = set.getBoolean("dailyReset", true);
		this._isOneTime = set.getBoolean("isOneTime", true);
		this._isMainClassOnly = set.getBoolean("isMainClassOnly", true);
		this._isDualClassOnly = set.getBoolean("isDualClassOnly", false);
		this._isDisplayedWhenNotAvailable = set.getBoolean("isDisplayedWhenNotAvailable", true);
		this._handler = handler != null ? handler.apply(this) : null;
		this._missionResetSlot = set.getObject("duration", MissionResetType.class, MissionResetType.DAY);
	}

	public int getId()
	{
		return this._id;
	}

	public List<PlayerClass> getClassRestriction()
	{
		return this._classRestriction;
	}

	public List<ItemHolder> getRewards()
	{
		return this._rewardsItems;
	}

	public int getRequiredCompletions()
	{
		return this._requiredCompletions;
	}

	public StatSet getParams()
	{
		return this._params;
	}

	public boolean dailyReset()
	{
		return this._dailyReset;
	}

	public boolean isOneTime()
	{
		return this._isOneTime;
	}

	public boolean isMainClassOnly()
	{
		return this._isMainClassOnly;
	}

	public boolean isDualClassOnly()
	{
		return this._isDualClassOnly;
	}

	public boolean isDisplayedWhenNotAvailable()
	{
		return this._isDisplayedWhenNotAvailable;
	}

	public boolean isDisplayable(Player player)
	{
		if (!this.isMainClassOnly() || !player.isSubClassActive() && !player.isDualClassActive())
		{
			if (this.isDualClassOnly() && !player.isDualClassActive())
			{
				return false;
			}
			else if (!this._classRestriction.isEmpty() && !this._classRestriction.contains(player.getPlayerClass()))
			{
				return false;
			}
			else
			{
				int status = this.getStatus(player);
				return !this.isDisplayedWhenNotAvailable() && status == DailyMissionStatus.NOT_AVAILABLE.getClientId() ? false : !this.isOneTime() || this.isRecentlyCompleted(player) || status != DailyMissionStatus.COMPLETED.getClientId();
			}
		}
		return false;
	}

	public void requestReward(Player player)
	{
		if (this._handler != null && this.isDisplayable(player))
		{
			this._handler.requestReward(player);
		}
	}

	public int getStatus(Player player)
	{
		return this._handler != null ? this._handler.getStatus(player) : DailyMissionStatus.NOT_AVAILABLE.getClientId();
	}

	public int getProgress(Player player)
	{
		return this._handler != null ? this._handler.getProgress(player) : DailyMissionStatus.NOT_AVAILABLE.getClientId();
	}

	public boolean isRecentlyCompleted(Player player)
	{
		return this._handler != null && this._handler.isRecentlyCompleted(player);
	}

	public void reset()
	{
		if (this._handler != null)
		{
			if (this._missionResetSlot == MissionResetType.WEEK && Calendar.getInstance().get(7) == 2)
			{
				this._handler.reset();
			}
			else if (this._missionResetSlot == MissionResetType.MONTH && Calendar.getInstance().get(5) == 1)
			{
				this._handler.reset();
			}
			else if (this._missionResetSlot == MissionResetType.WEEKEND && Calendar.getInstance().get(7) == 7)
			{
				this._handler.reset();
			}
			else if (this._dailyReset)
			{
				this._handler.reset();
			}
		}
	}
}
