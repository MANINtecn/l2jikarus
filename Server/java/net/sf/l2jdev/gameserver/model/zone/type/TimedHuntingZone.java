package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.xml.TimedHuntingZoneData;
import net.sf.l2jdev.gameserver.managers.MapRegionManager;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.TeleportWhereType;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class TimedHuntingZone extends ZoneType
{
	public TimedHuntingZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (player != null)
			{
				player.setInsideZone(ZoneId.TIMED_HUNTING, true);

				for (TimedHuntingZoneHolder holder : TimedHuntingZoneData.getInstance().getAllHuntingZones())
				{
					if (player.isInTimedHuntingZone(holder.getZoneId()))
					{
						int remainingTime = player.getTimedHuntingZoneRemainingTime(holder.getZoneId());
						if (remainingTime > 0)
						{
							player.startTimedHuntingZone(holder.getZoneId());
							if (holder.isPvpZone())
							{
								if (!player.isInsideZone(ZoneId.PVP))
								{
									player.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
								}

								player.setInsideZone(ZoneId.PVP, true);
								if (player.hasServitors())
								{
									player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.PVP, true));
								}

								if (player.hasPet())
								{
									player.getPet().setInsideZone(ZoneId.PVP, true);
								}
							}
							else if (holder.isNoPvpZone())
							{
								player.setInsideZone(ZoneId.NO_PVP, true);
								if (player.hasServitors())
								{
									player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.NO_PVP, true));
								}

								if (player.hasPet())
								{
									player.getPet().setInsideZone(ZoneId.NO_PVP, true);
								}
							}

							if (!player.isTeleporting())
							{
								player.broadcastInfo();
							}

							return;
						}
						break;
					}
				}

				if (!player.isGM())
				{
					player.teleToLocation(MapRegionManager.getInstance().getTeleToLocation(player, TeleportWhereType.TOWN));
				}
			}
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			Player player = creature.asPlayer();
			if (player != null)
			{
				player.setInsideZone(ZoneId.TIMED_HUNTING, false);
				TimedHuntingZoneHolder holder = player.getTimedHuntingZone();
				if (holder != null)
				{
					if (holder.isPvpZone())
					{
						player.setInsideZone(ZoneId.PVP, false);
						if (player.hasServitors())
						{
							player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.PVP, false));
						}

						if (player.hasPet())
						{
							player.getPet().setInsideZone(ZoneId.PVP, false);
						}

						if (!player.isInsideZone(ZoneId.PVP))
						{
							creature.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
						}
					}
					else if (holder.isNoPvpZone())
					{
						player.setInsideZone(ZoneId.NO_PVP, false);
						if (player.hasServitors())
						{
							player.getServitors().values().forEach(s -> s.setInsideZone(ZoneId.NO_PVP, false));
						}

						if (player.hasPet())
						{
							player.getPet().setInsideZone(ZoneId.NO_PVP, false);
						}
					}

					if (!player.isTeleporting())
					{
						player.broadcastInfo();
					}
				}
			}
		}
	}
}
