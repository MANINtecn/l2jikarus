package net.sf.l2jdev.gameserver.network.serverpackets.huntingzones;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class TimeRestrictFieldUserAlarm extends ServerPacket
{
	private final Player _player;
	private final int _zoneId;

	public TimeRestrictFieldUserAlarm(Player player, int zoneId)
	{
		this._player = player;
		this._zoneId = zoneId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TIME_RESTRICT_FIELD_USER_ALARM.writeId(this, buffer);
		buffer.writeInt(this._zoneId);
		buffer.writeInt(this._player.getTimedHuntingZoneRemainingTime(this._zoneId) / 1000 + 59);
	}
}
