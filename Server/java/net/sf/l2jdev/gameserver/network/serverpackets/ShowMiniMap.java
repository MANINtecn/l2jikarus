package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ShowMiniMap extends ServerPacket
{
	private final int _mapId;

	public ShowMiniMap(int mapId)
	{
		this._mapId = mapId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SHOW_MINIMAP.writeId(this, buffer);
		buffer.writeInt(this._mapId);
		buffer.writeByte(0);
	}
}
