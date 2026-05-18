package net.sf.l2jdev.gameserver.network.clientpackets.huntingzones;

import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.xml.TimedHuntingZoneData;
import net.sf.l2jdev.gameserver.managers.InstanceManager;
import net.sf.l2jdev.gameserver.managers.ScriptManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.enums.ItemProcessType;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadManager;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.huntingzones.TimedHuntingZoneEnter;

public class ExTimedHuntingZoneEnter extends ClientPacket
{
	private int _zoneId;

	@Override
	protected void readImpl()
	{
		this._zoneId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!player.isInsideZone(ZoneId.PEACE))
			{
				player.sendMessage("Can only enter to a peace zone.");
			}
			else if (player.isInCombat())
			{
				player.sendMessage("You can only enter in time-limited hunting zones while not in combat.");
			}
			else if (player.getReputation() < 0)
			{
				player.sendMessage("You can only enter in time-limited hunting zones when you have positive reputation.");
			}
			else if (player.isMounted())
			{
				player.sendMessage("Cannot use time-limited hunting zones while mounted.");
			}
			else if (player.isInDuel())
			{
				player.sendMessage("Cannot use time-limited hunting zones during a duel.");
			}
			else if (player.isInOlympiadMode() || OlympiadManager.getInstance().isRegistered(player))
			{
				player.sendMessage("Cannot use time-limited hunting zones while waiting for the Olympiad.");
			}
			else if (player.isRegisteredOnEvent())
			{
				player.sendMessage("Cannot use time-limited hunting zones while registered on an event.");
			}
			else if (player.isInInstance())
			{
				player.sendMessage("Cannot use time-limited hunting zones while in an instance.");
			}
			else if (player.isPrisoner())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_USE_THIS_FUNCTION_IN_THE_UNDERGROUND_LABYRINTH);
			}
			else
			{
				TimedHuntingZoneHolder holder = TimedHuntingZoneData.getInstance().getHuntingZone(this._zoneId);
				if (holder != null)
				{
					if (player.getLevel() >= holder.getMinLevel() && player.getLevel() <= holder.getMaxLevel())
					{
						long currentTime = System.currentTimeMillis();
						int instanceId = holder.getInstanceId();
						if (instanceId > 0 && holder.isSoloInstance())
						{
							if (instanceId == 228)
							{
								if (InstanceManager.getInstance().getInstanceTime(player, instanceId) > currentTime)
								{
									player.sendMessage("The training zone has not reset yet.");
									return;
								}
							}
							else
							{
								for (int instId = 208; instId <= 213; instId++)
								{
									if (InstanceManager.getInstance().getInstanceTime(player, instId) > currentTime)
									{
										player.sendMessage("The transcendent instance has not reset yet.");
										return;
									}
								}
							}
						}

						long endTime = currentTime + player.getTimedHuntingZoneRemainingTime(this._zoneId);
						long lastEntryTime = player.getVariables().getLong("HUNTING_ZONE_ENTRY_" + this._zoneId, 0L);
						if (lastEntryTime + holder.getResetDelay() < currentTime)
						{
							if (endTime == currentTime)
							{
								endTime += holder.getInitialTime();
							}

							player.getVariables().set("HUNTING_ZONE_ENTRY_" + this._zoneId, currentTime);
						}

						if (endTime > currentTime)
						{
							if (holder.getEntryItemId() == 57)
							{
								if (player.getAdena() <= holder.getEntryFee())
								{
									player.sendMessage("Not enough adena.");
									return;
								}

								player.reduceAdena(ItemProcessType.FEE, holder.getEntryFee(), player, true);
							}
							else if (!player.destroyItemByItemId(ItemProcessType.FEE, holder.getEntryItemId(), holder.getEntryFee(), player, true))
							{
								player.sendPacket(SystemMessageId.YOU_DO_NOT_HAVE_ENOUGH_REQUIRED_ITEMS);
								return;
							}

							player.getVariables().set("LAST_HUNTING_ZONE_ID", this._zoneId);
							player.getVariables().set("HUNTING_ZONE_TIME_" + this._zoneId, endTime - currentTime);
							if (instanceId == 0)
							{
								player.teleToLocation(holder.getEnterLocation());
								player.sendPacket(new TimedHuntingZoneEnter(player, this._zoneId));
							}
							else
							{
								ScriptManager.getInstance().getScript("TimedHunting").notifyEvent("ENTER " + this._zoneId, null, player);
								player.sendPacket(new TimedHuntingZoneEnter(player, this._zoneId));
							}
						}
						else
						{
							player.sendMessage("You don't have enough time available to enter the hunting zone.");
						}
					}
					else
					{
						player.sendMessage("Your level does not correspond the zone equivalent.");
					}
				}
			}
		}
	}
}
