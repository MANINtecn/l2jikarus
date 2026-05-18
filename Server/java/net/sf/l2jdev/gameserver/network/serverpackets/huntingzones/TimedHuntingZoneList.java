package net.sf.l2jdev.gameserver.network.serverpackets.huntingzones;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.TimedHuntingZoneHolder;
import net.sf.l2jdev.gameserver.data.xml.TimedHuntingZoneData;
import net.sf.l2jdev.gameserver.managers.GlobalVariablesManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class TimedHuntingZoneList extends ServerPacket
{
	private final Player _player;
	private final boolean _isInTimedHuntingZone;
	final List<TimedHuntingZoneHolder> _timedHuntingZoneList;

	public TimedHuntingZoneList(Player player)
	{
		this._player = player;
		this._isInTimedHuntingZone = player.isInsideZone(ZoneId.TIMED_HUNTING);
		this._timedHuntingZoneList = TimedHuntingZoneData.getInstance().getAllHuntingZones().stream().parallel().filter(t -> t.isEvenWeek() == GlobalVariablesManager.getInstance().getBoolean("IS_EVEN_WEEK", true) || !t.isSwapWeek()).toList();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		long currentTime = System.currentTimeMillis();
		ServerPackets.EX_TIME_RESTRICT_FIELD_LIST.writeId(this, buffer);
		buffer.writeInt(this._timedHuntingZoneList.size());

		for (TimedHuntingZoneHolder holder : this._timedHuntingZoneList)
		{
			buffer.writeInt(holder.getEntryFee() != 0);
			buffer.writeInt(holder.getEntryItemId());
			buffer.writeLong(holder.getEntryFee());
			buffer.writeInt(!holder.isWeekly());
			buffer.writeInt(holder.getZoneId());
			buffer.writeInt(holder.getMinLevel());
			buffer.writeInt(holder.getMaxLevel());
			buffer.writeInt(holder.getInitialTime() / 1000);
			int remainingTime = this._player.getTimedHuntingZoneRemainingTime(holder.getZoneId());
			if (remainingTime == 0 && this._player.getTimedHuntingZoneInitialEntry(holder.getZoneId()) + holder.getResetDelay() < currentTime)
			{
				remainingTime = holder.getInitialTime();
			}

			buffer.writeInt(remainingTime / 1000);
			buffer.writeInt(holder.getMaximumAddedTime() / 1000);
			buffer.writeInt(this._player.getVariables().getInt("HUNTING_ZONE_REMAIN_REFILL_" + holder.getZoneId(), holder.getRemainRefillTime()));
			buffer.writeInt(holder.getRefillTimeMax());
			boolean isFieldActivated = !this._isInTimedHuntingZone;
			if (holder.getZoneId() == 18 && !GlobalVariablesManager.getInstance().getBoolean("AvailableFrostLord", false))
			{
				isFieldActivated = false;
			}

			buffer.writeByte(isFieldActivated);
			buffer.writeByte(0);
			buffer.writeByte(0);
			buffer.writeByte(holder.zonePremiumUserOnly());
			buffer.writeByte(this._player.hasPremiumStatus());
			buffer.writeByte(holder.useWorldPrefix());
			buffer.writeByte(0);
			buffer.writeInt(0);
		}
	}
}
