package net.sf.l2jdev.gameserver.model.fishing;

import java.util.concurrent.ScheduledFuture;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.config.custom.PremiumSystemConfig;
import net.sf.l2jdev.gameserver.data.xml.FishingData;
import net.sf.l2jdev.gameserver.geoengine.GeoEngine;
import net.sf.l2jdev.gameserver.managers.ZoneManager;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.stat.PlayerStat;
import net.sf.l2jdev.gameserver.model.events.EventDispatcher;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.actor.player.OnPlayerFishing;
import net.sf.l2jdev.gameserver.model.interfaces.ILocational;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.item.enums.ShotType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.item.type.WeaponType;
import net.sf.l2jdev.gameserver.model.stats.Stat;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.model.zone.type.FishingZone;
import net.sf.l2jdev.gameserver.model.zone.type.WaterZone;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.PlaySound;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.fishing.ExFishingEnd;
import net.sf.l2jdev.gameserver.network.serverpackets.fishing.ExFishingStart;
import net.sf.l2jdev.gameserver.network.serverpackets.fishing.ExUserInfoFishing;
import net.sf.l2jdev.gameserver.util.LocationUtil;

public class Fishing
{
	protected static final Logger LOGGER = Logger.getLogger(Fishing.class.getName());
	private ILocational _baitLocation = new Location(0, 0, 0);
	private final Player _player;
	private ScheduledFuture<?> _reelInTask;
	private ScheduledFuture<?> _startFishingTask;
	private boolean _isFishing = false;

	public Fishing(Player player)
	{
		this._player = player;
	}

	public synchronized boolean isFishing()
	{
		return this._isFishing;
	}

	public boolean isAtValidLocation()
	{
		return this._player.isInsideZone(ZoneId.FISHING);
	}

	public boolean canFish()
	{
		return !this._player.isDead() && !this._player.isAlikeDead() && !this._player.hasBlockActions() && !this._player.isSitting();
	}

	private FishingBait getCurrentBaitData()
	{
		Item bait = this._player.getInventory().getPaperdollItem(7);
		return bait != null ? FishingData.getInstance().getBaitData(bait.getId()) : null;
	}

	private void cancelTasks()
	{
		if (this._reelInTask != null)
		{
			this._reelInTask.cancel(false);
			this._reelInTask = null;
		}

		if (this._startFishingTask != null)
		{
			this._startFishingTask.cancel(false);
			this._startFishingTask = null;
		}
	}

	public synchronized void startFishing()
	{
		if (!this._isFishing)
		{
			this._isFishing = true;
			this.castLine();
		}
	}

	private void castLine()
	{
		if (!GeneralConfig.ALLOW_FISHING && !this._player.isGM())
		{
			this._player.sendMessage("Fishing is disabled.");
			this._player.sendPacket(ActionFailed.STATIC_PACKET);
			this.stopFishing(FishingEndType.ERROR);
		}
		else
		{
			this.cancelTasks();
			if (!this.canFish())
			{
				if (this._isFishing)
				{
					this._player.sendPacket(SystemMessageId.YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED);
				}

				this.stopFishing(FishingEndType.ERROR);
			}
			else
			{
				FishingBait baitData = this.getCurrentBaitData();
				if (baitData == null)
				{
					this._player.sendPacket(SystemMessageId.YOU_MUST_PUT_BAIT_ON_YOUR_HOOK_BEFORE_YOU_CAN_FISH);
					this._player.sendPacket(ActionFailed.STATIC_PACKET);
					this.stopFishing(FishingEndType.ERROR);
				}
				else
				{
					if (PremiumSystemConfig.PREMIUM_SYSTEM_ENABLED)
					{
						if (PremiumSystemConfig.PREMIUM_ONLY_FISHING && !this._player.hasPremiumStatus())
						{
							this._player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_AS_YOU_DO_NOT_MEET_THE_REQUIREMENTS);
							this._player.sendPacket(ActionFailed.STATIC_PACKET);
							this.stopFishing(FishingEndType.ERROR);
							return;
						}

						if (baitData.isPremiumOnly() && !this._player.hasPremiumStatus())
						{
							this._player.sendPacket(SystemMessageId.FAILED_PLEASE_TRY_AGAIN_USING_THE_CORRECT_BAIT);
							this._player.sendPacket(ActionFailed.STATIC_PACKET);
							this.stopFishing(FishingEndType.ERROR);
							return;
						}
					}

					int minPlayerLevel = baitData.getMinPlayerLevel();
					int maxPLayerLevel = baitData.getMaxPlayerLevel();
					if (this._player.getLevel() >= minPlayerLevel && this._player.getLevel() <= maxPLayerLevel)
					{
						Item rod = this._player.getActiveWeaponInstance();
						if (rod != null && rod.getItemType() == WeaponType.FISHINGROD)
						{
							FishingRod rodData = FishingData.getInstance().getRodData(rod.getId());
							if (rodData == null)
							{
								this._player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_FISHING_ROD);
								this._player.sendPacket(ActionFailed.STATIC_PACKET);
								this.stopFishing(FishingEndType.ERROR);
							}
							else if (this._player.isTransformed() || this._player.isInBoat())
							{
								this._player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_WHILE_RIDING_AS_A_PASSENGER_OF_A_BOAT_OR_TRANSFORMED);
								this._player.sendPacket(ActionFailed.STATIC_PACKET);
								this.stopFishing(FishingEndType.ERROR);
							}
							else if (this._player.isCrafting() || this._player.isInStoreMode())
							{
								this._player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_WHILE_USING_A_RECIPE_BOOK_PRIVATE_WORKSHOP_OR_PRIVATE_STORE);
								this._player.sendPacket(ActionFailed.STATIC_PACKET);
								this.stopFishing(FishingEndType.ERROR);
							}
							else if (!this._player.isInsideZone(ZoneId.WATER) && !this._player.isInWater())
							{
								this._baitLocation = this.calculateBaitLocation();
								if (this._player.isInsideZone(ZoneId.FISHING) && this._baitLocation != null)
								{
									if (!this._player.isChargedShot(ShotType.FISH_SOULSHOTS))
									{
										this._player.rechargeShots(false, false, true);
									}

									long fishingTime = Math.max(Rnd.get(baitData.getTimeMin(), baitData.getTimeMax()) - rodData.getReduceFishingTime(), 1000);
									long fishingWaitTime = Rnd.get(baitData.getWaitMin(), baitData.getWaitMax());
									this._reelInTask = ThreadPool.schedule(() -> {
										this._player.getFishing().reelInWithReward();
										this._startFishingTask = ThreadPool.schedule(() -> this._player.getFishing().castLine(), fishingWaitTime);
									}, fishingTime);
									this._player.stopMove(null);
									this._player.broadcastPacket(new ExFishingStart(this._player, -1, this._baitLocation));
									this._player.sendPacket(new ExUserInfoFishing(this._player, true, this._baitLocation));
									this._player.sendPacket(new PlaySound(1, "sf_p_01", 0, 0, 0, 0, 0));
									this._player.sendPacket(SystemMessageId.YOU_CAST_YOUR_LINE_AND_START_TO_FISH);
								}
								else
								{
									if (this._isFishing)
									{
										this._player.sendPacket(ActionFailed.STATIC_PACKET);
									}
									else
									{
										this._player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_HERE);
										this._player.sendPacket(ActionFailed.STATIC_PACKET);
									}

									this.stopFishing(FishingEndType.ERROR);
								}
							}
							else
							{
								this._player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_WHILE_UNDER_WATER);
								this._player.sendPacket(ActionFailed.STATIC_PACKET);
								this.stopFishing(FishingEndType.ERROR);
							}
						}
						else
						{
							this._player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_A_FISHING_ROD);
							this._player.sendPacket(ActionFailed.STATIC_PACKET);
							this.stopFishing(FishingEndType.ERROR);
						}
					}
					else
					{
						this._player.sendPacket(SystemMessageId.YOU_DO_NOT_MEET_THE_FISHING_LEVEL_REQUIREMENTS);
						this._player.sendPacket(ActionFailed.STATIC_PACKET);
						this.stopFishing(FishingEndType.ERROR);
					}
				}
			}
		}
	}

	public void reelInWithReward()
	{
		FishingBait baitData = this.getCurrentBaitData();
		if (baitData == null)
		{
			this.reelIn(FishingEndReason.LOSE, false);
			LOGGER.warning("Player " + this._player + " is fishing with unhandled bait: " + this._player.getInventory().getPaperdollItem(7));
		}
		else
		{
			double chance = baitData.getChance();
			if (this._player.isChargedShot(ShotType.FISH_SOULSHOTS))
			{
				chance *= 2.0;
			}

			if (Rnd.get(100) <= chance)
			{
				this.reelIn(FishingEndReason.WIN, true);
			}
			else
			{
				this.reelIn(FishingEndReason.LOSE, true);
			}
		}
	}

	private void reelIn(FishingEndReason reasonValue, boolean consumeBait)
	{
		if (this._isFishing)
		{
			this.cancelTasks();
			FishingEndReason reason = reasonValue;

			try
			{
				Item bait = this._player.getInventory().getPaperdollItem(7);
				if (consumeBait && (bait == null || !this._player.getInventory().updateItemCount(null, bait, -1L, this._player, null)))
				{
					reason = FishingEndReason.LOSE;
					return;
				}

				if (reason == FishingEndReason.WIN && bait != null)
				{
					FishingBait baitData = FishingData.getInstance().getBaitData(bait.getId());
					FishingCatch fishingCatchData = baitData.getRandom();
					if (fishingCatchData != null)
					{
						FishingData fishingData = FishingData.getInstance();
						double lvlModifier = Math.pow(this._player.getLevel(), 2.2) * fishingCatchData.getMultiplier();
						PlayerStat stat = this._player.getStat();
						long xp = (long) (Rnd.get(fishingData.getExpRateMin(), fishingData.getExpRateMax()) * lvlModifier * stat.getMul(Stat.FISHING_EXP_SP_BONUS, 1.0));
						long sp = (long) (Rnd.get(fishingData.getSpRateMin(), fishingData.getSpRateMax()) * lvlModifier * stat.getMul(Stat.FISHING_EXP_SP_BONUS, 1.0));
						this._player.addExpAndSp(xp, sp, true);
						this._player.getInventory().addItem(ItemProcessType.PICKUP, fishingCatchData.getItemId(), 1L, this._player, null);
						SystemMessage msg = new SystemMessage(SystemMessageId.YOU_HAVE_OBTAINED_S1_2);
						msg.addItemName(fishingCatchData.getItemId());
						this._player.sendPacket(msg);
						this._player.unchargeShot(ShotType.FISH_SOULSHOTS);
						this._player.rechargeShots(false, false, true);
					}
					else
					{
						LOGGER.warning("Could not find fishing rewards for bait " + bait.getId());
					}
				}
				else if (reason == FishingEndReason.LOSE)
				{
					this._player.sendPacket(SystemMessageId.THE_BAIT_HAS_BEEN_LOST_BECAUSE_THE_FISH_GOT_AWAY);
				}

				if (consumeBait && EventDispatcher.getInstance().hasListener(EventType.ON_PLAYER_FISHING, this._player))
				{
					EventDispatcher.getInstance().notifyEventAsync(new OnPlayerFishing(this._player, reason), this._player);
				}
			}
			finally
			{
				this._player.broadcastPacket(new ExFishingEnd(this._player, reason));
				this._player.sendPacket(new ExUserInfoFishing(this._player, false));
			}
		}
	}

	public void stopFishing()
	{
		this.stopFishing(FishingEndType.PLAYER_STOP);
	}

	public synchronized void stopFishing(FishingEndType endType)
	{
		if (this._isFishing)
		{
			this.reelIn(FishingEndReason.STOP, false);
			this._isFishing = false;
			switch (endType)
			{
				case PLAYER_STOP:
					this._player.sendPacket(SystemMessageId.YOU_REEL_YOUR_LINE_IN_AND_STOP_FISHING);
					break;
				case PLAYER_CANCEL:
					this._player.sendPacket(SystemMessageId.YOUR_ATTEMPT_AT_FISHING_HAS_BEEN_CANCELLED);
			}
		}
	}

	public ILocational getBaitLocation()
	{
		return this._baitLocation;
	}

	private Location calculateBaitLocation()
	{
		int distMin = FishingData.getInstance().getBaitDistanceMin();
		int distMax = FishingData.getInstance().getBaitDistanceMax();
		int distance = Rnd.get(distMin, distMax);
		double angle = LocationUtil.convertHeadingToDegree(this._player.getHeading());
		double radian = Math.toRadians(angle);
		double sin = Math.sin(radian);
		double cos = Math.cos(radian);
		int baitX = (int) (this._player.getX() + cos * distance);
		int baitY = (int) (this._player.getY() + sin * distance);
		FishingZone fishingZone = null;

		for (ZoneType zone : ZoneManager.getInstance().getZones(this._player))
		{
			if (zone instanceof FishingZone)
			{
				fishingZone = (FishingZone) zone;
				break;
			}
		}

		WaterZone waterZone = null;

		for (ZoneType zonex : ZoneManager.getInstance().getZones(baitX, baitY))
		{
			if (zonex instanceof WaterZone)
			{
				waterZone = (WaterZone) zonex;
				break;
			}
		}

		int baitZ = computeBaitZ(this._player, baitX, baitY, fishingZone, waterZone);
		if (baitZ == Integer.MIN_VALUE)
		{
			this._player.sendPacket(SystemMessageId.YOU_CANNOT_FISH_HERE);
			return null;
		}
		return new Location(baitX, baitY, baitZ);
	}

	private static int computeBaitZ(Player player, int baitX, int baitY, FishingZone fishingZone, WaterZone waterZone)
	{
		if (fishingZone == null)
		{
			return Integer.MIN_VALUE;
		}
		else if (waterZone == null)
		{
			return Integer.MIN_VALUE;
		}
		else
		{
			int baitZ = waterZone.getWaterZ();
			if (GeoEngine.getInstance().hasGeo(baitX, baitY))
			{
				if ((GeoEngine.getInstance().getHeight(baitX, baitY, baitZ) > baitZ) || (GeoEngine.getInstance().getHeight(baitX, baitY, player.getZ()) > baitZ))
				{
					return Integer.MIN_VALUE;
				}
			}

			return baitZ;
		}
	}
}
