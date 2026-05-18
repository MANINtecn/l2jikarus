package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExNevitAdventPointInfoPacket extends ServerPacket
{
	private final int _points;

	public ExNevitAdventPointInfoPacket(int points)
	{
		this._points = points;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_AGATHION_ENERGY_INFO.writeId(this, buffer);
		buffer.writeInt(this._points);
	}
}
