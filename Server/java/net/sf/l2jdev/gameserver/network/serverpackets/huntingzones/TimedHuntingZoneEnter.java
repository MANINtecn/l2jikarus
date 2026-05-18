package net.sf.l2jdev.gameserver.network.serverpackets.huntingzones;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class TimedHuntingZoneEnter extends ServerPacket
{
	private final Player _player;
	private final int _zoneId;

	public TimedHuntingZoneEnter(Player player, int zoneId)
	{
		this._player = player;
		this._zoneId = zoneId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TIME_RESTRICT_FIELD_USER_ENTER.writeId(this, buffer);
		buffer.writeByte(1);
		buffer.writeInt(this._zoneId);
		buffer.writeInt((int) (System.currentTimeMillis() / 1000L));
		buffer.writeInt(this._player.getTimedHuntingZoneRemainingTime(this._zoneId) / 1000 + 59);
	}
}
