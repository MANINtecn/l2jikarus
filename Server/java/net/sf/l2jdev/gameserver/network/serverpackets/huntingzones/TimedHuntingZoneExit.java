package net.sf.l2jdev.gameserver.network.serverpackets.huntingzones;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class TimedHuntingZoneExit extends ServerPacket
{
	private final int _zoneId;

	public TimedHuntingZoneExit(int zoneId)
	{
		this._zoneId = zoneId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TIME_RESTRICT_FIELD_USER_EXIT.writeId(this, buffer);
		buffer.writeInt(this._zoneId);
	}

	@Override
	public void runImpl(Player player)
	{
		player.getVariables().set("LAST_HUNTING_ZONE_ID", 0);
	}
}
