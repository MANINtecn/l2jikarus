package net.sf.l2jdev.gameserver.model.prison;

import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.gameserver.config.PrisonConfig;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;
import net.sf.l2jdev.gameserver.network.serverpackets.prison.ExPrisonUserDonation;

public class Prisoner
{
	private long _sentenceTime = 0L;
	private long _timeSpent = 0L;
	private ScheduledFuture<?> sentenceTask = null;
	private int _charId = 0;
	private ItemHolder _bailHolder = null;
	private ItemHolder _donationBailHolder = null;
	private int _currentBail = 0;
	private int _zoneId = 0;

	public Prisoner()
	{
	}

	public Prisoner(int charId, int zoneId, long sentenceTime)
	{
		this._charId = charId;
		this._zoneId = zoneId;
		this._sentenceTime = sentenceTime;
		this._timeSpent = 0L;
		this.loadBailHolder(zoneId);
		this._currentBail = 0;
	}

	public Prisoner(int charId, int zoneId, long sentenceTime, long timeSpent, int bailAmount)
	{
		this._charId = charId;
		this._zoneId = zoneId;
		this._sentenceTime = sentenceTime;
		this._timeSpent = timeSpent;
		this._currentBail = 0;
		this.loadBailHolder(zoneId);
	}

	public void startSentenceTimer(Player player)
	{
		this.sentenceTask = ThreadPool.scheduleAtFixedRate(() -> {
			this._timeSpent += 60000L;
			if (player != null && player.isOnline())
			{
				World.getInstance().getPlayer(this._charId).sendMessage("Time left in Underground Labyrinth: " + (this._sentenceTime - this._timeSpent) / 60000L + " minutes.");
			}

			if (this._timeSpent >= this._sentenceTime)
			{
				if (player != null && player.isOnline())
				{
					World.getInstance().getPlayer(this._charId).sendMessage("Your time in Underground Labyrinth is over, next time be careful!");
				}

				this.processFreedom(true);
				this.sentenceTask.cancel(false);
			}
		}, 60000L, 60000L);
	}

	public void stopSentenceTimer()
	{
		this.sentenceTask.cancel(true);
		this.sentenceTask = null;
	}

	public void endSentence()
	{
		if (this.sentenceTask != null)
		{
			this.sentenceTask.cancel(true);
		}
	}

	public long getSentenceTime()
	{
		return this._sentenceTime;
	}

	public int getZoneId()
	{
		return this._zoneId;
	}

	public long getTimeSpent()
	{
		return this._timeSpent;
	}

	public long getTimeLeft()
	{
		return (this._sentenceTime - this._timeSpent) / 1000L;
	}

	private void loadBailHolder(int id)
	{
		switch (id)
		{
			case 1:
				this.setBailHolder(PrisonConfig.BAIL_ZONE_1);
				this.setDonationBailHolder(PrisonConfig.DONATION_BAIL_ZONE_1);
				break;
			case 2:
				this.setBailHolder(PrisonConfig.BAIL_ZONE_2);
				this.setDonationBailHolder(PrisonConfig.DONATION_BAIL_ZONE_2);
				break;
			default:
				this.setBailHolder(PrisonConfig.BAIL_ZONE_1);
				this.setDonationBailHolder(new ItemHolder(57, 2000000000L));
		}
	}

	public void requestFreedomByDonation(Player player)
	{
		if (!player.destroyItemByItemId(ItemProcessType.FEE, this._donationBailHolder.getId(), this._donationBailHolder.getCount(), player, true))
		{
			player.sendPacket(new ExPrisonUserDonation(false));
		}
		else
		{
			player.getPrisonerInfo().processFreedom(false);
			player.sendPacket(new ExPrisonUserDonation(true));
		}
	}

	public void processFreedom(boolean fromTimer)
	{
		Player player = World.getInstance().getPlayer(this._charId);
		if (player != null)
		{
			this._sentenceTime = 0L;
			this._timeSpent = 0L;
			player.setReputation(PrisonManager.getRepPointsReceived(this._zoneId));
			player.teleToLocation(PrisonManager.getReleaseLoc(this._zoneId), 250);
			this._zoneId = 0;
			this.setBailHolder(null);
			PrisonManager.PRISONERS.remove(this._charId);
			if (!fromTimer && this.sentenceTask != null)
			{
				this.sentenceTask.cancel(true);
				this.sentenceTask = null;
			}
		}
	}

	public int getCurrentBail()
	{
		return this._currentBail;
	}

	public void setCurrentBail(int currentBail)
	{
		this._currentBail = currentBail;
	}

	public void increaseCurrentBail(int amount)
	{
		this._currentBail += amount;
		if (this._currentBail >= this._bailHolder.getCount() && this._timeSpent >= this._sentenceTime)
		{
			this.processFreedom(false);
		}
	}

	public ItemHolder getBailHolder()
	{
		return this._bailHolder;
	}

	public void setBailHolder(ItemHolder bailHolder)
	{
		this._bailHolder = bailHolder;
	}

	public ItemHolder getDonationBailHolder()
	{
		return this._donationBailHolder;
	}

	public void setDonationBailHolder(ItemHolder donationBailHolder)
	{
		this._donationBailHolder = donationBailHolder;
	}
}
