package net.sf.l2jdev.gameserver.network.clientpackets;

import java.util.Calendar;
import java.util.Date;

import net.sf.l2jdev.gameserver.config.FeatureConfig;
import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.SiegeInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.util.Broadcast;

public class RequestSetCastleSiegeTime extends ClientPacket
{
	private int _castleId;
	private long _time;

	@Override
	protected void readImpl()
	{
		this._castleId = this.readInt();
		this._time = this.readInt();
		this._time *= 1000L;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		Castle castle = CastleManager.getInstance().getCastleById(this._castleId);
		if (player != null && castle != null)
		{
			if (castle.getOwnerId() > 0 && castle.getOwnerId() != player.getClanId())
			{
				PacketLogger.warning(this.getClass().getSimpleName() + ": activeChar: " + player + " castle: " + castle + " castleId: " + this._castleId + " is trying to change siege date of not his own castle!");
			}
			else if (!player.isClanLeader())
			{
				PacketLogger.warning(this.getClass().getSimpleName() + ": activeChar: " + player + " castle: " + castle + " castleId: " + this._castleId + " is trying to change siege date but is not clan leader!");
			}
			else if (!castle.isTimeRegistrationOver())
			{
				if (isSiegeTimeValid(castle.getSiegeDate().getTimeInMillis(), this._time))
				{
					castle.getSiegeDate().setTimeInMillis(this._time);
					castle.setTimeRegistrationOver(true);
					castle.getSiege().saveSiegeDate();
					SystemMessage msg = new SystemMessage(SystemMessageId.S1_HAS_ANNOUNCED_THE_NEXT_CASTLE_SIEGE_TIME);
					msg.addCastleId(this._castleId);
					Broadcast.toAllOnlinePlayers(msg);
					player.sendPacket(new SiegeInfo(castle, player));
				}
				else
				{
					PacketLogger.warning(this.getClass().getSimpleName() + ": activeChar: " + player + " castle: " + castle + " castleId: " + this._castleId + " is trying to an invalid time (" + new Date(this._time) + " !");
				}
			}
			else
			{
				PacketLogger.warning(this.getClass().getSimpleName() + ": activeChar: " + player + " castle: " + castle + " castleId: " + this._castleId + " is trying to change siege date but currently not possible!");
			}
		}
		else
		{
			PacketLogger.warning(this.getClass().getSimpleName() + ": activeChar: " + player + " castle: " + castle + " castleId: " + this._castleId);
		}
	}

	private static boolean isSiegeTimeValid(long siegeDate, long choosenDate)
	{
		Calendar cal1 = Calendar.getInstance();
		cal1.setTimeInMillis(siegeDate);
		cal1.set(12, 0);
		cal1.set(13, 0);
		Calendar cal2 = Calendar.getInstance();
		cal2.setTimeInMillis(choosenDate);

		for (int hour : FeatureConfig.SIEGE_HOUR_LIST)
		{
			cal1.set(11, hour);
			if (isEqual(cal1, cal2, 1, 2, 5, 10, 12, 13))
			{
				return true;
			}
		}

		return false;
	}

	private static boolean isEqual(Calendar cal1, Calendar cal2, int... fields)
	{
		for (int field : fields)
		{
			if (cal1.get(field) != cal2.get(field))
			{
				return false;
			}
		}

		return true;
	}
}
