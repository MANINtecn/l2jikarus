package net.sf.l2jdev.gameserver.model;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.HuntPassConfig;
import net.sf.l2jdev.gameserver.data.xml.HuntPassData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.huntpass.HuntPassSimpleInfo;

public class HuntPass
{
	private static final Logger LOGGER = Logger.getLogger(HuntPass.class.getName());
	public static final String INSERT_SEASONPASS = "REPLACE INTO huntpass (`account_name`, `current_step`, `points`, `reward_step`, `is_premium`, `premium_reward_step`, `sayha_points_available`, `sayha_points_used`, `unclaimed_reward`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";
	public static final String RESTORE_SEASONPASS = "SELECT * FROM huntpass WHERE account_name=?";
	private final Player _user;
	private int _availableSayhaTime;
	private int _points;
	private boolean _isPremium = false;
	private boolean _rewardAlert = false;
	private int _rewardStep;
	private int _currentStep;
	private int _premiumRewardStep;
	private boolean _toggleSayha = false;
	private ScheduledFuture<?> _sayhasSustentionTask = null;
	private int _toggleStartTime = 0;
	private int _usedSayhaTime;
	private static int _dayEnd = 0;

	public HuntPass(Player user)
	{
		this._user = user;
		this.restoreHuntPass();
		this.huntPassDayEnd();
		this.store();
	}

	public void restoreHuntPass()
	{
		try (Connection con = DatabaseFactory.getConnection(); PreparedStatement statement = con.prepareStatement("SELECT * FROM huntpass WHERE account_name=?");)
		{
			statement.setString(1, this.getAccountName());

			try (ResultSet rset = statement.executeQuery())
			{
				if (rset.next())
				{
					this.setPoints(rset.getInt("points"));
					this.setCurrentStep(rset.getInt("current_step"));
					this.setRewardStep(rset.getInt("reward_step"));
					this.setPremium(rset.getBoolean("is_premium"));
					this.setPremiumRewardStep(rset.getInt("premium_reward_step"));
					this.setAvailableSayhaTime(rset.getInt("sayha_points_available"));
					this.setUsedSayhaTime(rset.getInt("sayha_points_used"));
					this.setRewardAlert(rset.getBoolean("unclaimed_reward"));
				}

				rset.close();
				statement.close();
			}
		}
		catch (Exception var12)
		{
			LOGGER.log(Level.SEVERE, "Could not restore Season Pass for playerId: " + this._user.getAccountName());
		}
	}

	public void resetHuntPass()
	{
		this.setPoints(0);
		this.setCurrentStep(0);
		this.setRewardStep(0);
		this.setPremium(false);
		this.setPremiumRewardStep(0);
		this.setAvailableSayhaTime(0);
		this.setUsedSayhaTime(0);
		this.setRewardAlert(false);
		this.store();
	}

	public String getAccountName()
	{
		return this._user.getAccountName();
	}

	public void store()
	{
		try (Connection con = DatabaseFactory.getConnection();
			PreparedStatement statement = con.prepareStatement("REPLACE INTO huntpass (`account_name`, `current_step`, `points`, `reward_step`, `is_premium`, `premium_reward_step`, `sayha_points_available`, `sayha_points_used`, `unclaimed_reward`) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)");)
		{
			statement.setString(1, this.getAccountName());
			statement.setInt(2, this.getCurrentStep());
			statement.setInt(3, this.getPoints());
			statement.setInt(4, this.getRewardStep());
			statement.setBoolean(5, this.isPremium());
			statement.setInt(6, this.getPremiumRewardStep());
			statement.setInt(7, this.getAvailableSayhaTime());
			statement.setInt(8, this.getUsedSayhaTime());
			statement.setBoolean(9, this.rewardAlert());
			statement.execute();
			statement.close();
		}
		catch (SQLException var9)
		{
			LOGGER.log(Level.SEVERE, "Could not store Season-Pass data for Account " + this._user.getAccountName() + ": ", var9);
		}
	}

	public int getHuntPassDayEnd()
	{
		return _dayEnd;
	}

	public void huntPassDayEnd()
	{
		Calendar calendar = Calendar.getInstance();
		calendar.setTimeInMillis(System.currentTimeMillis());
		calendar.set(5, HuntPassConfig.HUNT_PASS_PERIOD);
		calendar.set(11, 6);
		calendar.set(12, 30);
		if (calendar.getTimeInMillis() < System.currentTimeMillis())
		{
			calendar.add(2, 1);
		}

		_dayEnd = (int) (calendar.getTimeInMillis() / 1000L);
	}

	public boolean toggleSayha()
	{
		return this._toggleSayha;
	}

	public int getPoints()
	{
		return this._points;
	}

	public void addPassPoint()
	{
		if (HuntPassConfig.ENABLE_HUNT_PASS)
		{
			int points = this.getPoints() + 1;
			if (this._user.isInTimedHuntingZone())
			{
				points++;
			}

			boolean hasNewLevel;
			for (hasNewLevel = false; points >= HuntPassConfig.HUNT_PASS_POINTS_FOR_STEP; hasNewLevel = true)
			{
				points -= HuntPassConfig.HUNT_PASS_POINTS_FOR_STEP;
				this.setCurrentStep(this.getCurrentStep() + 1);
			}

			this.setPoints(points);
			if (hasNewLevel)
			{
				this.setRewardAlert(true);
				this._user.sendPacket(new HuntPassSimpleInfo(this._user));
			}
		}
	}

	public void setPoints(int points)
	{
		this._points = points;
	}

	public int getCurrentStep()
	{
		return this._currentStep;
	}

	public void setCurrentStep(int step)
	{
		this._currentStep = Math.max(0, Math.min(step, HuntPassData.getInstance().getRewardsCount()));
	}

	public int getRewardStep()
	{
		return this._rewardStep;
	}

	public void setRewardStep(int step)
	{
		if (!this._isPremium || this._premiumRewardStep > this._rewardStep)
		{
			this._rewardStep = Math.max(0, Math.min(step, HuntPassData.getInstance().getRewardsCount()));
		}
	}

	public boolean isPremium()
	{
		return this._isPremium;
	}

	public void setPremium(boolean premium)
	{
		this._isPremium = premium;
	}

	public int getPremiumRewardStep()
	{
		return this._premiumRewardStep;
	}

	public void setPremiumRewardStep(int step)
	{
		this._premiumRewardStep = Math.max(0, Math.min(step, HuntPassData.getInstance().getPremiumRewardsCount()));
	}

	public boolean rewardAlert()
	{
		return this._rewardAlert;
	}

	public void setRewardAlert(boolean enable)
	{
		this._rewardAlert = enable;
	}

	public int getAvailableSayhaTime()
	{
		return this._availableSayhaTime;
	}

	public void setAvailableSayhaTime(int time)
	{
		this._availableSayhaTime = time;
	}

	public void addSayhaTime(int time)
	{
		this._availableSayhaTime += time * 60;
	}

	public int getUsedSayhaTime()
	{
		return this._usedSayhaTime;
	}

	private void onSayhaEndTime()
	{
		this.setSayhasSustention(false);
	}

	public void setUsedSayhaTime(int time)
	{
		this._usedSayhaTime = time;
	}

	public void addSayhasSustentionTimeUsed(int time)
	{
		this._usedSayhaTime += time;
	}

	public int getToggleStartTime()
	{
		return this._toggleStartTime;
	}

	public void setSayhasSustention(boolean active)
	{
		this._toggleSayha = active;
		if (active)
		{
			this._toggleStartTime = (int) (System.currentTimeMillis() / 1000L);
			if (this._sayhasSustentionTask != null)
			{
				this._sayhasSustentionTask.cancel(true);
				this._sayhasSustentionTask = null;
			}

			this._user.sendPacket(SystemMessageId.SAYHA_S_GRACE_SUSTENTION_EFFECT_OF_THE_SEASON_PASS_IS_ACTIVATED_AVAILABLE_SAYHA_S_GRACE_SUSTENTION_TIME_IS_BEING_CONSUMED);
			this._sayhasSustentionTask = ThreadPool.schedule(this::onSayhaEndTime, Math.max(0, this.getAvailableSayhaTime() - this.getUsedSayhaTime()) * 1000L);
		}
		else if (this._sayhasSustentionTask != null)
		{
			this.addSayhasSustentionTimeUsed((int) (System.currentTimeMillis() / 1000L - this._toggleStartTime));
			this._toggleStartTime = 0;
			this._sayhasSustentionTask.cancel(true);
			this._sayhasSustentionTask = null;
			this._user.sendPacket(SystemMessageId.SAYHA_S_GRACE_SUSTENTION_EFFECT_OF_THE_SEASON_PASS_HAS_BEEN_DEACTIVATED_THE_SUSTENTION_TIME_YOU_HAVE_DOES_NOT_DECREASE);
		}
	}
}
