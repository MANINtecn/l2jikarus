package net.sf.l2jdev.gameserver.network.serverpackets.teleports;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRaidTeleportInfo extends ServerPacket
{
	private final boolean _available;

	public ExRaidTeleportInfo(Player player)
	{
		this._available = System.currentTimeMillis() - player.getVariables().getLong("LastFreeRaidTeleportTime", 0L) < 86400000L;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RAID_TELEPORT_INFO.writeId(this, buffer);
		buffer.writeInt(this._available);
	}
}
